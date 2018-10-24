package retcalc

import org.scalatest.{Matchers, WordSpec}

class InflationDataSpec extends WordSpec with Matchers {
  "InflationData.fromResource" should {
    "load CPI data from a tsv file" in {
      val data = InflationData.fromResource("cpi_2017.tsv")
      data should ===(Vector(
        InflationData("2016.09", 241.428),
        InflationData("2016.10", 241.729),
        InflationData("2016.11", 241.353),
        InflationData("2016.12", 241.432),
        InflationData("2017.01", 242.839),
        InflationData("2017.02", 243.603),
        InflationData("2017.03", 243.801),
        InflationData("2017.04", 244.524),
        InflationData("2017.05", 244.733),
        InflationData("2017.06", 244.955),
        InflationData("2017.07", 244.786),
        InflationData("2017.08", 245.519),
        InflationData("2017.09", 246.819)
      ))
    }
  }
}
