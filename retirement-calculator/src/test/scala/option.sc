val opt0: Option[Int] = None
val opt1: Option[Int] = Some(1)

val list0 = List.empty[String]
list0.headOption
list0.lastOption
val list3 = List("Hello", "World")
list3.headOption
list3.lastOption


def personDescription(name: String, db: Map[String, Int]): String =
  db.get(name) match {
    case Some(age) => s"$name is $age years old"
    case None => s"$name is not present in db"
  }

val db = Map("John" -> 25, "Rob" -> 40)
personDescription("John", db)
personDescription("Michael", db)



def personDesc(name: String, db: Map[String, Int]): String = {
  val optString: Option[String] = db.get(name).map(age => s"$name is $age years old")
  optString.getOrElse(s"$name is not present in db")
}

def averageAgeA(name1: String, name2: String, db: Map[String, Int]): Option[Double] = {
  val optOptAvg: Option[Option[Double]] =
    db.get(name1).map(age1 =>
      db.get(name2).map(age2 =>
        (age1 + age2).toDouble / 2))
  optOptAvg.flatten
}

def averageAgeB(name1: String, name2: String, db: Map[String, Int]): Option[Double] =
  db.get(name1).flatMap(age1 =>
    db.get(name2).map(age2 =>
      (age1 + age2).toDouble / 2))

def averageAgeC(name1: String, name2: String, db: Map[String, Int]): Option[Double] =
  for {
    age1 <- db.get(name1)
    age2 <- db.get(name2)
  } yield (age1 + age2).toDouble / 2



averageAgeA("John", "Rob", db)
// res6: Option[Double] = Some(32.5)
averageAgeA("John", "Michael", db)
// res7: Option[Double] = None

for {
  i <- Vector("one", "two")
  j <- Vector(1, 2, 3)
} yield (i, j)




