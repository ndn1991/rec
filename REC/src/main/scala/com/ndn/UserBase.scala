package com.ndn

import com.datastax.spark.connector.{SomeColumns, toSparkContextFunctions}
import com.ndn.tools.Utils._
import com.ndn.UserNeighs._
import com.typesafe.config.ConfigFactory
import org.apache.log4j.Logger
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import scala.collection.JavaConverters._

/**
 * Created by ndn on 3/6/2015.
 */
object UserBase {
  def main(args: Array[String]) {
    /*
    //get-config
    val param = parse(args)
    val inputDir = param("input_dir")
    val testDir = param("test_dir")
    val delimiter = param("delimiter")
    val numCore = param("num_core").toInt
    val numNeigh = param("num_neigh").toInt
//    val keySpace = param("key_space")
//    val tableName = param("table_name")
    val numRec = param("num_rec").toInt

    val simType = param("sim_type")
    require(simType.equalsIgnoreCase("cos") || simType.equalsIgnoreCase("acos") || simType.equalsIgnoreCase("pearson"), println("sim_type can be one of (cos, acos, pearson)"))

    val threshold = param("threshold").toDouble
    require(threshold >= 0, s"Threshold cannot be negative: $threshold")

//    val cqlCreates = List(s"CREATE KEYSPACE IF NOT EXISTS $keySpace WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1 }",
//      s"CREATE TABLE IF NOT EXISTS $tableName (item int, neighs map <int, double>,	PRIMARY KEY (item))",
//      s"TRUNCATE $keySpace.$tableName")
    //end-get-config

    val start = System.currentTimeMillis

    val sc = new SparkContext(new SparkConf().setAppName("User based"))

    val rows = sc.textFile(inputDir, numCore)
      .map(_.split(delimiter) match { case Array(user, item, rate, time) => (user.toLong, item.toLong, rate.toDouble)})
      .cache()

    val userNeighSims = if (simType.equalsIgnoreCase("cos")) findUserNeighborsByCosine(rows, threshold, numNeigh)
    else if (simType.equalsIgnoreCase("acos")) findUserNeighborsByACOS(rows, threshold, numNeigh)
    else findUserNeighborsByPearson(rows, threshold, numNeigh)

    val userRates = rows.map { case (user, item, rate) => (user, (item, rate))}
      .groupByKey()
      .mapValues(_.toList)
      .collectAsMap()
      .toMap
    val ursB = sc.broadcast(userRates)

    //Prediction of ratings
    val predictionPerUser = userNeighSims.flatMap(topNRecommendationsForUser(_, ursB.value, numRec))
    val prediction = predictionPerUser.map { case (user, (item, rate)) => ((user, item), rate)}
//    println("numrecs: " + prediction.count())

    val testData = sc.textFile(testDir)
      .map(_.split(args(0)) match { case Array(user, item, rate, time) => ((user.toLong, item.toLong), rate.toDouble)})

    val testVsPredict = prediction.join(testData)
//    println("join: " + testVsPredict.count())

    val MAE = testVsPredict.map { case ((user, item), (p, r)) => Math.abs(p - r)}.mean()
    println("MAE: " + MAE)

    val finish = System.currentTimeMillis
    println("Total time: " + (finish - start))*/

    execute()
  }

  val conf = ConfigFactory.load()
  val numRec = conf.getInt("rec.cql.numRec.userBase")
  val keySpace = conf.getString("rec.cql.keySpace")
  val table = conf.getString("rec.cql.table.orders")
  val simType = conf.getString("rec.cql.sim.type.userBase")
  val threshold = conf.getDouble("rec.cql.threshold.userBase")
  val numNeigh = conf.getInt("rec.cql.numNeigh.userBase")
  val resultTable = conf.getString("rec.cql.table.userBase")
  val sc = new SparkContext()
  val cqlCreates = conf.getStringList("rec.cql.create.table.userBase").asScala.toList

  val logger = Logger.getLogger(this.getClass)

  def execute(): Unit = {
    logger.error(s"rumRec: $numRec, keySpace: $keySpace, table: $table, simType: $simType, threshold: $threshold, numNeigh: $numNeigh, resultTable: $resultTable\ncqlCreates: $cqlCreates")

    val rows = sc.cassandraTable(keySpace, table)
      .map(v => ((getLong(v.get[String]("user_id")), v.get[Long]("product_id")), v.get[Double]("num_product")))
      .reduceByKey { case (v1, v2) => 1.0}
      .map { case ((user, item), product) => (user, item, product)}

    val userNeighSims = if (simType.equalsIgnoreCase("cos")) findUserNeighborsByCosine(rows, threshold, numNeigh)
    else if (simType.equalsIgnoreCase("acos")) findUserNeighborsByACOS(rows, threshold, numNeigh)
    else findUserNeighborsByPearson(rows, threshold, numNeigh)

    val userRates = rows.map { case (user, item, rate) => (user, (item, rate))}
      .groupByKey()
      .mapValues(_.toList)
      .collectAsMap()
      .toMap
    val ursB = sc.broadcast(userRates)

    val predictions = userNeighSims.mapValues(_.toMap).map(topNRecommendationsForUser(_, ursB.value, numRec))
    saveToCSD(predictions, keySpace, resultTable, cqlCreates, SomeColumns("user", "items"))
  }
}



