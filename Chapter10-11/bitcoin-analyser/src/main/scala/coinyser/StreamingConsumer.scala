package coinyser

import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.apache.spark.sql.functions._

object StreamingConsumer {
  def fromJson(df: DataFrame): Dataset[Transaction] = {
    import df.sparkSession.implicits._
    val schema = Seq.empty[Transaction].toDS().schema
    df.select(from_json(col("value").cast("string"), schema).alias("v"))
      .select("v.*").as[Transaction]
  }

  def transactionStream(implicit spark: SparkSession, config: KafkaConfig): Dataset[Transaction] =
    fromJson(spark.readStream.format("kafka")
      .option("kafka.bootstrap.servers", config.bootStrapServers)
      .option("startingoffsets", "earliest")
      .option("subscribe", config.transactionsTopic)
      .load()
    )

}
