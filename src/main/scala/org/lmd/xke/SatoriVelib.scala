package org.lmd.xke

import akka.actor.ActorDSL._
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.Logger
import org.json4s.{DefaultFormats, Extraction}
import org.lmd.xke.common.ActorProducer
import org.scalacheck.Gen

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * Created by loicmdivad on 24/06/2017.
  */
class SatoriVelib {

}

object SatoriVelib extends App {

  implicit val system = ActorSystem()
  implicit val dispatcher = system.dispatcher


  lazy val conf: Config = ConfigFactory.load("satori-velib.conf")
  lazy val logger: Logger = Logger(classOf[SatoriVelib])

  def actorStation(id: String): ActorRef =  system.actorOf(Props.create(classOf[VelibSensor],
    master, id,
    conf.getString("kafka.host"),
    conf.getString("kafka.topic"),
    conf.getString("kafka.serde.key"),
    conf.getString("kafka.serde.value"),
    conf.getString("kafka.schema.registry.url")
  ))

  val master = actor(new Act{

    var executors: Map[String, ActorRef] = Map[String, ActorRef]()

    whenStarting {

      executors += "stations-0" -> actorStation("stations-0")
      executors += "stations-1" -> actorStation("stations-1")
      executors += "stations-2" -> actorStation("stations-2")
      executors += "stations-3" -> actorStation("stations-3")

    }

    become {
      case name: String => executors(name) ! "Go!"
      case (id: String, date: String) => println(s"worker $id: lap n° $date")
      case msg => logger error s"Master Actor get a Illegal Message type: ${msg.getClass}"
    }

  })

  logger info "Starting the 4 executors"

  master ! "stations-0"
  //master ! "stations-1"
  //master ! "stations-2"
  //master ! "stations-3"
}

case class Station(id: Long, name: String, available_bikes: Int)


class VelibSensor(master: ActorRef,
               id: String,
               host: String,
               topic: String,
               keySerde: String,
               valueSerde: String,
               schemaRegistry: String)

  extends ActorProducer(host, topic, keySerde, valueSerde, schemaRegistry) with Actor {

  var (count, len) = (0, 0)

  def nextLen() = (count, len) match {
    case (10, _) => count = 0; len = Gen.choose[Int](0, 8).sample.get; len
    case (_, _) => count += 1; len
  }

  override def extract(smth: Any)(implicit formats: DefaultFormats) =
    Extraction.decompose(Station(id.last.toLong, host, nextLen()))

  override def receive: Receive = {
    case _ =>

      context.system.scheduler.scheduleOnce(0.5 seconds, self, {
        produce("push")
      })
  }
}