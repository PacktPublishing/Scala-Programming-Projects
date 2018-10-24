package retcalc

import scala.io.Source

case class EquityData(monthId: String, value: Double, annualDividend: Double) {
  val monthlyDividend: Double = annualDividend / 12
}

object EquityData {
  def fromResource(resource: String): Vector[EquityData] =
    Source.fromResource(resource).getLines().drop(1).map { line =>
      val fields = line.split("\t")
      EquityData(
        monthId = fields(0),
        value = fields(1).toDouble,
        annualDividend = fields(2).toDouble)
    }.toVector
}
