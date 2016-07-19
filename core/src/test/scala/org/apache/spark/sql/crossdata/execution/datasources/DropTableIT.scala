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
package org.apache.spark.sql.crossdata.execution.datasources

import org.apache.spark.sql.catalyst.{CatalystConf, TableIdentifier}
import org.apache.spark.sql.crossdata.catalog.{CatalogChain, XDCatalog}
import XDCatalog.CrossdataTable
import org.apache.spark.sql.crossdata.catalog.persistent.PersistentCatalogWithCache
import org.apache.spark.sql.crossdata.test.SharedXDContextTest
import org.apache.spark.sql.types.StringType
import org.apache.spark.sql.types.StructField
import org.apache.spark.sql.types.StructType
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import org.apache.spark.sql.crossdata.catalog.interfaces.XDCatalogCommon._

@RunWith(classOf[JUnitRunner])
class DropTableIT extends SharedXDContextTest {

  private val TableName = "tableId"
  private val DatabaseName = "dbId"
  private val DatasourceName = "json"
  private val Schema = StructType(Seq(StructField("col", StringType)))

  implicit def catalogToPersistenceWithCache(catalog: XDCatalog): PersistentCatalogWithCache = {
    catalog.asInstanceOf[CatalogChain].persistentCatalogs.head.asInstanceOf[PersistentCatalogWithCache]
  }

  implicit val conf: CatalystConf = xdContext.catalog.conf

  "DropTable command" should "remove a table from Crossdata catalog" in {

    _xdContext.catalog.persistTableMetadata(CrossdataTable(TableIdentifier(TableName, None).normalize, Some(Schema), DatasourceName, opts = Map("path" -> "fakepath")))
    _xdContext.catalog.tableExists(TableIdentifier(TableName)) shouldBe true
    sql(s"DROP TABLE $TableName")
    _xdContext.catalog.tableExists(TableIdentifier(TableName)) shouldBe false
  }

  it should "remove a qualified table from Crossdata catalog" in {
    _xdContext.catalog.persistTableMetadata(CrossdataTable(TableIdentifier(TableName, Some(DatabaseName)).normalize, Some(Schema), DatasourceName, opts = Map("path" -> "fakepath")))
    _xdContext.catalog.tableExists(TableIdentifier(TableName, Some(DatabaseName))) shouldBe true
    sql(s"DROP TABLE $DatabaseName.$TableName")
    _xdContext.catalog.tableExists(TableIdentifier(TableName, Some(DatabaseName))) shouldBe false
  }
}
