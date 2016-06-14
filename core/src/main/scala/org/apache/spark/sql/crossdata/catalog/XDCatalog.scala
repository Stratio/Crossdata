/*
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

import org.apache.spark.sql.catalyst.TableIdentifier
import org.apache.spark.sql.catalyst.analysis.Catalog
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan
import org.apache.spark.sql.crossdata
import org.apache.spark.sql.crossdata.catalog.XDCatalog.{CrossdataTable, ViewIdentifier}
import org.apache.spark.sql.crossdata.serializers.CrossdataSerializer
import org.apache.spark.sql.types.StructType
import org.json4s.jackson.Serialization._


object XDCatalog extends CrossdataSerializer {


implicit def asXDCatalog (catalog: Catalog): XDCatalog = catalog.asInstanceOf[XDCatalog]

  type ViewIdentifier = TableIdentifier


  case class CrossdataTable(tableName: String, dbName: Option[String], schema: Option[StructType],
                            datasource: String, partitionColumn: Array[String] = Array.empty,
                            opts: Map[String, String] = Map.empty, crossdataVersion: String = crossdata.CrossdataVersion)

  case class CrossdataApp(jar: String, appAlias: String, appClass: String)

  def serializeSchema(schema: StructType): String = write(schema)

  def deserializeUserSpecifiedSchema(schemaJSON: String): StructType = read[StructType](schemaJSON)

  def serializePartitionColumn(partitionColumn: Array[String]): String = write(partitionColumn)

  def deserializePartitionColumn(partitionColumn: String): Array[String] = read[Array[String]](partitionColumn)

  def serializeOptions(options: Map[String, String]): String =  write(options)

  def deserializeOptions(optsJSON: String): Map[String, String] = read[Map[String, String]](optsJSON)

}

trait XDCatalog extends Catalog
with ExternalCatalogAPI
with StreamingCatalogAPI {

  def registerTable(tableIdent: TableIdentifier, plan: LogicalPlan, crossdataTable: Option[CrossdataTable]): Unit
  def registerView(viewIdentifier: ViewIdentifier, logicalPlan: LogicalPlan, sql: Option[String] = None): Unit

  final def registerTable(tableIdent: TableIdentifier, plan: LogicalPlan): Unit =
    registerTable(tableIdent, plan, None)


  def unregisterView(viewIdentifier: ViewIdentifier): Unit

  /**
    * Check the connection to the set Catalog
    */
  def checkConnectivity: Boolean

}
