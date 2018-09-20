package coinyser

import java.sql.Timestamp

import coinyser.StreamingProducerSpec._
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{Matchers, WordSpec}

class StreamingProducerSpec extends WordSpec with Matchers with TypeCheckedTripleEquals {

  "StreamingProducer.deserializeWebsocketTransaction" should {
    "deserialize a valid String to a WebsocketTransaction" in {
      val str =
        """{"amount": 0.045318270000000001, "buy_order_id": 1969499130,
          |"sell_order_id": 1969495276, "amount_str": "0.04531827",
          |"price_str": "6339.73", "timestamp": "1533797395",
          |"price": 6339.7299999999996, "type": 0, "id": 71826763}""".stripMargin
      StreamingProducer.deserializeWebsocketTransaction(str) should
        ===(SampleWebsocketTransaction)
    }
  }

  "StreamingProducer.convertWsTransaction" should {
    "convert a WebSocketTransaction to a Transaction" in {
      StreamingProducer.convertWsTransaction(SampleWebsocketTransaction) should
        ===(SampleTransaction)
    }
  }

  "StreamingProducer.serializeTransaction" should {
    "serialize a Transaction to a String" in {
      StreamingProducer.serializeTransaction(SampleTransaction) should
        ===(SampleJsonTransaction)
    }
  }

  "StreamingProducer.subscribe" should {
    "register a callback that receives live trades" in {
      val pusher = new FakePusher(Vector("a", "b", "c"))
      var receivedTrades = Vector.empty[String]
      val io = StreamingProducer.subscribe(pusher) { trade => receivedTrades = receivedTrades :+ trade }
      io.unsafeRunSync()
      receivedTrades should ===(Vector("a", "b", "c"))
    }
  }
}

object StreamingProducerSpec {
  val SampleWebsocketTransaction = WebsocketTransaction(
    amount = 0.04531827, buy_order_id = 1969499130, sell_order_id = 1969495276,
    amount_str = "0.04531827", price_str = "6339.73", timestamp = "1533797395",
    price = 6339.73, `type` = 0, id = 71826763)

  val SampleTransaction = Transaction(
    timestamp = new Timestamp(1533797395000L), tid = 71826763,
    price = 6339.73, sell = false, amount = 0.04531827)

  val SampleJsonTransaction =
    """{"timestamp":"2018-08-09 06:49:55",
      |"date":"2018-08-09","tid":71826763,"price":6339.73,"sell":false,
      |"amount":0.04531827}""".stripMargin

}
