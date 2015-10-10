package assets.dao

import anorm.SQL
import assets.dao.interfaces.CategoryDAO
import assets.model._
import play.api.Play.current
import play.api.db._
import play.api.libs.ws.WS

import scala.collection.mutable
import scala.concurrent.Future

/**
 * Created by ndn on 4/3/2015.
 */
object CategoryDAOImpl extends CategoryDAO {
  override def getProducts(ids: Array[Int]): Array[Product] =  {
    WS.
    ConnectionTool.searchSession.execute(Const.cqlGetProducts.format(ids.mkString(","))).asScala.map(r => {
      val id = r.getInt("productid")
      val name = r.getString("productname")
      val scats = r.getString("categorytree").split(">>")
      val icats = r.getList("catpath", classOf[Integer]).asScala.map(_.toInt).toArray.reverse
      val cats = icats.zip(scats).map(v => Cate(v._1, v._2))
      val price = r.getDouble("sellprice")
      val image = Tools.getImage(r.getString("image"))
      Product(id, name, price, image, cats)
    }).toArray.filter(p => ItemNeighsDAOImpl.itemNeighsSet.contains(p.id))
  }

  private def getShortProducts(ids: Array[Int]): Array[ShortProduct] =  {
    ConnectionTool.searchSession.execute(Const.cqlGetShortProducts.format(ids.mkString(","))).asScala.map(r => {
      val id = r.getInt("productid")
      val name = r.getString("productname")
      val image = Tools.getImage(r.getString("image"))
      ShortProduct(id, name, image)
    }).toArray
  }

  override def getShortProducts(cat: Int, index: Int): Future[(Int, Array[ShortProduct])] = {
    val start = index * numProductPerCate
    WS.url(ConnectionTool.solr + Const.solrGetProducts.format(cat, start, numProductPerCate))
      .get()
      .map(_.json)
      .map(json => ((json \ "response" \ "numFound").as[Int], (json \\ "productid").map(_.toString().toInt).toArray.filter(ItemNeighsDAOImpl.itemNeighsSet.contains(_))))
      .map(v => (math.floor(v._1.toDouble / numProductPerCate).toInt, getShortProducts(v._2)))
  }

  override def getProduct(id: Int): Product = getProducts(Array(id)).head

  override def getDescendants(cat: Int): Cate = {
    if (cat1.contains(cat)) {
      getDescendants(cat, 1)
    }
    else if (treeMap.contains(cat)) {
      treeMap(cat)
    }
    else {
      null
    }
  }

  private val treeMap = new mutable.HashMap[Int, Cate]()
  getCateTree(0, "")
  private val cat1 = getDescendants(0, 1).children.map(c => (c.id)).toSet
  private def getCateTree(root: Int, rootName: String): Cate = DB.withConnection { implicit c =>
    val children = SQL(Const.sqlGetCatsFromParent)
      .on("parent" -> root)()
      .map(r => (r[Int]("id"), r[String]("name")))
      .toArray

    if (children == null || children.length == 0) {
      val c = Cate(root, rootName)
      treeMap += root -> c
      c
    }
    else {
      val _children = children.map { case (id, name) => getCateTree(id, name)}
      val c = Cate(root, rootName, _children)
      treeMap += root -> c
      c
    }
  }

  override def getDescendants(cat: Int, level: Int): Cate = {
    if (treeMap.contains(cat)) {
      val c = treeMap(cat)
      cut(c, 0, level)
    }
    else {
      null
    }
  }

  private def cut(cat: Cate, curLevel: Int, stopLevel: Int): Cate = {
    if (curLevel >= stopLevel || cat.children == null || cat.children.length == 0) {
      Cate(cat.id, cat.text)
    }
    else {
      val arr = cat.children.map(c => cut(c, curLevel + 1, stopLevel))
      Cate(cat.id, cat.text, arr)
    }
  }

  override def getAncestor(cat: Int): Array[Cate] = {
    val r = ConnectionTool.searchSession.execute(Const.cqlGetAncestor + cat).one()
    if (r == null) {
      Array[Cate]()
    }
    else {
      val scats = r.getString("categorytree").split(">>")
      val icats = r.getList("catpath", classOf[Integer]).asScala.map(_.toInt).toArray.reverse
      icats.zip(scats).map{case (id, name) => Cate(id, name)}
    }
  }
}