package actors


import akka.actor._
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax._
import io.fscala.shopping.shared.{CartEvent, WebsocketMessage}

object UserActor {
  def props(browser: ActorRef, browserManager :ActorRef) = Props(new UserActor(browser, browserManager))
}

class UserActor(browser: ActorRef, browserManager :ActorRef) extends Actor with ActorLogging {
  def receive = {
    case msg: String =>
      log.info("Received JSON message: {}",msg)
      decode[CartEvent](msg) match {
        case Right(cartEvent) =>
          log.info("Got {} message", cartEvent)
          browserManager forward cartEvent
        case Left(error) => log.info("Unhandled message : {}",error)
      }

  }
}

