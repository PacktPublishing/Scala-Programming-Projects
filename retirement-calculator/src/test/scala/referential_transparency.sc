def pureSquare(x: Int): Int = x * x
val pureExpr = pureSquare(4) + pureSquare(3)
// pureExpr: Int = 25

val pureExpr2 = 16 + 9
// pureExpr2: Int = 25

var globalState = 1
def impure(x: Int): Int = {
  globalState = globalState + x
  globalState
}
val impureExpr = impure(3)
val impureExpr2 = 4


import scala.util.Random
def impureRand(): Int = Random.nextInt()
impureRand()
//res0: Int = -528134321
val impureExprRand = impureRand() + impureRand()
//impureExprRand: Int = 681209667
val impureExprRand2 = -528134321 + -528134321

def pureRand(seed: Int): Int = new Random(seed).nextInt()
pureRand(10)
//res1: Int = -1157793070
val pureExprRand = pureRand(10) + pureRand(10)
//pureExprRand: Int = 1979381156
val pureExprRand2 = -1157793070 + -1157793070
//pureExprRand2: Int = 1979381156


def area(width: Double, height: Double): Double = {
  if (width > 5 || height > 5)
    throw new IllegalArgumentException("too big")
  else
    width * height
}

val total = try {
  area(6, 2) + area(4, 2)
} catch {
  case e: IllegalArgumentException => 0
}







