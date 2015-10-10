package com.vinecom.rec.app

import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import com.vinecom.utils.Initializer
import com.vinecom.utils.FileSystemUtils
import java.io.File
import com.typesafe.config.ConfigFactory
import com.vinecom.rec.app.vo.config.DBConnection
import com.vinecom.rec.app.vo.config.ItemBaseRecConfig
import com.vinecom.rec.app.vo.config.Storage
import com.vinecom.rec.app.vo.config.ItemBaseConfig
import com.vinecom.rec.app.ultil.Utils.getLong
import com.vinecom.rec.app.config.Config
import com.vinecom.rec.app.algorithm.Item2ItemRecommend

/**
 * @author noind
 */
object Export2File {
	val config = Config.readConfig()
	val sc = new SparkContext()
	var sqlContext = new SQLContext(sc)

	def main(args: Array[String]): Unit = {
		val guild = """
		  Chuong trinh nay dung de xuat du lieu log tu db ra file
			Usage: get-file.sh <file_name>
			Trong do: 
					file_name: la noi luu tru file rate ao
			"""
		println(guild)
		Item2ItemRecommend.init(config, sc)
		Item2ItemRecommend.getVirtualRate()
			.map { case (user, item, rate) => user + "\t" + item + "\t" + rate }
			.coalesce(1)
			.saveAsTextFile(FileSystemUtils.createAbsolutePathFrom(args(0)))
	}
}