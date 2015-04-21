package assets.dao

import assets.consts.Const
import assets.model.Cate
import play.api.cache.Cache
import play.api.libs.json.{JsObject, JsPath, Reads}
import play.api.libs.ws.WS
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by ndn on 4/8/2015.
 */
object CategoryDAO {
  def getRootCate() = {
    println(Const.apiGetChildren + "0")
    WS.url(Const.apiGetChildren + "0").get().map(_.json.as[Array[JsObject]].map(o => {
      val id = (o \ "id").as[Int]
      val text = (o \ "text").as[String]
      val hasChild = (o \ "children").as[Boolean]
      if (hasChild) {
        val children = WS.url(Const.apiGetChildren + id).get().map(_.json.as[Array[JsObject]].map(_o => {
          val _id = (_o \ "id").as[Int]
          val _text = (_o \ "text").as[String]
          Cate(_id, _text)
        })).value.get.get
        Cate(id, text, children)
      }
      else {
        Cate(id, text)
      }
    }))
  }
}
