trait Combine[A] {
  def combine(x: A, y: A): A
}

object Combine {
  def apply[A](implicit combineA: Combine[A]): Combine[A] = combineA

  implicit val combineInt: Combine[Int] = new Combine[Int] {
    override def combine(x: Int, y: Int): Int = x + y
  }

  implicit val combineString: Combine[String] = new Combine[String] {
    override def combine(x: String, y: String) = x + y
  }

  implicit def combineOption[A: Combine]: Combine[Option[A]] = new Combine[Option[A]] {
    override def combine(optX: Option[A], optY: Option[A]): Option[A] =
      for {
        x <- optX
        y <- optY
      } yield Combine[A].combine(x, y)
  }



  implicit class CombineOps[A](val x: A)(implicit combineA: Combine[A]) {
    def combine(y: A): A = combineA.combine(x, y)
  }

}


Combine[Int].combine(1, 2)
// res0: Int = 3
Combine[String].combine("Hello", " type class")
// res1: String = Hello type class

import Combine.CombineOps
2.combine(3)
// res2: Int = 5
"abc".combine("def")
// res3: String = abcdef

Option(3).combine(Option(4))
// res4: Option[Int] = Some(7)
Option(3) combine Option.empty
// res5: Option[Int] = None
Option("Hello ") combine Option(" world")
// res6: Option[String] = Some(Hello  world)