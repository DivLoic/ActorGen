package org.lmd.xke.kudu

import com.typesafe.scalalogging.Logger
import org.apache.avro.generic.GenericData.Record
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.producer.{Callback, KafkaProducer, ProducerRecord, RecordMetadata}

/**
  * Created by loicmdivad on 14/02/2017.
  * Wrapper for kafka producer.
  */
abstract class KFeeder(host: String,
                          topic: String,
                          keySerde: String,
                          valueSerde: String,
                          schemaRegistry: String) {

  import scala.collection.JavaConversions._

  lazy val logger: Logger = Logger("main")

  val producer = new KafkaProducer[String, Record](Map[String, String](
    "bootstrap.servers" -> host,
    "key.serializer" -> keySerde,
    "value.serializer" -> valueSerde,
    "schema.registry.url" -> schemaRegistry
  ))

  /**
    *
    * @param event
    */
  def produce(event: GenericRecord): Unit = {

    val message = event.asInstanceOf[Record]

    val record: ProducerRecord[String, Record] = new ProducerRecord(this.topic, message)

    producer.send(record, new Callback(){
      override def onCompletion(metadata: RecordMetadata, e: Exception): Unit = e match {
        case null =>
        case err: Exception => logger error err.getMessage
      }
    })
  }

}