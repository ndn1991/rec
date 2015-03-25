package controllers

import assets.dao.ItemNeighsDAO
import play.api.libs.json.Json
import play.api.mvc._
import scala.collection.JavaConverters._

object Application extends Controller {

  def index = Action {
    Ok("ssssss")
  }

  def get(item: Long) = Action {
    Ok(Json.toJson(ItemNeighsDAO.getItemNeighs(item)))
  }

  def get1(item: Long) = Action {
    Ok(Json.toJson(ItemNeighsDAO.getItemNeighsWithScore(item)))
  }
}