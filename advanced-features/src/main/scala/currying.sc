def multiply(x: Int, y: Int): Int = x * y
// multiply: multiply[](val x: Int,val y: Int) => Int
val multiplyVal = (x: Int, y: Int) => x * y
// multiplyVal: (Int, Int) => Int = $Lambda$1611/314131783@6a67fdf3
val multiplyVal2 = multiply _
// multiplyVal2: (Int, Int) => Int = $Lambda$1619/657050655@322b60f

multiply(2, 3)
multiplyVal(2, 3)
multiplyVal2(2, 3)

val multiplyCurried = multiplyVal.curried
// multiplyCurried: Int => (Int => Int) = ...

multiplyVal(2, 3)
// res3: Int = 6
multiplyCurried(2)
// res4: Int => Int = ...
multiplyCurried(2)(3)
// res5: Int = 6

case class Item(description: String, price: Double)

def discount(percentage: Double)(item: Item): Item =
  item.copy(price = item.price * (1 - percentage / 100))

discount(10)(Item("Monitor", 500))
// res6: Item = Item(Monitor,450.0)

val discount10 = discount(10) _
// discount10: Item => Item = ...
discount10(Item("Monitor", 500))
// res7: Item = Item(Monitor,450.0)


val items = Vector(Item("Monitor", 500), Item("Laptop", 700))
items.map(discount(10))
// res8: Vector[Item] = Vector(Item(Monitor,450.0), Item(Laptop,630.0))
