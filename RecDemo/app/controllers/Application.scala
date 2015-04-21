package controllers

import assets.consts.Const
import play.api.libs.ws.WS
import play.api.mvc._
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global

object Application extends Controller {
  def cat(cat: Int, index: Int) = Action.async {
    val data = for {
      rootCate <- WS.url(Const.apiGetRoot).get().map(_.body)
      _data <- WS.url(Const.apiGetShortProducts.format(cat, index)).get().map(_.body)
    } yield (rootCate, _data)
    data.map{case (root, _data) => Ok(views.html.list(root, _data, s"/cat?cat=$cat&index="))}
  }

  def detail(p: Int) = Action.async {
    val data = for {
      rootCate <- WS.url(Const.apiGetRoot).get().map(_.body)
      _data <- WS.url(Const.apiGetProduct + p).get().map(_.body)
    } yield (rootCate, _data)
    data.map{case (root, _data) => Ok(views.html.detail(root, _data))}
  }
}