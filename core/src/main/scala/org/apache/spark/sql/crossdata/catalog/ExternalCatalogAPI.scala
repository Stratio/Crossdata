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
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan
import org.apache.spark.sql.crossdata.catalog.XDCatalog.{CrossdataTable, ViewIdentifier}
import org.apache.spark.sql.crossdata.catalog.interfaces.XDAppsCatalog

private[crossdata] trait ExternalCatalogAPI extends XDAppsCatalog{

  def persistTable(crossdataTable: CrossdataTable, table: LogicalPlan): Unit
  def persistView(tableIdentifier: ViewIdentifier, plan: LogicalPlan, sqlText: String): Unit

  def dropTable(tableIdentifier: TableIdentifier): Unit
  def dropAllTables(): Unit

  def dropView(viewIdentifier: ViewIdentifier): Unit
  def dropAllViews(): Unit

  def tableMetadata(tableIdentifier: TableIdentifier): Option[CrossdataTable]

}



