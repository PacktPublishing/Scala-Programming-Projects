package coinyser

import java.util.Properties
import com.pusher.client.Pusher
import com.pusher.client.connection.ConnectionEventListener
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.receiver.Receiver
import com.pusher.client.channel.SubscriptionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scala.concurrent.duration._
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord

import scala.collection.JavaConversions._

// Use log compaction to ensure high availability ? (2 producers running concurrently)
// TODO use IOApp
object StreamingProducerApp extends App {
  val config = KafkaConfig("localhost:9092", "transactions_draft4")

  val pusher = new Pusher("de504dc5763aeef9ff52")
  StreamingProducer.start(pusher, config).unsafeRunSync()

  // TODO is there an IO solution for that ?
  Thread.sleep(Long.MaxValue)
}




