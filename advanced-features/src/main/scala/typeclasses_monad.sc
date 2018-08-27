import cats.{Id, Monad}
import cats.implicits._


val fa: Vector[Int] = Vector(1)
val f: Int => Vector[String] = i => Vector(i.toString)
val g: String => Vector[Double] = s => Vector(s.toDouble)
type F[X] = Vector[X]
// Law: FlatMap associativity
((fa flatMap f) flatMap g) == (fa flatMap(f(_) flatMap g))

// Law: Left/Right identity
val a = 2
Monad[F].pure(a).flatMap(f) == f(a)
fa.flatMap(Monad[F].pure) == fa

case class Item(id: Int, label: String, price: Double, category: String)


trait ItemApi[F[_]] {
  def findAllItems: F[Vector[Item]]

  def saveItem(item: Item): F[Unit]
}


def startSalesSeason[F[_] : Monad](api: ItemApi[F]): F[Unit] = {
  for {
    items <- api.findAllItems
    _ <- items.traverse { item =>
      val discount = if (item.category == "shoes") 0.80 else 0.70
      val discountedItem = item.copy(price = item.price * discount)
      api.saveItem(discountedItem)
    }
  } yield ()
}

// Exercise: implement Api using the Id monad
object IdItemApi extends ItemApi[Id] {
  var allItems = Map[Int, Item](
    1 -> Item(1, "Nick Air Superjump size 10", 50.0, "shoes"),
    2 -> Item(2, "Luis Boutton handbag", 300.0, "handbags")
  )

  def findAllItems: Vector[Item] = allItems.values.toVector

  def saveItem(item: Item): Unit = {
    allItems = allItems + (item.id -> item)
  }
}

startSalesSeason(IdItemApi)
IdItemApi.findAllItems.foreach(println)
