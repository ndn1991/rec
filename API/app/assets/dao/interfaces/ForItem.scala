package assets.dao.interfaces

/**
 * Created by ndn on 4/1/2015.
 */
trait ForItem {
  def getNeighs(item: Long): Array[Long]
  def getNeighsWithScore(item: Long): (Array[(Long, Double)])
}
