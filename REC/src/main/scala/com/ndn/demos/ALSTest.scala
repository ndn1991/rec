package com.ndn.demos

import com.ndn.spark.mlib.recommendation.{ALS1, Rating1}
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._

/**
 * Created by ndn on 3/30/2015.
 */
object ALSTest {
  def main(args: Array[String]) {
    val sc = new SparkContext()
    var start = System.currentTimeMillis()

    val data = sc.textFile(args(4), 42)
    val ratings = data.map(_.split(args(6)) match { case Array(user, item, rate, time) =>
      Rating1(user.toInt, item.toInt, rate.toDouble)
    })

//    val (ratings, testData) = NetFlixProccess.getTrainAndTest(0.8, NetFlixProccess.get(sc))
    // Build the recommendation model using ALS
    val rank = args(0).toInt
    val numIterations = args(1).toInt
    val lambda = args(2).toDouble
    val alpha = args(3).toDouble
    val model = if (alpha > 0.0) {
      ALS1.trainImplicit(ratings, rank, numIterations, lambda, alpha)
    }
    else {
      ALS1.train(ratings, rank, numIterations, lambda)
    }
    println(s"time to learning: ${System.currentTimeMillis() - start}")
    start = System.currentTimeMillis()

    val testData = sc.textFile(args(5), 42)
      .map(_.split(args(6)) match {case Array(user, item, rate, time) => Rating1(user.toLong, item.toLong, rate.toFloat)})
    // Evaluate the model on rating data
    val usersProducts = testData.map { case Rating1(user, product, rate) =>
      (user, product)
    }
    val predictions =
      model.predict(usersProducts).map { case Rating1(user, product, rate) =>
        ((user, product), rate)
      }
    val ratesAndPreds = testData.map { case Rating1(user, product, rate) =>
      ((user, product), rate)
    }.join(predictions)
    val MSE = ratesAndPreds.map { case ((user, product), (r1, r2)) =>
      val err = r1 - r2
      err * err
    }.mean()
    println("Mean Squared Error = " + MSE)

  }
}
