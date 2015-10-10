package com.vinecom.rec.app

import java.util.ArrayList
import org.apache.spark.HashPartitioner
import org.apache.spark.rdd.RDD.rddToPairRDDFunctions
import com.typesafe.config.ConfigFactory
import com.vinecom.common.data.CommonArray
import com.vinecom.common.data.CommonObject
import scala.collection.JavaConverters._
import com.vinecom.utils.Initializer
import com.vinecom.utils.FileSystemUtils
import java.io.File
import java.lang.Boolean
import com.vinecom.rec.app.vo.config.DBConnection
import com.vinecom.rec.app.vo.config.ItemBaseRecConfig
import com.vinecom.rec.app.vo.config.Storage
import com.vinecom.rec.app.vo.config.ItemBaseConfig
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import com.vinecom.rec.app.config.Config
import com.vinecom.rec.app.storage.RecommendSolrStorage
import com.vinecom.rec.app.algorithm.Item2ItemRecommend
/**
 * @author ndn
 */
object Main {
	val config = Config.readConfig()
	def main(args: Array[String]): Unit = {
		val storager = new RecommendSolrStorage(config.storage)
		val sc = new SparkContext()
		Item2ItemRecommend.init(config, sc)
		val recResult = Item2ItemRecommend.getRec(Item2ItemRecommend.getVirtualRate())
		val storgedData = recResult.map(v => new CommonObject("product_id", v._1.toString(),
			"recs", new CommonArray(v._2.map(w => w._1 + "_" + w._2).map(w => w.asInstanceOf[Object]).toList.asJava), "type", "cosin"))
		storgedData.collect().foreach(storager.save)

		storager.close()
	}
}