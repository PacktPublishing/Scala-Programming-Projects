import cats.Applicative
import cats.data.{Validated, ValidatedNel}
import cats.implicits._

Applicative[Option].pure(1)
// res0: Option[Int] = Some(1)
3.pure[Option]
// res1: Option[Int] = Some(3)

type Result[A] = ValidatedNel[Throwable, A]
Applicative[Result].pure("hi pure")
// res2: Result[String] = Valid(hi pure)
"hi pure".pure[Result]
// res3: Result[String] = Valid(hi pure)

// Laws: applicative identity
val fa = Option(1)
type A = Int
type F[X] = Option[X]
((identity[A] _).pure[F] <*> fa)  ==  fa

// Traverse
def parseIntO(s: String): Option[Int] = Either.catchNonFatal(s.toInt).toOption
Vector("1", "2" , "3").traverse(parseIntO)
// res5: Option[Vector[Int]] = Some(Vector(1, 2, 3))
Vector("1", "boom" , "3").traverse(parseIntO)
// res6: Option[Vector[Int]] = None

def parseIntV(s: String): ValidatedNel[Throwable, Int] = Validated.catchNonFatal(s.toInt).toValidatedNel
Vector("1", "2" , "3").traverse(parseIntV)
// res7: ValidatedNel[Throwable, Vector[Int]] = Valid(Vector(1, 2, 3))
Vector("1", "boom" , "crash").traverse(parseIntV)
// res8: ValidatedNel[Throwable, Vector[Int]] =
// Invalid(NonEmptyList(
//   NumberFormatException: For input string: "boom",
//   NumberFormatException: For input string: "crash"))

val vecOpt: Vector[Option[Int]] = Vector(Option(1), Option(2), Option(3))
val optVec: Option[Vector[Int]] = vecOpt.sequence
// optVec: Option[Vector[Int]] = Some(Vector(1, 2, 3))

import scala.concurrent._
import ExecutionContext.Implicits.global
import duration.Duration

val vecFut: Vector[Future[Int]] = Vector(Future(1), Future(2), Future(3))
val futVec: Future[Vector[Int]] = vecFut.sequence

Await.result(futVec, Duration.Inf)
// res9: Vector[Int] = Vector(1, 2, 3)


