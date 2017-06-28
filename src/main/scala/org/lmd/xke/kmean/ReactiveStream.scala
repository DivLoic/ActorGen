package org.lmd.xke.kmean

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.Source
import com.sksamuel.avro4s.RecordFormat
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.Logger
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.avro.generic.GenericData.Record
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.duration._

/**
  * Created by loicmdivad on 09/05/2017.
  */
object ReactiveStream extends App {


    implicit val system = ActorSystem()

    val logger: Logger = Logger("main")

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

    Source(1 to 100)
      .map(sender.scoring)
      .map(format.to)
      .map(_.asInstanceOf[Record])
      .map{ new ProducerRecord[String, Record]("points", _) }
      //.runWith(Producer.plainSink(producerSettings))

}
