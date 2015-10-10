package com.vinecom.recommend

import org.apache.spark.rdd.RDD

/**
 * Created by ndn on 3/17/2015.
 */
object UserNeighs {
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
}
