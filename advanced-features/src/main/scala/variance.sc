trait Animal
case class Cat(name: String) extends Animal
case class Dog(name: String) extends Animal

val animal1: Animal = Cat("Max")
val animal2: Animal = Dog("Dolly")
implicitly[Dog <:< Animal]


// Decoder
trait InvariantDecoder[A] {
  def decode(s: String): Option[A]
}
object InvariantCatDecoder extends InvariantDecoder[Cat] {
  val CatRegex = """Cat\((\w+\))""".r
  def decode(s: String): Option[Cat] = s match {
    case CatRegex(name) => Some(Cat(name))
    case _ => None
  }
}
// does not compile:
// val invariantAnimalDecoder: InvariantDecoder[Animal] = InvariantCatDecoder

trait CovariantDecoder[+A] {
  def decode(s: String): Option[A]
}
implicitly[CovariantDecoder[Cat] <:< CovariantDecoder[Animal]]

object CovariantCatDecoder extends CovariantDecoder[Cat] {
  val CatRegex = """Cat\((\w+\))""".r
  def decode(s: String): Option[Cat] = s match {
    case CatRegex(name) => Some(Cat(name))
    case _ => None
  }
}

val covariantAnimalDecoder: CovariantDecoder[Animal] = CovariantCatDecoder
covariantAnimalDecoder.decode("Cat(Ulysse)")
// res0: Option[Animal] = Some(Cat(Ulysse)))
implicitly[CovariantDecoder[Cat] <:< CovariantDecoder[Animal]]



object CovariantCatAndDogDecoder extends CovariantDecoder[Animal] {
  val CatRegex = """Cat\((\w+\))""".r
  val DogRegex = """Dog\((\w+\))""".r
  def decode(s: String): Option[Animal] = s match {
    case CatRegex(name) => Some(Cat(name))
    case DogRegex(name) => Some(Dog(name))
    case _ => None
  }
}

val covariantCatsAndDogsDecoder = CovariantCatAndDogDecoder

covariantCatsAndDogsDecoder.decode("Cat(Garfield)")
covariantCatsAndDogsDecoder.decode("Dog(Aiko)")


// Contravariant Encoder
trait Encoder[-A] {
  def encode(a: A): String
}
object AnimalEncoder extends Encoder[Animal] {
  def encode(a: Animal): String = a.toString
}
val catEncoder: Encoder[Cat] = AnimalEncoder
catEncoder.encode(Cat("Luna"))
// res1: String = Cat(Luna)


/*
trait Codec[+A] {
  def encode(a: A): String
  def decode(s: String): Option[A]
}
Error:(55, 15) covariant type A occurs in contravariant position in type A of value a
def encode(a: A): String
     ^
*/
trait Codec[A] {
  def encode(a: A): String
  def decode(s: String): Option[A]
}

object CatAndDogCodec extends Codec[Animal] {
  val CatRegex = """Cat\((\w+\))""".r
  val DogRegex = """Dog\((\w+\))""".r

  override def encode(a: Animal) = a.toString

  override def decode(s: String): Option[Animal] = s match {
    case CatRegex(name) => Some(Cat(name))
    case DogRegex(name) => Some(Dog(name))
    case _ => None
  }
}

val cat = CatAndDogCodec.decode("Cat(Garfield)")

CatAndDogCodec.encode(cat.get)


// Covariance in collections
val cats: Vector[Cat] = Vector(Cat("Max"))
val animals: Vector[Animal] = cats

val catsAndDogs = cats :+ Dog("Medor")
// catsAndDogs: Vector[Product with Serializable with Animal] = Vector(Cat(Max), Dog(Medor))

val serializables = catsAndDogs :+ "string"
// serializables: Vector[Serializable] = Vector(Cat(Max), Dog(Medor), string)
val anys = serializables :+ 1
// anys: Vector[Any] = Vector(Cat(Max), Dog(Medor), string, 1)


