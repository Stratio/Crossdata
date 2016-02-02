/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.crossdata.streaming.actors

import akka.actor.Actor
import com.github.nscala_time.time.Imports._
import com.stratio.crossdata.streaming.constants.ApplicationConstants
import EphemeralStatusActor._
import ApplicationConstants._
import org.apache.curator.framework.recipes.cache.NodeCache
import org.apache.spark.sql.crossdata.daos.EphemeralTableStatusMapDAO
import org.apache.spark.sql.crossdata.models.{EphemeralExecutionStatus, EphemeralStatusModel}

class EphemeralStatusActor(zookeeperConfiguration: Map[String, String],
                           ephemeralTableName: String) extends Actor
with EphemeralTableStatusMapDAO {

  val memoryMap = Map(ZookeeperPrefixName -> zookeeperConfiguration)
  var ephemeralStatus: Option[EphemeralStatusModel] = getRepositoryStatusTable
  var listenerAdded: Boolean = false

  override def receive: Receive = {
    case GetStatus => doGetStatus()
    case SetStatus(status) => doSetStatus(status)
    case AddListener => doAddListener()
  }

  private def doGetStatus(): Unit = {
    sender ! StatusResponse(getStatusFromTable(ephemeralStatus))
  }

  private def doSetStatus(newStatus: EphemeralExecutionStatus.Value) : Unit = {

    val startTime = if(newStatus == EphemeralExecutionStatus.Started) Option(DateTime.now.getMillis) else None
    val stopTime = if(newStatus == EphemeralExecutionStatus.Stopped) Option(DateTime.now.getMillis) else None

    ephemeralStatus = ephemeralStatus.fold(ephemeralStatus) {
      ephStatus => Option(ephStatus.copy(status = newStatus,
        stoppedTime = stopTime,
        startedTime = startTime.orElse(ephStatus.startedTime)))
    }

    ephemeralStatus.fold(dao.create(ephemeralTableName,
      EphemeralStatusModel(ephemeralTableName, newStatus, startTime, stopTime)))
    { ephemeralTable =>
      dao.upsert(ephemeralTableName, ephemeralTable)
    }
  }

  private def doAddListener(): Unit = {
    if (!listenerAdded) {
      repository.addListener[EphemeralStatusModel](dao.entity,
        ephemeralTableName,
        (newEphemeralStatus: EphemeralStatusModel, nodeCache: NodeCache) => {
          ephemeralStatus = Option(newEphemeralStatus)
        })
      listenerAdded = true
    }
  }

  private def getRepositoryStatusTable: Option[EphemeralStatusModel] = {
    dao.get(ephemeralTableName)
  }

  private def getStatusFromTable(ephemeralTable : Option[EphemeralStatusModel]) = {
    ephemeralTable.fold(EphemeralExecutionStatus.NotStarted) { tableStatus =>
      tableStatus.status
    }
  }
}

object EphemeralStatusActor {

  case object GetStatus

  case object AddListener

  case class SetStatus(status: EphemeralExecutionStatus.Value)

  case class StatusResponse(status: EphemeralExecutionStatus.Value)

}