import org.scalactic.{Equality, TolerantNumerics, TypeCheckedTripleEquals}
import org.scalatest.{Matchers, WordSpec}

class EqualitySpec extends WordSpec with Matchers with TypeCheckedTripleEquals{
  implicit val doubleEquality: Equality[Double] = TolerantNumerics.tolerantDoubleEquality(0.0001)

  implicit def vectorEquality[A](implicit eqA: Equality[A]): Equality[Vector[A]] = new Equality[Vector[A]] {
    override def areEqual(v1: Vector[A], b: Any): Boolean = b match {
      case v2: Vector[_] =>
        v1.size == v2.size &&
          v1.zip(v2).forall { case ((x, y)) => eqA.areEqual(x, y)}
      case _ => false
    }
  }

  "Equality" should {
    "allow to compare two Double with a tolerance" in {
      1.6 + 1.8 should ===(3.4)
    }

    "allow to compare two Vector[Double] with a tolerance" in {
      Vector(1.6 + 1.8, 0.0051) should === (Vector(3.4, 0.0052))
    }
  }
}
