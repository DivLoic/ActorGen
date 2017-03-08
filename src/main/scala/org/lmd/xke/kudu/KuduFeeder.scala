package org.lmd.xke.kudu

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.Logger
import org.scalacheck.Gen

/**
  * Created by loicmdivad on 14/02/2017.
  */
object KuduFeeder {

  def main(args: Array[String]): Unit = {
    val feeder = new KuduFeeder()
    //feeder.run()
  }
}

class KuduFeeder {

  import Browser._
  import Tag._

  lazy val logger: Logger = Logger(classOf[KuduFeeder])
  lazy val conf: Config = ConfigFactory.load("kudufeeder")

  //def run(): Unit = {

    //val kfeeeder = new KFeeder(
    //  conf.getString("kafka.host"),
    //  conf.getString("kafka.topic"),
    //  conf.getString("kafka.serde.key"),
    //  conf.getString("kafka.serde.value"),
    //  conf.getString("kafka.schema.registry.url"),
    //  conf.getString("kafka.schema.template")
    //)

    //for (i <- 0 to 100){

      //val isMobile = Gen.frequency((7, false),(3, true)).sample.get

      //val genNode = for {
      //  t <- tagGen(None)
      //  y <- browseGen(isMobile)
      //} yield Foo(t, y, isMobile, DateTime.now().toString("yyyy-MM-dd HH:mm:ss"))

      //Thread.sleep(Gen.choose(3000, 5000).sample.get)

      //val event = genNode.sample.get
      //val date = DateTime.now().toString("yyyy-MM-dd HH:mm:ss")

      //kfeeeder.send(event)

      //logger info s"$date;${event.t};${event.b};$isMobile"

    //}}

  /**
    *
    * @param f
    * @return
    */
  def mobileGen(f: Int): Gen[Boolean] = Gen.frequency((f, true), (f - 10, false))

  /**
    *
    * @param weigths
    * @return
    */
  def tagGen(weigths: Option[List[Int]]): Gen[Tag.Value] = {

    val probs = weigths.getOrElse(Gen.listOfN(4, Gen.chooseNum(1, 6)).sample.get)

    Gen.frequency((5, Data), (probs(1), Back), (probs(2), Mobile), (probs(3), Front))
    /*probs.head*/
  }

  /**
    *
    * @param isMobile
    * @return
    */
  def browserGen(isMobile: Boolean): Gen[Browser.Value] = {

    val probs = if(isMobile) List(3, 2, 5, 0, 0) else List(2, 5, 2, 2, 1)

    Gen.frequency((probs.head, Chrome), (probs(1), Firefox), (probs(2), Safari), (probs(3), Opera), (probs(4), Edge))
  }

}

