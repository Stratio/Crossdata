/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package org.apache.spark.sql.crossdata.util

import com.stratio.common.utils.components.logger.impl.SparkLoggerComponent
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.crossdata.catalog.XDCatalog
import XDCatalog.CrossdataTable
import org.apache.spark.sql.execution.datasources.{LogicalRelation, ResolvedDataSource}
import org.apache.spark.sql.sources.{HadoopFsRelationProvider, RelationProvider, SchemaRelationProvider}

object CreateRelationUtil extends SparkLoggerComponent{

  protected[crossdata] def createLogicalRelation(sqlContext: SQLContext, crossdataTable: CrossdataTable): LogicalRelation = {

    /** Although table schema is inferred and persisted in XDCatalog, the schema can't be specified in some cases because
      *the source does not implement SchemaRelationProvider (e.g. JDBC) */

    val tableSchema = ResolvedDataSource.lookupDataSource(crossdataTable.datasource).newInstance() match {
      case _: SchemaRelationProvider | _: HadoopFsRelationProvider =>
        crossdataTable.schema
      case _: RelationProvider =>
        None
      case other =>
        val msg = s"Unexpected datasource: $other"
        logError(msg)
        throw new RuntimeException(msg)
    }

    val resolved = ResolvedDataSource(sqlContext, tableSchema, crossdataTable.partitionColumn, crossdataTable.datasource, crossdataTable.opts)
    LogicalRelation(resolved.relation)
  }

}
