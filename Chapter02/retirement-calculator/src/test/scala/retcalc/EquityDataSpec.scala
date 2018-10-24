package retcalc

import org.scalatest.{Matchers, WordSpec}

class EquityDataSpec extends WordSpec with Matchers {
  "EquityData.fromResource" should {
    "load market data from a tsv file" in {
      val data = EquityData.fromResource("sp500_2017.tsv")
      data should ===(Vector(
        EquityData("2016.09", 2157.69, 45.03),
        EquityData("2016.10", 2143.02, 45.25),
        EquityData("2016.11", 2164.99, 45.48),
        EquityData("2016.12", 2246.63, 45.7),
        EquityData("2017.01", 2275.12, 45.93),
        EquityData("2017.02", 2329.91, 46.15),
        EquityData("2017.03", 2366.82, 46.38),
        EquityData("2017.04", 2359.31, 46.66),
        EquityData("2017.05", 2395.35, 46.94),
        EquityData("2017.06", 2433.99, 47.22),
        EquityData("2017.07", 2454.10, 47.54),
        EquityData("2017.08", 2456.22, 47.85),
        EquityData("2017.09", 2492.84, 48.17)
      ))
    }
  }

  "EquityData.monthlyDividend" should {
    "return a monthly dividend" in {
      EquityData("2016.09", 2157.69, 45.03).monthlyDividend should === (45.03 / 12)
    }
  }

}
