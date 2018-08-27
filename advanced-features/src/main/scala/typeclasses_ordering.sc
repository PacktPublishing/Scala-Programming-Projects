
Vector(1,3,2).sorted

import java.time.LocalDate
implicit val dateOrdering: Ordering[LocalDate] = Ordering.fromLessThan[LocalDate](_ isBefore _)
import Ordering.Implicits._

LocalDate.of(2018, 5, 18) < LocalDate.of(2017, 1, 1)
// res1: Boolean = false
Vector(LocalDate.of(2018, 5, 18), LocalDate.of(2018, 6, 1)).sorted(dateOrdering.reverse)
// res2: Vector[LocalDate] = Vector(2018-06-01, 2018-05-18)
