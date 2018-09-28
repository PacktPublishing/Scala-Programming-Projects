import cats.data.ValidatedNel

case class Timeout(millis: Int)

trait PriceService {
  def getPrice(productName: String)(implicit timeout: Timeout): ValidatedNel[String, Double]
}


case class Product(name: String, price: Double)

trait DataService {
  def getProduct(name: String): ValidatedNel[String, Product]

  def saveProduct(product: Product): ValidatedNel[String, Unit]
}


class AppContext(implicit val defaultTimeout: Timeout,
                 val priceService: PriceService,
                 val dataService: DataService)

import cats.implicits._
def updatePrice(productName: String)(implicit appContext: AppContext): ValidatedNel[String, Double] = {
  import appContext._
  (dataService.getProduct(productName), priceService.getPrice(productName)).tupled.andThen {
    case (product, newPrice) =>
      dataService.saveProduct(product.copy(price = newPrice)).map(_ =>
        newPrice
      )
  }
}


