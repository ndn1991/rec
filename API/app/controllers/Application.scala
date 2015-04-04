package controllers

import assets.dao.tools.JsonParser
import assets.dao.{CategoryDAOImpl, ALSDAOImpl, ItemBaseDAOImpl, ItemNeighsDAOImpl}
import play.api.libs.json.Json
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global

object Application extends Controller {

  def index = Action {
    Ok("ssssss")
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

  def getChildrenCate(cate: Int) = Action {
    Ok(JsonParser.jCats(CategoryDAOImpl.getChildren(cate)))
  }

  def getProducts(ids: List[Int]) = Action {
    Ok(JsonParser.jProducts(CategoryDAOImpl.getProducts(ids)))
  }

  def getShortProducts(cat: Int, index: Int) = Action.async {
    CategoryDAOImpl.getShortProducts(cat, index)
    .map {case (count, products) => Json.obj("count" -> count, "products" -> JsonParser.jsProducts(products))}
    .map(Ok(_))
  }

  def getProduct(id: Int) = Action {
    Ok(JsonParser.jProduct(CategoryDAOImpl.getProduct(id)))
  }
}