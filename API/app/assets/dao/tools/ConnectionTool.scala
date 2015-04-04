package assets.dao.tools

import com.datastax.driver.core.{Session, Cluster}
import com.typesafe.config.ConfigFactory
import scala.collection.JavaConverters._

/**
 * Created by ndn on 3/24/2015.
 */
object ConnectionTool {
  private val conf = ConfigFactory.load()
  private val searchHosts = conf.getStringList("api.cql.hosts.search").asScala.toArray
  private val searchKeySpace = conf.getString("api.cql.keySpace.search")
  private val searchUser = conf.getString("api.cql.user.search")
  private val searchPass = conf.getString("api.cql.pass.search")
  private val recHosts = conf.getStringList("api.cql.hosts.rec").asScala.toArray
  private val recKeySpace = conf.getString("api.cql.keySpace.rec")
  private val recUser = conf.getString("api.cql.user.rec")
  private val recPass = conf.getString("api.cql.pass.rec")

  val solr = "http://10.220.75.84:8983/solr/search.product/select?q="

  val (searchCluster, searchSession) = csdConnection(searchHosts, searchKeySpace, searchUser, searchPass)
  val (recCluster, recSession) = csdConnection(recHosts, recKeySpace, recUser, recPass)

  /**
   * Get cassandra connection
   */
  private def csdConnection(hosts: Array[String], keySpace: String, user: String, password: String): (Cluster, Session) = {
    val builder = Cluster.builder()
    hosts.foreach(builder.addContactPoint)
    val cluster = builder.withCredentials(user, password).build()
    val session = cluster.connect(keySpace)
    (cluster, session)
  }

  def closeSearchSession(): Unit = {
    searchSession.close()
    searchCluster.close()
  }

  def closeRecSession(): Unit = {
    recSession.close()
    recCluster.close()
  }
}
