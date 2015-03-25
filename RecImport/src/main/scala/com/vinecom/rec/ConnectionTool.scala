package com.vinecom.rec

import java.sql.{DriverManager, SQLException}

/**
 * Created by ndn on 3/19/2015.
 */
object ConnectionTool {
  /**
   * Get connection from sql server database
   * @param host ip or host name of sql server database
   * @param user user login
   * @param pass password login
   * @return connection
   */
  @throws(classOf[ClassNotFoundException])
  @throws(classOf[SQLException])
  def sqlServerConnection(host: String, user: String, pass: String) = {
    Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver")
    DriverManager.getConnection(s"jdbc:sqlserver://$host;user=$user;password=$pass;")
  }
}
