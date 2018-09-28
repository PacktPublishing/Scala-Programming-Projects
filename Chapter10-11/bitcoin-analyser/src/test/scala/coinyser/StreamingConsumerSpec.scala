package coinyser

import java.util.TimeZone

import coinyser.StreamingProducerSpec.SampleJsonTransaction
import org.apache.spark.sql.{Dataset, SparkSession}
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{BeforeAndAfterAll, EitherValues, Matchers, WordSpec}
import org.scalatest.concurrent.Eventually

class StreamingConsumerSpec extends WordSpec with Matchers with BeforeAndAfterAll with TypeCheckedTripleEquals with Eventually with EitherValues {
  TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

  implicit val spark: SparkSession = SparkSession
    .builder
    .master("local[*]")
    .appName("coinyser")
    .getOrCreate()

  import spark.implicits._

  "StreamingConsumer.fromJson" should {
    "convert a DataFrame of Json values to a Dataset[Transaction]" in {
      val df = Seq(SampleJsonTransaction).toDF("value")
      val ds: Dataset[Transaction] = StreamingConsumer.fromJson(df)
      ds.collect().toSeq should === (Seq(StreamingProducerSpec.SampleTransaction))


    }

  }

  // TODO test transactionStream

}
