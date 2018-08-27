
import cats.data.Validated
import cats.implicits._


Option[String => String]("Hello " + _).ap(Some("Apply"))
// res0: Option[String] = Some(Hello Apply)
Option[String => String]("Hello " + _) <*> None
// res1: Option[String] = None
Option.empty[String => String] <*> Some("Apply")
// res2: Option[String] = None

val addOne: Int => Int = _ + 1
val multByTwo: Int => Int = _ * 2
Vector(addOne, multByTwo) <*> Vector(1, 2, 3)
// res3: Vector[Int] = Vector(2, 3, 4, 2, 4, 6)

def parseIntO(s: String): Option[Int] = Either.catchNonFatal(s.toInt).toOption
parseIntO("6").map2(parseIntO("2"))(_ / _)
// res4: Option[Int] = Some(3)
parseIntO("abc").map2(parseIntO("def"))(_ / _)
// res5: Option[Int] = None

def parseIntE(s: String): Either[Throwable, Int] = Either.catchNonFatal(s.toInt)
parseIntE("6").map2(parseIntE("2"))(_ / _)
// res6: Either[Throwable,Int] = Right(3)
parseIntE("abc").map2(parseIntE("3"))(_ / _)
// res7: Either[Throwable,Int] = Left(java.lang.NumberFormatException: For input string: "abc")

(parseIntE("1"), parseIntE("2"), parseIntE("3")).mapN( (a,b,c) => a + b + c)
// res8: Either[Throwable,Int] = Right(6)

import cats.data.ValidatedNel
def parseIntV(s: String): ValidatedNel[Throwable, Int] = Validated.catchNonFatal(s.toInt).toValidatedNel
(parseIntV("abc"), parseIntV("def"), parseIntV("3")).mapN( (a,b,c) => a + b + c)
// res9: ValidatedNel[Throwable,Int] = Invalid(NonEmptyList(
// java.lang.NumberFormatException: For input string: "abc",
// java.lang.NumberFormatException: For input string: "def"))

// Exercise: associativity law
val fa = Option(1)
val fb = Option(2)
val fc = Option(3)
(fa product (fb product fc)) == ((fa product fb) product fc).map { case ((a, b), c) => (a, (b, c))}

// Exercise: ap function composition
val fab: Option[Int => String] = Option(_.toString)
val fbc: Option[String => Double] = Option(_.toDouble / 2)
(fbc <*> (fab <*> fa)) == ((fbc.map(_.compose[Int] _) <*> fab) <*> fa)
