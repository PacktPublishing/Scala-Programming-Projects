package coinyser

import java.io.File
import java.sql.Timestamp

import cats.effect.IO
import org.apache.spark.sql.test.SharedSparkSession
import org.scalatest.{Assertion, Matchers, WordSpec}

class BatchProducerIT extends WordSpec with Matchers with SharedSparkSession {

  import testImplicits._

  "BatchProducer.unsafeSave" should {
    "save a Dataset[Transaction] to parquet" in withTempDir { tmpDir =>
      val transaction1 = Transaction(timestamp = new Timestamp(1532365695000L), tid = 70683282, price = 7740.00, sell = false, amount = 0.10041719)
      val transaction2 = Transaction(timestamp = new Timestamp(1532365693000L), tid = 70683281, price = 7739.99, sell = false, amount = 0.00148564)
      val sourceDS = Seq(transaction1, transaction2).toDS()

      val uri = tmpDir.toURI
      BatchProducer.unsafeSave(sourceDS, uri)
      tmpDir.list() should contain("date=2018-07-23")
      val readDS = spark.read.parquet(uri.toString).as[Transaction]
      spark.read.parquet(uri + "/date=2018-07-23").show()
      sourceDS.collect() should contain theSameElementsAs readDS.collect()
    }
  }

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
}
