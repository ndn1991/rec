package assets.model

/**
 * Created by ndn on 4/3/2015.
 */
object Const {
  val sqlGetCatsFromParent = "with C as (select Id _id, CategoryName _catName from Category where ParentCategoryId={parent})" +
    "\nselect *, case when Exists(select 1 from Category where ParentCategoryId=c._id) then 1 else 0 end as child from c"

  val cqlGetProducts = "SELECT productid, categorytree, catpath, image, productname FROM product where productid in ?;"

  val cqlGetShortProducts = "SELECT productid, image, productname FROM product where productid in ?;"

  val solrGetProducts = "catpath:%d&start=%d&rows=%d&wt=json"
}
