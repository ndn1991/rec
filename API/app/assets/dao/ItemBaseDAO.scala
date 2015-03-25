package assets.dao

import assets.dao.ConnectionTool._
import scala.collection.JavaConverters._

/**
 * Created by ndn on 3/24/2015.
 */
object ItemBaseDAO {
  private val cqlGetRecs = "select items from item_base where user=?"
  private val cqlGetRecsWithScore = "select items, scores from item_base where user=?"

  def getRecs(user: java.lang.Long): List[Long]= {
    val r = session.execute(cqlGetRecs, user).one
    if (r == null)
      null
    else
      r.getList("items", classOf[java.lang.Long]).asScala.toList.map(_.toLong)
  }

  def getRecsWithScore(user: java.lang.Long): List[(Long, Double)]= {
    val r = session.execute(cqlGetRecsWithScore, user).one
    if (r == null)
      null
    else {
      val lNeighs = r.getList("items", classOf[java.lang.Long])
      val lScores = r.getList("scores", classOf[java.lang.Double])
      val size = lNeighs.size() - 1
      val list = for (i <- 0 to size) yield (lNeighs.get(i).toLong, lScores.get(i).toDouble)
      list.toList
    }
  }
}
