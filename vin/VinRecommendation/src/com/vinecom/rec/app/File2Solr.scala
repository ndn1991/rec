package com.vinecom.file2solr

import scala.io.Source
import com.vinecom.common.data.CommonObject
import com.vinecom.common.data.CommonArray
import scala.collection.JavaConverters._
import com.vinecom.utils.Initializer
import com.vinecom.utils.FileSystemUtils
import com.typesafe.config.ConfigFactory
import com.vinecom.rec.app.vo.config.DBConnection
import com.vinecom.rec.app.vo.config.ItemBaseRecConfig
import com.vinecom.rec.app.vo.config.Storage
import com.vinecom.rec.app.vo.config.ItemBaseConfig
import java.io.File
import com.vinecom.rec.app.storage.RecommendSolrStorage
import com.vinecom.rec.app.config.Config

/**
 * @author noind
 */
object File2Solr {
	def main(args: Array[String]): Unit = {
		val guide = """
		  Chuong trinh nay doc du lieu ket qua tu file vao solr
			Usage: read-file-2-solr.sh <source_path>
			Trong do:
					source_path: dia chi file ket qua
			De chinh dia chi cua solr thi sua "storage" trong file application.conf
		"""
		println(guide)
		val config = Config.readConfig()
		val storager = new RecommendSolrStorage(config.storage)
		Source.fromFile(args(0))
			.getLines()
			.map(line => {
				val parts = line.split("#", 2);
				val oriItem = parts(0)
				val neighs = parts(1).split(";").map(s => s.split(",", 2) match { case Array(item, sim) => (item, sim) })

				new CommonObject("product_id", oriItem,
					"recs", new CommonArray(neighs.map(w => w._1 + "_" + w._2).map(w => w.asInstanceOf[Object]).toList.asJava), "type", "cosin_b_v")
			})
			.map(storager.save)
	}
}