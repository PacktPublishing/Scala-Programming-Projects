package io.fscala.shopping.shared

case class Product(name: String, code : String, description : String, price: Double)

abstract class CartKey {
  def user: String
  def productCode: String
}

case class ProductInCart(user:String, productCode: String) extends CartKey

case class Cart(user:String, productCode: String, quantity: Int) extends CartKey

case class User(sessionID: String)
