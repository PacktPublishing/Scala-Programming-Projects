import cats.data._
import cats.data.Validated._
import cats.implicits._

val valid1: Validated[NonEmptyList[String], Int] = Valid(1)
// valid1: cats.data.Validated[cats.data.NonEmptyList[String],Int] = Valid(1)
val valid2: ValidatedNel[String, Int] = 2.validNel
// valid2: cats.data.ValidatedNel[String,Int] = Valid(2)
valid1.map2(valid2) { case (i1, i2) => i1 + i2 }
// res0: cats.data.Validated[cats.data.NonEmptyList[String],Int] = Valid(3)
(valid1, valid2).mapN { case (i1, i2) => i1 + i2 }
// res1: cats.data.ValidatedNel[String,Int] = Valid(3)


val invalid3: ValidatedNel[String, Int] = Invalid(NonEmptyList.of("error"))
val invalid4 = "another error".invalidNel[Int]
(valid1, valid2, invalid3, invalid4).mapN { case (i1, i2, i3, i4) => i1 + i2 + i3 + i4 }
// res2: cats.data.ValidatedNel[String,Int] = Invalid(NonEmptyList(error, another error))