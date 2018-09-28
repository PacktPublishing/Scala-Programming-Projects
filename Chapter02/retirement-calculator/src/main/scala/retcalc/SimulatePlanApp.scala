package retcalc

import cats.data.Validated._
import cats.data.{NonEmptyList, Validated, ValidatedNel}
import cats.implicits._
import retcalc.RetCalcError.{InvalidArgument, InvalidNumber, RetCalcResult}

object SimulatePlanApp extends App {
  strMain(args) match {
    case Invalid(err) =>
      println(err)
      sys.exit(1)

    case Valid(result) =>
      println(result)
      sys.exit(0)
  }

  def parseInt(name: String, value: String): RetCalcResult[Int] =
    Validated
      .catchOnly[NumberFormatException](value.toInt)
      .leftMap(_ => NonEmptyList.of(InvalidNumber(name, value)))

  def parseParams(args: Array[String]): RetCalcResult[RetCalcParams] =
    (
      parseInt("nbOfYearsRetired", args(2)),
      parseInt("netIncome", args(3)),
      parseInt("currentExpenses", args(4)),
      parseInt("initialCapital", args(5))
    ).mapN { case (nbOfYearsRetired, netIncome, currentExpenses, initialCapital) =>
      RetCalcParams(
        nbOfMonthsInRetirement = nbOfYearsRetired * 12,
        netIncome = netIncome,
        currentExpenses = currentExpenses,
        initialCapital = initialCapital)
    }

  def parseFromUntil(fromUntil: String): RetCalcResult[(String, String)] = {
    val array = fromUntil.split(",")
    if (array.length != 2)
      InvalidArgument(name = "fromUntil", value = fromUntil, expectedFormat = "from,until"
      ).invalidNel
    else
      (array(0), array(1)).validNel
  }

  def strSimulatePlan(returns: Returns, nbOfYearsSaving: Int, params: RetCalcParams)
  : RetCalcResult[String] = {
    RetCalc.simulatePlan(
      returns = returns,
      params = params,
      nbOfMonthsSavings = nbOfYearsSaving * 12
    ).map {
      case (capitalAtRetirement, capitalAfterDeath) =>
        val nbOfYearsInRetirement = params.nbOfMonthsInRetirement / 12
        s"""
           |Capital after $nbOfYearsSaving years of savings:    ${capitalAtRetirement.round}
           |Capital after $nbOfYearsInRetirement years in retirement: ${capitalAfterDeath.round}
           |""".stripMargin
    }.toValidatedNel
  }


  def strMain(args: Array[String]): Validated[String, String] = {
    if (args.length != 6)
      """Usage:
        |simulatePlan from,until nbOfYearsSaving nbOfYearsRetired netIncome currentExpenses initialCapital
        |
        |Example:
        |simulatePlan 1952.09,2017.09 25 40 3000 2000 10000
        |""".stripMargin.invalid
    else {
      val allReturns = Returns.fromEquityAndInflationData(
        equities = EquityData.fromResource("sp500.tsv"),
        inflations = InflationData.fromResource("cpi.tsv"))

      val vFromUntil = parseFromUntil(args(0))
      val vNbOfYearsSaving = parseInt("nbOfYearsSaving", args(1))
      val vParams = parseParams(args)

      (vFromUntil, vNbOfYearsSaving, vParams)
        .tupled
        .andThen { case ((from, until), nbOfYearsSaving, params) =>
          strSimulatePlan(allReturns.fromUntil(from, until), nbOfYearsSaving, params)
        }
        .leftMap(nel => nel.map(_.message).toList.mkString("\n"))
    }
  }
}

case class SimulatePlanArgs(fromMonth: String,
                            untilMonth: String,
                            retCalcParams: RetCalcParams,
                            nbOfMonthsSavings: Int)


