import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.libs.ws.WSClient

import scala.concurrent.duration.DurationInt
import scala.concurrent.Await
import org.scalatest._
import Matchers._

class IntegrationSpec extends PlaySpec with GuiceOneServerPerSuite {
  "Application" should {
    val wsClient = app.injector.instanceOf[WSClient]
    val myPublicAddress = s"localhost:$port"
    "work from within a browser" in {

      val testURL = s"http://$myPublicAddress/"

      val response = Await.result(wsClient.url(testURL).get(), 1 seconds)

      response.body should include ("shouts out")
    }
  }
}
