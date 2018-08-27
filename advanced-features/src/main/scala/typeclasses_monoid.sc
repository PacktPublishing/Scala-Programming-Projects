import cats.implicits._
import cats.kernel.Monoid


Monoid[Int].empty
// res0: Int = 0
Monoid[String].empty
// res1: String =
Monoid[Option[Double]].empty
// res2: Option[Double] = None
Monoid[Vector[Int]].empty
// res2: Vector[Int] = Vector()
Monoid[Either[String, Int]].empty
// res4: Either[String,Int] = Right(0)

(3 |+| Monoid[Int].empty) == 3
("Hello identity" |+| Monoid[String].empty) == "Hello identity"
(Option(3) |+| Monoid[Option[Int]].empty) == Option(3)

Vector(1, 2, 3).combineAll
// res8: Int = 6

Vector(1, 2, 3).foldLeft(0) { case (acc, i) => acc + i }

Vector("1", "2", "3").foldMap(s => (s, s.toInt))
// res10: (String, Int) = (123,6)

// Exercise
val monoidMultInt: Monoid[Int] = new Monoid[Int] {
  override def empty: Int = 1

  override def combine(x: Int, y: Int): Int = x * y
}
Vector(1, 2, 3, 4).combineAll(monoidMultInt)

// Exercise
val (count, sum) = Vector(10, 12, 14, 8).foldMap(i => (1, i))
val average = sum.toDouble / count

