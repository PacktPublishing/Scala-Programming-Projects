import cats.data.NonEmptyList


NonEmptyList(1, List(2, 3))
// res0: cats.data.NonEmptyList[Int] = NonEmptyList(1, 2, 3)
NonEmptyList.fromList(List(1, 2, 3))
// res3: Option[cats.data.NonEmptyList[Int]] = Some(NonEmptyList(1, 2, 3))
NonEmptyList.fromList(List.empty[Int])
// res4: Option[cats.data.NonEmptyList[Int]] = None
val nel = NonEmptyList.of(1, 2, 3)
// nel: cats.data.NonEmptyList[Int] = NonEmptyList(1, 2, 3)

nel.head
// res0: Int = 1
nel.tail
// res1: List[Int] = List(2, 3)
nel.map(_ + 1)
// res2: cats.data.NonEmptyList[Int] = NonEmptyList(2, 3, 4)

