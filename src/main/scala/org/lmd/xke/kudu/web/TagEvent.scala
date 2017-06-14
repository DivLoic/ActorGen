package org.lmd.xke.kudu.web

import org.lmd.xke.kudu.Event


/**
  * Created by loicmdivad on 25/03/2017.
  */
case class TagEvent(host: String,
                    ts: String,
                    tag: Tag.Value,
                    browser: Browser.Value,
                    is_mobile: Boolean)

  extends Event(host, ts)
