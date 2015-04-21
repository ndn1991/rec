package controllers

import assets.dao.tools.JsonParser
import assets.dao.{CategoryDAOImpl, ALSDAOImpl, ItemBaseDAOImpl, ItemNeighsDAOImpl}
import play.api.cache.Cached
import play.api.libs.json.Json
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Play.current

object Application extends Controller {

  def index = Action {
    Ok("xxx")
  }

  def getItemNeighs(item: Long) = Action {
    Ok(Json.toJson(ItemNeighsDAOImpl.getNeighs(item)))
  }

  def getItemNeighsWithScore(item: Long) = Action {
    Ok(Json.toJson(ItemNeighsDAOImpl.getNeighsWithScore(item).map(v => v._1 + "-" + v._2)))
  }

  def getRecItemsItemBase(user: Long) = Action {
    Ok(Json.toJson(ItemBaseDAOImpl.getRecItems(user)))
  }

  def getRecItemsItemBaseWithScore(user: Long) = Action {
    Ok(Json.toJson(ItemBaseDAOImpl.getRecItemsWithScore(user).map(v => v._1 + "-" + v._2)))
  }

  def getRecItemsALS(user: Long) = Action {
    Ok(Json.toJson(ALSDAOImpl.getRecItems(user)))
  }

  def getRecItemsALSWithScore(user: Long) = Action {
    Ok(Json.toJson(ALSDAOImpl.getRecItemsWithScore(user).map(v => v._1 + "-" + v._2)))
  }

  def getRootCate = Cached("rootCate"){ Action {
    Ok(JsonParser.jCat(CategoryDAOImpl.getDescendants(0, 2)))
  }}

  def getSubCate(root: Int) = Action {
    Ok(JsonParser.jCat(CategoryDAOImpl.getDescendants(root)))
  }

  def getProducts(ids: String) = Action {
    val _ids = ids.split(",").map(_.toInt)
    Ok(JsonParser.jProducts(CategoryDAOImpl.getProducts(_ids)))
  }

  def getShortProducts(cat: Int, index: Int) = Action.async {
    val ancestor = CategoryDAOImpl.getAncestor(cat)
    val subCate = CategoryDAOImpl.getDescendants(cat)
    CategoryDAOImpl.getShortProducts(cat, index)
      .map {case (count, products) => Json.obj("index" -> index ,"subCate" -> JsonParser.jCat(subCate), "ancestor" -> JsonParser.jCats(ancestor), "count" -> count, "products" -> JsonParser.jsProducts(products))}
      .map(Ok(_))
  }

  def getProduct(id: Int) = Action {
    val detail = JsonParser.jProduct(CategoryDAOImpl.getProduct(id))
    val recs = JsonParser.jProducts(CategoryDAOImpl.getProducts(ItemNeighsDAOImpl.getNeighs(id).map(_.toInt)))
    Ok(Json.obj("detail" -> detail, "recs" -> recs))
  }

  def getAncestor(cat: Int) = Action {
    Ok(JsonParser.jCats(CategoryDAOImpl.getAncestor(cat)))
  }
}