package com.vinecom.recommend.demos

import com.vinecom.recommend.ItemNeighs
import ItemNeighs._
import com.vinecom.recommend.tools.Utils
import Utils._
import org.apache.spark.{SparkConf, SparkContext}

/**
 * Created by ndn on 4/2/2015.
 */
object ItemBaseTest {
  def main(args: Array[String]) {
    val simType = "cosin"
    val threshold = 0.0
    val numNeigh = 1000
    val numRec = 100

    val conf = new SparkConf().setMaster("local[*]").setAppName("Test")
    val sc = new SparkContext(conf)

    val data = sc.textFile("D:\\ml-100k\\u1.base", 4)
    val rows = data.map(_.split("\t") match { case Array(user, item, rate, time) => (user.toLong, item.toLong, rate.toDouble)
    })

    val userNeighSims = if (simType.equalsIgnoreCase("cos")) findItemNeighborsByCosine(rows, threshold, numNeigh)
    else if (simType.equalsIgnoreCase("acos")) findItemNeighborsByACOS(rows, threshold, numNeigh)
    else findItemNeighborsByPearson(rows, threshold, numNeigh)

    val itemNeighSims = if (simType.equalsIgnoreCase("cos")) findItemNeighborsByCosine(rows, threshold, numNeigh)
    else if (simType.equalsIgnoreCase("acos")) findItemNeighborsByACOS(rows, threshold, numNeigh)
    else findItemNeighborsByPearson(rows, threshold, numNeigh)

    val ibs = sc.broadcast(itemNeighSims.mapValues(_.toMap).collectAsMap().toMap)

    val predictions = rows.map{ case (user, item, rate) => (user, (item, rate))}
      .groupByKey()
      .mapValues(_.toList)
      .mapValues(topNRecommendationsForItem(_, ibs.value, numRec))
      .filter(_._2 != null)
      .flatMap{case (user, arr) => arr.map{case (item, rate) => ((user, item), rate)}}


    val mse = sc.textFile("D:\\ml-100k\\u1.test", 42)
      .map(_.split("\t") match {case Array(user, item, rate, time) => ((user.toLong, item.toLong), rate.toFloat)})
      .join(predictions)
      .mapValues{case(v1, v2) => (v1-v2)*(v1-v2)}
      .values
      .mean()
    val rmse = math.sqrt(mse)
    println(s"rmse: $rmse")
  }
}
