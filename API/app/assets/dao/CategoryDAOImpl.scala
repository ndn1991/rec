package assets.dao

import anorm.{Row, SQL}
import assets.dao.interfaces.CategoryDAO
import assets.dao.tools.ConnectionTool
import assets.model._
import play.api.db._
import play.api.libs.ws.WS
import scala.collection.JavaConverters._
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by ndn on 4/3/2015.
 */
object CategoryDAOImpl extends CategoryDAO {
  val sqlGetChildCats = ""

  override def getChildren(parent: Int): Array[Cate] = DB.withConnection { implicit c =>
    SQL(Const.sqlGetCatsFromParent)
      .on("parent" -> parent)()
      .map { case Row(id: Int, text: String, children: Int) => Cate(id, text, children == 1)}
      .toArray
  }

  override def getProducts(ids: Seq[Int]): Array[Product] = {
    ConnectionTool.searchSession.execute(Const.cqlGetProducts, ids)
      .asScala.map(r => {
      val catNames = r.getString("categorytree").split(">>").reverse
      val catIds = r.getList("catpath", classOf[java.lang.Integer]).asScala.toArray.map(_.toInt)
      val cats = catIds.zip(catNames).map { case (id, name) => Cate(id, name)}
      Product(r.getInt("productid"),
        r.getString("productname"),
        Tools.getImage(r.getString("image")),
        cats)
    }).toArray
  }

  private def getShortProducts(ids: Seq[Int]): Array[ShortProduct] = {
    ConnectionTool.searchSession.execute(Const.cqlGetShortProducts, ids)
      .asScala.map(r => ShortProduct(r.getInt("productid"), r.getString("productname"), Tools.getImage(r.getString("image"))))
      .toArray
  }

  override def getShortProducts(cat: Int, index: Int): Future[(Int, Array[ShortProduct])] = {
    val start = index * numProductPerCate
    WS.url(ConnectionTool.solr + Const.solrGetProducts.format(cat, start, numProductPerCate))
      .get()
      .map(_.json)
      .map(json => ((json \ "response" \ "numFound").as[Int], (json \\ "productid").map(_.toString().toInt).toSeq))
      .map(v => (v._1, getShortProducts(v._2)))
  }

  override def getProduct(id: Int): Product = getProducts(Seq(id)).head
}
