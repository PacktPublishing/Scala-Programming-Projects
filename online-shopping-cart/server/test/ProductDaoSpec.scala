

import dao.ProductDao
import io.fscala.shopping.shared.Product
import org.scalatest.Matchers._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application


class ProductDaoSpec extends PlaySpec with ScalaFutures with GuiceOneAppPerSuite {
  "ProductDao" should {
    "Have default rows on database creation" in {
      val app2dao = Application.instanceCache[ProductDao]
      val dao: ProductDao = app2dao(app)

      val expected = Set(
        Product("PEPPER", "ALD2", "PEPPER is a robot moving with wheels and with a screen as human interaction", 7000),
        Product("NAO", "ALD1", "NAO is an humanoid robot.", 3500),
        Product("BEOBOT", "BEO1", "Beobot is a multipurpose robot.", 159.0)
      )

      dao.all().futureValue should contain theSameElementsAs (expected)
    }
  }
}
