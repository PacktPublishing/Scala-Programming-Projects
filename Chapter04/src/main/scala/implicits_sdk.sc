import scala.collection.breakOut
val map: Map[String, Int] = Vector("hello", "world").map(s => s -> s.length)(breakOut)
