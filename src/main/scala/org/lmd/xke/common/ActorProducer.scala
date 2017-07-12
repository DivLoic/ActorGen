package org.lmd.xke.common

import com.fasterxml.jackson.databind.JsonNode
import com.typesafe.scalalogging.Logger
import org.apache.kafka.clients.producer.{Callback, KafkaProducer, ProducerRecord, RecordMetadata}
import org.json4s.jackson.JsonMethods.asJsonNode
import org.json4s.{DefaultFormats, JValue}
import sun.reflect.generics.reflectiveObjects.NotImplementedException

import scala.collection.JavaConversions._

/**
  * Created by loicmdivad on 24/06/2017.
  */
abstract class ActorProducer(host: String,
                       topic: String,
                       keySerde: String,
                       valueSerde: String,
                       schemaRegistry: String) {

  implicit val formats = DefaultFormats

  lazy val logger: Logger = Logger(classOf[ActorProducer])

  val producer = new KafkaProducer[String, JsonNode](Map[String, String](
    "bootstrap.servers" -> host,
    "key.serializer" -> keySerde,
    "value.serializer" -> valueSerde,
    "schema.registry.url" -> schemaRegistry
  ))

  def extract(smth: Any)(implicit formats:DefaultFormats): JValue = throw new NotImplementedException()

  def produce(event: Any): Unit = {

    val node: JsonNode = asJsonNode(extract(event))

    val message: ProducerRecord[String, JsonNode] = new ProducerRecord(topic, node.get("id").asText(), node)

    producer.send(message, new Callback(){
      override def onCompletion(metadata: RecordMetadata, e: Exception): Unit = e match {
        case null =>
        case err: Exception => logger error err.getMessage
      }
    })
  }

}