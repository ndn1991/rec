package assets.model

import play.api.libs.json.Json

/**
 * Created by ndn on 4/3/2015.
 */
object Tools {
  /**
   * Get first image from json string in image column in sql server
   */
  def getImage(images: String): String = {
    try {
      val json = Json.parse(images)
      json(0).\("ImageUrl").as[String]
    }
    catch{
      case e: Exception => ""
    }
  }
}
