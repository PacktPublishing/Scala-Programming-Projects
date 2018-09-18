package coinyser

import com.pusher.client.Client
import com.pusher.client.channel._
import com.pusher.client.connection.{Connection, ConnectionEventListener, ConnectionState}

class FakePusher(val fakeTrades: Vector[String]) extends Client {
  var connected = false


  def subscribe(channelName: String): Channel = new FakeChannel(channelName, fakeTrades)

  def connect(): Unit = {
    connected = true
  }

  def disconnect(): Unit = ???


  def subscribe(channelName: String, listener: ChannelEventListener, eventNames: String*): Channel = ???

  def getPrivateChannel(channelName: String): PrivateChannel = ???

  def subscribePrivate(channelName: String): PrivateChannel = ???

  def subscribePrivate(channelName: String, listener: PrivateChannelEventListener, eventNames: String*): PrivateChannel = ???

  def subscribePresence(channelName: String): PresenceChannel = ???

  def subscribePresence(channelName: String, listener: PresenceChannelEventListener, eventNames: String*): PresenceChannel = ???

  def getPresenceChannel(channelName: String): PresenceChannel = ???

  def getConnection: Connection = ???

  def getChannel(channelName: String): Channel = ???

  def unsubscribe(channelName: String): Unit = ???


  def connect(eventListener: ConnectionEventListener, connectionStates: ConnectionState*): Unit = ???
}

class FakeChannel(val channelName: String, fakeTrades: Vector[String]) extends Channel {
  def getName: String = ???

  def isSubscribed: Boolean = ???

  def bind(eventName: String, listener: SubscriptionEventListener): Unit = {
    fakeTrades foreach { t =>
      listener.onEvent(channelName, eventName, t)
    }
  }

  def unbind(eventName: String, listener: SubscriptionEventListener): Unit = ???
}
