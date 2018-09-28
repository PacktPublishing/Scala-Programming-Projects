package dao

import io.fscala.shopping.shared
import io.fscala.shopping.shared.{Cart, CartKey, Product, ProductInCart}
import javax.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}


class ProductDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def all(): Future[Seq[shared.Product]] = db.run(products.result)

  def insert(product: shared.Product): Future[Unit] = db.run(products insertOrUpdate product).map { _ => () }

  private class ProductTable(tag: Tag) extends Table[shared.Product](tag, "PRODUCT") {
    def name = column[String]("NAME")

    def code = column[String]("CODE")

    def description = column[String]("DESCRIPTION")

    def price = column[Double]("PRICE")

    override def * = (name, code, description, price) <> (Product.tupled, Product.unapply)
  }

  private val products = TableQuery[ProductTable]
}

class CartDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._


  def cart4(usr: String): Future[Seq[Cart]] = db.run(carts.filter(_.user === usr).result)

  def insert(cart: Cart): Future[_] = db.run(carts += cart)

  def remove(cart: ProductInCart): Future[Int] = db.run(carts.filter(c => matchKey(c, cart)).delete)

  def update(cart: Cart): Future[Int] = {
    val q = for {
      c <- carts if matchKey(c, cart)
    } yield c.quantity
    db.run(q.update(cart.quantity))
  }

  private def matchKey(c: CartTable, cart: CartKey): Rep[Boolean] = {
    c.user === cart.user && c.productCode === cart.productCode
  }

  def all(): Future[Seq[Cart]] = db.run(carts.result)

  private class CartTable(tag: Tag) extends Table[Cart](tag, "CART") {

    def user = column[String]("USER")

    def productCode = column[String]("CODE")

    def quantity = column[Int]("QTY")

    override def * = (user, productCode, quantity) <> (Cart.tupled, Cart.unapply)
  }

  private val carts = TableQuery[CartTable]

}


