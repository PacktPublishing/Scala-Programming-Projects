package io.fscala.shopping.client

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSGlobal, ScalaJSDefined}

@js.native
@JSGlobal("$")
object NotifyJS extends js.Object {
  def notify(msg: String, option: Options): String = js.native
}

@ScalaJSDefined
trait Options extends js.Object {
  // whether to hide the notification on click
  var clickToHide: js.UndefOr[Boolean] = js.undefined
  // whether to auto-hide the notification
  var autoHide: js.UndefOr[Boolean] = js.undefined
  // if autoHide, hide after milliseconds
  var autoHideDelay: js.UndefOr[Int] = js.undefined
  // show the arrow pointing at the element
  var arrowShow: js.UndefOr[Boolean] = js.undefined
  // arrow size in pixels
  var arrowSize: js.UndefOr[Int] = js.undefined
  // position defines the notification position though uses the defaults below
  var position: js.UndefOr[String] = js.undefined
  // default positions
  var elementPosition: js.UndefOr[String] = js.undefined
  var globalPosition: js.UndefOr[String] = js.undefined
  // default style
  var style: js.UndefOr[String] = js.undefined
  // default class (string or [string])
  var className: js.UndefOr[String] = js.undefined
  // show animation
  var showAnimation: js.UndefOr[String] = js.undefined
  // show animation duration
  var showDuration: js.UndefOr[Int] = js.undefined
  // hide animation
  var hideAnimation: js.UndefOr[String] = js.undefined
  // hide animation duration
  var hideDuration: js.UndefOr[Int] = js.undefined
  // padding between element and notification
  var gap: js.UndefOr[Int] = js.undefined
}
