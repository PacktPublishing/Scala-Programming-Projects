package io.fscala.shopping.client


import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import io.fscala.shopping.shared._
import org.querki.jquery._
import org.scalajs.dom
import org.scalajs.dom.html.Document
import org.scalajs.dom.raw.{CloseEvent, Event, MessageEvent, WebSocket}

import scala.scalajs.js.UndefOr
import scala.util.{Random, Try}


object UIManager {

  val origin: UndefOr[String] = dom.document.location.origin
  val cart: CartDiv = CartDiv(Set.empty[CartLine])

  val webSocket: WebSocket = getWebSocket

  val dummyUserName = s"user-${Random.nextInt(1000)}"


  def main(args: Array[String]): Unit = {
    val settings = JQueryAjaxSettings.url(s"$origin/v1/login").data(dummyUserName).contentType("text/plain")
    $.post(settings._result).done((_: String) => {
      initUI(origin)
    })
  }

  private def initUI(origin: UndefOr[String]) = {

    $.get(url = s"$origin/v1/products", dataType = "text")
      .done((answers: String) => {
        val products = decode[Seq[Product]](answers)
        products.right.map { seq =>
          seq.foreach(p => {
            $("#products").append(ProductDiv(p).content)
          })
          initCartUI(origin, seq)
        }
      })
      .fail((xhr: JQueryXHR, textStatus: String, textError: String) =>
        println(s"call failed: $textStatus with status code: ${xhr.status} $textError")
      )
  }

  private def initCartUI(origin: UndefOr[String], products: Seq[Product]) = {
    $.get(url = s"$origin/v1/cart/products", dataType = "text")
      .done((answers: String) => {
        val carts = decode[Seq[Cart]](answers)
        carts.right.map { cartLines =>
          cartLines.foreach { cartDao =>
            val product = products.find(_.code == cartDao.productCode)
            product match {
              case Some(p) =>
                val cartLine = CartLine(cartDao.quantity, p)
                val cartContent = UIManager.cart.addProduct(cartLine).content
                $("#cartPanel").append(cartContent)
              case None =>
                println(s"product code ${cartDao.productCode} doesn't exists in the catalog")
            }
          }
        }
      })
      .fail((xhr: JQueryXHR, textStatus: String, textError: String) =>
        println(s"call failed: $textStatus with status code: ${xhr.status} $textError")
      )
  }

  def addOneProduct(product: Product): JQueryDeferred = {
    val quantity = 1

    def onDone = () => {
      val cartContent = cart.addProduct(CartLine(quantity, product)).content
      $("#cartPanel").append(cartContent)
      println(s"Product $product added in the cart")
      webSocket.send(CartEvent(dummyUserName, product, Add).asJson.noSpaces)
    }

    postInCart(product.code, quantity, onDone)
  }

  def updateProduct(product: Product): JQueryDeferred = {
    putInCart(product.code, quantity(product.code))
  }

  def deleteProduct(product: Product): JQueryDeferred = {
    def onDone = () => {
      val cartContent = $(s"#cart-${product.code}-row")
      cartContent.remove()
      webSocket.send(CartEvent(dummyUserName, product, Remove).asJson.noSpaces)
      println(s"Product ${product.code} removed from the cart")
    }

    deletefromCart(product.code, onDone)
  }

  private def getWebSocket: WebSocket = {
    val ws = new WebSocket(getWebsocketUri(dom.document, "v1/cart/events"))
    ws.onopen = { (event: Event) ⇒
      println(s"webSocket.onOpen '${event.`type`}'")
      event.preventDefault()
    }

    ws.onerror = { (event: Event) =>
      System.err.println(s"webSocket.onError '${event.getClass}'")
    }

    ws.onmessage = { (event: MessageEvent) =>
      println(s"[webSocket.onMessage] '${event.data.toString}'...")
      val msg = decode[Alarm](event.data.toString)
      msg match {
        case Right(alarm) =>
          println(s"[webSocket.onMessage]  Got alarm event : $alarm)")
          notify(alarm)
        case Left(e) =>
          println(s"[webSocket.onMessage] Got a unknown event : $msg)")
      }
    }

    ws.onclose = { (event: CloseEvent) ⇒
      println(s"webSocket.onClose '${event.`type`}'")
    }
    ws
  }

  private def notify(alarm: Alarm): Unit = {
    val notifyClass = if (alarm.action == Add) "info" else "warn"
    NotifyJS.notify(alarm.message, new Options {
      className = notifyClass
      globalPosition = "right bottom"
    })
  }

  private def quantity(productCode: String) = Try {
    val inputText = $(s"#cart-$productCode-qty")
    if (inputText.length != 0)
      Integer.parseInt(inputText.`val`().asInstanceOf[String])
    else 1
  }.getOrElse(1)

  private def postInCart(productCode: String, quantity: Int, onDone: () => Unit) = {
    val url = s"${UIManager.origin}/v1/cart/products/$productCode/quantity/$quantity"
    $.post(JQueryAjaxSettings.url(url)._result)
      .done(onDone)
      .fail(() => println("cannot add a product twice"))
  }

  private def putInCart(productCode: String, updatedQuantity: Int) = {
    val url = s"${UIManager.origin}/v1/cart/products/$productCode/quantity/$updatedQuantity"
    $.ajax(JQueryAjaxSettings.url(url).method("PUT")._result)
      .done()
  }

  private def deletefromCart(productCode: String, onDone: () => Unit) = {
    val url = s"${UIManager.origin}/v1/cart/products/$productCode"
    $.ajax(JQueryAjaxSettings.url(url).method("DELETE")._result)
      .done(onDone)
  }

  private def getWebsocketUri(document: Document, context: String): String = {
    val wsProtocol = if (dom.document.location.protocol == "https:") "wss" else "ws"

    s"$wsProtocol://${
      dom.document.location.host
    }/$context"
  }

}
