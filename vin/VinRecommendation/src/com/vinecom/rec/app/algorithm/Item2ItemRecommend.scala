package com.vinecom.rec.app.algorithm

import scala.collection.mutable
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.rdd.RDD.rddToPairRDDFunctions
import com.vinecom.common.scala.tool.topK.ArrayTopK
import com.vinecom.common.scala.tool.topK.TopK
import com.vinecom.rec.app.ultil.Utils.getLong
import com.vinecom.rec.app.ultil.Utils.keyOnFirstItem
import org.apache.spark.sql.SQLContext
import com.vinecom.rec.app.vo.config.ItemBaseConfig
import org.apache.spark.HashPartitioner
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.linalg.distributed.RowMatrix
import com.vinecom.common.scala.tool.random.XORShiftRandom
import scala.collection.mutable.ListBuffer
import org.apache.spark.Logging
/**
 * @author ndn
 */
object Item2ItemRecommend extends Logging {
	var config: ItemBaseConfig = null
	var sc: SparkContext = null
	var sqlContext: SQLContext = null
	val partitioner = new HashPartitioner(128)

	def getRec(data: RDD[(Long, Int, Double)]) = {
		columnSimilarities(data, config.recConfig.threshold)
			.flatMap(keyOnFirstItem)
			.mapValues(v => (v._1, v._2.toDouble))
			.combineByKey(v => new ArrayTopK[Int](50).put(v),
				(topK: TopK[Int], v) => topK.put(v),
				(topK1: TopK[Int], topK2: TopK[Int]) => topK1.put(topK2))
	}

	def init(_config: ItemBaseConfig, _sc: SparkContext) {
		config = _config
		sc = _sc
		sqlContext = new SQLContext(sc)
	}

	def getVirtualRate(rate: Int = 5) = {
		val userProductOrder = getOrderData().mapValues(_ * rate)
		val userProductView = getViewData()
		val data = userProductView
			.union(userProductOrder)
			.combineByKey((c: Int) => c, (sum: Int, c: Int) => (sum + c), (sum1: Int, sum2: Int) => sum1 + sum2, partitioner)
		val max = data.map { case ((u, i), count) => (u, count) }
			.combineByKey(c => c, (max: Int, c) => math.max(max, c), (max1: Int, max2: Int) => math.max(max1, max2))
			.collectAsMap()
			.toMap
		val maxB = sc.broadcast(max)
		data.mapPartitions(it => {
			val m = maxB.value
			it.map { case ((user, item), count) => (user, item, 5.0 * count / max(user)) }
		})
	}

	def getViewData() = {
		val jdbcDFView = sqlContext.read.format("jdbc").options(Map(
			"url" -> config.dbConnections("view").getUrl(),
			"driver" -> config.dbConnections("view").clazz,
			"dbtable" -> s"(${config.recConfig.queryView}) as tmp_xyz"))
		val dataView = jdbcDFView.load()
			.map(row => row.toSeq match {
				case Seq(userId: Int, visistorId: String, productId: Int) => (userId, visistorId.toLong, productId)
				case _ => null
			})
			.filter(_ != null)
		val visitorId2UserId = dataView.filter { case (u, v, p) => u > 0 }
			.map { case (u, v, p) => (v, u) }
			.collectAsMap
			.toMap
		val visitorId2UserIdBC = sc.broadcast(visitorId2UserId)
		val userProductView = dataView.mapPartitions(it => {
			val v2u = visitorId2UserIdBC.value
			it.map {
				case (u, v, p) => {
					if (u > 0) (u.toLong, p)
					else {
						if (v2u.contains(v)) (v2u(v).toLong, p)
						else (v, p)
					}
				}
			}
		}).map { case (u, i) => ((u, i), 1) }
			.combineByKey((once: Int) => once, (sum: Int, once: Int) => sum + once, (sum1: Int, sum2: Int) => sum1 + sum2, partitioner)
		//		val dataViewUserL2 = userProductView.map { case ((user, item), numView) => (user, numView) }
		//			.combineByKey(n => n * n, (sqr: Int, n) => sqr + n * n, (sqr1: Int, sqr2: Int) => sqr1 + sqr2)
		//			.mapValues(sqr => math.sqrt(sqr.toDouble))
		//			.collectAsMap()
		//			.toMap
		//		val dataViewUserL2Bc = sc.broadcast(dataViewUserL2)
		//		userProductView.mapPartitions(it => {
		//			val l2 = dataViewUserL2Bc.value
		//			it.map { case ((user, item), numView) => ((user, item), 5 * numView / l2(user)) }
		//		})
		userProductView
	}

	def getOrderData() = {
		val jdbcDFOms = sqlContext.read.format("jdbc").options(Map(
			"url" -> config.dbConnections("order").getUrl(),
			"driver" -> config.dbConnections("order").clazz,
			"dbtable" -> s"(${config.recConfig.queryOms}) as tmp_xyz"))
		jdbcDFOms.load()
			.map(row => row.toSeq match {
				case Seq(userId: String, orderId: Int, productId: Int, numProduct: Int) => (getLong(userId), productId, orderId)
				case _ => null
			})
			.filter(_ != null)
			.map { case (user, item, order) => ((user, item), 1) }
			.combineByKey((once: Int) => once, (sum: Int, once: Int) => sum + once, (sum1: Int, sum2: Int) => sum1 + sum2, partitioner)
		//			.mapValues(numOrder => if (numOrder > 1) 5.0 else 4.0)
	}

	def columnSimilarities(matrix: RDD[(Long, Int, Double)], threshold: Double): RDD[((Int, Int), Float)] = {
		require(threshold >= 0, s"Threshold cannot be negative: $threshold")
		if (threshold > 1) {
			logWarning(s"Threshold is greater than 1: $threshold " +
				"Computation will be more efficient with promoted sparsity, " +
				" however there is no correctness guarantee.")
		}

		val colMags = matrix.map { case (i, j, v) => (j, v) }
			.combineByKey(v => v * v, (sqr: Double, v: Double) => sqr + v * v, (sqr1: Double, sqr2: Double) => sqr1 + sqr2)
			.mapValues(math.sqrt)
			.collectAsMap()
			.toMap
		val gamma = if (threshold < 1e-6) {
			Double.PositiveInfinity
		} else {
			10 * math.log(colMags.size) / threshold
		}
		val rows = matrix.map { case (i, j, v) => (i, (j, v)) }
			.groupByKey()
			.values
			.map(_.toArray.unzip)
			.map { case (indices, values) => RVector(indices.toArray, values.toArray) }

		columnSimilaritiesDIMSUM(rows, colMags, gamma)
	}

	private def columnSimilaritiesDIMSUM(rows: RDD[RVector], colMags: Map[Int, Double], gamma: Double, minMatch: Int = 3): RDD[((Int, Int), Float)] = {
		require(gamma > 1.0, s"Oversampling should be greater than 1: $gamma")
		val sg = math.sqrt(gamma)
		val colMagsCorrected = colMags.map { case (index, x) => (index, if (x == 0) 1.0 else x) }
		val sc = rows.context
		val pBV = sc.broadcast(colMagsCorrected.map { case (index, value) => (index, sg / value) })
		val qBV = sc.broadcast(colMagsCorrected.map { case (index, value) => (index, math.min(sg, value)) })

		rows.mapPartitionsWithIndex { (indx, iter) =>
			val p = pBV.value
			val q = qBV.value

			val rand = new XORShiftRandom(indx)
			val scaled = new Array[Double](p.size)
			iter.flatMap {
				case RVector(indices, values) => {
					val nnz = indices.size
					var k = 0
					while (k < nnz) {
						scaled(k) = values(k) / q(indices(k))
						k += 1
					}

					Iterator.tabulate(nnz) { k =>
						val buf = new ListBuffer[((Int, Int), (Float, Byte))]()
						val i = indices(k)
						val iVal = scaled(k)
						if (iVal != 0 && rand.nextDouble() < p(i)) {
							var l = k + 1
							while (l < nnz) {
								val j = indices(l)
								val jVal = scaled(l)
								if (jVal != 0 && rand.nextDouble() < p(j)) {
									buf += (((i, j), ((iVal * jVal).toFloat, 1)))
								}
								l += 1
							}
						}
						buf
					}.flatten
				}
			}
		}.reduceByKey {
			case ((sim1, count1), (sim2, count2)) => {
				val sumCount = count1 + count2
				val sumSim = sim1 + sim2
				if (sumCount > 100) (sumSim, 100) else (sumSim, sumCount.toByte)
			}
		}
			.filter { case ((i1, i2), (sim, count)) => count > minMatch }
			.mapValues { case (sim, count) => sim }
	}

	private case class RVector(indices: Array[Int], values: Array[Double]) {
		require(indices.length == values.length, "indices size is not equal values size")
	}
}