name := "API"

version := "1.0"

lazy val `api` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.10.5"

libraryDependencies ++= Seq( jdbc , anorm , cache , ws )

libraryDependencies += "com.datastax.cassandra" % "cassandra-driver-core" % "2.1.5"

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  