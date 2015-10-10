package com.vinecom.recommend.tools

import java.math.BigInteger
import com.vinecom.recommend.tools.random.XORShiftRandom
import com.vinecom.recommend.tools.topK.ArrayTopK
import org.apache.spark.rdd.RDD
import scala.collection.mutable
import scala.util.Random

/**
 * Created by ndn on 3/7/2015.
 */
object Utils {
  val minThreshold = 1e-6
  val factorOfGamma = 10

  /**
   * Lấy mẫu ngẫu nhiên từ một tập hợp đưa vào
   * @param collect tập ban đầu
   * @param n số phần tử lấy ra
   * @tparam A kiểu dữ liệu thành phần
   * @return mẫu
   */
  def sample[A](collect: Iterable[A], n: Int = 500): List[A] = {
    val list = collect.toList
    if (n == -1 || list.size <= n)
      list
    else
      Random.shuffle(list).take(n)
  }

  /**
   * Lấy mẫu ngẫu nhiên từ một tập hợp đưa vào, mỗi phần từ được trừ đi giá trị trung bình của tập
   * @param collect tập ban đầu
   * @param n số phần tử lấy ra
   * @return mẫu
   */
  def sampleAvg(collect: Iterable[(Int, Double)], n: Int = 500): List[(Int, Double)] = {
    val x = collect.toList
    val avg = x.map(_._2).sum / x.size
    val list = x.map(v => (v._1, v._2 - avg))

    if (n == -1 || list.size <= n)
      list
    else
      Random.shuffle(list).take(n)
  }

  /**
   * Tính độ tương tự giữa các cột của ma trận thưa. Có sử dụng ngưỡng để tiết kiệm tài nguyên
   * @param rows tập các dòng, mỗi dòng là một mảng các (chỉ số, phần tử)
   * @param colLens tập độ dài cột
   * @param gamma ngưỡng sử dụng, xem ví dụ tại lớp com.ndn.ItemNeighs
   * @return ma trận đọ tương tự giữ các item, dạng RDD[((item, item), sim)]
   */
  def columnSimilarities(rows: RDD[Array[(Long, Double)]], colLens: Map[Long, Double], numPar: Int = 8, gamma: Double): RDD[((Long, Long), Double)] = {
    println(s"columnSimilarities with gamma($gamma)")
    require(gamma > 1.0, s"Oversampling should be greater than 1: $gamma")

    val sg = math.sqrt(gamma)
    val colLensCorrected = colLens.map(v => (v._1, if (v._2 == 0) 1 else v._2))
    val sc = rows.context
    val pBV = sc.broadcast(colLensCorrected.map(v => (v._1, sg / v._2)))
    val qBV = sc.broadcast(colLensCorrected.map(v => (v._1, math.min(sg, v._2))))

    val numCol = colLens.size
    rows.coalesce(numPar).cache().mapPartitionsWithIndex((index, it) => {
      val p = pBV.value
      val q = qBV.value

      val rand = new XORShiftRandom(index)
      val buf = mutable.HashMap[(Long, Long), Double]().withDefaultValue(0)
      val scaled = new Array[Double](numCol)
      it.foreach(row => {
        val nnz = row.size
        var k = 0
        while (k < nnz) {
          val row_k = row(k)
          scaled(k) = row_k._2 / q(row_k._1)
          k += 1
        }
        k = 0
        while (k < nnz) {
          val i = row(k)._1
          val iVal = scaled(k)
          if (iVal != 0 && rand.nextDouble() < p(i)) {
            var l = k + 1
            while (l < nnz) {
              val j = row(l)._1
              val jVal = scaled(l)
              if (jVal != 0 && rand.nextDouble() < p(j)) {
                buf(i, j) += iVal * jVal
              }
              l += 1
            }
          }
          k += 1
        }
      })
      buf.toIterator
    }).reduceByKey(_ + _)
  }

  /**
   * Tính độ tương tự giữa các cột của ma trận thưa
   * @param rows tập các dòng, mỗi dòng là một mảng các (chỉ số, phần tử)
   * @param colLens tập độ dài cột
   * @return ma trận đọ tương tự giữ các item, dạng RDD[((item, item), sim)]
   */
  def _columnSimilarities(rows: RDD[Array[(Long, Double)]], colLens: Map[Long, Double], numPar: Int = 8): RDD[((Long, Long), Double)] = {
    println(s"columnSimilarities without gamma")
    val sc = rows.context
    val qBV = sc.broadcast(colLens.map(v => (v._1, if (v._2 == 0) 1 else v._2)))

    val numCol = colLens.size
    rows.coalesce(numPar).cache().mapPartitions(it => {
      val q = qBV.value

      val buf = mutable.HashMap[(Long, Long), Double]().withDefaultValue(0)
      val scaled = new Array[Double](numCol)
      it.foreach(row => {
        val nnz = row.size
        var k = 0
        while (k < nnz) {
          val row_k = row(k)
          scaled(k) = row_k._2 / q(row_k._1)
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
                buf(i, j) += iVal * jVal
              }
              l += 1
            }
          }
          k += 1
        }
      })
      buf.toIterator
    }).reduceByKey(_ + _)
  }

  /**
   * Tính độ tương tự giữa các cột của ma trận bằng độ đo xác xuất
   */
  def colSimPr(rows: RDD[Array[(Long, Double)]], colLens: Map[Long, Int], alpha: Double, numPar: Int): RDD[((Long, Long), Double)] = {
    require(alpha >= 0 && alpha <= 1, "alpha must be greater than zero and less than 1")
    println(s"column similarity by Probabilistic without gamma")
    colLens.map(_._2).foreach(v => require(v > 0, "Length of a column must be greater than zero"))
    val sc = rows.context
    val qBV = sc.broadcast(colLens)
    val numCol = colLens.size
    rows.coalesce(numPar)
      .map(arr => (arr, arr.length))
      .map { case (arr, len) => arr.map { case (id, value) => (id, value / len)}}
      .mapPartitions(it => {
      val q = qBV.value //colLens
      val buf = mutable.HashMap[(Long, Long), Double]().withDefaultValue(0) //temp map save temp similarity between two item
      val scaled = new Array[Double](numCol) // temp array save r(u,j) / (freq(j)^alpha)
      it.foreach(row => {
        val nnz = row.length
        var k = 0
        while (k < nnz) {
          val row_k = row(k)
          scaled(k) = row_k._2 / math.pow(q(row_k._1), alpha)
          k += 1
        }
        k = 0
        while (k < nnz) {
          val i = row(k)._1
          val iVal = scaled(k)
          val iLen = q(i)
          if (iVal != 0) {
            var l = k + 1
            while (l < nnz) {
              val j = row(l)._1
              val jVal = scaled(l)
              val jLen = q(j)
              if (jVal != 0) {
                buf(i, j) += jVal / iLen
                buf(j, i) += iVal / jLen
              }
              l += 1
            }
          }
          k += 1
        }
      })
      buf.iterator
    }).reduceByKey(_ + _)
  }


  def keyOnFirstItem[A, C](x: ((A, A), C)) = List((x._1._1, (x._1._2, x._2)), (x._1._2, (x._1._1, x._2)))

  /**
   * Lấy ra top các sản phẩm tương tự với các item đã rate của một user. Được sử dụng từ mapValues
   * @param items_with_rates các item + rate của một user
   * @param item_neighs item + các hàng xóm của item đó
   * @param n số kết quả trả về
   * @return top n sản phẩm recommended
   */
  def topNRecommendationsForItem(items_with_rates: List[(Long, Double)], item_neighs: Map[Long, Map[Long, Double]], n: Int = 500) = {
    val items = items_with_rates.map(_._1).toSet
    val totals = new mutable.HashMap[Long, Double]().withDefaultValue(0)
    val sim_sums = new mutable.HashMap[Long, Double]().withDefaultValue(0)
    items_with_rates.foreach { case (item, rate) =>
      if (item_neighs.contains(item)) {
        val nearest_neighbors = item_neighs(item)
        nearest_neighbors.foreach { case (neigh, sim) =>
          if (!items.contains(neigh)) {
            totals(neigh) += sim * rate
            sim_sums(neigh) += sim
          }
        }
      }
    }

    val topK = new ArrayTopK[Long](n)
    for ((item, total) <- totals) {
      topK.put(item, total / sim_sums(item))
    }

    if (topK.size > 0)
      topK.real
    else
      null
  }

  /**
   * Tìm k hàng xóm gần nhất cho user. Được sử dụng từ map
   * @param nearestNeighbors các hàng xóm của user hiện tại
   * @param userRates các vector rate của các user
   * @param n số kết quả trả về
   * @return các item được reccommend
   */
  def topNRecommendationsForUser(nearestNeighbors: (Long, Map[Long, Double]), userRates: Map[Long, List[(Long, Double)]], n: Int = 500) = {
    val user = nearestNeighbors._1
    val thisUserRates = userRates(user).map(_._1).toSet
    val sum_rate = new mutable.HashMap[Long, Double]().withDefaultValue(0)
    val sum_sim = new mutable.HashMap[Long, Double]().withDefaultValue(0)
    nearestNeighbors._2.foreach { case (neigh, sim) =>
      val rates_of_neigh = userRates(neigh)
      rates_of_neigh.foreach { case (item, rate) =>
        if (!thisUserRates.contains(item)) {
          sum_rate(item) += sim * rate
          sum_sim(item) += sim
        }
      }
    }
    val topK = new ArrayTopK[Long](n)
    for ((item, sumRate) <- sum_rate) {
      topK.put(item, sumRate / sum_sim(item))
    }
    (user, topK.arr.sortBy(_._2)(Ordering[Double].reverse))
  }

  def sim(rows: RDD[Array[(Long, Double)]], itemL2: Map[Long, Double], threshold: Double, numPar: Int = 8) = {
    println(s"sim with threshold($threshold)")
    if (threshold < minThreshold) {
      _columnSimilarities(rows, itemL2, numPar)
    }
    else {
      val gamma = factorOfGamma * math.log(itemL2.size) / threshold
      columnSimilarities(rows, itemL2, numPar, gamma)
    }
  }

  def getLong(s: String): Long = {
    try {
      s.toLong
    }
    catch {
      case e: NumberFormatException =>
        val l = new BigInteger(s.replaceAll("-", ""), 16).longValue()
        if (l < 0) l
        else -l
    }
  }

  def getInt(s: String): Int = {
    try {
      s.toInt
    }
    catch {
      case e: NumberFormatException =>
        val l = new BigInteger(s.replaceAll("-", ""), 16).intValue()
        if (l < 0) l
        else -l
    }
  }
}
