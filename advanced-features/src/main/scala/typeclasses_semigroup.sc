import cats.implicits._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

1 |+| 2
// res0: Int = 3
"Hello " |+| "World !"
// res1: String = Hello World !
(1, 2, "Hello ") |+| (2, 4, "World !")
// res2: (Int, Int, String) = (3,6,Hello World !)

Vector(1, 2) |+| Vector(3, 4)
// res3: Vector[Int] = Vector(1, 2, 3, 4)
Option(1) |+| Option(2)
// res4: Option[Int] = Some(3)
Option(1) |+|  None |+| Option(2)
// res5: Option[Int] = Some(3)


1.asRight |+| 2.asRight
// res6: Either[B,Int] = Right(3)
1.asRight[String] |+| 2.asRight |+| "error".asLeft
// res7: Either[String,Int] = Left(error)
"error1".asLeft[Int] |+| "error2".asLeft
// res8: Either[String,Int] = Left(error1)

// Exercise
1.validNel[String] |+| 2.validNel
// res9: Validated[NonEmptyList[String],Int] = Valid(3)
1.validNel[String] |+| "error1".invalidNel |+| "error2".invalidNel
// res10: Validated[NonEmptyList[String],Int] = Invalid(NonEmptyList(error1, error2))


