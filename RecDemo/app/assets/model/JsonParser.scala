package assets.model

import play.api.libs.json._

/**
 * Created by ndn on 4/4/2015.
 */
object JsonParser {
  def jCat(c: Cate): JsObject = {
    val children = if (c.children == null) Json.arr()
    else Json.arr(c.children.map(jCat))
    Json.obj("id" -> c.id, "text" -> c.text, "children" -> children)
  }
  def jCats(cats: Array[Cate]) = Json.arr(cats.map(jCat))
}

