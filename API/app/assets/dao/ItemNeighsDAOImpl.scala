package assets.dao

import assets.dao.interfaces.ForItem
import assets.dao.tools.ConnectionTool
import ConnectionTool._
import scala.collection.JavaConverters._

/**
 * Created by ndn on 3/24/2015.
 */
object ItemNeighsDAOImpl extends ForItem {
  private val cqlGetItemNeighs = "select neighs from item_neighs where item=?"
  private val cqlGetItemNeighsWithScore = "select neighs, sims from item_neighs where item=?"
  private val cqlGetSet = "select item from item_neighs"

  override def getNeighs(item: Long): Array[Long] = {
    val _item: java.lang.Long = item
    val r = recSession.execute(cqlGetItemNeighs, _item).one
    if (r == null)
      Array.empty[Long]
    else
      r.getList("neighs", classOf[java.lang.Long]).asScala.toArray.map(_.toLong)
  }

  override def getNeighsWithScore(item: Long): Array[(Long, Double)] = {
    val _item: java.lang.Long = item
    val r = recSession.execute(cqlGetItemNeighsWithScore, _item).one
    if (r == null)
      Array.empty[(Long, Double)]
    else {
      val lNeighs = r.getList("neighs", classOf[java.lang.Long]).asScala.toArray.map(_.toLong)
      val lScores = r.getList("sims", classOf[java.lang.Double]).asScala.toArray.map(_.toDouble)
      lNeighs.zip(lScores)
    }
  }

  val itemNeighsSet = getItemNeighsSet()
  private def getItemNeighsSet(): Set[Int] = {
    ConnectionTool.recSession.execute(cqlGetSet).asScala.map(_.getLong("item").toInt).toSet
  }
}
