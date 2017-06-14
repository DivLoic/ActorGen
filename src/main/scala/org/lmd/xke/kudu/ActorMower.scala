package org.lmd.xke.kudu


import akka.actor.ActorDSL._
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.sksamuel.avro4s.{AvroSchema, RecordFormat}
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.Logger
import org.lmd.xke.kudu.mower.{MowerEvent, MowerEventGenerator}

import scala.concurrent.duration._

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by loicmdivad on 01/04/2017.
  */
object ActorMower extends App {

  lazy val logger: Logger = Logger("main")
  lazy val conf: Config = ConfigFactory.load("xke-kudu")

  implicit val system = ActorSystem()

  def execFactory(id: String, v: String, median: String): ActorRef =  system.actorOf(Props.create(classOf[ActorMowerExec],
    master, v, median, id,
    conf.getString("kafka.host"),
    conf.getString("kafka.topic"),
    conf.getString("kafka.serde.key"),
    conf.getString("kafka.serde.value"),
    conf.getString("kafka.schema.registry.url")
  ))

  val master = actor(new Act{

    var executors: Map[String, ActorRef] = Map[String, ActorRef]()

    whenStarting {

      executors += "actor-mower-0" -> execFactory("actor-mower-0", "7d77-3a9a3933", "45.0")
      executors += "actor-mower-1" -> execFactory("actor-mower-1", "9cf4-8d2d442f", "10.0")
      executors += "actor-mower-2" -> execFactory("actor-mower-2", "060b-602c6eb0", "75.0")
      executors += "actor-mower-3" -> execFactory("actor-mower-3", "9cf4-8d2d442f", "10.0")

    }

    become {
      case name: String => executors(name) ! "Go!"
        logger info s"The actor-mower: $name has succefully started."
      case (id: String, date: String) => println(s"worker $id: lap n° $date")
      case msg => logger error s"Master Actor get a Illegal Message type: ${msg.getClass}"
    }

  })

  logger info "Starting the 4 executors"

  master ! "actor-mower-0"
  master ! "actor-mower-1"
  master ! "actor-mower-2"
  master ! "actor-mower-3"

}

class ActorMowerExec(master: ActorRef,
                     v: String,
                     median: String,
                     id: String,
                     host: String,
                     topic: String,
                     keySerde: String,
                     valueSerde: String,
                     schemaRegistry: String)

  extends KFeeder(host, topic, keySerde, valueSerde, schemaRegistry) with Actor {

  val frequency = 0.2
  val generator = new MowerEventGenerator(frequency, median.toDouble)

  val schema: String = AvroSchema[MowerEvent].toString
  val format: RecordFormat[MowerEvent] = RecordFormat[MowerEvent]

  //logger debug s"$id is configure to send records with the following schema - $schema"

  override def receive: Receive = {
    case msg: String =>
      context.system.scheduler.scheduleOnce(frequency seconds, self, generator.build(id, v))

    case mower: MowerEvent =>
      context.system.scheduler.scheduleOnce(frequency seconds, self, {
        //println(format.to(mower).toString)
        produce(format.to(mower))
        generator.next(mower)
      })
  }
}


