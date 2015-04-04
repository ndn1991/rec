package assets.dao.interfaces

import assets.model.{Cate, Product, ShortProduct}

import scala.concurrent.Future

/**
 * Created by ndn on 4/3/2015.
 */
trait CategoryDAO {
  var numProductPerCate = 24

  def getChildren(parent: Int): Array[Cate]

  def getShortProducts(cat: Int, index: Int): Future[(Int, Array[ShortProduct])]

  def getProducts(ids: Seq[Int]): Array[Product]

  def getProduct(id: Int): Product
}
