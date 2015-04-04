package assets.dao.tools

import assets.model.{ShortProduct, Product, Cate}
import play.api.libs.json._

/**
 * Created by ndn on 4/4/2015.
 */
object JsonParser {
  def jCat(c: Cate) = Json.obj("id" -> c.id, "text" -> c.text, "children" -> c.children)
  def jCats(cats: Array[Cate]) = Json.arr(cats.map(jCat))
  def jProduct(p: Product) = Json.obj("id" -> p.id, "name" -> p.name, "image" -> p.image, "fullCate" -> jCats(p.fullCate))

  def jsProduct(p: ShortProduct) = Json.obj("id" -> p.id, "name" -> p.name, "image" -> p.image)
  def jsProducts(products: Array[ShortProduct]) = Json.arr(products.map(jsProduct))
  def jProducts(products: Array[Product]) = Json.arr(products.map(jProduct))
}

