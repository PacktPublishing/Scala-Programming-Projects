import cats.kernel.Monoid
import cats.implicits._

def monoidMonad[A] = new Monoid[Vector[A] => Vector[A]] {
  override def empty = identity

  override def combine(f: Vector[A] => Vector[A], g: Vector[A] => Vector[A])
  : Vector[A] => Vector[A] =
    xs => f(xs) flatMap(y => g(Vector(y)))
}

implicit val monoidMonadInt: Monoid[Vector[Int] => Vector[Int]] = monoidMonad[Int]

val fn = ((xs: Vector[Int]) => xs.map(_+1)) |+| ((xs: Vector[Int]) => xs.map(_*2))
fn(Vector(1,2,3))
Vector(1,2,3) flatMap (i => Vector(i * 2))

// TODO try KleisliMonoid
