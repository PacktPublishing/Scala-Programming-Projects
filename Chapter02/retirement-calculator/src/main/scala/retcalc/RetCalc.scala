package retcalc

import retcalc.RetCalcError.MoreExpensesThanIncome

import scala.annotation.tailrec

case class RetCalcParams(nbOfMonthsInRetirement: Int,
                         netIncome: Int,
                         currentExpenses: Int,
                         initialCapital: Double)


case class MultiSimResults(successCount: Int,
                           simCount: Int,
                           minCapitalAfterDeath: Double,
                           maxCapitalAfterDeath: Double) {
  def successProbability: Double = successCount.toDouble / simCount
}


object RetCalc {

  def simulatePlan(returns: Returns, params: RetCalcParams, nbOfMonthsSavings: Int,
                   monthOffset: Int = 0): Either[RetCalcError, (Double, Double)] = {
    import params._

    for {
      capitalAtRetirement <- futureCapital(
        returns = OffsetReturns(returns, monthOffset),
        nbOfMonths = nbOfMonthsSavings, netIncome = netIncome, currentExpenses = currentExpenses,
        initialCapital = initialCapital)

      capitalAfterDeath <- futureCapital(
        returns = OffsetReturns(returns, monthOffset + nbOfMonthsSavings),
        nbOfMonths = nbOfMonthsInRetirement,
        netIncome = 0, currentExpenses = currentExpenses,
        initialCapital = capitalAtRetirement)
    } yield (capitalAtRetirement, capitalAfterDeath)
  }


  def nbOfMonthsSaving(params: RetCalcParams, returns: Returns): Either[RetCalcError, Int] = {
    import params._
    @tailrec
    def loop(months: Int): Either[RetCalcError, Int] = {
      simulatePlan(returns, params, months) match {
        case Right((capitalAtRetirement, capitalAfterDeath)) =>
          if (capitalAfterDeath > 0.0)
            Right(months)
          else
            loop(months + 1)

        case Left(err) => Left(err)
      }
    }

    if (netIncome > currentExpenses)
      loop(0)
    else
      Left(MoreExpensesThanIncome(netIncome, currentExpenses))
  }


  def futureCapital(returns: Returns, nbOfMonths: Int, netIncome: Int, currentExpenses: Int,
                    initialCapital: Double): Either[RetCalcError, Double] = {
    val monthlySavings = netIncome - currentExpenses
    (0 until nbOfMonths).foldLeft[Either[RetCalcError, Double]](Right(initialCapital)) {
      case (accumulated, month) =>
        for {
          acc <- accumulated
          monthlyRate <- Returns.monthlyRate(returns, month)
        } yield acc * (1 + monthlyRate) + monthlySavings
    }
  }

  def multiSim(params: RetCalcParams, nbOfMonthsSavings: Int, variableReturns: VariableReturns): MultiSimResults = {
    variableReturns.returns.indices.foldLeft(MultiSimResults(0, 0, Double.PositiveInfinity, Double.NegativeInfinity)) {
      case (acc, i) =>
        simulatePlan(variableReturns, params, nbOfMonthsSavings, i) match {
          case Right((capitalAtRetirement, capitalAfterDeath)) =>
            MultiSimResults(
              successCount = if (capitalAfterDeath > 0) acc.successCount + 1 else acc.successCount,
              simCount = i + 1,
              minCapitalAfterDeath = if (capitalAfterDeath < acc.minCapitalAfterDeath) capitalAfterDeath else acc.minCapitalAfterDeath,
              maxCapitalAfterDeath = if (capitalAfterDeath > acc.maxCapitalAfterDeath) capitalAfterDeath else acc.maxCapitalAfterDeath)

            // Could have a more clever rule which would reduce params.nbOfMonthsInRetirement.
            // say If the capital after nbOfMonthsInRetirement/2 is > 2*capitalAtRetirement,
            // it is very likely that we can count it as a success, even if we cannot run
            // the simulation until the end
          case Left(err) => acc
        }
    }
  }
}
