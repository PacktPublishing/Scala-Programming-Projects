package coinyser

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.TimeZone

import cats.effect.IO
import com.fasterxml.jackson.databind.{ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.pusher.client.{Client}
import com.pusher.client.channel.SubscriptionEventListener
import com.typesafe.scalalogging.StrictLogging
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

object StreamingProducer extends StrictLogging {
  def convertWsTransaction(wsTx: WebsocketTransaction): Transaction =
    Transaction(
      timestamp = new Timestamp(wsTx.timestamp.toLong * 1000),
      tid = wsTx.id,
      price = wsTx.price,
      sell = wsTx.`type` == 1,
      amount = wsTx.amount)

  val mapper: ObjectMapper = {
    val m = new ObjectMapper()
    m.registerModule(DefaultScalaModule)
    // Issue with Spark reading seconds, TODO create a StackOverflow thread
    m.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    // Very important: the storage must be in UTC
    sdf.setTimeZone(TimeZone.getTimeZone("UTC"))
    m.setDateFormat(sdf)
    m
  }

  def deserializeWebsocketTransaction(s: String): WebsocketTransaction = {
    mapper.readValue(s, classOf[WebsocketTransaction])
  }

  def serializeTransaction(tx: Transaction): String = {
    mapper.writeValueAsString(tx)
  }

  def subscribe(pusher: Client)(onTradeReceived: String => Unit): IO[Unit] =
    for {
      _ <- IO(pusher.connect())
      channel <- IO(pusher.subscribe("live_trades"))

      _ <- IO(channel.bind("trade", new SubscriptionEventListener() {
        override def onEvent(channel: String, event: String, data: String): Unit = {
          logger.info(s"Received event: $event with data: $data")
          onTradeReceived(data)
        }
      }))
    } yield ()


  def start(pusher: Client, config: KafkaConfig): IO[Unit] = {
    val props = Map(
      ("bootstrap.servers", "localhost:9092"),
      ("key.serializer", "org.apache.kafka.common.serialization.StringSerializer"),
      ("value.serializer", "org.apache.kafka.common.serialization.StringSerializer"))
    // TODO are these properties necessary ?
    /*    ("acks", "all"),
        ("retries", 0),
        ("batch.size", 16384),
        ("linger.ms", 1),
        ("buffer.memory", 33554432),
        */
    import scala.collection.JavaConversions._
    val kafkaProducer = new KafkaProducer[String, String](props.mapValues(_.asInstanceOf[AnyRef]))

    subscribe(pusher) { wsTx =>
      val tx = serializeTransaction(convertWsTransaction(deserializeWebsocketTransaction(wsTx)))
      // TODO pass topic in a context object
      kafkaProducer.send(new ProducerRecord[String, String](config.transactionsTopic, tx))
    }
  }
}
