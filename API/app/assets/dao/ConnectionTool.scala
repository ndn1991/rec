package assets.dao

import com.datastax.driver.core.Cluster
import com.typesafe.config.ConfigFactory
import scala.collection.JavaConverters._

/**
 * Created by ndn on 3/24/2015.
 */
object ConnectionTool {
  private val conf = ConfigFactory.load()
  private val hosts = conf.getStringList("api.cql.hosts").asScala
  private val keySpace = conf.getString("api.cql.keySpace")
  private val builder = Cluster.builder();
  for (host <- hosts) builder.addContactPoint(host)

  private val cluster = builder.build()
  val session = cluster.connect(keySpace)

  def closeSession(): Unit = {
    session.close()
    cluster.close()
  }
}
