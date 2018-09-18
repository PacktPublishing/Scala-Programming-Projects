


case class Person(name: String, age: Int)

case class AgeNegativeException(message: String) extends Exception(message)

def createPerson(description: String): Person = {
  val split = description.split(" ")
  val age = split(1).toInt
  if (age < 0)
    throw AgeNegativeException(s"age: $age should be > 0")
  else
    Person(split(0), age)
}

//createPerson("John -10")
//createPerson("John 1000000000000000000000000000")
//createPerson("John 10.45")

def averageAge(descriptions: Vector[String]): Double = {
  val total = descriptions.map(createPerson).map(_.age).sum
  total / descriptions.length
}

import scala.util.control.NonFatal

def personsSummary(personsInput: String): String = {
  val descriptions = personsInput.split("\n").toVector
  val avg = try {
    averageAge(descriptions)
  } catch {
    case e: AgeNegativeException =>
      println(s"one of the persons has a negative age: $e")
      0
    case NonFatal(e) =>
      println(s"something was wrong in the input: $e")
      0
  }
  s"${descriptions.length} persons with an average age of $avg"
}

personsSummary(
  """John 25
    |Sharleen 45""".stripMargin)


import java.io.IOException
import java.net.URL
import scala.annotation.tailrec

val stream = new URL("https://www.packtpub.com/").openStream()
val htmlPage: String =
  try {
    @tailrec
    def loop(builder: StringBuilder): String = {
      val i = stream.read()
      if (i != -1)
        loop(builder.append(i.toChar))
      else
        builder.toString()
    }
    loop(StringBuilder.newBuilder)
  } catch {
    case e: IOException => s"cannot read URL: $e"
  }
  finally {
    stream.close()
  }


val htmlPage2 = scala.io.Source.fromURL("https://www.packtpub.com/").mkString
