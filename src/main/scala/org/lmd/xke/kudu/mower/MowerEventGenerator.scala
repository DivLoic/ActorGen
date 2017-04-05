package org.lmd.xke.kudu.mower

import breeze.stats.distributions.{ChiSquared, Gaussian}
import org.joda.time.DateTime
import org.lmd.xke.kudu.EventGenerator
import org.scalacheck.Gen


/**
  * Created by loicmdivad on 01/04/2017.
  */
class MowerEventGenerator(frequency: Double) extends EventGenerator[MowerEvent]{

  import  MowerEventGenerator._

  val chiSpeed = ChiSquared(1)
  val gaussianHeat = Gaussian(mu = 0, sigma = 5.5)
  val gaussianRevolution = Gaussian(mu = 0, sigma = 1.5)

  override def build(id: String): MowerEvent = MowerEvent(
    ts = DateTime.now().toString("yyyy-MM-dd HH:mm:ss"),
    revolution = MIN_REVOLUTION,
    heat = MIN_HEAT + 1,
    speed = 0.0,
    elapsedTime = 0.0, // elapsed
    host = id
  )

  def next(mowerEvent: MowerEvent): MowerEvent = MowerEvent(
    host = mowerEvent.host,
    ts = DateTime.now().toString("yyyy-MM-dd HH:mm:ss"),
    revolution = round(nextRevolution(mowerEvent)),
    speed = round(nextSpeed),
    heat = round(nextHeat(mowerEvent)),
    cooling = nextCooling(mowerEvent),
    elapsedTime = nextTimeElapse(mowerEvent)
  )


  def nextHeat(mowerEvent: MowerEvent): Double = {
    if(mowerEvent.cooling)
      MIN_HEAT * (1 - Math.exp( - 0.05 * mowerEvent.elapsedTime)) +
        (MAX_HEAT * Math.exp( - 0.05 * mowerEvent.elapsedTime)) + 2 * gaussianHeat.sample(1).head
    else
      MIN_HEAT* Math.exp( - 0.1 * mowerEvent.elapsedTime)+
        MAX_HEAT * (1 - Math.exp( - 0.1 * mowerEvent.elapsedTime)) + 2 * gaussianHeat.sample(1).head
  }

  def nextCooling(mowerEvent: MowerEvent): Boolean =
    (mowerEvent.heat, mowerEvent.cooling, mowerEvent.elapsedTime) match {
      case (heat, true, els) if heat < MIN_HEAT  & els >= 75 => false
      case (heat, true, _) if heat > MIN_HEAT => true
      case (heat, false, _) if heat > MAX_HEAT => true
      case (heat, false, _) if heat < MAX_HEAT => false
      case _ => false
  }

  def nextTimeElapse(mowerEvent: MowerEvent): Double =
    if(mowerEvent.cooling != nextCooling(mowerEvent)) 0.0
    else mowerEvent.elapsedTime + frequency

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
}

object MowerEventGenerator {

  val MAX_HEAT: Double = 250.0
  val MIN_HEAT: Double = 50.0

  val MAX_SPEED: Double = 25.0

  val MAX_REVOLUTION: Double = 4000.0
  val MIN_REVOLUTION: Double = 1000.0

}