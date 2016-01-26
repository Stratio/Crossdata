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

package org.apache.spark.sql.crossdata.catalog

import org.apache.spark.Logging
import org.apache.spark.sql.crossdata.XDContext
import org.apache.spark.sql.crossdata.models.{EphemeralQueryModel, EphemeralExecutionStatus, EphemeralStatusModel, EphemeralTableModel}

/**
 * CrossdataStreamingCatalog aims to provide a mechanism to persist the
 * Streaming metadata executions.
 */
abstract class XDStreamingCatalog(xdContext: XDContext) extends Logging with Serializable {

  /**
   * Ephemeral Table Functions
   */
  def existsEphemeralTable(tableIdentifier: String): Boolean
  
  def getEphemeralTable(tableIdentifier: String) : EphemeralTableModel

  def getAllEphemeralTables() : Seq[EphemeralTableModel]

  def createEphemeralTable(ephemeralTable: EphemeralTableModel): EphemeralTableModel

  def updateEphemeralTable(ephemeralTable: EphemeralTableModel): Unit = {}

  def dropEphemeralTable(tableIdentifier: String): Unit = {}

  def dropAllEphemeralTables(): Unit = {}

  /**
   * Ephemeral Status Functions
   */
  def getEphemeralStatus(tableIdentifier: String) : EphemeralStatusModel

  def getAllEphemeralStatuses() : Seq[EphemeralStatusModel]

  def updateEphemeralStatus(tableIdentifier: String, status: EphemeralExecutionStatus.Value) : Unit

  protected[crossdata] def dropEphemeralStatus(tableIdentifier: String): Unit = {}
  
  protected[crossdata] def dropAllEphemeralStatus(): Unit = {}

  /**
   * Ephemeral Queries Functions
   */
  def existsEphemeralQuery(queryAlias: String): Boolean

  def getEphemeralQuery(queryAlias: String) : EphemeralQueryModel

  def getAllEphemeralQueries() : Seq[EphemeralQueryModel]

  def createEphemeralQuery(ephemeralTable: EphemeralQueryModel): EphemeralQueryModel

  def updateEphemeralQuery(ephemeralTable: EphemeralQueryModel): Unit = {}

  def dropEphemeralQuery(tableIdentifier: String): Unit = {}

  def dropAllEphemeralQueries(): Unit = {}

}

object XDStreamingCatalog {

}