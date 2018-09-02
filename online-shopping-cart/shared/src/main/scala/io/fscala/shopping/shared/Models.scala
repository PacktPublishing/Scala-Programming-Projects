package io.fscala.shopping.shared

case class Product(name: String, code : String, description : String, price: Double)

case class Cart(user:String, productCode: String, quantity: Int)

case class ProductInCart(user:String, productCode: String)

case class User(sessionID: String)
