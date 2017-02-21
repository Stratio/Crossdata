package org.apache.spark.sql.crossdata.catalyst.catalog.persistent.models

import org.apache.spark.sql.catalyst.catalog.CatalogTable
import org.apache.spark.sql.crossdata


case class TableModel private (
                       tableDefinition: CatalogTable,
                       version: String =  crossdata.CrossdataVersion
                     ) extends CatalogEntityModel

