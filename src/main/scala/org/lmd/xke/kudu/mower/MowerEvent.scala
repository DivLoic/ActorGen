package org.lmd.xke.kudu.mower

import org.lmd.xke.kudu.Event

/**
  * Created by loicmdivad on 25/03/2017.
  */
case class MowerEvent(host: String,
                      ts: String,
                      revolution: Double,
                      speed: Double,
                      heat: Double,
                      elapsed: Double,
                      cooling: Boolean = false,
                      version: String)

  extends Event(host, ts) {}
