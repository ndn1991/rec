package com.vinecom.recommend.demos

import com.vinecom.recommend.UserNeighs
import UserNeighs.{findUserNeighborsByACOS, findUserNeighborsByCosine, findUserNeighborsByPearson}
import com.vinecom.recommend.tools.Utils
import Utils._
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._

/**
 * Created by ndn on 4/2/2015.
 */
object UserBaseTest {
  def main(args: Array[String]) {
    val simType = args(0)
    val threshold = 0.0
    val numNeigh = args(1).toInt
    val numRec = args(2).toInt
    val sc = new SparkContext()
    val data = sc.textFile(args(4), 42)
    val rows = data.map(_.split(args(6)) match { case Array(user, item, rate, time) => (user.toLong, item.toLong, rate.toDouble)
    })

    val userNeighSims = if (simType.equalsIgnoreCase("cos")) findUserNeighborsByCosine(rows, threshold, numNeigh)
    else if (simType.equalsIgnoreCase("acos")) findUserNeighborsByACOS(rows, threshold, numNeigh)
    else findUserNeighborsByPearson(rows, threshold, numNeigh)

    val userRates = rows.map { case (user, item, rate) => (user, (item, rate))}
      .groupByKey()
      .mapValues(_.toList)
      .collectAsMap()
      .toMap
    val ursB = sc.broadcast(userRates)

    val predictions = userNeighSims.mapValues(_.toMap).map(topNRecommendationsForUser(_, ursB.value, numRec)).flatMap{case(user, arr) => arr.map{case(item, rate) => ((user, item), rate)}}

    val mse = sc.textFile(args(5), 42)
      .map(_.split(args(6)) match {case Array(user, item, rate, time) => ((user.toLong, item.toLong), rate.toFloat)})
      .join(predictions)
      .mapValues{case(v1, v2) => (v1-v2)*(v1-v2)}
      .values
      .mean()
    val rmse = math.sqrt(mse)
    println(s"rmse: $rmse")
  }
}
