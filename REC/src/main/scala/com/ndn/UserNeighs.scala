package com.ndn

import com.datastax.spark.connector.{toSparkContextFunctions, SomeColumns}
import com.ndn.tools.Utils._
import com.typesafe.config.ConfigFactory
import org.apache.log4j.Logger
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD
import scala.collection.JavaConverters._

/**
 * Created by ndn on 3/17/2015.
 */
object UserNeighs {
  var numPartitionOutput = 7

  def main(args: Array[String]) {
    val start = System.currentTimeMillis()

    val finish = System.currentTimeMillis()
    println("total_time: " + (finish - start))
  }

  def findUserNeighborsByCosine(rows: RDD[(Long, Long, Double)], threshold: Double, numNeigh: Int) = {
    ItemNeighs.findItemNeighborsByCosine(rows.map{ case (user, item, rate) => (item, user, rate)}, threshold, numNeigh)
  }

  def findUserNeighborsByACOS(rows: RDD[(Long, Long, Double)], threshold: Double, numNeigh: Int) = {
    ItemNeighs.findItemNeighborsByACOS(rows.map{ case (user, item, rate) => (item, user, rate)}, threshold, numNeigh)
  }

  def findUserNeighborsByPearson(rows: RDD[(Long, Long, Double)], threshold: Double, numNeigh: Int) = {
    ItemNeighs.findItemNeighborsByPearson(rows.map{ case (user, item, rate) => (item, user, rate)}, threshold, numNeigh)
  }

  val conf = ConfigFactory.load()
  val keySpace = conf.getString("rec.cql.keySpace")
  val table = conf.getString("rec.cql.table.orders")
  val simType = conf.getString("rec.cql.sim.type.userNeighs")
  val threshold = conf.getDouble("rec.cql.threshold.userNeighs")
  val numNeigh = conf.getInt("rec.cql.numNeigh.userNeighs")
  val resultTable = conf.getString("rec.cql.table.userNeighs")
  val sc = new SparkContext()
  val cqlCreates = conf.getStringList("rec.cql.create.table.userNeighs").asScala.toList

  val logger = Logger.getLogger(this.getClass)

  def execute(): Unit = {
    logger.error(s"keySpace: $keySpace, table: $table, simType: $simType, threshold: $threshold, numNeigh: $numNeigh, resultTable: $resultTable\ncqlCreates: $cqlCreates")
    val rows = sc.cassandraTable(keySpace, table)
      .map(v => ((getLong(v.get[String]("user_id")), v.get[Long]("product_id")), v.get[Double]("num_product")))
      .reduceByKey { case (v1, v2) => 1.0}
      .map { case ((user, item), product) => (user, item, product)}

    val userNeighSims = if (simType.equalsIgnoreCase("cos")) findUserNeighborsByCosine(rows, threshold, numNeigh)
    else if (simType.equalsIgnoreCase("acos")) findUserNeighborsByACOS(rows, threshold, numNeigh)
    else findUserNeighborsByPearson(rows, threshold, numNeigh)

    saveToCSD(userNeighSims, keySpace, resultTable, cqlCreates, SomeColumns("user", "neighs", "sims"))
  }
}
