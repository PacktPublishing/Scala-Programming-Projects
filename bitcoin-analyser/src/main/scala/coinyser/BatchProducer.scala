package coinyser

import java.net.URI
import java.time.Instant
import java.util.concurrent.TimeUnit

import cats.Monad
import cats.effect.{IO, Timer}
import cats.implicits._
import org.apache.spark.sql.functions.{explode, from_json, lit}
import org.apache.spark.sql.types._
import org.apache.spark.sql.{Dataset, SaveMode, SparkSession, TypedColumn}

import scala.concurrent.duration._

class AppContext(implicit val config: AppConfig,
                 implicit val spark: SparkSession,
                 implicit val timer: Timer[IO])

object BatchProducer {
  /** Maximum time required to read transactions from the API */
  val MaxReadTime: FiniteDuration = 15.seconds
  /** Number of seconds required by the API to make a transaction visible */
  val ApiLag: FiniteDuration = 5.seconds


  def processRepeatedly(initialJsonTxs: IO[String], jsonTxs: IO[String])
                       (implicit appContext: AppContext): IO[Unit] = {
    import appContext._

    for {
      beforeRead <- currentInstant
      firstEnd = beforeRead.minusSeconds(ApiLag.toSeconds)
      firstTxs <- readTransactions(initialJsonTxs)
      firstStart = truncateInstant(firstEnd, config.firstInterval)
      _ <- Monad[IO].tailRecM((firstTxs, firstStart, firstEnd)) {
        case (txs, start, instant) =>
          processOneBatch(readTransactions(jsonTxs), txs, start, instant).map(_.asLeft)
      }
    } yield ()
  }

  def processOneBatch(lastTransactionsIO: IO[Dataset[Transaction]],
                      transactions: Dataset[Transaction],
                      saveStart: Instant,
                      saveEnd: Instant)(implicit appCtx: AppContext)
  : IO[(Dataset[Transaction], Instant, Instant)] = {
    import appCtx._
    import spark.implicits._

    println("saveStart : " + saveStart)
    println("saveEnd   : " + saveEnd)
    val firstTxs = filterTxs(transactions, saveStart, saveEnd)
    for {
      _ <- BatchProducer.save(firstTxs, appCtx.config.transactionStorePath)
      _ <- IO.sleep(config.intervalBetweenReads - MaxReadTime)

      beforeRead <- currentInstant
      // We are sure that lastTransactions contain all transactions until end
      end = beforeRead.minusSeconds(ApiLag.toSeconds)
      nextTransactions <- lastTransactionsIO
      _ <- IO {
        // TODO use logger
        println("end        : " + end)
        println("beforeRead : " + beforeRead)
        println(nextTransactions.map(_.tid).collect().toSet)
      }

    } yield (nextTransactions, saveEnd, end)
  }


  def currentInstant(implicit timer: Timer[IO]): IO[Instant] =
    timer.clockRealTime(TimeUnit.SECONDS) map Instant.ofEpochSecond

  // Truncates to the start of interval
  def truncateInstant(instant: Instant, interval: FiniteDuration): Instant = {
    Instant.ofEpochSecond(instant.getEpochSecond / interval.toSeconds * interval.toSeconds)

  }

  def jsonToHttpTransactions(json: String)(implicit spark: SparkSession): Dataset[HttpTransaction] = {
    import spark.implicits._
    val ds: Dataset[String] = Seq(json).toDS()
    val txSchema: StructType = Seq.empty[HttpTransaction].toDS().schema
    val schema = ArrayType(txSchema)
    val arrayColumn = from_json($"value", schema)
    ds.select(explode(arrayColumn).alias("v"))
      .select("v.*")
      .as[HttpTransaction]
  }

  def httpToDomainTransactions(ds: Dataset[HttpTransaction]): Dataset[Transaction] = {
    import ds.sparkSession.implicits._
    ds.select(
      $"date".cast(LongType).cast(TimestampType).as("timestamp"),
      $"date".cast(LongType).cast(TimestampType).cast(DateType).as("date"),
      $"tid".cast(IntegerType),
      $"price".cast(DoubleType),
      $"type".cast(BooleanType).as("sell"),
      $"amount".cast(DoubleType))
      .as[Transaction]
  }

  def readTransactions(jsonTxs: IO[String])(implicit spark: SparkSession): IO[Dataset[Transaction]] = {
    jsonTxs.map(json => httpToDomainTransactions(jsonToHttpTransactions(json)))
  }

  def filterTxs(transactions: Dataset[Transaction], fromInstant: Instant, untilInstant: Instant)
  : Dataset[Transaction] = {
    import transactions.sparkSession.implicits._
    val filtered = transactions.filter(
      ($"timestamp" >= lit(fromInstant.getEpochSecond).cast(TimestampType)) &&
        ($"timestamp" < lit(untilInstant.getEpochSecond).cast(TimestampType)))
    println(s"filtered ${filtered.count()}/${transactions.count()} from $fromInstant until $untilInstant")
    filtered
  }

  def unsafeSave(transactions: Dataset[Transaction], path: URI): Unit =
    transactions
      .write
      .mode(SaveMode.Append)
      .partitionBy("date")
      .parquet(path.toString)


  def save(transactions: Dataset[Transaction], path: URI): IO[Unit] =
    IO(unsafeSave(transactions, path))


}
