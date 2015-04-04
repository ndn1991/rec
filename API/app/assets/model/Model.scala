package assets.model

/**
 * Created by ndn on 4/3/2015.
 */
case class ShortProduct (id: Int, name: String, image: String)
case class Product(id: Int, name: String, image: String, fullCate: Array[Cate])
case class Cate(id: Int, text: String, children: Boolean = false)