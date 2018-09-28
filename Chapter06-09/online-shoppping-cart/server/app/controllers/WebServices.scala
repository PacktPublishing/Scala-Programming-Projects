package controllers

import dao.{CartDao, ProductDao}
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import io.fscala.shopping.shared.{Cart, Product, ProductInCart}
import io.swagger.annotations._
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.libs.circe.Circe
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


@Singleton
@Api(value = "Product and Cart API")
class WebServices @Inject()(cc: ControllerComponents, productDao: ProductDao, cartsDao: CartDao) extends AbstractController(cc) with Circe {


  val recoverError: PartialFunction[Throwable, Result] = {
    case e: org.h2.jdbc.JdbcSQLException =>
      Logger.error("Inserting duplicate in the database", e)
      BadRequest("Cannot insert duplicates in the database")
    case e: Throwable =>
      Logger.error("Error while writing in the database", e)
      InternalServerError("Cannot write in the database")
  }

  // *********** User Controler ******** //
  @ApiOperation(value = "Login to the service", consumes = "text/plain")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(
      value = "Create a session for this user",
      required = true,
      dataType = "java.lang.String", // complete path
      paramType = "body"
    )
  ))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "login success"), new ApiResponse(code = 400, message = "Invalid user name supplied")))
  def login() = Action { request =>
    request.body.asText match {
      case None => BadRequest
      case Some(user) => Ok.withSession("user" -> user)
    }
  }

  // *********** CART Controler ******** //
  @ApiOperation(value = "List the product in the cart", consumes = "text/plain")
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Product added"),
    new ApiResponse(code = 401, message = "unauthorized, please login before to proceed"),
    new ApiResponse(code = 500, message = "Internal server error, database error")))
  def listCartProducts(): Action[AnyContent] = Action.async { request =>
    val userOption = request.session.get("user")
    userOption match {
      case Some(user) =>
        Logger.info(s"User '$user' is asking for the list of product in the cart")
        val futureInsert = cartsDao.cart4(user)

        futureInsert.map(products => Ok(products.asJson)).recover(recoverError)
      case None => Future.successful(Unauthorized)
    }
  }

  @ApiOperation(value = "Delete a product from the cart", consumes = "text/plain")
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Product delete from the cart"),
    new ApiResponse(code = 401, message = "unauthorized, please login before to proceed"),
    new ApiResponse(code = 500, message = "Internal server error, database error")))
  def deleteCartProduct(@ApiParam(name = "id", value = "The product code", required = true) id: String): Action[AnyContent] = Action.async { request =>
    val userOption = request.session.get("user")
    userOption match {
      case Some(user) =>
        Logger.info(s"User '$user' is asking to delete the product '$id' from the cart")
        val futureInsert = cartsDao.remove(ProductInCart(user, id))
        futureInsert.map(_ => Ok).recover(recoverError)
      case None => Future.successful(Unauthorized)
    }
  }

  @ApiOperation(value = "Add a product in the cart", consumes = "text/plain")
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Product added in the cart"),
    new ApiResponse(code = 400, message = "Cannot insert duplicates in the database"),
    new ApiResponse(code = 401, message = "unauthorized, please login before to proceed"),
    new ApiResponse(code = 500, message = "Internal server error, database error")))
  def addCartProduct(@ApiParam(name = "id", value = "The product code", required = true) id: String, @ApiParam(name = "quantity", value = "The quantity to add", required = true) quantity: String): Action[AnyContent] = Action.async { request =>
    val userOption = request.session.get("user")
    userOption match {
      case Some(user) =>
        Logger.info(s"User '$user' is adding $quantity times the product'$id' in it's cart")
        val futureInsert = cartsDao.insert(Cart(user, id, quantity.toInt))
        futureInsert.map(_ => Ok).recover(recoverError)
      case None => Future.successful(Unauthorized)
    }
  }

  @ApiOperation(value = "Update a product quantity in the cart", consumes = "text/plain")
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Product updated in the cart"),
    new ApiResponse(code = 401, message = "unauthorized, please login before to proceed"),
    new ApiResponse(code = 500, message = "Internal server error, database error")))
  def updateCartProduct(@ApiParam(name = "id", value = "The product code", required = true, example = "ALD1") id: String, @ApiParam(name = "quantity", value = "The quantity to update", required = true) quantity: String): Action[AnyContent] = Action.async { request =>
    val userOption = request.session.get("user")
    userOption match {
      case Some(user) =>
        Logger.info(s"User '$user' is updating the product'$id' in it's cart with a quantity of $quantity")
        val futureInsert = cartsDao.update(Cart(user, id, quantity.toInt))
        futureInsert.map(_ => Ok).recover(recoverError)
      case None => Future.successful(Unauthorized)
    }
  }

  // *********** Product Controler ******** //
  @ApiOperation(value = "List all the products")
  @ApiResponses(Array(new ApiResponse(code = 200, message = "The list of all the product")))
  def listProduct(): Action[AnyContent] = Action.async { _ =>
    val futureProducts = productDao.all()
    for (
      products <- futureProducts
    ) yield Ok(products.asJson)
  }

  @ApiOperation(value = "Add a product", consumes = "text/plain")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(
      value = "The product to add",
      required = true,
      dataType = "io.fscala.shopping.shared.Product", // complete path
      paramType = "body"
    )
  ))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Product added"),
    new ApiResponse(code = 400, message = "Invalid body supplied"),
    new ApiResponse(code = 500, message = "Internal server error, database error")))
  def addProduct(): Action[AnyContent] = Action.async { request =>
    val productOrNot = decode[Product](request.body.asText.getOrElse(""))
    productOrNot match {
      case Right(product) =>
        val futureInsert = productDao.insert(product)
        futureInsert.map(_ => Ok).recover(recoverError)
      case Left(error) =>
        Logger.error("Error while adding a product", error)
        Future.successful(BadRequest)
    }
  }

}
