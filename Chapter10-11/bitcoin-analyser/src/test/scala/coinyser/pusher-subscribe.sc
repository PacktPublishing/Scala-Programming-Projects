import com.pusher.client.Pusher
import com.pusher.client.channel.SubscriptionEventListener


val pusher = new Pusher("de504dc5763aeef9ff52")
pusher.connect()
val channel = pusher.subscribe("live_trades")

channel.bind("trade", new SubscriptionEventListener() {
  override def onEvent(channel: String, event: String, data: String): Unit = {
    println(s"Received event: $event with data: $data")
  }
})
