package assets.model

import play.api.libs.json._
import play.api.libs.functional.syntax._

/**
 * Created by ndn on 4/3/2015.
 */
case class ShortProduct(id: Int, name: String, image: String)

case class Product(id: Int, name: String, image: String, fullCate: Array[Cate])

case class Cate(id: Int, text: String, children: Array[Cate] = null)

case class RootCate(id: Int, text: String, children: Array[RootCate] = null)