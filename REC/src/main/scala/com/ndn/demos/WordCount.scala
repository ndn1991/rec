package com.ndn.demos

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._

/**
 * Created by ndn on 3/16/2015.
 */
object WordCount {
  def main(args: Array[String]) {
    val sc = new SparkContext()

    val start = System.currentTimeMillis()

    val file = sc.textFile(args(0), args(2).toInt)
    val counts = file.flatMap(line => {line.split(" ")})
      .map(word => (word, 1))
      .reduceByKey(_ + _)
    counts.saveAsTextFile(args(1))

    val finish = System.currentTimeMillis()
    println("total_time: " + (finish - start))
  }
}
