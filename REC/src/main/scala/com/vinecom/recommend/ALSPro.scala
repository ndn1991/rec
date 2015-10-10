package com.vinecom.recommend

import com.github.fommil.netlib.BLAS.{getInstance => blas}
import com.typesafe.config.ConfigFactory
import com.vinecom.common.Loggable
import com.vinecom.recommend.tools.topK.{TopK, ArrayTopK}
import org.apache.spark.mllib.recommendation.{ALS, MatrixFactorizationModel, Rating}
import org.apache.spark.rdd.RDD

import scala.collection.mutable

/**
 * Created by ndn on 3/7/2015.
 */
class ALSPro extends Loggable {
  val conf = ConfigFactory.load()
  var model: MatrixFactorizationModel = null

  /**
   * Tính toán các sản phẩm được recommend cho từng user
   * @return
   */
  def getRecommend(rates: RDD[Rating], k: Int) = {
    if (this.model != null) {

      val itemBlocks = model.productFeatures.mapPartitionsWithIndex { case (idx, iter) =>
        Iterator.single((1, iter.toArray))
      }

      val userBlocks = rates.map(v => (v.user, v.product))
        .combineByKey(
          (v: Int) => mutable.ArrayBuilder.make[Int].+=(v),
          (set: mutable.ArrayBuilder[Int], v: Int) => set.+=(v),
          (set1: mutable.ArrayBuilder[Int], set2: mutable.ArrayBuilder[Int]) => set1.++=(set2.result())
        )
        .mapValues(_.result().toSet)
        .join(model.userFeatures)
        .mapPartitionsWithIndex { case (idx, iter) => Iterator.single((1, (idx, iter.toArray)))}

      userBlocks.join(itemBlocks)
        .values
        .map(v => (v._1._1, (v._1._2, v._2)))
        .mapValues { case (userBlock, itemBlock) =>
        userBlock.map { case (user, (ratedItems, userFactor)) =>
          val topK: TopK[Int] = new ArrayTopK[Int](k)
          itemBlock.filterNot(v => ratedItems.contains(v._1))
            .foreach { case (item, itemFactor) => {
            val p = blas.ddot(itemFactor.length, itemFactor, 1, userFactor, 1)
            topK.put(item, p)
          }
          }
          (user, topK)
        }
      }
        .combineByKey(
          (v: Array[(Int, TopK[Int])]) => v,
          (_v: Array[(Int, TopK[Int])], v: Array[(Int, TopK[Int])]) => {
            _v.zip(v.map(_._2)).map { case ((user, topK1), topK2) => (user, topK1.put(topK2))}
          },
          (_v1: Array[(Int, TopK[Int])], _v2: Array[(Int, TopK[Int])]) => {
            _v1.zip(_v2.map(_._2)).map { case ((user, topK1), topK2) => (user, topK1.put(topK2))}
          }
        )
        .flatMap(_._2)
        .mapValues(_.real)
    }
  }

  def createModel(rates: RDD[Rating], rank: Int, numIterations: Int, lambda: Double, alpha: Double) {
    this.model = if (alpha > 0.0) {
      ALS.trainImplicit(rates, rank, numIterations, lambda, 32, alpha)
    } else {
      ALS.train(rates, rank, numIterations, 32)
    }
  }
}
