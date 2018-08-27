package coinyser

import org.apache.spark.sql.{Dataset, SparkSession}
import org.apache.spark.sql.functions._

object StreamingConsumerApp extends App {

  implicit val spark: SparkSession = SparkSession
    .builder
    .master("local[*]")
    .appName("StreamingConsumerApp")
    .getOrCreate()

  implicit val config: KafkaConfig = KafkaConfig(
    bootStrapServers = "localhost:9092",
    transactionsTopic = "transactions_draft3"
  )

  val txStream: Dataset[Transaction] = StreamingConsumer.transactionStream

  import spark.implicits._

  // TODO move that to a Query class between batch and streaming
  val groupedStream = txStream
    .withWatermark("date", "1 second")
    .groupBy(window($"date", "1 minutes").as("window"))
    .agg(
      count($"tid").as("count"),
      avg("price").as("avgPrice"),
      stddev("price").as("stddevPrice"),
      last("price").as("lastPrice"),
      sum("amount").as("sumAmount")
    )
    .select("window.start", "count", "avgPrice", "lastPrice", "stddevPrice", "sumAmount")

  groupedStream
    .writeStream
    .format("console")
    .queryName("groupedTx")
    .outputMode("append")
    .start()


  Thread.sleep(Long.MaxValue)

}
