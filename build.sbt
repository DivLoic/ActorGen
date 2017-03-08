name := "ActorGen"

version := "1.0"

scalaVersion := "2.11.8"

resolvers ++= Seq (
  Opts.resolver.mavenLocalFile,
  "Confluent" at "http://packages.confluent.io/maven"
)

libraryDependencies ++= Seq(
  "joda-time" % "joda-time" % "2.9.7"
    exclude("org.slf4j", "slf4j-log4j12"),
  "com.typesafe" % "config" % "1.3.1",
  "org.scalacheck" %% "scalacheck" % "1.13.4",
  "ch.qos.logback" %  "logback-classic" % "1.2.1" force(),
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",

  "org.apache.avro" % "avro" % "1.8.1"
    exclude("org.slf4j", "slf4j-log4j12"),
  "io.confluent" % "kafka-avro-serializer" % "3.1.1"
    exclude("org.slf4j", "slf4j-log4j12"),
  "org.apache.kafka" %% "kafka" % "0.10.1.0-cp2"
    exclude("org.slf4j", "slf4j-log4j12"),

  "com.typesafe.akka" %% "akka-actor" % "2.4.17"
)