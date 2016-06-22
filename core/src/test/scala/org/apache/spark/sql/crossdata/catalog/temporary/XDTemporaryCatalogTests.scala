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
package org.apache.spark.sql.crossdata.catalog.temporary

import org.apache.spark.sql.catalyst.TableIdentifier
import org.apache.spark.sql.catalyst.plans.logical.LocalRelation
import org.apache.spark.sql.crossdata.XDContext
import org.apache.spark.sql.crossdata.catalog.CatalogConstants
import org.apache.spark.sql.crossdata.catalog.XDCatalog.CrossdataTable
import org.apache.spark.sql.crossdata.catalog.interfaces.XDTemporaryCatalog
import org.apache.spark.sql.crossdata.test.SharedXDContextTest
import org.apache.spark.sql.types._

// TODO: WARNING It is only valid for HazelcastCatalog until we create the proper plan to make it generic. (null!!)
// TODO use SharedSparkContext instead of SharedXDContext
trait XDTemporaryCatalogTests extends SharedXDContextTest with CatalogConstants {

  def catalogName: String

  def temporaryCatalog: XDTemporaryCatalog

  implicit var implicitContext: XDContext = _

  s"${catalogName}CatalogSpec" must "return a dataframe from a register table without catalog using json datasource" in {
    val fields = Seq[StructField](Field1, Field2)
    val columns = StructType(fields)
    val opts = Map("path" -> "/fake_path")
    val tableIdentifier = TableIdentifier(TableName)
    val crossdataTable = CrossdataTable(tableIdentifier.table, tableIdentifier.database, Some(Columns), SourceDatasource, Array.empty, opts)

    temporaryCatalog.relation(tableIdentifier) shouldBe empty

    temporaryCatalog.saveTable(tableIdentifier, null, Some(crossdataTable))

    temporaryCatalog.relation(tableIdentifier) shouldBe defined
  }

  it should s"register a table with catalog and partitionColumns in $catalogName" in {
    val tableIdentifier = TableIdentifier(TableName, Some(Database))
    val crossdataTable = CrossdataTable(TableName, Some(Database), Some(Columns), SourceDatasource, Array(Field1Name), OptsJSON)

    temporaryCatalog.saveTable(tableIdentifier, null, Some(crossdataTable))

    temporaryCatalog.relation(tableIdentifier) shouldBe defined

  }


  it should s"register a table with catalog and partitionColumns with multiple subdocuments as schema in $catalogName" in {
    temporaryCatalog.dropAllTables()
    val tableIdentifier = TableIdentifier(TableName, Some(Database))
    val crossdataTable = CrossdataTable(TableName, Some(Database), Some(ColumnsWithSubColumns), SourceDatasource, Array.empty, OptsJSON)

    temporaryCatalog.saveTable(tableIdentifier, null, Some(crossdataTable))

    temporaryCatalog.relation(tableIdentifier) shouldBe defined
  }


  it should "returns list of tables" in {
    temporaryCatalog.dropAllTables()
    val tableIdentifier1 = TableIdentifier(TableName, Some(Database))
    val tableIdentifier2 = TableIdentifier(TableName, None)

    val crossdataTable1 = CrossdataTable(tableIdentifier1.table, tableIdentifier1.database, Some(Columns), SourceDatasource, Array(Field1Name), OptsJSON)
    val crossdataTable2 = CrossdataTable(tableIdentifier2.table, tableIdentifier2.database, Some(Columns), SourceDatasource, Array(Field1Name), OptsJSON)

    temporaryCatalog.saveTable(tableIdentifier1, null, Some(crossdataTable1))
    temporaryCatalog.saveTable(tableIdentifier2, null, Some(crossdataTable2))

    val tables = temporaryCatalog.allRelations(Some(Database))
    tables should have length 1

    val tables2 = temporaryCatalog.allRelations()
    tables2 should have length 2
  }

  it should "not unregister tables that not exist" ignore {
    temporaryCatalog.dropAllTables()

    val tableIdentifier = TableIdentifier(TableName, Some(Database))

    a[RuntimeException] shouldBe thrownBy {
      temporaryCatalog.dropTable(tableIdentifier)
    }
  }

  it should s"unregister view" in {
    val viewIdentifier = TableIdentifier(ViewName, Option(Database))
    val plan = new LocalRelation(Seq.empty)
    temporaryCatalog.saveView(viewIdentifier, plan, Some(sqlView))
    temporaryCatalog.dropView(viewIdentifier)
    temporaryCatalog.relation(viewIdentifier) shouldBe empty
  }


  it should s"not unregister view that not exists " ignore { // TODO Modify temporary relations' behaviour
    a[RuntimeException] shouldBe thrownBy {
      val viewIdentifier = TableIdentifier(ViewName, Option(Database))
      temporaryCatalog.dropView(viewIdentifier)
    }

  }

  protected override def beforeAll(): Unit = {
    super.beforeAll()
    implicitContext = _xdContext
  }
}
