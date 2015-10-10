package com.vinecom.rec.app.config

import com.vinecom.utils.Initializer
import com.vinecom.utils.FileSystemUtils
import com.typesafe.config.ConfigFactory
import java.io.File
import com.vinecom.rec.app.vo.config.DBConnection
import com.vinecom.rec.app.vo.config.ItemBaseRecConfig
import com.vinecom.rec.app.vo.config.Storage
import com.vinecom.rec.app.vo.config.ItemBaseConfig
import com.vinecom.rec.app.vo.config.TempWrite
import com.vinecom.rec.app.vo.config.Temp
import com.vinecom.rec.app.vo.config.TempRead

/**
 * @author noind
 */
object Config {
	def readConfig() = {
		Initializer.bootstrap(this.getClass)
		val path = FileSystemUtils.createAbsolutePathFrom("conf", "application.conf")
		val cf = ConfigFactory.parseFile(new File(path))

		val orderDbConnection = new DBConnection(
			cf.getString("rec.dbConnection.order.ip"),
			cf.getString("rec.dbConnection.order.port"),
			cf.getString("rec.dbConnection.order.db"),
			cf.getString("rec.dbConnection.order.user"),
			cf.getString("rec.dbConnection.order.password"),
			cf.getString("rec.dbConnection.order.class"))

		val viewDbConnection = new DBConnection(
			cf.getString("rec.dbConnection.view.ip"),
			cf.getString("rec.dbConnection.view.port"),
			cf.getString("rec.dbConnection.view.db"),
			cf.getString("rec.dbConnection.view.user"),
			cf.getString("rec.dbConnection.view.password"),
			cf.getString("rec.dbConnection.view.class"))

		val recConfig = new ItemBaseRecConfig(
			cf.getInt("rec.recConfig.numNeigh"),
			cf.getDouble("rec.recConfig.threshold"),
			cf.getString("rec.recConfig.queryOms"),
			cf.getString("rec.recConfig.queryView"))

		val storage = new Storage(
			cf.getString("rec.storage.solrPath"),
			cf.getInt("rec.storage.bufferSize"),
			cf.getInt("rec.storage.numThread"),
			cf.getBoolean("rec.storage.isCommit"),
			cf.getInt("rec.storage.numDocPerReq"))

		new ItemBaseConfig(Map("order" -> orderDbConnection, "view" -> viewDbConnection), recConfig, storage)
	}
}