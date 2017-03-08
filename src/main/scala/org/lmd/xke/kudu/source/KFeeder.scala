package org.lmd.xke.kudu.source

import com.typesafe.scalalogging.Logger
import org.apache.avro.Schema
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericData.Record
import org.apache.kafka.clients.producer.{Callback, KafkaProducer, ProducerRecord, RecordMetadata}
import org.lmd.xke.kudu.{Event, TagEvent}

import scala.io.Source

/**
  * Created by loicmdivad on 14/02/2017.
  */
abstract class KFeeder(host: String,
              topic: String,
              keySerde: String,
              valueSerde: String,
              schemaRegistry: String,
              schemaPath: String) {

  import scala.collection.JavaConversions._

  lazy val logger: Logger = Logger("main")

  val tagEvent: TagEvent = new TagEvent()

  val schema: Schema = new Schema.Parser().parse(Source.fromFile(schemaPath).mkString)

  val producer = new KafkaProducer[String, Record](Map[String, String](
    "bootstrap.servers" -> host,
    "key.serializer" -> keySerde,
    "value.serializer" -> valueSerde,
    "schema.registry.url" -> schemaRegistry
  ))

  def produce(event: Event): Unit = {
    val message = new GenericData.Record(schema)
    message.put("host", event.host)
    message.put("ts", event.dt)
    message.put("tag", event.tag.toString)
    message.put("browser", event.browser.toString)
    message.put("is_mobile", event.mobile)

    val record: ProducerRecord[String, Record] = new ProducerRecord(this.topic, message)
    producer.send(record, new Callback(){
      override def onCompletion(metadata: RecordMetadata, e: Exception): Unit = e match {
        case null =>
        case err: Exception => logger error err.getMessage
      }
    })
  }

}