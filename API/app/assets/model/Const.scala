package assets.model

/**
 * Created by ndn on 4/3/2015.
 */
object Const {
  val sqlGetCatsFromParent = "select id, CategoryName name from Category where ParentCategoryId={parent} and CategoryStatus=1"
  val sqlGetCat = "select CategoryName name from Category where Id={id} and CategoryStatus=1"

  val solrGetProducts = "catpath:%d&fl=productid&start=%d&rows=%d&wt=json"

  val cqlGetProducts = "SELECT productid, productname, categorytree, catpath, sellprice, image FROM product where productid in (%s);"

  val cqlGetShortProducts = "SELECT productid, productname, image FROM product where productid in (%s);"

  val cqlGetAncestor = "SELECT categorytree, catpath FROM category WHERE categoryid="
}