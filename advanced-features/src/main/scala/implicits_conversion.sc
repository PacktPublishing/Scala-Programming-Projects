import java.time.LocalDate
import java.time.temporal.ChronoUnit.DAYS

import scala.collection.immutable.StringOps


implicit def stringToLocalDate(s: String): LocalDate = LocalDate.parse(s)
"2018-09-01".getDayOfWeek
"2018-09-01".getYear
DAYS.between("2018-09-01", "2018-10-10")

//"2018".getMonth

class IntOps(val i: Int) {
  def square: Int = i * i
}
implicit def intToIntOps(i: Int): IntOps = new IntOps(i)

5.square

//implicit class IntOps2(val i: Int) extends AnyVal {
//  def square: Int = i * i
//}
//
//5.square

"abcd".reverse
val abcd: StringOps = Predef.augmentString("abcd")
abcd.reverse

case class Person(name: String, age: Int)
object Person {
  implicit val ordering: Ordering[Person] = Ordering.by(_.age)
}

List(Person("Omer", 40), Person("Bart", 10)).sorted
