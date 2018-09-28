import cats.Functor
import cats.implicits._

def addOne[F[_] : Functor](fa: F[Int]): F[Int] = fa.map(_ + 1)
addOne(Vector(1, 2, 3))
// res0: Vector[Int] = Vector(2, 3, 4)
addOne(Option(1))
// res1: Option[Int] = Some(2)
addOne(1.asRight)
// res2: Either[Nothing,Int] = Right(2)
addOne[cats.Id](1)

// Exercise
val brokenFunctor: Functor[Vector] = new Functor[Vector] {
  override def map[A, B](fa: Vector[A])(f: A => B): Vector[B] = {
    fa.map(f).reverse
  }
}

val fa = Vector(1, 2, 3)
val f: Int => Int = _  + 1
val g: Int => Int = _  * 2
brokenFunctor.map(fa)(identity) == fa
brokenFunctor.map(brokenFunctor.map(fa)(f))(g) == brokenFunctor.map(fa)(f andThen g)


def square(x: Double): Double = x * x
def squareVector: Vector[Double] => Vector[Double] =
  Functor[Vector].lift(square)
squareVector(Vector(1, 2, 3))
// res0: Vector[Double] = Vector(1.0, 4.0, 9.0)

def squareOption: Option[Double] => Option[Double] =
  Functor[Option].lift(square)
squareOption(Some(3))
// res1: Option[Double] = Some(9.0)

Vector("Functors", "are", "great").fproduct(_.length).toMap
// res2: Map[String,Int] = Map(Functors -> 8, are -> 3, great -> 5)
