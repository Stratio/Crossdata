/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package org.apache.spark.sql.crossdata.execution.datasources

import java.nio.file.Paths

import org.apache.spark.sql.catalyst.{CatalystConf, TableIdentifier}
import org.apache.spark.sql.crossdata.catalog.XDCatalog.CrossdataTable
import org.apache.spark.sql.crossdata.catalog.interfaces.XDCatalogCommon._
import org.apache.spark.sql.crossdata.catalog.persistent.PersistentCatalogWithCache
import org.apache.spark.sql.crossdata.catalog.{CatalogChain, XDCatalog}
import org.apache.spark.sql.crossdata.test.SharedXDContextTest
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CreateTableIfNotExistsIT extends SharedXDContextTest {


  "CreateTableIfNotExists command" should "allow multiple creations of the table persisting just the first" in {
    _xdContext.sql(s"CREATE TABLE tabNameIfExists USING org.apache.spark.sql.json OPTIONS (path '${Paths.get(getClass.getResource("/foo.json").toURI).toString}')")

    an[Exception] should be thrownBy _xdContext.sql(s"CREATE TABLE tabNameIfExists USING org.apache.spark.sql.json OPTIONS (path '${Paths.get(getClass.getResource("/foo.json").toURI).toString}')")

    _xdContext.sql(s"CREATE TABLE IF NOT EXISTS tabNameIfExists USING org.apache.spark.sql.json OPTIONS (path '${Paths.get(getClass.getResource("/foo.json").toURI).toString}')")
    _xdContext.sql(s"CREATE TABLE IF NOT EXISTS tabNameIfExists USING org.apache.spark.sql.json OPTIONS (path '${Paths.get(getClass.getResource("/foo.json").toURI).toString}')")

    _xdContext.sql("DROP TABLE tabNameIfExists")
  }
  
}
