package controllers

import javax.inject._
import play.api.libs.circe.Circe
import play.api.mvc._


@Singleton
class Application @Inject()(cc: ControllerComponents) extends AbstractController(cc) with Circe {

  def index = Action {
    Ok(views.html.index("Shopping Page"))
  }

}
