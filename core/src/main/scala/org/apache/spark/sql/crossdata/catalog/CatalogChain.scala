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

import com.stratio.common.utils.components.logger.impl.SparkLoggerComponent
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan
import org.apache.spark.sql.catalyst.{CatalystConf, TableIdentifier}
import org.apache.spark.sql.crossdata.catalog.XDCatalog.{CrossdataApp, CrossdataTable, ViewIdentifier}
import org.apache.spark.sql.crossdata.catalog.interfaces.{XDCatalogCommon, XDPersistentCatalog, XDStreamingCatalog, XDTemporaryCatalog}
import org.apache.spark.sql.crossdata.models.{EphemeralQueryModel, EphemeralStatusModel, EphemeralTableModel}


object CatalogChain {
  def apply(catalogs: XDCatalogCommon*)(conf: CatalystConf): CatalogChain = {
    val temporaryCatalogs = catalogs.collect { case a: XDTemporaryCatalog => a }
    val persistentCatalogs = catalogs.collect { case a: XDPersistentCatalog => a }
    val streamingCatalogs = catalogs.collect { case a: XDStreamingCatalog => a }
    require(streamingCatalogs.length <= 1, "Only one streaming catalog can be included")
    require(
      temporaryCatalogs.headOption.orElse(persistentCatalogs.headOption).isDefined,
      "At least one catalog (temporary or persistent ) must be included"
    )
    new CatalogChain(temporaryCatalogs, persistentCatalogs, streamingCatalogs.headOption)(conf)
  }
}

/*
  Write through (always true for this class)-> Each write is synchronously done to all catalogs in the chain
  No-Write allocate (always true) -> A miss at levels 0...i-1,i isn't written to these levels when found at level i+1
 */
private[crossdata] class CatalogChain private(val temporaryCatalogs: Seq[XDTemporaryCatalog],
                                              val persistentCatalogs: Seq[XDPersistentCatalog],
                                              val streamingCatalogs: Option[XDStreamingCatalog]
                                               )(override val conf: CatalystConf) extends XDCatalog with SparkLoggerComponent {

  import XDCatalogCommon._

  private val catalogs: Seq[XDCatalogCommon] = temporaryCatalogs ++: persistentCatalogs ++: streamingCatalogs.toSeq

  private implicit def crossdataTable2tableIdentifier(xdTable: CrossdataTable): TableIdentifier =
    TableIdentifier(xdTable.tableName, xdTable.dbName)

  private def chainedLookup[R](lookup: XDCatalogCommon => Option[R]): Option[R] =
    catalogs.view map lookup collectFirst {
      case Some(res) => res
    }


  private def persistentChainedLookup[R](lookup: XDPersistentCatalog => Option[R]): Option[R] =
    persistentCatalogs.view map lookup collectFirst {
      case Some(res) => res
    }

  /**
   * TemporaryCatalog
   */

  override def registerView(viewIdentifier: ViewIdentifier, logicalPlan: LogicalPlan, sql: Option[String]): Unit =
    temporaryCatalogs.foreach(_.saveView(viewIdentifier, logicalPlan, sql))

  // TODO throw an exception if there is no temp catalogs! Review CatalogChain
  override def registerTable(tableIdent: ViewIdentifier, plan: LogicalPlan, crossdataTable: Option[CrossdataTable]): Unit =
    temporaryCatalogs.foreach(_.saveTable(tableIdent, plan, crossdataTable))

  override def unregisterView(viewIdentifier: ViewIdentifier): Unit =
    temporaryCatalogs.foreach(_.dropView(viewIdentifier))

  override def unregisterTable(tableIdent: TableIdentifier): Unit =
    temporaryCatalogs.foreach(_.dropTable(tableIdent))

  override def unregisterAllTables(): Unit =
    temporaryCatalogs.foreach(_.dropAllTables())


  /**
   * CommonCatalog
   */

  private def lookupRelationOpt(tableIdent: TableIdentifier, alias: Option[String] = None): Option[LogicalPlan] =
    chainedLookup(_.relation(tableIdent, alias))

  override def lookupRelation(tableIdent: TableIdentifier, alias: Option[String]): LogicalPlan =
    lookupRelationOpt(tableIdent, alias) getOrElse {
      log.debug(s"Relation not found: ${tableIdent.unquotedString}")
      sys.error(s"Relation not found: ${tableIdent.unquotedString}")
    }

  override def tableExists(tableIdent: TableIdentifier): Boolean =
    lookupRelationOpt(tableIdent).isDefined

  // TODO streaming tables
  override def getTables(databaseName: Option[String]): Seq[(String, Boolean)] = {
    def getRelations(catalogSeq: Seq[XDCatalogCommon], isTemporary: Boolean): Seq[(String, Boolean)] = {
      catalogSeq.flatMap { cat =>
        cat.allRelations(databaseName).map(normalizeTableName(_, conf) -> isTemporary)
      }
    }
    getRelations(temporaryCatalogs, isTemporary = true) ++ getRelations(persistentCatalogs, isTemporary = false)
  }

  /**
   * Check the connection to the set Catalog
   */
  override def checkConnectivity: Boolean = catalogs.forall(_.isAvailable)

  /**
   * ExternalCatalog
   */

  override def persistTable(crossdataTable: CrossdataTable, table: LogicalPlan): Unit =
    persistentCatalogs.foreach(_.saveTable(crossdataTable, table))

  override def persistView(tableIdentifier: ViewIdentifier, plan: LogicalPlan, sqlText: String): Unit =
    persistentCatalogs.foreach(_.saveView(tableIdentifier, plan, sqlText))

  override def dropTable(tableIdentifier: TableIdentifier): Unit = {
    val strTable = tableIdentifier.unquotedString
    if (!tableExists(tableIdentifier)) throw new RuntimeException(s"Table $strTable can't be deleted because it doesn't exist")
    logInfo(s"Deleting table $strTable from catalog")
    temporaryCatalogs foreach (_.dropTable(tableIdentifier))
    persistentCatalogs foreach (_.dropTable(tableIdentifier))
  }

  override def dropAllTables(): Unit = {
    dropAllViews()
    temporaryCatalogs foreach (_.dropAllTables())
    persistentCatalogs foreach (_.dropAllTables())
  }

  override def dropView(viewIdentifier: ViewIdentifier): Unit = {
    val strView = viewIdentifier.unquotedString
    if (lookupRelationOpt(viewIdentifier).isEmpty) throw new RuntimeException(s"View $strView can't be deleted because it doesn't exist")
    logInfo(s"Deleting view ${viewIdentifier.unquotedString} from catalog")
    temporaryCatalogs foreach (_.dropView(viewIdentifier))
    persistentCatalogs foreach (_.dropView(viewIdentifier))
  }

  override def dropAllViews(): Unit = {
    temporaryCatalogs foreach (_.dropAllViews())
    persistentCatalogs foreach (_.dropAllViews())
  }

  override def tableMetadata(tableIdentifier: TableIdentifier): Option[CrossdataTable] =
    persistentChainedLookup(_.lookupTable(tableIdentifier))

  override def refreshTable(tableIdent: TableIdentifier): Unit =
    persistentCatalogs.foreach(_.refreshCache(tableIdent))

  /**
   * StreamingCatalog
   */

  // Ephemeral Table Functions

  override def existsEphemeralTable(tableIdentifier: String): Boolean =
    getEphemeralTable(tableIdentifier).isDefined

  override def getEphemeralTable(tableIdentifier: String): Option[EphemeralTableModel] =
    executeWithStrCatalogOrNone(_.getEphemeralTable(tableIdentifier))


  override def createEphemeralTable(ephemeralTable: EphemeralTableModel): Either[String, EphemeralTableModel] =
    withStreamingCatalogDo(_.createEphemeralTable(ephemeralTable))


  override def dropEphemeralTable(tableIdentifier: String): Unit =
    withStreamingCatalogDo(_.dropEphemeralTable(tableIdentifier))

  override def getAllEphemeralTables: Seq[EphemeralTableModel] =
    executeWithStrCatalogOrEmptyList(_.getAllEphemeralTables)

  override def dropAllEphemeralTables(): Unit =
    withStreamingCatalogDo(_.dropAllEphemeralTables())

  // Ephemeral Queries Functions

  override def createEphemeralQuery(ephemeralQuery: EphemeralQueryModel): Either[String, EphemeralQueryModel] =
    withStreamingCatalogDo(_.createEphemeralQuery(ephemeralQuery))

  override def getEphemeralQuery(queryAlias: String): Option[EphemeralQueryModel] =
    executeWithStrCatalogOrNone(_.getEphemeralQuery(queryAlias))

  override def dropEphemeralQuery(queryAlias: String): Unit =
    withStreamingCatalogDo(_.dropEphemeralQuery(queryAlias))

  override def existsEphemeralQuery(queryAlias: String): Boolean =
    getEphemeralQuery(queryAlias).isDefined

  override def getAllEphemeralQueries: Seq[EphemeralQueryModel] =
    executeWithStrCatalogOrEmptyList(_.getAllEphemeralQueries)

  override def dropAllEphemeralQueries(): Unit =
    withStreamingCatalogDo(_.dropAllEphemeralQueries())


  // Ephemeral Status Functions

  override protected[crossdata] def getEphemeralStatus(tableIdentifier: String): Option[EphemeralStatusModel] =
    executeWithStrCatalogOrNone(_.getEphemeralStatus(tableIdentifier))

  override protected[crossdata] def getAllEphemeralStatuses: Seq[EphemeralStatusModel] =
    executeWithStrCatalogOrEmptyList(_.getAllEphemeralStatuses)

  override protected[crossdata] def dropEphemeralStatus(tableIdentifier: String): Unit =
    withStreamingCatalogDo(_.dropEphemeralStatus(tableIdentifier))

  override protected[crossdata] def dropAllEphemeralStatus(): Unit =
    withStreamingCatalogDo(_.dropAllEphemeralStatus())

  override protected[crossdata] def createEphemeralStatus(tableIdentifier: String, ephemeralStatusModel: EphemeralStatusModel): EphemeralStatusModel =
    withStreamingCatalogDo(_.createEphemeralStatus(tableIdentifier, ephemeralStatusModel))

  override protected[crossdata] def updateEphemeralStatus(tableIdentifier: String, status: EphemeralStatusModel): Unit =
    withStreamingCatalogDo(_.updateEphemeralStatus(tableIdentifier, status))

  // Utils
  private def withStreamingCatalogDo[R](streamingCatalogOperation: XDStreamingCatalog => R): R = {
    streamingCatalogs.map(streamingCatalogOperation).getOrElse {
      throw new RuntimeException("There is no streaming catalog")
    }
  }
  private def executeWithStrCatalogOrNone[R](streamingCatalogOperation: XDStreamingCatalog => Option[R]): Option[R] =
    streamingCatalogs.flatMap(streamingCatalogOperation)

  private def executeWithStrCatalogOrEmptyList[R](streamingCatalogOperation: XDStreamingCatalog => Seq[R]): Seq[R] =
    streamingCatalogs.toSeq.flatMap(streamingCatalogOperation)

  override def lookupApp(alias: String): Option[CrossdataApp] =
    persistentChainedLookup(_.getApp(alias))

  override def persistAppMetadata(crossdataApp: CrossdataApp): Unit =
    persistentCatalogs.foreach(_.saveAppMetadata(crossdataApp))
}
