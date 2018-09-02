package coinyser

import java.net.{URI, URL}

import cats.effect.{ExitCode, IO, IOApp}
import com.typesafe.scalalogging.StrictLogging
import org.apache.spark.sql.SparkSession

import scala.io.Source

object BatchProducerApp extends IOApp with StrictLogging {

  implicit val spark: SparkSession = SparkSession
    .builder
    .master("local[*]")
    .appName("coinyser")
    .getOrCreate()

  implicit val appContext: AppContext = new AppContext(transactionStorePath =
    // In Prod, use a distributed filesystem
    new URI("/home/mikael/projects/scala-fundamentals/bitcoin-analyser/data/transactions2/currency_pair=btcusd"))

  def jsonIO(params: String): IO[String] = {
    val url = new URL("https://www.bitstamp.net/api/v2/transactions/btcusd" + params)
    IO {
      logger.info(s"calling $url")
      Source.fromURL(url).mkString
    }
  }

  val initialJsonTxs: IO[String] = jsonIO("?time=day")
  val nextJsonTxs: IO[String] = jsonIO("?time=hour")

  def run(args: List[String]): IO[ExitCode] =
    BatchProducer.processRepeatedly(initialJsonTxs, nextJsonTxs).map(_ => ExitCode.Success)

}
