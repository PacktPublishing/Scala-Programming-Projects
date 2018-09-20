import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

val f1 = Future {1}
val f2 = Future {2}
val f3 = Future {3}


val s = for {
  v1 <- f1
  v2 <- f2
  v3 <- f3
  } yield (v1 + v2 + v3)

val fma = f1.flatMap { v1 =>
  f2.map(v2 =>
    v1 + v2
  )
}



def sum(v: Int*) = {
  v.sum
}

val minExpected = 7
val res = for {
  v1 <- f1
  v2 <- f2
  v3 <- f3
  if (sum(v1, v2, v3) > minExpected)
} yield (v1, v2, v3)



res.onComplete {
  case Success(result) => println(s"The result is $result")
  case Failure(e) => println("The sum is not big enough")
}

Await.ready(res, 1 second)


