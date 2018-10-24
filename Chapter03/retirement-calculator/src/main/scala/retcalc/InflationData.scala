package retcalc

import scala.io.Source

case class InflationData(monthId: String, value: Double)

object InflationData {
  def fromResource(resource: String): Vector[InflationData] =
    Source.fromResource(resource).getLines().drop(1).map { line =>
      val fields = line.split("\t")
      InflationData(monthId = fields(0), value = fields(1).toDouble)
    }.toVector

}
