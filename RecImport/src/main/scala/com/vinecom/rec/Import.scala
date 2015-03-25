package com.vinecom.rec

import java.util.{Calendar, Date}

import com.datastax.driver.core.Cluster
import com.datastax.driver.core.exceptions.QueryExecutionException
import com.typesafe.config.ConfigFactory
import org.apache.log4j.Logger
import org.quartz.impl.StdSchedulerFactory
import org.quartz.{JobBuilder, SimpleScheduleBuilder, TriggerBuilder}
import com.vinecom.rec.ConnectionTool._
import scala.collection.mutable

/**
 * Created by ndn on 3/19/2015.
 */
object Import {
  val logger = Logger.getLogger(this.getClass)

  val calendar = Calendar.getInstance()
  val conf = ConfigFactory.load()
  val host = conf.getString("rec.sql.host")
  val user = conf.getString("rec.sql.user")
  val pass = conf.getString("rec.sql.pass")
  val sql = conf.getString("rec.sql.query")
  val cqlInsertOrder = conf.getString("rec.cql.insert.order")
  val cqlInsertUpdateTime = conf.getString("rec.cql.insert.updateTime")
  val cqlSelectUpdateTime = conf.getString("rec.cql.select.updateTime")
  val csdHosts = conf.getString("rec.cql.hosts").split(",")
  val builder = Cluster.builder(); csdHosts.foreach(host => builder.addContactPoint(host))
  val cluster = builder.build()
  val session = cluster.connect()

  session.execute(conf.getString("rec.cql.create.keySpace"))
  session.execute(conf.getString("rec.cql.create.table.orders"))
  session.execute(conf.getString("rec.cql.create.table.params"))

  def execute(): Unit = {
    val start = System.currentTimeMillis()
    val date = getDate
    updateDate()
    _import(date)
    val finish = System.currentTimeMillis()
    logger.info(s"total time: ${finish - start}")
  }

  def getDate = {
    val row = session.execute(cqlSelectUpdateTime).one()
    if (row == null) {

      calendar.setTime(new Date(0))
      val date = "%d-%d-%d %d:%d:%d".format(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DATE), calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND))
      date
    }
    else {
      row.getString("value")
    }
  }

  def updateDate(): Unit = {
    calendar.setTime(new Date())
    val date = "%d-%d-%d %d:%d:%d".format(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DATE), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND))
    session.execute(cqlInsertUpdateTime, date)
  }

  def _import(date: String): Unit = {
    val conn = sqlServerConnection(host, user, pass)
    val stmt = conn.createStatement()
    val q = sql.format(date, date)
    val rs = stmt.executeQuery(q)

    val failRows = new mutable.Queue[OrderRow]()
    var count = 0
    while (rs.next()) {
      count += 1
      val r = OrderRow(rs.getString("user_id"), rs.getLong("order_id"), rs.getInt("product_id"), rs.getInt("num_product"))
      try {
        session.execute(cqlInsertOrder, r.user, r.orderId, r.product, r.numProduct)
      }
      catch {
        case e: QueryExecutionException => failRows.enqueue(r)
        case e: Exception => throw e
      }
    }
    conn.close()
    logger.info(s"total count: $count")

    logger.info(s"retry with ${failRows.size} rows")
    while (failRows.nonEmpty) {
      logger.debug("*")
      val r = failRows.dequeue()
      try {
        session.execute(cqlInsertOrder, r.user, r.orderId, r.product, r.numProduct)
      }
      catch {
        case e: QueryExecutionException => failRows.enqueue(r)
        case e: Exception => throw e
      }
    }
  }

  def closeConnection(): Unit = {
    logger.info("close connection")
    session.close()
    cluster.close()
  }

  case class OrderRow(user: String, orderId: java.lang.Long, product: java.lang.Integer, numProduct: java.lang.Integer)
}