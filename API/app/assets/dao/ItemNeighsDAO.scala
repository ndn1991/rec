package assets.dao

import assets.dao.ConnectionTool._
import scala.collection.JavaConverters._

/**
 * Created by ndn on 3/24/2015.
 */
object ItemNeighsDAO {
  private val cqlGetItemNeighs = "select neighs from item_neighs where item=?"
  private val cqlGetItemNeighsWithScore = "select neighs, sims from item_neighs where item=?"

  def getItemNeighs(item: java.lang.Long): List[Long]= {
    val r = session.execute(cqlGetItemNeighs, item).one
    if (r == null)
      List()
    else
      r.getList("neighs", classOf[java.lang.Long]).asScala.toList.map(_.toLong)
  }

  def getItemNeighsWithScore(item: java.lang.Long): List[String]= {
    val r = session.execute(cqlGetItemNeighsWithScore, item).one
    if (r == null)
      List()
    else {
      try {
        val lNeighs = r.getList("neighs", classOf[java.lang.Long])
        val lScores = r.getList("sims", classOf[java.lang.Double])
        val size = lNeighs.size() - 1
        val list = for (i <- 0 to size) yield lNeighs.get(i).toLong + "," + lScores.get(i).toDouble
        list.toList
      }
      catch {
        case e: IndexOutOfBoundsException => List()
      }
    }
  }
}
