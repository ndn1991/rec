package assets.dao.tools

import assets.model.{ShortProduct, Product, Cate}
import play.api.libs.json._

/**
 * Created by ndn on 4/4/2015.
 */
object JsonParser {
  def jCat(c: Cate): JsObject = Json.obj("id" -> c.id, "text" -> c.text, "children" -> jCats(c.children))
  def jCats(cats: Array[Cate]) = if (cats == null || cats.length == 0) JsNull else Json.toJson(cats.map(jCat))
  def jProduct(p: Product) = Json.obj("id" -> p.id, "name" -> p.name, "price" -> p.price ,"image" -> p.image, "fullCate" -> jCats(p.fullCate))
  def jsProduct(p: ShortProduct) = Json.obj("id" -> p.id, "name" -> p.name, "image" -> p.image)
  def jsProducts(products: Array[ShortProduct]): JsValue = if (products == null || products.length == 0) JsNull else Json.toJson(products.map(jsProduct))
  def jProducts(products: Array[Product]): JsValue = if (products == null || products.length == 0) JsNull else Json.toJson(products.map(jProduct))
}

