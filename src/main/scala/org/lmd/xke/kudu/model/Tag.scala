package org.lmd.xke.kudu.model

import org.scalacheck.Gen

/**
  * Created by loicmdivad on 14/02/2017.
  */
object Tag extends Enumeration {
  type Tag = Value
  val Data, Mobile, Back, Front = Value

  def generator(weigths: Option[List[Int]]): Gen[Tag.Value] = {
    val probs: List[Int] = weigths match {
      case Some(values) => values
      case None => Gen.listOfN(4, Gen.choose(1, 2)).sample.get
    }

    Gen.frequency(
      (probs.head, Data),
      (probs(1), Mobile),
      (probs(2), Back),
      (probs(3), Front)
    )
  }
}


