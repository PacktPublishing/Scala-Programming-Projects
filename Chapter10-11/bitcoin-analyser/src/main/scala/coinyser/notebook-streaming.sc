// The code in this worksheet is meant to be run in Zeppelin.
// However it can also can be run in IntelliJ: just paste it in a REPL session.

// These definitions are provided in Zeppelin, do not paste in paragraphs
import org.apache.spark.sql.{Dataset, SparkSession}
implicit val spark: SparkSession = SparkSession.builder.master("local[*]").appName("coinyser").getOrCreate()
import spark.implicits._
import org.apache.spark.sql.functions._
val z = new {
  def show[A](ds: Dataset[A]): Unit = ds.show(false)
}

// Paste the following code in the Notebook
case class Transaction(timestamp: java.sql.Timestamp,
                       date: String,
                       tid: Int,
                       price: Double,
                       sell: Boolean,
                       amount: Double)
val schema = Seq.empty[Transaction].toDS().schema

val dfStream = {
  spark.readStream.format("kafka")
    .option("kafka.bootstrap.servers", "localhost:9092")
    .option("startingoffsets", "latest")
    .option("subscribe", "transactions")
    .load()
    .select(
      from_json(col("value").cast("string"), schema)
        .alias("v")).select("v.*").as[Transaction]
}

val query = {
  dfStream
    .writeStream
    .format("memory")
    .queryName("transactionsStream")
    .outputMode("append")
    .start()
}

z.show(spark.table("transactionsStream").sort("timestamp"))


val aggDfStream = {
  dfStream
    .withWatermark("timestamp", "1 second")
    .groupBy(window($"timestamp", "10 seconds").as("window"))
    .agg(
      count($"tid").as("count"),
      avg("price").as("avgPrice"),
      stddev("price").as("stddevPrice"),
      last("price").as("lastPrice"),
      sum("amount").as("sumAmount")
    )
    .select("window.start", "count", "avgPrice", "lastPrice", "stddevPrice", "sumAmount")
}

val aggQuery = {
  aggDfStream
    .writeStream
    .format("memory")
    .queryName("aggregateStream")
    .outputMode("append")
    .start()
}

z.show(spark.table("aggregateStream").sort("start"))