package coinyser

import java.io.{BufferedReader, InputStreamReader}
import java.net.{URI, URL}

import cats.effect.{ExitCode, IO, IOApp}
import com.typesafe.scalalogging.StrictLogging
import org.apache.spark.sql.{Dataset, SparkSession}

import scala.io.Source

object BatchProducerApp extends IOApp with StrictLogging {

  implicit val spark: SparkSession = SparkSession.builder.master("local[*]").getOrCreate()
  implicit val appContext: AppContext = new AppContext(new URI("./data/transactions"))

  def bitstampUrl(timeParam: String): URL =
    new URL("https://www.bitstamp.net/api/v2/transactions/btcusd?time=" + timeParam)

  def transactionsIO(timeParam: String): IO[Dataset[Transaction]] = {
    val url = bitstampUrl(timeParam)
    val jsonIO = IO {
      logger.info(s"calling $url")
      Source.fromURL(url).mkString
    }
    BatchProducer.readTransactions(jsonIO)
  }


  def optimizedTransactionsIO(timeParam: String): IO[Dataset[Transaction]] = {
    val url = bitstampUrl(timeParam)
    val jsonIO = IO {
      logger.info(s"calling $url")
      new BufferedReader(new InputStreamReader(url.openStream()))
    }
    BatchProducer.optimizedReadTransactions(jsonIO)
  }


  // If you want to use the optimized JSON reading,
  // comment the next two lines, and uncomment the next two.
  // You should see that the parquet files are split in more parts when using the optimized version,
  // which indicates that we achieved a better parallelism
  val initialJsonTxs: IO[Dataset[Transaction]] = transactionsIO("day")
  val nextJsonTxs: IO[Dataset[Transaction]] = transactionsIO("hour")
//  val initialJsonTxs: IO[Dataset[Transaction]] = optimizedTransactionsIO("day")
//  val nextJsonTxs: IO[Dataset[Transaction]] = optimizedTransactionsIO("hour")

  def run(args: List[String]): IO[ExitCode] =
    BatchProducer.processRepeatedly(initialJsonTxs, nextJsonTxs).map(_ => ExitCode.Success)

}
