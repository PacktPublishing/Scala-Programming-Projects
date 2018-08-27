package coinyser

import java.io.File
import java.sql.Timestamp
import java.time.{Instant, LocalDateTime, OffsetDateTime}
import java.util.concurrent.TimeUnit

import cats.effect.{IO, Timer}
import org.apache.spark.sql.test.SharedSparkSession
import org.scalatest.{Assertion, Matchers, WordSpec}
import BatchProducerIT._

import scala.concurrent.duration.FiniteDuration


class BatchProducerIT extends WordSpec with Matchers with SharedSparkSession {

  import testImplicits._

  "BatchProducer.save" should {
    "save a Dataset[Transaction] to parquet" in withTempDir { tmpDir =>
      val transaction1 = Transaction(timestamp = new Timestamp(1532365695000L), tid = 70683282, price = 7740.00, sell = false, amount = 0.10041719)
      val transaction2 = Transaction(timestamp = new Timestamp(1532365693000L), tid = 70683281, price = 7739.99, sell = false, amount = 0.00148564)
      val sourceDS = Seq(transaction1, transaction2).toDS()

      val uri = tmpDir.toURI
      BatchProducer.save(sourceDS, uri).unsafeRunSync()
      tmpDir.list() should contain("date=2018-07-23")
      val readDS = spark.read.parquet(uri.toString).as[Transaction]
      spark.read.parquet(uri + "/date=2018-07-23").show()
      sourceDS.collect() should contain theSameElementsAs readDS.collect()
    }
  }

  "BatchProducer.processOneBatch" should {
    "filter and save a batch of transaction, wait 59 mn, fetch the next batch" in withTempDir { tmpDir =>
      implicit val fakeTimer: FakeTimer = new FakeTimer
      // TODO rename to BatchAppConfig, remove kafka stuff, remove intervals (hardcode them)
      import scala.concurrent.duration._
      implicit val appConfig: AppConfig = AppConfig(
        topic = "transaction_btcusd",
        bootstrapServers = "localhost:9092",
        transactionStorePath = tmpDir.toURI,
        firstInterval = 1.day,
        intervalBetweenReads = 1.hour
      )
      implicit val appContext: AppContext = new AppContext


      val transactions = Seq(
        ("2018-08-01T23:00:00Z", 1, 7657.58, true, 0.021762),
        ("2018-08-02T01:00:00Z", 2, 7663.85, false, 0.01385517),
        ("2018-08-02T01:58:30Z", 3, 7663.85, false, 0.03782426),
        ("2018-08-02T01:58:59Z", 4, 7663.86, false, 0.15750809),
        ("2018-08-02T02:30:00Z", 5, 7661.49, true, 0.1)
      ).map(parseTransaction)

      // Start at 01:00, tx 2 ignored (too soon)
      val txs0 = transactions.filter(tx => tx.tid <= 1)
      // Fetch at 01:59, get nb 2 and 3, but will miss nb 4 because of Api lag
      val txs1 = transactions.filter(tx => tx.tid >= 2 && tx.tid <= 3)
      // Fetch at 02:58, get nb 3, 4, 5
      val txs2 = transactions.filter(tx => tx.tid >= 3 && tx.tid <= 5)
      // Fetch at 03:57, get nothing
      val txs3 = Seq.empty[Transaction]

      val initialClock = Instant.parse("2018-08-02T01:00:00Z").toEpochMilli
      fakeTimer.clockRealTimeInMillis = initialClock
      val start0 = Instant.parse("2018-08-02T00:00:00Z")
      val end0 = Instant.parse("2018-08-02T00:59:55Z")
      val threeBatchesIO =
        for {
          tuple1 <- BatchProducer.processOneBatch(IO(txs1.toDS()), txs0.toDS(), start0, end0) // end - Api lag
          (ds1, start1, end1) = tuple1

          tuple2 <- BatchProducer.processOneBatch(IO(txs2.toDS()), ds1, start1, end1)
          (ds2, start2, end2) = tuple2

          _ <- BatchProducer.processOneBatch(IO(txs3.toDS()), ds2, start2, end2)
        } yield (ds1, start1, end1, ds2, start2, end2)

      val (ds1, start1, end1, ds2, start2, end2) = threeBatchesIO.unsafeRunSync()
      ds1.collect() should contain theSameElementsAs txs1
      start1 should ===(end0)
      end1 should ===(Instant.parse("2018-08-02T01:58:55Z")) // initialClock + 1mn - 15s - 5s

      ds2.collect() should contain theSameElementsAs txs2
      start2 should ===(end1)
      end2 should ===(Instant.parse("2018-08-02T02:57:55Z")) // initialClock + 1mn -15s + 1mn -15s -5s = end1 + 45s

      val savedTransactions = spark.read.parquet(tmpDir.toString).as[Transaction].collect()
      val expectedTxs = transactions.filter(tx => tx.tid >= 2)
      // We do not use .toSet to make sure we don't have duplicates
      savedTransactions.map(_.tid).sorted should contain theSameElementsAs expectedTxs.map(_.tid).sorted
    }
  }


}

object BatchProducerIT {
  def parseTransaction(tx: (String, Int, Double, Boolean, Double)): Transaction = tx match {
    case (date, tid, price, sell, amount) =>
      Transaction(Timestamp.from(Instant.parse(date)), tid, price, sell, amount)
  }

  class FakeTimer extends Timer[IO] {
    var clockRealTimeInMillis = 0L

    def clockRealTime(unit: TimeUnit): IO[Long] =
      IO(unit.convert(clockRealTimeInMillis, TimeUnit.MILLISECONDS))

    def clockMonotonic(unit: TimeUnit): IO[Long] = ???

    def sleep(duration: FiniteDuration): IO[Unit] = IO {
      clockRealTimeInMillis = clockRealTimeInMillis + duration.toMillis
    }

    def shift: IO[Unit] = ???
  }

}


