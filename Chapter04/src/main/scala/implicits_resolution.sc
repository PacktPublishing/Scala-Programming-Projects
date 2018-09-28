class A(val n: Int) {
  def +(other: A) = new A(n + other.n)
}
object A {
  implicit def fromInt(n: Int) = new A(n)
}

1 + new A(1)