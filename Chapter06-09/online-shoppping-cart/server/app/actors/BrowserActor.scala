package actors


import akka.actor._
import io.circe.generic.auto._
import io.circe.parser.decode
import io.fscala.shopping.shared.CartEvent

object BrowserActor {
  def props(browserManager: ActorRef) = Props(new BrowserActor(browserManager))
}

class BrowserActor(browserManager: ActorRef) extends Actor with ActorLogging {
  def receive = {
    case msg: String =>
      log.info("Received JSON message: {}", msg)
      decode[CartEvent](msg) match {
        case Right(cartEvent) =>
          log.info("Got {} message", cartEvent)
          browserManager forward cartEvent
        case Left(error) => log.info("Unhandled message : {}", error)
      }

  }
}