package assets.dao

import assets.dao.interfaces.ForUser
import assets.dao.tools.ConnectionTool
import ConnectionTool._
import scala.collection.JavaConverters._

/**
 * Created by ndn on 3/24/2015.
 */
object ItemBaseDAOImpl extends ForUser{
  private val cqlGetRecs = "select items from item_base where user=?"
  private val cqlGetRecsWithScore = "select items, scores from item_base where user=?"

  override def getRecItems(user: Long): Array[Long] = {
    val _user: java.lang.Long = user
    val r = recSession.execute(cqlGetRecs, _user).one
    if (r == null)
      Array.empty[Long]
    else
      r.getList("items", classOf[java.lang.Long]).asScala.toArray.map(_.toLong)
  }

  override def getRecItemsWithScore(user: Long): (Array[(Long, Double)]) = {
    val _user: java.lang.Long = user
    val r = recSession.execute(cqlGetRecsWithScore, _user).one
    if (r == null) Array.empty[(Long, Double)]
    else {
      val lNeighs = r.getList("items", classOf[java.lang.Long]).asScala.toArray.map(_.toLong)
      val lScores = r.getList("scores", classOf[java.lang.Double]).asScala.toArray.map(_.toDouble)
      lNeighs.zip(lScores)
    }
  }
}
