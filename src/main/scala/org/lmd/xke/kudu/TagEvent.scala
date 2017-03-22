package org.lmd.xke.kudu

import org.joda.time.DateTime
import org.scalacheck.Gen

/**
  * Created by loicmdivad on 03/03/2017.
  */
class TagEvent {

  import Browser._
  import Tag._

  def tagFactory(hostName: String): Event = {

    val isMobile = Gen.frequency((7, false),(3, true)).sample.get

    val genNode = for {
      tag <- tagGen(None)
      browser <- browserGen(isMobile)
    } yield Event(hostName, DateTime.now().toString("yyyy-MM-dd HH:mm:ss"), tag, browser, isMobile)

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

object Tag extends Enumeration {
  type Tag = Value
  val Data, Mobile, Back, Front = Value
}

object Browser extends Enumeration {
  type Browser = Value
  val Chrome, Firefox, Safari, Opera, Edge = Value
}

case class Event(host: String, ts: String, tag: Tag.Value, browser: Browser.Value, is_mobile: Boolean)
