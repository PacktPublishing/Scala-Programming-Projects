package coinyser

import java.net.{URI, URL}

import cats.effect.{ExitCode, IO, IOApp}
import coinyser.BatchProducer.{httpToDomainTransactions, jsonToHttpTransactions}
import com.typesafe.scalalogging.StrictLogging
import org.apache.spark.sql.{Dataset, SparkSession}

import scala.io.Source

class BatchProducerApp extends IOApp with StrictLogging {

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
    jsonIO.map(json => httpToDomainTransactions(jsonToHttpTransactions(json)))
  }

  val initialJsonTxs: IO[Dataset[Transaction]] = transactionsIO("day")
  val nextJsonTxs: IO[Dataset[Transaction]] = transactionsIO("hour")

  def run(args: List[String]): IO[ExitCode] =
    BatchProducer.processRepeatedly(initialJsonTxs, nextJsonTxs).map(_ => ExitCode.Success)

}

object BatchProducerAppSpark extends BatchProducerApp
