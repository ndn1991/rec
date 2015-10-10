package com.vinecom.rec.app.vo.config

import com.vinecom.common.data.CommonObject
import scala.collection.JavaConversions._

/**
 * @author noind
 */
case class ItemBaseConfig(dbConnections: Map[String, DBConnection], recConfig: ItemBaseRecConfig, storage: Storage)

case class DBConnection(ip: String, port: String, db: String, user: String, password: String, clazz: String) {
	def getUrl() = {
		clazz match {
			case "com.microsoft.sqlserver.jdbc.SQLServerDriver" => s"jdbc:sqlserver://$ip:$port;databaseName=$db;user=$user;password=$password;"
			case "com.mysql.jdbc.Driver" => s"jdbc:mysql://$ip:$port/$db?user=$user&password=$password"
			case _ => throw new RuntimeException("Hien tai chi support mssql + mysql thoi anh")
		}
	}
}

case class ItemBaseRecConfig(numNeigh: Int, threshold: Double, queryOms: String, queryView: String)

case class Storage(solrPath: String, bufferSize: Int, numThread: Int, isCommit: Boolean, numDocPerReq: Int) {
	def toCommonObject() = {
		new CommonObject(mapAsJavaMap(Map("solrPath" -> solrPath,
			"bufferSize" -> bufferSize.asInstanceOf[Integer],
			"numThread" -> numThread.asInstanceOf[Integer],
			"isCommit" -> isCommit.asInstanceOf[java.lang.Boolean],
			"numDocPerReq" -> numDocPerReq.asInstanceOf[Integer])).asInstanceOf[java.util.Map[java.lang.String, java.lang.Object]])
	}
}

case class TempWrite(view: String, order: String)
case class TempRead(view: String, order: String)
case class Temp(write: TempWrite, read: TempRead, result: String)