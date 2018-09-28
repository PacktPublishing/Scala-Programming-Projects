def divide(x: Double, y: Double): Either[String, Double] =
  if (y == 0)
    Left(s"$x cannot be divided by zero")
  else
    Right(x / y)

divide(6, 3)
// res0: Either[String,Double] = Right(2.0)
divide(6, 0)
// res1: Either[String,Double] = Left(6.0 cannot be divided by zero)

def getPersonAge(name: String, db: Map[String, Int]): Either[String, Int] =
  db.get(name).toRight(s"$name is not present in db")

def personDescription(name: String, db: Map[String, Int]): String =
  getPersonAge(name, db) match {
    case Right(age) => s"$name is $age years old"
    case Left(error) => error
  }

val db = Map("John" -> 25, "Rob" -> 40)
personDescription("John", db)
// res4: String = John is 25 years old
personDescription("Michael", db)
// res5: String = Michael is not present in db

def averageAge(name1: String, name2: String, db: Map[String, Int]): Either[String, Double] =
  getPersonAge(name1, db).flatMap(age1 =>
    getPersonAge(name2, db).map(age2 =>
      (age1 + age2).toDouble / 2))

averageAge("John", "Rob", db)
// res4: Either[String,Double] = Right(32.5)
averageAge("John", "Michael", db)
// res5: Either[String,Double] = Left(Michael is not present in db)

getPersonAge("bob", db).left.map(err => s"The error was: $err")
// res6: scala.util.Either[String,Int] = Left(The error was: bob is not present in db)

def averageAge2(name1: String, name2: String, db: Map[String, Int]): Either[String, Double] =
  for {
    age1 <- getPersonAge(name1, db)
    age2 <- getPersonAge(name2, db)
  } yield (age1 + age2).toDouble / 2