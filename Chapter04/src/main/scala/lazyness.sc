class StrictDemo {
  val strictVal = {
    println("Evaluating strictVal")
    "Hello"
  }
}

val strictDemo = new StrictDemo

//Evaluating strictVal
//strictDemo: StrictDemo = StrictDemo@32fac009

class LazyDemo {
  lazy val lazyVal = {
    println("Evaluating lazyVal")
    "Hello"
  }
}

val lazyDemo = new LazyDemo
// lazyDemo: LazyDemo = LazyDemo@13ca84d5
lazyDemo.lazyVal + " World"

// Evaluating lazyVal
// res0: String = Hello World

class LazyChain {
  lazy val val1 = {
    println("Evaluating val1")
    "Hello"
  }
  lazy val val2 = {
    println("Evaluating val2")
    val1 + " lazy"
  }
  lazy val val3 = {
    println("Evaluating val3")
    val2 + " chain"
  }
}

val lazyChain = new LazyChain
// lazyChain: LazyChain = LazyChain@4ca51fa
lazyChain.val3

// Evaluating val3
// Evaluating val2
// Evaluating val1
// res1: String = Hello lazy chain

object AppConfig {
  lazy val greeting: String = {
    println("Loading greeting")
    "Hello "
  }
}

def greet(name: String, greeting: => String): String = {
  if (name == "Mikael")
    greeting + name
  else
    s"I don't know you $name"
}
greet("Bob", AppConfig.greeting)
// res2: String = I don't know you Bob
greet("Mikael", AppConfig.greeting)
// Loading greeting
// res3: String = Hello Mikael

// Lazy data structures
def evenPlusOne(xs: Vector[Int]): Vector[Int] =
  xs.filter { x => println(s"filter $x"); x % 2 == 0 }
    .map    { x => println(s"map $x");    x + 1      }

evenPlusOne(Vector(0, 1, 2))


def lazyEvenPlusOne(xs: Vector[Int]): Vector[Int] =
  xs.withFilter { x => println(s"filter $x"); x % 2 == 0 }
    .map        { x => println(s"map $x")   ; x + 1      }

lazyEvenPlusOne(Vector(0, 1, 2))

def lazyEvenPlusTwo(xs: Vector[Int]): Vector[Int] =
  xs.withFilter { x => println(s"filter $x"); x % 2 == 0 }
    .map        { x => println(s"map $x")   ; x + 1      }
    .map        { x => println(s"map2 $x")  ; x + 1      }

lazyEvenPlusTwo(Vector(0, 1, 2))


def lazyEvenPlusTwoStream(xs: Stream[Int]): Stream[Int] =
  xs.filter     { x => println(s"filter $x"); x % 2 == 0 }
    .map        { x => println(s"map $x")   ; x + 1      }
    .map        { x => println(s"map2 $x")  ; x + 1      }

lazyEvenPlusTwoStream(Stream(0, 1, 2)).toVector


// Infinite Stream
val evenInts: Stream[Int] = 0 #:: 2 #:: evenInts.tail.map(_ + 2)
evenInts.take(10).toVector
// res8: Vector[Int] = Vector(0, 2, 4, 6, 8, 10, 12, 14, 16, 18)

