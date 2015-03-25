package com.ndn

import com.datastax.spark.connector.{SomeColumns, toSparkContextFunctions}
import com.ndn.ItemNeighs.{findItemNeighborsByACOS, findItemNeighborsByCosine, findItemNeighborsByPearson}
import com.ndn.tools.Utils
import Utils._
import com.typesafe.config.ConfigFactory
import org.apache.log4j.Logger
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import scala.collection.JavaConverters._

/**
 * Created by ndn on 3/7/2015.
 */
object ItemBase {
  def main(args: Array[String]) {
    /*
    //get-config
    val param = parse(args)
    val inputDir = param("input_dir")
    val testDir = param("test_dir")
    val delimiter = param("delimiter")
    val numCore = param("num_core").toInt
    val numNeigh = param("num_neigh").toInt
    val numRec = param("num_rec").toInt

    val simType = param("sim_type")
    require(simType.equalsIgnoreCase("cos") || simType.equalsIgnoreCase("acos") || simType.equalsIgnoreCase("pearson"), println("sim_type can be one of (cos, acos, pearson)"))

    val threshold = param("threshold").toDouble
    require(threshold >= 0, s"Threshold cannot be negative: $threshold")
    //end-get-config

    val sc = new SparkContext()

    val start = System.currentTimeMillis()

    val rows = sc.textFile(inputDir, numCore)
      .map(_.split(delimiter) match { case Array(user, item, rate, time) => (user.toLong, item.toLong, rate.toDouble)})
      .cache()

    val itemNeighSims = if (simType.equalsIgnoreCase("cos")) findItemNeighborsByCosine(rows, threshold, numNeigh)
    else if (simType.equalsIgnoreCase("acos")) findItemNeighborsByACOS(rows, threshold, numNeigh)
    else findItemNeighborsByPearson(rows, threshold, numNeigh)

    val ibs = sc.broadcast(itemNeighSims.collectAsMap().toMap)

    val user_item_recs = rows.map{ case (user, item, rate) => (user, (item, rate))}
      .groupByKey()
      .mapValues(_.toList)
      .flatMapValues(topNRecommendationsForItem(_, ibs.value, numRec))
      .map(v => ((v._1, v._2._1), v._2._2))
//    println("numrec: " + user_item_recs.count())

    val test_user_item_pairs = sc.textFile(testDir, numCore)
      .map{ _.split(delimiter) match { case Array(user, item, rate, time) => ((user.toLong, item.toLong), rate.toDouble)}}

    val join = test_user_item_pairs.join(user_item_recs).values
//    println("join: " + join.count())

    val MAE = join.map(v => Math.abs(v._1 - v._2)).mean()
    println("MAE: " + MAE)

    val finish = System.currentTimeMillis()
    println("total_time: " + (finish - start))*/

    val sc = new SparkContext()
    execute(sc)
  }


  private val conf = ConfigFactory.load()
  private val numRec = conf.getInt("rec.cql.numRec.itemBase")
  private val keySpace = conf.getString("rec.cql.keySpace")
  private val table = conf.getString("rec.cql.table.orders")
  private val simType = conf.getString("rec.cql.sim.type.itemBase")
  private val threshold = conf.getDouble("rec.cql.threshold.itemBase")
  private val numNeigh = conf.getInt("rec.cql.numNeigh.itemBase")
  private val resultTable = conf.getString("rec.cql.table.itemBase")
  private val cqlCreates = conf.getStringList("rec.cql.create.table.itemBase").asScala.toList

  private val logger = Logger.getLogger(this.getClass)

  def execute(sc: SparkContext): Unit = {
    logger.error(s"rumRec: $numRec, keySpace: $keySpace, table: $table, simType: $simType, threshold: $threshold, numNeigh: $numNeigh, resultTable: $resultTable\ncqlCreates: $cqlCreates")
    val rows = sc.cassandraTable(keySpace, table)
      .map(v => ((getLong(v.get[String]("user_id")), v.get[Long]("product_id")), v.get[Double]("num_product")))
      .reduceByKey { case (v1, v2) => 1.0}
      .map { case ((user, item), product) => (user, item, product)}

    val itemNeighSims = if (simType.equalsIgnoreCase("cos")) findItemNeighborsByCosine(rows, threshold, numNeigh)
    else if (simType.equalsIgnoreCase("acos")) findItemNeighborsByACOS(rows, threshold, numNeigh)
    else findItemNeighborsByPearson(rows, threshold, numNeigh)

    val ibs = sc.broadcast(itemNeighSims.mapValues(_.toMap).collectAsMap().toMap)

    val userItemRecs = rows.map{ case (user, item, rate) => (user, (item, rate))}
      .groupByKey()
      .mapValues(_.toList)
      .mapValues(topNRecommendationsForItem(_, ibs.value, numRec))
      .filter(_._2 != null)

    saveToCSD(userItemRecs, keySpace, resultTable, cqlCreates, SomeColumns("user", "items", "scores"))
  }
}
