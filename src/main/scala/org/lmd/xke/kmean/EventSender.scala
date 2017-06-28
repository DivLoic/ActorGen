package org.lmd.xke.kmean

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import breeze.stats.distributions.Gaussian
import com.sksamuel.avro4s.RecordFormat
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.Logger
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.kafka.clients.producer.{Callback, KafkaProducer, ProducerRecord, RecordMetadata}
import org.apache.kafka.common.record.Record
import org.apache.kafka.common.serialization.StringSerializer
import org.scalacheck.Gen

import scala.concurrent.duration._
import scala.collection.JavaConversions._

/**
  * Created by loicmdivad on 22/04/2017.
  */
class EventSender {

  private val distrib = Gaussian(mu = 1, sigma = 5.5)

  val teamgen: Gen[String] = Gen.frequency(
    (4, "GuardianA"),
    (3, "GuardianB"),
    (3, "Guardian2"),
    (1, "GuardianC"),
    (1, "GuardianD"),
    (1, "GuardianE")
  )

  private val scoregen: Gen[Double] = Gen.choose(8.0, 20.0)

  private val generator: Gen[Points] = for {
    team <- teamgen
    points <- scoregen
  } yield Points(team, adjust(points, distrib.get()), "")

  private def adjust(point: Double, power: Double): Double =
    Math.floor(Math.abs(point * power) * 100) / 100d

  def scoring(foo: Any): Points = generator.sample.get

}

object EventSender extends App {

  implicit val system = ActorSystem()

  def Logger(s: String): Any = ???

  val logger: Any = Logger("main")

  val sender = new EventSender()

  val conf: Config = ConfigFactory.load()

  val format: RecordFormat[Points] = RecordFormat[Points]

  val producerSettings: ProducerSettings[String, Record] = ProducerSettings(
    system,
    new StringSerializer,
    new KafkaAvroSerializer
  )
    .withParallelism(5)
    .withCloseTimeout(60 seconds)
    .withBootstrapServers("localhost:9092")
    .asInstanceOf[ProducerSettings[String, Record]]

  /*Source(1 to 100)
    .map(sender.scoring)
    .map(format.to)
    .map(_.asInstanceOf[Record])
    .map{ new ProducerRecord[String, Record]("points", _) }
    .runWith(Producer.plainSink(producerSettings))*/

  val producer = new KafkaProducer[String, Record](Map[String, String](
    "bootstrap.servers" -> s"${conf.getString("kafka.broker")}",
    "key.serializer" -> classOf[KafkaAvroSerializer].getName,
    "value.serializer" -> classOf[KafkaAvroSerializer].getName,
    "schema.registry.url" -> conf.getString("kafka.schema-registry")
  ))

  /*while(true){
    Thread.sleep(2000)
    var point = sender.scoring()
    var score = format.to(point).asInstanceOf[Record]
    var record = new ProducerRecord[String, Record]("points", point.team, score)
    logger info record.toString
    producer.send(record)
  }*/

}

object EventReference extends App {

  val logger: Logger = Logger("main")

  val conf: Config = ConfigFactory.load()

  val format: RecordFormat[Guild] = RecordFormat[Guild]

  logger info "kafka.broker: " + conf.getString("kafka.broker")
  logger info "kafka.serde.key: " + conf.getString("kafka.serde.key")
  logger info "kafka.serde.value: " + conf.getString("kafka.serde.value")
  logger info "kafka.schema-registry: " + conf.getString("kafka.schema-registry")

  val producer = new KafkaProducer[String, Record](Map[String, String](
    "bootstrap.servers" -> s"${conf.getString("kafka.broker")}",
    "key.serializer" -> classOf[KafkaAvroSerializer].getName,
    "value.serializer" -> classOf[KafkaAvroSerializer].getName,
    "schema.registry.url" -> conf.getString("kafka.schema-registry")
  ))

  Seq(
    Guild(name = "GuardianA", lvl = 155, heroes = true, color = "#123456"),
    Guild(name = "GuardianB", lvl = 250, heroes = true, color = "#223456"),
    Guild(name = "GuardianC", lvl = 350, heroes = true, color = "#323456"),
    Guild(name = "GuardianD", lvl = 250, heroes = true, color = "#423456"),
    Guild(name = "GuardianE", lvl = 150, heroes = true, color = "#523456"),
    Guild(name = "GuardianF", lvl =  55, heroes = true, color = "#623456"),
    Guild(name = "GuardianG", lvl = 755, heroes = true, color = "#723456")
  )
    .map(guild =>  (guild.name, format.to(guild).asInstanceOf[Record]))
    .map(keyvalue => { new ProducerRecord[String, Record]("guild", keyvalue._1, keyvalue._2)})
    .foreach(record => {
      logger info record.toString

      Thread.sleep(2000)

      producer.send(record, new Callback(){
        override def onCompletion(metadata: RecordMetadata, e: Exception): Unit = e match {
          case null =>
          case err: Exception => logger error err.getMessage
        }
      })
    })

}