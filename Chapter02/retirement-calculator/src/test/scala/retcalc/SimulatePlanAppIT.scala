package retcalc

import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{Matchers, WordSpec}

class SimulatePlanAppIT extends WordSpec with Matchers with TypeCheckedTripleEquals {
  "SimulatePlanApp.strMain" should {
    "simulate a retirement plan using market returns" in {
      val actualResult = SimulatePlanApp.strMain(
        Array("1952.09,2017.09", "25", "40", "3000", "2000", "10000"))

      val expectedResult =
        s"""
           |Capital after 25 years of savings:    468925
           |Capital after 40 years in retirement: 2958842
        """.stripMargin
      actualResult should === (expectedResult)
    }
  }
}
