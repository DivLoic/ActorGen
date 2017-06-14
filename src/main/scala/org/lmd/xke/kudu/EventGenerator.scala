package org.lmd.xke.kudu

/**
  * Created by loicmdivad on 25/03/2017.
  */
trait EventGenerator[T] {
  def build(id: String): T
  def build(id: String, v: String): T
}
