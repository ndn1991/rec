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
import com.vinecom.rec.app.ultil.Utils.getLong
import com.vinecom.rec.app.ultil.Utils.keyOnFirstItem
import com.google.common.base.Joiner
import org.apache.spark.mllib.linalg.distributed.MatrixEntry
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.linalg.distributed.RowMatrix
import com.vinecom.rec.app.config.Config
import com.vinecom.rec.app.algorithm.Item2ItemRecommend

/**
 * @author noind
 */
object GetRecommendFromFileV2 {

	def main(args: Array[String]): Unit = {
		val guide = """
		  Day la chuong trinh lay du lieu tu rate va tien hanh ghi ket qua ra file
		  Usage: recommend-from-file-v2.sh <source_file> <dest_file
		  Trong do:
		  		source_file: file rates nguon
		  		dest_file: file luu tru ket qua  
		  """

		val sc = new SparkContext()
		val config = Config.readConfig()
		val data = sc.textFile(args(0), 128)
			.map(_.split("\\t") match { case Array(u, i, r) => (u.toLong, i.toInt, r.toDouble) })
		Item2ItemRecommend.init(config, sc)
		Item2ItemRecommend.getRec(data)
			.mapValues(_.sorted.map { case (item, neigh) => item + "," + neigh }.mkString(";"))
			.map { case (item, neighs) => item + "#" + neighs }
			.coalesce(1)
			.saveAsTextFile(args(1))
	}
}