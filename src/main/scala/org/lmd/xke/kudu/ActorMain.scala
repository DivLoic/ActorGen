package org.lmd.xke.kudu

import akka.actor._
import akka.actor.ActorDSL._
import akka.actor.ActorRef

/**
  * Created by loicmdivad on 03/03/2017.
  */
object ActorMain {

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem()

    val master = actor(new Act{

      var workerPool = List[ActorRef]()

      whenStarting {
        (1 to 5).foreach{ i =>
          val a = actor(context)(new Act{
            become {
              case value => println(s"worker $i: lap n° $value")
            }
          })
          workerPool ::= a
        }
      }

      become {
        case loop: List[Int] =>
          loop.foreach{
            lap =>  workerPool.foreach(worker => worker ! lap)
          }
      }

    })

    master ! (1 to 10).toList

  }
}
