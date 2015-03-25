package com.ndn

import com.datastax.spark.connector.{toSparkContextFunctions, SomeColumns}
import com.datastax.bdp.spark.DseSparkConfHelper._
import com.ndn.tools.TopK
import com.ndn.tools.Utils._
import com.typesafe.config.ConfigFactory
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{Path, FileSystem}
import org.apache.log4j.Logger
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.SparkContext._
import org.apache.spark.mllib.recommendation.{MatrixFactorizationModel, ALS, Rating}
import scala.collection.JavaConverters._

/**
 * Created by ndn on 3/7/2015.
 */
object ALSPro {
  def main(args: Array[String]) {
    val start = System.currentTimeMillis
    execute()
    val finish = System.currentTimeMillis
    println("Total time: " + (finish - start))
  }

  /**
   * Tính toán các sản phẩm được recommend cho từng user
   * @return
   */
  def getRecommend(rates: RDD[(Int, Int, Double)], rank: Int, numIterations: Int, lambda: Double, alpha: Double, k: Int) = {
    val sc = rates.sparkContext

    val ratings = rates.map { case (user, item, rate) => Rating(user, item, rate)}
    val model = if (alpha > 0.0)
      ALS.trainImplicit(ratings, rank, numIterations, lambda, alpha)
    else
      ALS.train(ratings, rank, numIterations, lambda)

    val modelB = sc.broadcast(model)
    val itemsB = sc.broadcast(rates.map(_._2).distinct().collect())

    rates.map(v => (v._1, v._2))
      .groupByKey()
      .mapValues(_.toList)
      .map(findKRecommendedItems(_, modelB.value, itemsB.value, k))
  }

  /**
   * Tìm k item được recommend cho một user
   * @param userItems user + list item mà user đã tương tác, danh sách này dùng để tránh việc gợi ý các sản phẩm mà user đó đã mua
   * @param model model của phương pháp ALS
   * @param items tập tất cả các item có trong hệ thống
   * @param k số sản phẩm recommend
   * @return k item được recommend cho user đó
   */
  private def findKRecommendedItems(userItems: (Int, List[Int]), model: MatrixFactorizationModel, items: Array[Int], k: Int) = {
    val topK = new TopK[Long](k)
    val ratedItems = userItems._2.toSet
    val user = userItems._1
    for (item <- items; if !ratedItems.contains(item)) {
      val p = model.predict(user, item)
      topK.put(item, p)
    }
    (user.toLong, topK.real)
  }

  val conf = ConfigFactory.load()
  val keySpace = conf.getString("rec.cql.keySpace")
  val table = conf.getString("rec.cql.table.orders")
  val rank = conf.getInt("rec.cql.als.rank")
  val numIterations = conf.getInt("rec.cql.als.numIterations")
  val lambda = conf.getDouble("rec.cql.als.lambda")
  val alpha = conf.getDouble("rec.cql.als.alpha")
  val numRec = conf.getInt("rec.cql.numRec.als")
  val resultTable = conf.getString("rec.cql.table.als")
  val cqlCreates = conf.getStringList("rec.cql.create.table.als").asScala.toList
  val logger = Logger.getLogger(this.getClass)

  def execute(): Unit = {
    logger.error(s"rumRec: $numRec, keySpace: $keySpace, table: $table, rank: $rank, numIterations: $numIterations, lambda: $lambda, alpha: $alpha, numRec: $numRec, resultTable: $resultTable")
    logger.error(s"cqlCreates: $cqlCreates")
    val sc = new SparkContext(new SparkConf().forDse)
    val rows = sc.cassandraTable(keySpace, table)
      .map(v => ((getInt(v.get[String]("user_id")), v.get[String]("product_id").toInt), v.get[String]("num_product").toDouble))
      .reduceByKey(_ + _)
    .map{ case ((user, item), product) => (user, item, product)}
    .map(v => v._1 + "," + v._2 + "," + v._3)
    val fs = FileSystem.get(new Configuration())
    fs.delete(new Path("/root/data/tmp/rec"), true)
    fs.close()
    rows.saveAsTextFile("/root/data/tmp/rec")
    val _rows = sc.textFile("/root/data/tmp/rec")
      .map(_.split(",") match {case Array(user, item, rate) => (user.toInt, item.toInt, rate.toDouble)})

    val recs = getRecommend(_rows, rank, numIterations, lambda, alpha, numRec)
    saveToCSD(recs, keySpace, resultTable, cqlCreates, SomeColumns("user", "items", "scores"))
  }
}
