package coinyser

import java.io.{BufferedOutputStream, StringReader}
import java.nio.CharBuffer
import java.sql.Timestamp

import cats.effect.IO
import org.apache.spark.sql._
import org.apache.spark.sql.test.SharedSparkSession
import org.scalatest.{Matchers, WordSpec}


class BatchProducerSpec extends WordSpec with Matchers with SharedSparkSession {

  val httpTransaction1 = HttpTransaction("1532365695", "70683282", "7740.00", "0", "0.10041719")
  val httpTransaction2 = HttpTransaction("1532365693", "70683281", "7739.99", "0", "0.00148564")

  "BatchProducer.jsonToHttpTransaction" should {
    "create a Dataset[HttpTransaction] from a Json string" in {
      val json =
        """[{"date": "1532365695", "tid": "70683282", "price": "7740.00", "type": "0", "amount": "0.10041719"},
          |{"date": "1532365693", "tid": "70683281", "price": "7739.99", "type": "0", "amount": "0.00148564"}]""".stripMargin

      val ds: Dataset[HttpTransaction] = BatchProducer.jsonToHttpTransactions(json)
      ds.collect() should contain theSameElementsAs Seq(httpTransaction1, httpTransaction2)
    }
  }

  "BatchProducer.httpToDomainTransactions" should {
    "transform a Dataset[HttpTransaction] into a Dataset[Transaction]" in {
      import testImplicits._
      val source: Dataset[HttpTransaction] = Seq(httpTransaction1, httpTransaction2).toDS()
      val target: Dataset[Transaction] = BatchProducer.httpToDomainTransactions(source)
      val transaction1 = Transaction(timestamp = new Timestamp(1532365695000L), tid = 70683282, price = 7740.00, sell = false, amount = 0.10041719)
      val transaction2 = Transaction(timestamp = new Timestamp(1532365693000L), tid = 70683281, price = 7739.99, sell = false, amount = 0.00148564)

      target.collect() should contain theSameElementsAs Seq(transaction1, transaction2)
    }
  }

}


