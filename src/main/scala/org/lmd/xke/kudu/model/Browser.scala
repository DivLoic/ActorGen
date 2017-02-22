package org.lmd.xke.kudu.model

import org.scalacheck.Gen

/**
  * Created by loicmdivad on 14/02/2017.
  */
object Browser extends Enumeration {
  type Browser = Value
  val Chrome, Firefox, Safari, Opera, Edge = Value

  def generator(isMobile: Boolean) = {
    val probs = if(isMobile) List(3,2,5,0,0) else List(2, 5, 2, 2, 1)
      Gen.frequency(
        (probs.head, Chrome),
        (probs(1), Firefox),
        (probs(2), Safari),
        (probs(3), Opera),
        (probs(4), Edge)
      )
  }
}
