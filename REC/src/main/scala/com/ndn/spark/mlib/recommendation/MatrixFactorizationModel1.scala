package com.ndn.spark.mlib.recommendation

import com.github.fommil.netlib.BLAS.{getInstance => blas}
import org.apache.spark.Logging
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD
import org.apache.spark.storage.StorageLevel

class MatrixFactorizationModel1(
                                val rank: Int,
                                val userFeatures: RDD[(Long, Array[Double])],
                                val productFeatures: RDD[(Long, Array[Double])])
  extends Serializable with Logging {

  require(rank > 0)
  validateFeatures("User", userFeatures)
  validateFeatures("Product", productFeatures)

  private def validateFeatures(name: String, features: RDD[(Long, Array[Double])]): Unit = {
    require(features.first()._2.size == rank,
      s"$name feature dimension does not match the rank $rank.")
    if (features.partitioner.isEmpty) {
      logWarning(s"$name factor does not have a partitioner. "
        + "Prediction on individual records could be slow.")
    }
    if (features.getStorageLevel == StorageLevel.NONE) {
      logWarning(s"$name factor is not cached. Prediction could be slow.")
    }
  }

  def predict(usersProducts: RDD[(Long, Long)]): RDD[Rating1] = {
    val users = userFeatures.join(usersProducts).map{
      case (user, (uFeatures, product)) => (product, (user, uFeatures))
    }
    users.join(productFeatures).map {
      case (product, ((user, uFeatures), pFeatures)) =>
        Rating1(user, product, blas.ddot(uFeatures.length, uFeatures, 1, pFeatures, 1))
    }
  }
}