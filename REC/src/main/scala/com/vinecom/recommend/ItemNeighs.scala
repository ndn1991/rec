package com.vinecom.recommend

import com.vinecom.common.Loggable
import com.vinecom.recommend.tools.topK.{TopK, ArrayTopK}
import com.vinecom.recommend.tools.Utils
import Utils._
import org.apache.spark.rdd.RDD

/**
 * Created by ndn on 3/10/2015.
 */
object ItemNeighs extends Loggable{
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
        (v) => new ArrayTopK[Long](numNeigh).put(v),
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
        (v) => new ArrayTopK[Long](numNeigh).put(v),
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
        (v) => new ArrayTopK[Long](numNeigh).put(v),
        (topK: TopK[Long], v) => topK.put(v),
        (topK1: TopK[Long], topK2: TopK[Long]) => topK1.put(topK2)
      )
      .mapValues(_.real)
  }

  def findItemNeighborsByPr(rows: RDD[(Long, Long, Double)], numNeigh: Int, alpha: Double, numPar: Int) = {
    val itemLen = rows.map{case (user, item, rate) => (item, 1)}
      .combineByKey(
        c => c,
        (count: Int, c: Int) => count + c,
        (count1: Int, count2: Int) => count1 + count2
      ).collectAsMap().toMap
    val userRates = rows.map{case (user, item, rate) => (user, (item, rate))}
      .groupByKey()
      .filter(_._2.size > 1)
      .values
      .map(_.toArray)
    colSimPr(userRates, itemLen, alpha, numPar)
      .map{case ((i1, i2), sim) => (i1, (i2, sim))}
      .combineByKey(
        (v) => new ArrayTopK[Long](numNeigh).put(v),
        (topK: TopK[Long], v) => topK.put(v),
        (topK1: TopK[Long], topK2: TopK[Long]) => topK1.put(topK2)
      )
      .mapValues(_.real)
  }
}
