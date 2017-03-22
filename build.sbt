name := "ActorGen"

version := "0.1"

scalaVersion := "2.11.8"

resolvers ++= Seq (
  Opts.resolver.mavenLocalFile,
  "Confluent" at "http://packages.confluent.io/maven"
)

libraryDependencies ++= Seq(

  // config & loggin
  "com.typesafe" % "config" % "1.3.1",
  "org.scalacheck" %% "scalacheck" % "1.13.4",
  "ch.qos.logback" %  "logback-classic" % "1.2.1" force(),
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",


  "joda-time" % "joda-time" % "2.9.7" exclude("org.slf4j", "slf4j-log4j12"),

  // kafka
  "com.sksamuel.avro4s" % "avro4s-core_2.11" % "1.6.4",
  "org.apache.kafka" %% "kafka" % "0.10.1.0-cp2" exclude("org.slf4j", "slf4j-log4j12"),
  "io.confluent" % "kafka-avro-serializer" % "3.1.1"exclude("org.apache", "avro") exclude("org.slf4j", "slf4j-log4j12"),

  // akka
  "com.typesafe.akka" %% "akka-actor" % "2.4.17"
)

assemblyJarName in assembly := "actorgen-assembly.jar"