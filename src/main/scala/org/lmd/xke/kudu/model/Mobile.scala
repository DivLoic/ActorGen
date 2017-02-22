package org.lmd.xke.kudu.model

import org.scalacheck.Gen

/**
  * Created by loicmdivad on 14/02/2017.
  */
object Mobile {

  def generator(f: Int) = {
    assert(f <= 10)
    Gen.frequency((f, true), (f - 10, false))
  }
}
