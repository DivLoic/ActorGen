package org.lmd.xke.tweets

import java.sql.Timestamp

import breeze.stats.distributions.Gaussian
import com.fasterxml.jackson.databind.JsonNode
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.Logger
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.connect.json.JsonSerializer
import org.joda.time.DateTime
import org.json4s.jackson.JsonMethods._
import org.json4s.{DefaultFormats, Extraction}

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

case class KeyStats(timestamp: Long)
case class TrumpStats(datetime: java.sql.Timestamp, positve: Double, negative: Double)

/**
  * Created by loicmdivad on 22/05/2017.
  */
object TrumpStats extends App {

  implicit val formats = DefaultFormats

  val logger: Logger = Logger(this.getClass)

  val conf: Config = ConfigFactory.load("tweets/producer.conf")

  val topic: String =  conf.getString("kafka.topic")

  val ratio = Gaussian(mu = 30, sigma = 5.8)

  val producer =  new KafkaProducer[JsonNode, JsonNode](Map[String, String](
    "bootstrap.servers" -> conf.getString("kafka.host"),
    "key.serializer" -> classOf[JsonSerializer].getName,
    "value.serializer" -> classOf[JsonSerializer].getName,
    "schema.registry.url" -> conf.getString("kafka.schema-registry")
  ))

  def truncate(d: Double): Double = math.floor( d * 100 ) / 100d

  def produice(): Unit = {
    val pos: Double = ratio.get()
    val neg: Double = 100 - pos
    val date: DateTime = DateTime.now()
    val time: Timestamp = new Timestamp(date.getMillis)

    producer.send(new ProducerRecord[JsonNode, JsonNode](
      topic,
      //date.toString("yyyy-MM-dd HH:mm:ss.SSS"),
      asJsonNode(Extraction.decompose(KeyStats(date.getMillis))),
      asJsonNode(Extraction.decompose(TrumpStats(time, truncate(pos), truncate(neg))))
    ))
  }


  val system = akka.actor.ActorSystem("system")

  system.scheduler.schedule(0 seconds, 5 seconds)(produice)


}


