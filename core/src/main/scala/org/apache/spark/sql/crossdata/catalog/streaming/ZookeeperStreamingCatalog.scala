/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package org.apache.spark.sql.crossdata.catalog.streaming

import com.typesafe.config.Config
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan
import org.apache.spark.sql.catalyst.{CatalystConf, TableIdentifier}
import org.apache.spark.sql.crossdata.catalog.{StringNormalized, TableIdentifierNormalized}
import org.apache.spark.sql.crossdata.catalog.interfaces.{XDCatalogCommon, XDStreamingCatalog}
import org.apache.spark.sql.crossdata.catalyst.streaming.StreamingRelation
import org.apache.spark.sql.crossdata.config.CoreConfig
import org.apache.spark.sql.crossdata.daos.impl._
import org.apache.spark.sql.crossdata.models._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.Try

class ZookeeperStreamingCatalog(val catalystConf: CatalystConf, serverConfig: Config) extends XDStreamingCatalog {

  private[spark] val streamingConfig = serverConfig.getConfig(CoreConfig.StreamingConfigKey)
  private[spark] val ephemeralTableDAO =
    new EphemeralTableTypesafeDAO(streamingConfig.getConfig(CoreConfig.CatalogConfigKey))
  private[spark] val ephemeralQueriesDAO =
    new EphemeralQueriesTypesafeDAO(streamingConfig.getConfig(CoreConfig.CatalogConfigKey))
  private[spark] val ephemeralTableStatusDAO =
    new EphemeralTableStatusTypesafeDAO(streamingConfig.getConfig(CoreConfig.CatalogConfigKey))


  override def relation(tableIdent: TableIdentifierNormalized)(implicit sqlContext: SQLContext): Option[LogicalPlan] = {
    import XDCatalogCommon._
    val tableIdentifier: String = stringifyTableIdentifierNormalized(tableIdent)
    if (futurize(existsEphemeralTable(tableIdentifier)))
      Some(StreamingRelation(tableIdentifier))
    else
      None
  }

  // TODO
  override def isAvailable: Boolean = true

  // TODO It must not return the relations until the catalog can distinguish between real/ephemeral tables
  override def allRelations(databaseName: Option[StringNormalized]): Seq[TableIdentifierNormalized] = Seq.empty

  private def futurize[P](operation: => P): P =
    Await.result(Future(operation), 5 seconds)

  /**
   * Ephemeral Table Functions
   */
  override def existsEphemeralTable(tableIdentifier: String): Boolean =
    futurize(ephemeralTableDAO.dao.exists(tableIdentifier))

  override def getEphemeralTable(tableIdentifier: String): Option[EphemeralTableModel] =
    futurize(ephemeralTableDAO.dao.get(tableIdentifier))

  override def createEphemeralTable(ephemeralTable: EphemeralTableModel): Either[String, EphemeralTableModel] =
    if (!existsEphemeralTable(ephemeralTable.name)) {
      createEphemeralStatus(ephemeralTable.name, EphemeralStatusModel(ephemeralTable.name, EphemeralExecutionStatus.NotStarted))
      Right(ephemeralTableDAO.dao.upsert(ephemeralTable.name, ephemeralTable))
    }
    else Left("Ephemeral table exists")


  override def dropEphemeralTable(tableIdentifier: String): Unit = {
    val isRunning = ephemeralTableStatusDAO.dao.get(tableIdentifier).map { tableStatus =>
      tableStatus.status == EphemeralExecutionStatus.Started || tableStatus.status == EphemeralExecutionStatus.Starting
    } getOrElse notFound(tableIdentifier)

    if (isRunning) throw new RuntimeException("The ephemeral is running. The process should be stopped first using 'Stop <tableIdentifier>'")

    ephemeralTableDAO.dao.delete(tableIdentifier)
    ephemeralTableStatusDAO.dao.delete(tableIdentifier)

    ephemeralQueriesDAO.dao.getAll().filter(_.ephemeralTableName == tableIdentifier) foreach { query =>
      ephemeralQueriesDAO.dao.delete(query.alias)
    }
  }

  override def dropAllEphemeralTables(): Unit = {
    // TODO it should be improved after changing ephemeralTableDAO.dao.deleteAll
    Try {
      ephemeralTableDAO.dao.deleteAll
      ephemeralTableStatusDAO.dao.deleteAll
      ephemeralQueriesDAO.dao.deleteAll
    }
  }

  override def getAllEphemeralTables: Seq[EphemeralTableModel] =
    ephemeralTableDAO.dao.getAll()


  /**
   * Ephemeral Queries Functions
   */
  override def existsEphemeralQuery(queryAlias: String): Boolean =
    ephemeralQueriesDAO.dao.exists(queryAlias)

  override def createEphemeralQuery(ephemeralQuery: EphemeralQueryModel): Either[String, EphemeralQueryModel] =
    if (!existsEphemeralQuery(ephemeralQuery.alias))
      Right(ephemeralQueriesDAO.dao.upsert(ephemeralQuery.alias, ephemeralQuery))
    else Left("Ephemeral query exists")

  override def getEphemeralQuery(queryAlias: String): Option[EphemeralQueryModel] =
    ephemeralQueriesDAO.dao.get(queryAlias)

  override def getAllEphemeralQueries: Seq[EphemeralQueryModel] =
    ephemeralQueriesDAO.dao.getAll()

  override def dropEphemeralQuery(queryAlias: String): Unit =
    ephemeralQueriesDAO.dao.delete(queryAlias)

  override def dropAllEphemeralQueries(): Unit = ephemeralQueriesDAO.dao.deleteAll

  /**
   * Ephemeral Status Functions
   */
  override def createEphemeralStatus(tableIdentifier: String,
                                     ephemeralStatusModel: EphemeralStatusModel): EphemeralStatusModel =
    ephemeralTableStatusDAO.dao.upsert(tableIdentifier, ephemeralStatusModel)

  override def getEphemeralStatus(tableIdentifier: String): Option[EphemeralStatusModel] =
    ephemeralTableStatusDAO.dao.get(tableIdentifier)

  override def getAllEphemeralStatuses: Seq[EphemeralStatusModel] =
    ephemeralTableStatusDAO.dao.getAll()

  override def updateEphemeralStatus(tableIdentifier: String, status: EphemeralStatusModel): Unit =
    ephemeralTableStatusDAO.dao.update(tableIdentifier, status)

  override protected[crossdata] def dropEphemeralStatus(tableIdentifier: String): Unit =
    ephemeralTableStatusDAO.dao.delete(tableIdentifier)

  override protected[crossdata] def dropAllEphemeralStatus(): Unit =
    ephemeralTableStatusDAO.dao.deleteAll

}
