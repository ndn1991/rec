package com.ndn

import com.datastax.spark.connector.{SomeColumns, toSparkContextFunctions}
import org.apache.log4j.Logger
import scala.collection.JavaConverters._
import com.ndn.tools.{TopK, Utils}
import Utils._
import com.typesafe.config.ConfigFactory
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD

/**
 * Created by ndn on 3/10/2015.
 */
object ItemNeighs {
  def main(args: Array[String]) {
    val start = System.currentTimeMillis()
    val sc = new SparkContext()
    execute(sc)
    val finish = System.currentTimeMillis()
    println("total_time: " + (finish - start))
  }

  def findItemNeighborsByCosine(rows: RDD[(Long, Long, Double)], threshold: Double, numNeigh: Int, numPar: Int = 8) = {
    val itemL2 = rows.map { case (user, item, rate) => (item, rate)}
      .combineByKey(
        rate => rate * rate,
        (sqr: Double, rate: Double) => sqr + rate * rate,
        (sqr1: Double, sqr2: Double) => sqr1 + sqr2
      )
      .mapValues(math.sqrt)
      .collectAsMap()
      .toMap

    val userRates = rows.map { case (user, item, rate) => (user, (item, rate))}
      .groupByKey()
      .filter(_._2.size > 1)
      .values
      .map(_.toArray)

    val pairItemSims = sim(userRates, itemL2, threshold, numPar)

    pairItemSims.flatMap(keyOnFirstItem)
      .combineByKey(
        (v) => new TopK[Long](numNeigh).put(v),
        (topK: TopK[Long], v) => topK.put(v),
        (topK1: TopK[Long], topK2: TopK[Long]) => topK1.put(topK2)
      )
      .mapValues(_.real)
  }

  def findItemNeighborsByACOS(rows: RDD[(Long, Long, Double)], threshold: Double, numNeigh: Int, numPar: Int = 8) = {
    val userAvg = rows.map { case (user, item, rate) => (user, (rate, 1))}
      .reduceByKey((x, y) => (x._1 + y._1, x._2 + y._2))
      .mapValues(v => v._1 / v._2)

    val rs = rows.map { case (user, item, rate) => (user, (item, rate))}
      .join(userAvg)
      .mapValues { case ((item, rate), avg) => (item, rate - avg)}

    val itemL2 = rs.map { case (user, (item, rate)) => (item, rate)}
      .combineByKey(
        rate => rate * rate,
        (sqr: Double, rate: Double) => sqr + rate * rate,
        (sqr1: Double, sqr2: Double) => sqr1 + sqr2
      )
      .mapValues(math.sqrt)
      .collectAsMap()
      .toMap

    val userRates = rs.groupByKey()
      .filter(_._2.size > 1)
      .values
      .map(_.toArray)

    val pairItemSims = sim(userRates, itemL2, threshold, numPar)

    pairItemSims.flatMap(keyOnFirstItem)
      .combineByKey(
        (v) => new TopK[Long](numNeigh).put(v),
        (topK: TopK[Long], v) => topK.put(v),
        (topK1: TopK[Long], topK2: TopK[Long]) => topK1.put(topK2)
      )
      .mapValues(_.real)
  }

  def findItemNeighborsByPearson(rows: RDD[(Long, Long, Double)], threshold: Double, numNeigh: Int, numPar: Int = 8) = {
    val itemAvg = rows.map { case (user, item, rate) => (item, (rate, 1))}
      .reduceByKey((x, y) => (x._1 + y._1, x._2 + y._2))
      .mapValues(v => v._1 / v._2)

    val rs = rows.map { case (user, item, rate) => (item, (user, rate))}
      .join(itemAvg)
      .mapValues { case ((user, rate), avg) => (user, rate - avg)}

    val itemL2 = rs.mapValues { case (user, rate) => rate}
      .combineByKey(
        rate => rate * rate,
        (sqr: Double, rate: Double) => sqr + rate * rate,
        (sqr1: Double, sqr2: Double) => sqr1 + sqr2
      )
      .mapValues(math.sqrt)
      .collectAsMap()
      .toMap

    val userRates = rs.map { case (item, (user, rate)) => (user, (item, rate))}
      .groupByKey()
      .filter(_._2.size > 1)
      .values
      .map(_.toArray)

    val pairItemSims = sim(userRates, itemL2, threshold, numPar)

    pairItemSims.flatMap(keyOnFirstItem)
      .combineByKey(
        (v) => new TopK[Long](numNeigh).put(v),
        (topK: TopK[Long], v) => topK.put(v),
        (topK1: TopK[Long], topK2: TopK[Long]) => topK1.put(topK2)
      )
      .mapValues(_.real)
  }

  private val conf = ConfigFactory.load()
  private val keySpace = conf.getString("rec.cql.keySpace")
  private val table = conf.getString("rec.cql.table.orders")
  private val simType = conf.getString("rec.cql.sim.type.itemNeighs")
  private val threshold = conf.getDouble("rec.cql.threshold.itemNeighs")
  private val numNeigh = conf.getInt("rec.cql.numNeigh.itemNeighs")
  private val resultTable = conf.getString("rec.cql.table.itemNeighs")
  private val cqlCreates = conf.getStringList("rec.cql.create.table.itemNeighs").asScala.toList

  private val logger = Logger.getLogger(this.getClass)

  def execute(sc: SparkContext): Unit = {
    logger.error(s"keySpace: $keySpace, table: $table, simType: $simType, threshold: $threshold, numNeigh: $numNeigh, resultTable: $resultTable")
    logger.error(s"\ncqlCreates: $cqlCreates")
    val sc = new SparkContext()
    val rows = sc.cassandraTable(keySpace, table)
      .map(v => ((getLong(v.get[String]("user_id")), v.get[Long]("product_id")), v.get[Double]("num_product")))
      .reduceByKey { case (v1, v2) => 1.0}
      .map { case ((user, item), product) => (user, item, product)}

    val itemNeighSims = if (simType.equalsIgnoreCase("cos")) findItemNeighborsByCosine(rows, threshold, numNeigh)
    else if (simType.equalsIgnoreCase("acos")) findItemNeighborsByACOS(rows, threshold, numNeigh)
    else findItemNeighborsByPearson(rows, threshold, numNeigh)

    saveToCSD(itemNeighSims, keySpace, resultTable, cqlCreates, SomeColumns("item", "neighs", "sims"))
  }
}
