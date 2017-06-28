package org.lmd.xke.kmean

import akka.actor.ActorDSL._
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import breeze.stats.distributions.Gaussian
import com.fasterxml.jackson.databind.JsonNode
import com.sksamuel.avro4s.RecordFormat
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.Logger
import org.apache.kafka.clients.producer.{Callback, KafkaProducer, ProducerRecord, RecordMetadata}
import org.apache.kafka.connect.json.JsonSerializer
import org.json4s.{DefaultFormats, Extraction, JValue}
import org.json4s.jackson.JsonMethods._
import org.scalacheck.Gen

import scala.concurrent.ExecutionContext.Implicits.global

import scala.collection.JavaConversions._
import scala.concurrent.duration._

/**
  * Created by loicmdivad on 26/04/2017.
  */
case class Event(id: String, x: Double, y: Double)

class Kactor(x: String,
             y: String,
             label: String,
             config: Config) extends Actor  {

  lazy val logger: Logger = Logger(this.getClass)

  lazy val format: RecordFormat[Event] = RecordFormat[Event]

  val xGauss = Gaussian(mu = x.toDouble, sigma = 1.5)
  val yGauss = Gaussian(mu = y.toDouble, sigma = 1.5)

  val producer =  new KafkaProducer[String, JsonNode](Map[String, String](
    "bootstrap.servers" -> config.getString("kafka.host"),
    "key.serializer" -> classOf[JsonSerializer].getName,
    "value.serializer" -> classOf[JsonSerializer].getName,
    "schema.registry.url" -> config.getString("kafka.schema-registry")
  ))

  def produce(): Unit = {

    implicit val formats = DefaultFormats

    val value: JValue = Extraction.decompose(Event(label, xGauss.get(), yGauss.get()))

    val message: ProducerRecord[String, JsonNode] =
      new ProducerRecord(config.getString("kafka.topic"), asJsonNode(value))

    producer.send(message, new Callback(){
      override def onCompletion(metadatap: RecordMetadata, e: Exception): Unit = e match {
        case null =>
        case err: Exception => logger error err.getMessage
      }
    })
  }

  override def receive: Receive = {
    case _ =>
      val dt = Gen.choose(1.0, 4.0).sample.get
      context.system.scheduler.scheduleOnce(dt seconds, self, produce())
  }
}

object Kmeans extends App {

  val logger: Logger = Logger("main")

  val conf: Config = ConfigFactory.load("kmean/kmean.conf")

  implicit val system = ActorSystem()

  def exec(x: String, y: String, label: String, config: Config): ActorRef =
    system.actorOf(Props.create(classOf[Kactor], x, y, label, config))

  val master = actor(new Act{

    var executors: Map[String, ActorRef] = Map[String, ActorRef]()

    whenStarting {

      executors += "K0" -> exec("4.5", "12.5", "K0", conf)
      executors += "K1" -> exec("1.5", "3.0", "K1", conf)
      executors += "K2" -> exec("11.0", "7.5", "K2", conf)

    }

    become {
      case name: String => executors(name) ! "Go!"
      case (id: String, date: String) => println(s"worker $id: lap n° $date")
      case msg => logger error s"Master Actor get a Illegal Message type: ${msg.getClass}"
    }

  })

  logger info "Starting the 4 executors"

  master ! "K0"
  master ! "K1"
  master ! "K2"

}