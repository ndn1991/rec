package com.vinecom.rec

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import com.vinecom.utils.Initializer
import com.vinecom.utils.FileSystemUtils
import com.typesafe.config.ConfigFactory
import java.io.File
import org.apache.spark.SparkConf
import com.vinecom.rec.app.ultil.Utils._
import org.apache.spark.rdd.RDD

/**
 * @author noind
 */
object Test {
	def main(args: Array[String]): Unit = {
		val conf = new SparkConf().setMaster("local[*]").setAppName("Rec Item2Item").set("spark.executor.memory", "3g")
		val sc = new SparkContext(conf)
		val _data = sc.textFile("D:/oms.dat")
			.map(_.split("\t") match {
				case Array(user, order, product, num) => (getLong(user), getLong(product))
				case _ => null
			})
			.filter(_ != null)
			.distinct()
		val data1 = _data.groupByKey()
			.filter(_._2.size == 1)
			.mapValues (v => v.head)
			.values
			.distinct()
			
		val data2 = sc.textFile("D:/oms.dat")
			.map(_.split("\t") match {
				case Array(user, order, product, num) => (getLong(product), 1)
				case _ => null
			})
			.filter(_ != null)
			.reduceByKey(_ + _)
			.filter(_._2 == 1)
			.keys
			.distinct()
//			_data.map(v => (v._2, v._1))
//			.groupByKey()
//			.filter(_._2.size == 1)
//			.keys
//			.distinct()
			
		val data = data1.intersection(data2)
			.collect()
		data.foreach(println)
		
		println(s"--- ${data.length}")
		val x: RDD[Double] = null 
//		x.me
		
	}
}