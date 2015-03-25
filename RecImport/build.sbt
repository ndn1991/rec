import AssemblyKeys._  // put this at the top of the file

assemblySettings

name := "RecImport"

version := "1.0"

scalaVersion := "2.10.5"

resolvers += Resolver.mavenLocal

libraryDependencies += "log4j" % "log4j" % "1.2.17"

libraryDependencies += "org.quartz-scheduler" % "quartz" % "2.2.1"

libraryDependencies += "com.datastax.cassandra" % "cassandra-driver-core" % "2.1.5"

libraryDependencies += "com.typesafe" % "config" % "1.2.1"

libraryDependencies += "com.microsoft" % "sqlserver-jdbc" % "1.0"