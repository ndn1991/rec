import AssemblyKeys._  // put this at the top of the file

assemblySettings

name := "REC"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies += "org.apache.spark" % "spark-core_2.10" % "1.1.0" % "provided"

libraryDependencies += "com.datastax.spark" % "spark-cassandra-connector_2.10" % "1.1.0" % "provided"

libraryDependencies += "org.apache.spark" % "spark-mllib_2.10" % "1.1.0" % "provided"

libraryDependencies += "log4j" % "log4j" % "1.2.17" % "provided"

libraryDependencies += "org.quartz-scheduler" % "quartz" % "2.2.1" % "provided"

resolvers += Resolver.mavenLocal

libraryDependencies += "com.datastax" % "bdp" % "4.6.0" % "provided"