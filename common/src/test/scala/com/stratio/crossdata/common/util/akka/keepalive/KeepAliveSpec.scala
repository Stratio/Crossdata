/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.crossdata.common.util.akka.keepalive

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.TestKit
import com.stratio.crossdata.common.util.akka.keepalive.KeepAliveMaster.{DoCheck, HeartbeatLost}
import com.stratio.crossdata.common.util.akka.keepalive.LiveMan.HeartBeat
import org.scalatest.{FlatSpecLike, Matchers}

import scala.concurrent.duration._


class KeepAliveSpec extends TestKit(ActorSystem("KeepAliveSpec"))
  with FlatSpecLike with Matchers {

  class MonitoredActor(override val keepAliveId: Int, override val master: ActorRef) extends LiveMan[Int] {
    override val period: FiniteDuration = 100 milliseconds

    override def receive: Receive = PartialFunction.empty
  }


  "A LiveMan Actor" should "periodically send HearBeat message providing its id" in {

    val kaId = 1

    val liveMan: ActorRef = system.actorOf(Props(new MonitoredActor(kaId, testActor)))
    expectMsg(HeartBeat(kaId))

    system.stop(liveMan)
  }



  "A Master Actor" should "detect when a LiveManActor stops beating" in {

    val master: ActorRef = system.actorOf(KeepAliveMaster.props[Int](testActor))

    val liveMen: Seq[(Int, ActorRef)] = (1 to 5) map { idx =>
      master ! DoCheck(idx, 200 milliseconds)
      idx -> system.actorOf(Props(new MonitoredActor(idx, master)))
    }

    // All live actors are letting the master know that they're alive
    expectNoMsg(500 milliseconds)

    // Lets stop the first one
    system.stop(liveMen.head._2)

    // And wait for the right detection of its loss
    expectMsg(500 milliseconds, HeartbeatLost(liveMen.head._1))

    // Since the master is set to stop monitoring down actors and the rest of `liveMen` are working, no more loss
    // notifications should be expected.
    expectNoMsg(1 second)

    // Until another monitored actor is down
    val (lastId, lastActor) = liveMen.last

    system.stop(lastActor)

    expectMsg(500 milliseconds, HeartbeatLost(lastId))
    
    liveMen foreach {
      case (_, monitoredActor) => system.stop(monitoredActor)
    }

    system.stop(master)

  }


}
