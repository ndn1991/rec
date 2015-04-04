package com.ndn

import com.datastax.spark.connector.{SomeColumns, toSparkContextFunctions}
import com.ndn.spark.mlib.recommendation.{ALS1, Rating1}
import com.ndn.tools.TopK
import com.ndn.tools.Utils._
import com.typesafe.config.ConfigFactory
import org.apache.log4j.Logger
import org.apache.spark.rdd.RDD
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import scala.collection.JavaConverters._
import scala.collection.mutable
import com.github.fommil.netlib.BLAS.{getInstance => blas}

/**
 * Created by ndn on 3/7/2015.
 */
object ALSPro {
  def main(args: Array[String]) {
    val start = System.currentTimeMillis
    val sc = new SparkContext()
    execute(sc)
    val finish = System.currentTimeMillis
    println("Total time: " + (finish - start))
  }

  /**
   * Tính toán các sản phẩm được recommend cho từng user
   * @return
   */
  def getRecommend(rates: RDD[Rating1], rank: Int, numIterations: Int, lambda: Double, alpha: Double, k: Int) = {
    val model = if (alpha > 0.0)
      ALS1.trainImplicit(rates, rank, numIterations, lambda, 32, alpha)
    else
      ALS1.train(rates, rank, numIterations, 32)

    val itemBlocks = model.productFeatures.mapPartitionsWithIndex { case (idx, iter) =>
      Iterator.single((1, iter.toArray))
    }

    val userBlocks = rates.map(v => (v.user, v.product))
      .combineByKey(
        (v: Long) => mutable.ArrayBuilder.make[Long].+=(v),
        (set: mutable.ArrayBuilder[Long], v: Long) => set.+=(v),
        (set1: mutable.ArrayBuilder[Long], set2: mutable.ArrayBuilder[Long]) => set1.++=(set2.result())
      )
      .mapValues(_.result().toSet)
      .join(model.userFeatures)
      .mapPartitionsWithIndex { case (idx, iter) => Iterator.single((1, (idx, iter.toArray)))}

    userBlocks.join(itemBlocks)
      .values
      .map(v => (v._1._1, (v._1._2, v._2)))
      .mapValues { case (userBlock, itemBlock) =>
        userBlock.map { case (user, (ratedItems, userFactor)) =>
          val topK = new TopK[Long](k)
          itemBlock.filterNot(v => ratedItems.contains(v._1))
            .foreach { case (item, itemFactor) =>
            val p = blas.ddot(itemFactor.length, itemFactor, 1, userFactor, 1)
            topK.put(item, p)
          }
          (user, topK)
        }
      }
      .combineByKey(
        (v: Array[(Long, TopK[Long])]) => v,
        (_v: Array[(Long, TopK[Long])], v: Array[(Long, TopK[Long])]) => {_v.zip(v.map(_._2)).map{case ((user, topK1), topK2) => (user, topK1.put(topK2))}},
        (_v1: Array[(Long, TopK[Long])], _v2: Array[(Long, TopK[Long])]) => {_v1.zip(_v2.map(_._2)).map{case ((user, topK1), topK2) => (user, topK1.put(topK2))}}
      )
      .flatMap(_._2)
      .mapValues(_.real)
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

  def execute(sc: SparkContext): Unit = {
    logger.error(s"rumRec: $numRec, keySpace: $keySpace, table: $table, rank: $rank, numIterations: $numIterations, lambda: $lambda, alpha: $alpha, numRec: $numRec, resultTable: $resultTable")
    logger.error(s"cqlCreates: $cqlCreates")
    val rows = sc.cassandraTable(keySpace, table)
      .map(v => ((getLong(v.get[String]("user_id")), v.get[String]("product_id").toLong), v.get[String]("num_product").toFloat))
      .reduceByKey(_ + _)
      .map { case ((user, item), rate) => Rating1(user, item, rate)}

    val recs = getRecommend(rows, rank, numIterations, lambda, alpha, numRec)
    saveToCSD(recs, keySpace, resultTable, cqlCreates, SomeColumns("user", "items", "scores"))
  }
}
