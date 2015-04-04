package com.ndn.demos

import com.ndn.spark.mlib.recommendation.Rating1
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

import scala.util.Random

/**
 * Created by ndn on 3/31/2015.
 */
object NetFlixProccess {
  def get(sc: SparkContext): RDD[Rating1] =  {
    sc.wholeTextFiles("/user/root/nflx/training_set", 64).map(_._2).flatMap {
      file =>
        val splitted = file.split("\n")
        var flag = true
        var first = ""
        val map = splitted.map {
          line =>
            if (flag) {
              first = line
              flag = false
            }
            (first.substring(0, first.length - 1).toLong, line)
        }
        map
    }.filter(a => !a._2.endsWith(":"))
      .map(pair => {
      val splitted = pair._2.split(",")
      //user, movie, rating
      Rating1(splitted(0).toLong, pair._1, splitted(1).toFloat)
    })
  }

  def getTrainAndTest(por: Double, rdd: RDD[Rating1]) = {
    val a = rdd.randomSplit(Array(0.8, 0.2), Random.nextLong())

    (a(0), a(1))
  }
}
