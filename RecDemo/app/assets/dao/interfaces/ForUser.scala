package assets.dao.interfaces

/**
 * Created by ndn on 4/1/2015.
 */
trait ForUser {
  def getRecItems(user: Long): Array[Long]
  def getRecItemsWithScore(user: Long): (Array[(Long, Double)])
}
