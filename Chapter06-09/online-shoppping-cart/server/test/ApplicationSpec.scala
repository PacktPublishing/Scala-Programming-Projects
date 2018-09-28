import org.scalatest.Matchers._
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.libs.ws.WSClient
import play.api.test.Helpers._
import scala.concurrent.duration._



class ApplicationSpec extends PlaySpec with ScalaFutures with GuiceOneServerPerSuite {
  "Application" should {
    val wsClient = app.injector.instanceOf[WSClient]
    val myPublicAddress = s"localhost:$port"

    "send 404 on a bad request" in {
      val testURL = s"http://$myPublicAddress/boom"

      whenReady(wsClient.url(testURL).get(), Timeout(1 second)) { response =>
        response.status mustBe NOT_FOUND
      }
    }

    "render the index page" in {
      val testURL = s"http://$myPublicAddress/"

      whenReady(wsClient.url(testURL).get(), Timeout(1 second)) { response =>
        response.status mustBe OK
        response.contentType should include("text/html")
      }
    }
  }
}