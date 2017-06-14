package org.lmd.xke.kudu.mower

import breeze.stats.distributions.{ChiSquared, Gaussian}
import org.joda.time.DateTime
import org.lmd.xke.kudu.EventGenerator
import org.scalacheck.Gen

/**
  * Created by loicmdivad on 01/04/2017.
  */
class MowerEventGenerator(frequency: Double, median: Double) extends EventGenerator[MowerEvent]{

  import MowerEventGenerator._

  val chiSpeed = ChiSquared(1)
  val gaussianHeat = Gaussian(mu = 0, sigma = 5.5)
  val gaussianRevolution = Gaussian(mu = 0, sigma = 1.5)

  //val median: Double = Gen.choose(-50.0, 50.0).sample.get
  //val median: Double = Gen.choose(0.0, 30.0).sample.get

  override def build(id: String, v: String): MowerEvent = MowerEvent(
    ts = DateTime.now().plusHours(6).toString("yyyy-MM-dd HH:mm:ss.SSS"),
    revolution = MIN_REVOLUTION,
    heat = (MIN_HEAT + median) + 1,
    speed = 0.0,
    elapsed = 0.0,
    host = id,
    version = v
  )

  def next(mowerEvent: MowerEvent): MowerEvent = MowerEvent(
    host = mowerEvent.host,
    ts = DateTime.now().plusHours(6).toString("yyyy-MM-dd HH:mm:ss.SSS"),
    revolution = round(nextRevolution(mowerEvent)),
    speed = round(nextSpeed),
    heat = round(nextHeat(mowerEvent)),
    cooling = nextCooling(mowerEvent),
    elapsed = nextTimeElapse(mowerEvent),
    version = mowerEvent.version
  )


  def nextHeat(mowerEvent: MowerEvent): Double = {
    if(mowerEvent.cooling)
      (MIN_HEAT + median) * (1 - Math.exp( - 0.05 * mowerEvent.elapsed)) +
        ((MAX_HEAT + median) * Math.exp( - 0.05 * mowerEvent.elapsed)) + 2 * gaussianHeat.sample(1).head
    else
      (MIN_HEAT + median) * Math.exp( - 0.1 * mowerEvent.elapsed)+
        (MAX_HEAT + median) * (1 - Math.exp( - 0.1 * mowerEvent.elapsed)) + 2 * gaussianHeat.sample(1).head
  }

  def nextCooling(mowerEvent: MowerEvent): Boolean =
    (mowerEvent.heat, mowerEvent.cooling, mowerEvent.elapsed) match {
      case (heat, true, els) if heat < (MIN_HEAT + median)  & els >= 55 => false
      case (heat, true, _) if heat > (MIN_HEAT + median) => true
      case (heat, false, _) if heat > (MAX_HEAT + median) => true
      case (heat, false, _) if heat < (MAX_HEAT + median) => false
      case _ => false
  }

  def nextTimeElapse(mowerEvent: MowerEvent): Double =
    if(mowerEvent.cooling != nextCooling(mowerEvent)) 0.0
    else mowerEvent.elapsed + frequency

  def nextRevolution(mowerEvent: MowerEvent): Double = mowerEvent.revolution match {
    case r if r <= MAX_REVOLUTION => r + 500 + 100 * gaussianRevolution.sample(1).head
    case r if r >= MAX_REVOLUTION => MIN_REVOLUTION + 100 * gaussianRevolution.sample(1).head
  }

  def nextSpeed: Double =
    MAX_SPEED + Gen.frequency(
      (25, 5), (25, -5),
      (25, 2), (25, -2),
      (2, 100)
    ).sample.get * chiSpeed.sample(1).head


  def round(d: Double): Double =
    (d * 100.0 floor) / 100

  override def build(id: String): MowerEvent = build(id, "")
}

object MowerEventGenerator {

  val MAX_HEAT: Double = 250.0
  val MIN_HEAT: Double = 50.0

  val MAX_SPEED: Double = 25.0

  val MAX_REVOLUTION: Double = 4000.0
  val MIN_REVOLUTION: Double = 1000.0

}