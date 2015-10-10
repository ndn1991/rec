package com.vinecom.rec.app

import com.vinecom.utils.Initializer
import com.vinecom.utils.FileSystemUtils
import com.typesafe.config.ConfigFactory
import java.io.File
import com.vinecom.rec.app.vo.config.DBConnection
import com.vinecom.rec.app.vo.config.ItemBaseRecConfig
import com.vinecom.rec.app.vo.config.Storage
import com.vinecom.rec.app.vo.config.ItemBaseConfig
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.apache.spark.rdd.RDD
import scala.collection.mutable
import com.vinecom.common.scala.tool.topK.ArrayTopK
import com.vinecom.common.scala.tool.topK.TopK
import com.vinecom.rec.app.ultil.Utils.keyOnFirstItem
import com.google.common.base.Joiner
import com.vinecom.rec.app.config.Config
import com.vinecom.rec.app.vo.config.Temp

/**
 * @author noind
 */
@Deprecated
object GetRecommendFromFile {
	val sc = new SparkContext()
	def main(args: Array[String]): Unit = {
		val config = Config.readConfig()
		val tempConfig: Temp = null
		val omsPath = tempConfig.read.order
		val viewPath = tempConfig.read.view

		val dataOms = sc.textFile(omsPath, 128)
			.map(_.split("\t") match { case Array(user, product, order, numProduct) => ((user.toLong, product.toInt), order.toInt) })
			.groupByKey()
			.mapValues(_.toSet)
			.map { case ((user, item), orders) => (user, item, orders) }
		val orderPairItemSims = orderColumnSimilarity(dataOms, 3)
			.mapValues(_ * 4)
			.map{case ((i1, i2), sim) => {
				if (i1 < i2) ((i1, i2), sim)
				else ((i2, i1), sim)
			}}
			.persist()

		val dataView = sc.textFile(viewPath, 512)
			.map(_.split("\t") match { case Array(user, item) => ((user.toLong, item.toInt), 1) })
			.combineByKey(once => once, (sum: Int, once) => sum + once, (sum1: Int, sum2: Int) => sum1 + sum2)
			.mapValues(_.toDouble)
			.map { case ((user, item), numView) => (user, item, numView) }
		val dataViewUserL2 = dataView.map { case (user, item, numView) => (user, numView) }
			.combineByKey(n => n * n,
				(sqr: Double, n) => sqr + n * n,
				(sqr1: Double, sqr2: Double) => sqr1 + sqr2)
			.mapValues(math.sqrt)
			.collectAsMap()
			.toMap
		val dataViewUserL2Bc = sc.broadcast(dataViewUserL2)
		val dataViewPerUserItem = dataView.mapPartitions(it => {
			val userL2 = dataViewUserL2Bc.value
			it.map { case (user, item, numView) => (user, item, (numView / userL2(user)).toFloat) }
		})
		val viewPairItemSims = viewColumnSimilarity(dataViewPerUserItem, 50, 3)
			.map{case ((i1, i2), sim) => {
				if (i1 < i2) ((i1, i2), sim)
				else ((i2, i1), sim)
			}}
			.persist()

		val pairItemSims = viewPairItemSims.union(orderPairItemSims)
			.combineByKey(score => score,
				(sum: Float, score) => sum + score,
				(sum1: Float, sum2: Float) => sum1 + sum2)
			.mapValues(_.toDouble)
			.flatMap(keyOnFirstItem)

		pairItemSims.combineByKey(
			(itemAndScore) => new ArrayTopK[Int](config.recConfig.numNeigh).put(itemAndScore),
			(topK: TopK[Int], itemAndScore) => topK.put(itemAndScore),
			(topK1: TopK[Int], topK2: TopK[Int]) => topK1.put(topK2))
			.mapValues(_.sorted)
			.mapValues(v => v.mkString("#"))
			.saveAsTextFile(tempConfig.result)
	}

	private def viewColumnSimilarity(rows: RDD[(Long, Int, Float)], numNeigh: Int, minMatch: Int = 3) = {
		val itemL2 = rows.map { case (user, item, rate) => (item, rate) }
			.combineByKey(
				rate => rate * rate,
				(sqr: Float, rate: Float) => sqr + rate * rate,
				(sqr1: Float, sqr2: Float) => sqr1 + sqr2)
			.mapValues(v => math.sqrt(v.toDouble).toFloat)
			.collectAsMap()
			.toMap

		val userRates = rows.map { case (user, item, rate) => (user, (item, rate)) }
			.groupByKey()
			.filter(_._2.size > 1)
			.values
			.map(_.toArray)

		val qBV = sc.broadcast(itemL2.map(v => (v._1, if (v._2 == 0) 1 else v._2)))

		val numCol = itemL2.size
		val pairItemSims = userRates.cache().mapPartitions(it => {
			val q = qBV.value

			val buf = mutable.HashMap[(Int, Int), (Float, Int)]().withDefaultValue((0, 0))
			val scaled = new Array[Float](numCol)
			it.foreach(row => {
				val nnz = row.size
				var k = 0
				while (k < nnz) {
					val row_k = row(k)
					scaled(k) = 1 / q(row_k._1)
					k += 1
				}
				k = 0
				while (k < nnz) {
					val i = row(k)._1
					val iVal = scaled(k)
					if (iVal != 0) {
						var l = k + 1
						while (l < nnz) {
							val j = row(l)._1
							val jVal = scaled(l)
							if (jVal != 0) {
								val tup = (buf(i, j)._1 + iVal * jVal, buf(i, j)._2 + 1)
								buf.put((i, j), tup)
							}
							l += 1
						}
					}
					k += 1
				}
			})
			buf.toIterator
		}).reduceByKey((x1, x2) => (x1._1 + x2._1, x1._2 + x2._2))
			.filter(_._2._2 > minMatch)
			.mapValues(v => v._1 * math.log10(v._2))
			.mapValues(_.toFloat)

		pairItemSims
	}
	private def orderColumnSimilarity(data: RDD[(Long, Int, Set[Int])], minMatch: Int = 3): RDD[((Int, Int), Float)] = {
		val itemL2 = data.map { case (user, item, orders) => (item, 1) }
			.combineByKey(
				rate => 1.0,
				(sqr: Double, rate) => sqr + 1,
				(sqr1: Double, sqr2: Double) => sqr1 + sqr2)
			.mapValues(v => math.sqrt(v).toFloat)
			.collectAsMap()
			.toMap

		val userRates = data.map { case (user, item, rate) => (user, (item, rate)) }
			.groupByKey()
			.filter(_._2.size > 1)
			.values
			.map(_.toArray)

		val qBV = sc.broadcast(itemL2.map(v => (v._1, if (v._2 == 0) 1 else v._2)))

		val numCol = itemL2.size
		val pairItemSims = userRates.cache().mapPartitions(it => {
			val q = qBV.value

			val buf = mutable.HashMap[(Int, Int), (Float, Int)]().withDefaultValue((0, 0))
			val scaled = new Array[Float](numCol)
			it.foreach(row => {
				val nnz = row.size
				var k = 0
				while (k < nnz) {
					val row_k = row(k)
					scaled(k) = 1 / q(row_k._1)
					k += 1
				}
				k = 0
				while (k < nnz) {
					val i = row(k)._1
					val iVal = scaled(k)
					if (iVal != 0) {
						var l = k + 1
						while (l < nnz) {
							val j = row(l)._1
							val jVal = scaled(l)
							if (jVal != 0) {
								val jointOrderSize = (row(k)._2 & row(l)._2).size
								val tup = (buf(i, j)._1 + iVal * jVal, buf(i, j)._2 + 2 * jointOrderSize + 1)
								buf.put((i, j), tup)
							}
							l += 1
						}
					}
					k += 1
				}
			})
			buf.toIterator
		}).reduceByKey((x1, x2) => (x1._1 + x2._1, x1._2 + x2._2))
			.filter(_._2._2 > minMatch)
			.mapValues(v => v._1 * math.log10(v._2))
			.mapValues(_.toFloat)

		pairItemSims
	}
}