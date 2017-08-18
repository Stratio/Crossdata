/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package org.apache.spark.sql.crossdata.catalog

import com.typesafe.config.{Config, ConfigFactory, ConfigValueFactory}
import org.apache.spark.sql.SQLConf
import org.apache.spark.sql.catalyst.TableIdentifier
import org.apache.spark.sql.catalyst.plans.logical.{LogicalPlan, OneRowRelation}
import org.apache.spark.sql.crossdata.catalog.XDCatalog.CrossdataTable
import org.apache.spark.sql.crossdata.catalog.interfaces.XDCatalogCommon
import org.apache.spark.sql.crossdata.catalog.persistent.DerbyCatalogIT
import org.apache.spark.sql.execution.datasources.LogicalRelation
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class InsensitiveCatalogIT extends DerbyCatalogIT {

  override val coreConfig : Option[Config] =
    Some(ConfigFactory.empty().withValue(s"config.${SQLConf.CASE_SENSITIVE.key}", ConfigValueFactory.fromAnyRef(false)))


  it should s"persist a table and retrieve it changing some letters to upper case in $catalogName" in {

    val tableNameOriginal = "TableNameInsensitive"
    import XDCatalogCommon._
    val tableIdentifier = TableIdentifier(tableNameOriginal, Some(Database))
    val tableNormalized = tableIdentifier.normalize
    val crossdataTable = CrossdataTable(tableNormalized, Some(Columns), SourceDatasource, Array[String](Field1Name), OptsJSON)

    xdContext.catalog.persistTable(crossdataTable, OneRowRelation)
    xdContext.catalog.tableExists(tableIdentifier) shouldBe true

    val tableNameOriginal2 = "tablenameinsensitive"
    val tableIdentifier2 = TableIdentifier(tableNameOriginal2, Some(Database))
    xdContext.catalog.tableExists(tableIdentifier2) shouldBe true

    val tableNameOriginal3 = "TABLENAMEINSENSITIVE"
    val tableIdentifier3 = TableIdentifier(tableNameOriginal2, Some(Database))
    xdContext.catalog.tableExists(tableIdentifier3) shouldBe true

  }

}
