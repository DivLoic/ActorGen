package org.lmd.xke.kudu.web

import org.joda.time.DateTime
import org.lmd.xke.kudu.EventGenerator
import org.lmd.xke.kudu.web.Browser._
import org.lmd.xke.kudu.web.Tag._
import org.scalacheck.Gen

/**
  * Created by loicmdivad on 25/03/2017.
  */
class TagEventGenerator extends EventGenerator[TagEvent] {

  override def build(id: String): TagEvent = {

    val isMobile = Gen.frequency((7, false),(3, true)).sample.get

    val genNode = for {
      tag <- tagGen(None)
      browser <- browserGen(isMobile)
    } yield TagEvent(id, DateTime.now().toString("yyyy-MM-dd HH:mm:ss"), tag, browser, isMobile)

    genNode.sample.get

  }

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
