package controllers

import actors.{BrowserManagerActor, BrowserActor}
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.Materializer
import javax.inject._
import play.api.Logger
import play.api.libs.streams.ActorFlow
import play.api.mvc.{AbstractController, ControllerComponents, WebSocket}

@Singleton
class WebSockets @Inject()(
                            implicit actorSystem: ActorSystem,
                            materializer: Materializer,
                            cc: ControllerComponents) extends AbstractController(cc) {

  val managerActor: ActorRef = actorSystem.actorOf(BrowserManagerActor.props(), "manager-actor")

  def cartEventWS: WebSocket = WebSocket.accept[String, String] {
    implicit request =>
      ActorFlow.actorRef { out =>
        Logger.info(s"Got a new websocket connection from ${request.host}")
        managerActor ! BrowserManagerActor.AddBrowser(out)
        BrowserActor.props(managerActor)
      }
  }
}
