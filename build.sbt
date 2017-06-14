name := "ActorGen"

version := "0.1"

scalaVersion := "2.11.11"

resolvers ++= Seq (
  Opts.resolver.mavenLocalFile,
  Resolver.sonatypeRepo("releases"),
  "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"
)

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.1",
  "ch.qos.logback" %  "logback-classic" % "1.2.1" force(),
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",

  "com.github.melrief" %% "purecsv" % "0.0.9",
  "joda-time" % "joda-time" % "2.9.7" exclude("org.slf4j", "slf4j-log4j12"),

  "org.scalanlp" %% "breeze" % "0.13",
  "org.scalacheck" %% "scalacheck" % "1.13.4",

  "com.typesafe.akka" %% "akka-actor" % "2.4.17",

  "com.typesafe.akka" %% "akka-stream-kafka" % "0.15",

  "org.json4s" %% "json4s-jackson" % "3.5.1",
  "com.sksamuel.avro4s" %% "avro4s-core" % "1.6.4",
  "org.apache.kafka" %% "kafka" % "0.10.2.0"
    exclude("org.slf4j", "slf4j-log4j12"),
  "org.apache.kafka" % "connect-json" % "0.10.2.0",
  //exclude("org.apache", "avro") exclude("org.slf4j", "slf4j-log4j12"),
  "io.confluent" % "kafka-avro-serializer" % "3.1.1"
    exclude("org.apache", "avro") exclude("org.slf4j", "slf4j-log4j12")

)


assemblyJarName in assembly := "actorgen-assembly.jar"