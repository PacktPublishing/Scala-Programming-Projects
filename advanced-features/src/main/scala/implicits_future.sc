import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

Future(println(Thread.currentThread().getName))