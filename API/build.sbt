name := "API"

version := "1.0"

lazy val `api` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.5"

libraryDependencies ++= Seq( jdbc , anorm , cache , ws )

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

resolvers += Resolver.mavenLocal

libraryDependencies += "com.microsoft" % "sqlserver-jdbc" % "1.0"

libraryDependencies += "com.datastax.cassandra" % "cassandra-driver-core" % "2.1.5"