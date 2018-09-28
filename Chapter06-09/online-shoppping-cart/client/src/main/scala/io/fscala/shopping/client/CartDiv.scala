package io.fscala.shopping.client

import org.scalajs.dom.html.Div
import scalatags.JsDom.all._
import io.fscala.shopping.shared.Product

case class CartDiv(lines: Set[CartLine]) {
  def content: Div = lines.foldLeft(div.render) { (a, b) =>
    a.appendChild(b.content).render
    a
  }

  def addProduct(line: CartLine): CartDiv = {
    CartDiv(this.lines + line)
  }

}

case class CartLine(qty: Int, product: Product) {
  def content: Div = div(`class` := "row", id := s"cart-${product.code}-row")(
    div(`class` := "col-1")(getDeleteButton),
    div(`class` := "col-2")(getQuantityInput),
    div(`class` := "col-6")(getProductLabel),
    div(`class` := "col")(getPriceLabel)
  ).render

  private def getQuantityInput =  input(id := s"cart-${product.code}-qty", onchange := changeQty, value := qty.toString, `type` := "text", style := "width: 100%;").render

  private def getProductLabel = label(product.name).render

  private def getPriceLabel = label(product.price * qty).render

  private def getDeleteButton = button(`type` := "button", onclick := removeFromCart)("X").render

  private def changeQty() = () => UIManager.updateProduct(product)

  private def removeFromCart() = () => UIManager.deleteProduct(product)
}