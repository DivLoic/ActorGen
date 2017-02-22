package org.lmd.xke.kudu

import org.joda.time.DateTime
import org.lmd.xke.kudu.model.{Browser, Tag}
import org.scalacheck.Gen

/**
  * Created by loicmdivad on 14/02/2017.
  */
object KuduFeeder {

  case class Foo(t: Tag.Value, b: Browser.Value)

  def main(args: Array[String]): Unit = {

    for (i <- 0 to 20){

      val isMobile = Gen.frequency((7, false),(3, true)).sample.get

      val genNode = for{
        t <- Tag.generator(None)
        y <- Browser.generator(isMobile)
      } yield Foo(t, y)

      Thread.sleep(Gen.choose(3000, 5000).sample.get)

      val record = genNode.sample.get
      val date = DateTime.now().toString("yyyy-MM-dd HH:mm:ss")
      println(s"$date;${record.t};${record.b};$isMobile")
    }

  }
}
