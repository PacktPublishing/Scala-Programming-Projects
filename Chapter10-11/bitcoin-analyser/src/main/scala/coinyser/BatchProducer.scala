package coinyser

import java.net.URI
import java.time.Instant
import java.util.Scanner
import java.util.concurrent.TimeUnit

import cats.Monad
import cats.effect.{IO, Timer}
import cats.implicits._
import org.apache.spark.sql.functions.{explode, from_json, lit}
import org.apache.spark.sql.types._
import org.apache.spark.sql.{Dataset, SaveMode, SparkSession}

import scala.concurrent.duration._

class AppContext(val transactionStorePath: URI)
                (implicit val spark: SparkSession,
                 implicit val timer: Timer[IO])

object BatchProducer {
  val WaitTime: FiniteDuration = 59.minute
  /** Number of seconds required by the API to make a transaction visible */
  val ApiLag: FiniteDuration = 5.seconds


  def processRepeatedly(initialJsonTxs: IO[Dataset[Transaction]], jsonTxs: IO[Dataset[Transaction]])
                       (implicit appContext: AppContext): IO[Unit] = {
    import appContext._

    for {
      beforeRead <- currentInstant
      firstEnd = beforeRead.minusSeconds(ApiLag.toSeconds)
      firstTxs <- initialJsonTxs
      firstStart = truncateInstant(firstEnd, 1.day)
      _ <- Monad[IO].tailRecM((firstTxs, firstStart, firstEnd)) {
        case (txs, start, instant) =>
          processOneBatch(jsonTxs, txs, start, instant).map(_.asLeft)
      }
    } yield ()
  }

  def processOneBatch(fetchNextTransactions: IO[Dataset[Transaction]],
                      transactions: Dataset[Transaction],
                      saveStart: Instant,
                      saveEnd: Instant)(implicit appCtx: AppContext)
  : IO[(Dataset[Transaction], Instant, Instant)] = {
    import appCtx._

    val transactionsToSave = filterTxs(transactions, saveStart, saveEnd)
    for {
      _ <- BatchProducer.save(transactionsToSave, appCtx.transactionStorePath)
      _ <- IO.sleep(WaitTime)

      beforeRead <- currentInstant
      // We are sure that lastTransactions contain all transactions until end
      end = beforeRead.minusSeconds(ApiLag.toSeconds)
      nextTransactions <- fetchNextTransactions
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
    val txSchema: StructType = spark.emptyDataset[HttpTransaction].schema
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
    transactions.filter(
      ($"timestamp" >= lit(fromInstant.getEpochSecond).cast(TimestampType)) &&
        ($"timestamp" < lit(untilInstant.getEpochSecond).cast(TimestampType)))
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
