package com.ndn.demos

import com.ndn.XORShiftRandom
import com.typesafe.config.ConfigFactory
import org.apache.spark.SparkContext
import com.datastax.spark.connector.{toSparkContextFunctions, SomeColumns}

/**
 * Created by ndn on 3/6/2015.
 */
object Main {
  def main(args: Array[String]) {
    val sc = new SparkContext()
    val rows = sc.cassandraTable("rec", "orders")
    println(rows.count())
  }
}
