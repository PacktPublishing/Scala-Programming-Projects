import dao.CartDao
import io.fscala.shopping.shared.{Cart, ProductInCart}
import org.scalatest.Matchers._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.RecoverMethods._
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CartDaoSpec extends PlaySpec with ScalaFutures with GuiceOneAppPerSuite {

  "CartDao" should {
    val app2dao = Application.instanceCache[CartDao]
    "be empty on database creation" in {
      val dao: CartDao = app2dao(app)

      dao.all().futureValue shouldBe empty
    }

    "accept to add new cart" in {
      val dao: CartDao = app2dao(app)
      val user = "userAdd"

      val expected = Set(
        Cart(user, "ALD1", 1),
        Cart(user, "BEO1", 5)
      )
      val noise = Set(
        Cart("userNoise", "ALD2", 10)
      )
      val allCarts = expected ++ noise

      val insertFutures = allCarts.map(dao.insert)

      whenReady(Future.sequence(insertFutures)) { _ =>
        dao.cart4(user).futureValue should contain theSameElementsAs expected
        dao.all().futureValue.size should equal(allCarts.size)
      }
    }
    "error thrown when adding a cart with same user and productCode" in {
      val dao: CartDao = app2dao(app)
      val user = "userAdd"

      val expected = Set(
        Cart(user, "ALD1", 1),
        Cart(user, "BEO1", 5)
      )
      val noise = Set(
        Cart(user, "ALD1", 10)
      )
      val allCarts = expected ++ noise


      val insertFutures = allCarts.map(dao.insert)

      recoverToSucceededIf[org.h2.jdbc.JdbcSQLException]{
        Future.sequence(insertFutures)
      }
    }

    "accept to remove a product in a cart" in {
      val dao: CartDao = app2dao(app)
      val user = "userRmv"
      val initial = Vector(
        Cart(user, "ALD1", 1),
        Cart(user, "BEO1", 5)
      )
      val expected = Vector(Cart(user, "ALD1", 1))

      whenReady(Future.sequence(initial.map(dao.insert(_)))) { _ =>
        dao.remove(ProductInCart(user, "BEO1")).futureValue
        dao.cart4(user).futureValue should contain theSameElementsAs (expected)
      }
    }

    "accept to update quantities of an item in a cart" in {
      val dao: CartDao = app2dao(app)
      val user = "userUpd"
      val initial = Vector(Cart(user, "ALD1", 1))
      val expected = Vector(Cart(user, "ALD1", 5))

      whenReady(Future.sequence(initial.map(dao.insert(_)))) { _ =>
        dao.update(Cart(user, "ALD1", 5)).futureValue
        dao.cart4(user).futureValue should contain theSameElementsAs (expected)
      }
    }
  }
}
