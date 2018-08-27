package coinyser

import java.net.{URI, URL}

import cats.effect.IO
import org.apache.spark.sql.SparkSession

import scala.concurrent.duration._
import scala.io.Source
import scala.concurrent.ExecutionContext.Implicits.global

// TODO use IOApp
object BatchProducerApp extends App {

  // In prod, should be a distributed filesystem
  val checkpointDir = "/tmp/coinyser/TransactionDataProducerApp"
  implicit val appConfig: AppConfig = AppConfig(
    topic = "transaction_btcusd",
    bootstrapServers = "localhost:9092",
    transactionStorePath = new URI("/home/mikael/projects/scala-fundamentals/bitcoin-analyser/data/transactions2/currency_pair=btcusd"),
    firstInterval = 1.day,
    intervalBetweenReads = 1.hour
  )

  implicit val spark: SparkSession = SparkSession
    .builder
    .master("local[*]")
    .appName("coinyser")
    .getOrCreate()

  implicit val appContext: AppContext = new AppContext()

  val initialJsonTxs = IO {
    Source.fromURL(new URL("https://www.bitstamp.net/api/v2/transactions/btcusd/?time=day")).mkString
  }

  val nextJsonTxs = IO {
    Source.fromURL(new URL("https://www.bitstamp.net/api/v2/transactions/btcusd/?time=hour")).mkString
  }
  BatchProducer.processRepeatedly(initialJsonTxs, nextJsonTxs).unsafeRunSync()
}
