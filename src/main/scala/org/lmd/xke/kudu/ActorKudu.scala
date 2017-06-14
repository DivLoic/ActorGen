package org.lmd.xke.kudu

import akka.actor.ActorDSL._
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.sksamuel.avro4s.RecordFormat
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.Logger
import org.lmd.xke.kudu.web.{TagEvent, TagEventGenerator}
import org.scalacheck.Gen

import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.duration._

/**
  * Created by loicmdivad on 14/02/2017.
  * Start an actor system composed of 4 actors.
  * All sub actors extend a kafka-producer wrapper and generate randoms event.s
  */
object ActorKudu extends App {

  lazy val logger: Logger = Logger("main")
  lazy val conf: Config = ConfigFactory.load()

  implicit val system = ActorSystem()

  def execFactory(id: String): ActorRef =  system.actorOf(Props.create(classOf[Executor],
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

      executors += "kudu-tag-0" -> execFactory("kudu-tag-0")
      executors += "kudu-tag-1" -> execFactory("kudu-tag-1")
      executors += "kudu-tag-2" -> execFactory("kudu-tag-2")
      executors += "kudu-tag-3" -> execFactory("kudu-tag-3")

    }

    become {
      case name: String => executors(name) ! "Go!"
      case (id: String, date: String) => println(s"worker $id: lap n° $date")
      case msg => logger error s"Master Actor get a Illegal Message type: ${msg.getClass}"
    }

  })

  logger info "Starting the 4 executors"

  master ! "kudu-tag-0"
  master ! "kudu-tag-1"
  master ! "kudu-tag-2"
  master ! "kudu-tag-3"

}

/**
  *
  * @param master parent actor
  * @param id name of the executor
  * @param host kafka bootstrap server
  * @param topic target kafka topic
  * @param keySerde
  * @param valueSerde
  * @param schemaRegistry kafka schema registry
  */
class Executor(master: ActorRef,
                  id: String,
                  host: String,
                  topic: String,
                  keySerde: String,
                  valueSerde: String,
                  schemaRegistry: String)

  extends KFeeder(host, topic, keySerde, valueSerde, schemaRegistry) with Actor {

  val generator = new TagEventGenerator()

  val schema: RecordFormat[TagEvent] = RecordFormat[TagEvent]

  override def receive: Receive = {
    case _ =>
      val delta = Gen.choose(1, 4).sample.get
      val tag = generator.build(id)

      context.system.scheduler.scheduleOnce(delta seconds, self, {
        produce(schema.to(tag))
      })
  }

}

