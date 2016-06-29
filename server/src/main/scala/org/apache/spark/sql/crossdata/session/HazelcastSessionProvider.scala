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
package org.apache.spark.sql.crossdata.session

import com.hazelcast.config.{GroupConfig, XmlConfigBuilder, Config => HazelcastConfig}
import com.hazelcast.core.Hazelcast
import com.typesafe.config.Config
import org.apache.log4j.Logger
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLConf
import org.apache.spark.sql.catalyst.{CatalystConf, SimpleCatalystConf}
import org.apache.spark.sql.crossdata.XDSessionProvider.SessionID
import org.apache.spark.sql.crossdata._
import org.apache.spark.sql.crossdata.catalog.interfaces.{XDPersistentCatalog, XDStreamingCatalog, XDTemporaryCatalog}
import org.apache.spark.sql.crossdata.catalog.utils.CatalogUtils
import org.apache.spark.sql.crossdata.config.CoreConfig

import scala.util.{Failure, Success, Try}

object HazelcastSessionProvider {

  val SqlConfMapId = "sqlconfmap"
  val HazelcastCatalogMapId = "hazelcatalogmap"

  def checkNotNull[T]: T => Try[T] =
    a => Option(a).map(Success(_)).getOrElse(Failure(new RuntimeException(s"Map not found")))

}

class HazelcastSessionProvider( @transient sc: SparkContext,
                                userConfig: Config
                                ) extends XDSessionProvider(sc, Option(userConfig)) with CoreConfig { // TODO CoreConfig should not be a trait

  import HazelcastSessionProvider._
  import XDSharedState._

  override lazy val logger = Logger.getLogger(classOf[HazelcastSessionProvider])

  private lazy val catalogConfig = config.getConfig(CoreConfig.CatalogConfigKey)

  protected lazy val catalystConf: CatalystConf = {
    import CoreConfig.CaseSensitive
    val caseSensitive: Boolean = catalogConfig.getBoolean(CaseSensitive)
    new SimpleCatalystConf(caseSensitive)
  }

  @transient
  protected lazy val externalCatalog: XDPersistentCatalog = CatalogUtils.externalCatalog(catalystConf, catalogConfig)

  @transient
  protected lazy val streamingCatalog: Option[XDStreamingCatalog] = CatalogUtils.streamingCatalog(catalystConf, config)



  private val sharedState = new XDSharedState(sc, Option(userConfig), externalCatalog, streamingCatalog)

  private val hInstance = {
    // TODO it should only use Hazelcast.newHazelcastInstance() which internally creates a xmlConfig and the group shouldn't be hardcoded (blocked by CD)
    val xmlConfig = new XmlConfigBuilder().build()
    xmlConfig.setGroupConfig(new GroupConfig(scala.util.Properties.scalaPropOrElse("version.number", "unknown")))
    Hazelcast.newHazelcastInstance(xmlConfig)
  }

  private val sessionIDToSQLProps: java.util.Map[SessionID, Map[String,String]] = hInstance.getMap(SqlConfMapId)
  private val sessionIDToTempCatalogs: SessionManager[SessionID, Seq[XDTemporaryCatalog]] = new HazelcastSessionCatalogManager(hInstance, sharedState.sqlConf)

  override def newSession(sessionID: SessionID): Try[XDSession] =
    Try {
      val tempCatalogs = sessionIDToTempCatalogs.addSession(sessionID)
      sessionIDToSQLProps.put(sessionID, sharedState.sparkSQLProps)

      buildSession(sharedState.sqlConf, tempCatalogs)
    }

  override def closeSession(sessionID: SessionID): Try[Unit] =
    for {
      _ <- checkNotNull(sessionIDToSQLProps.remove(sessionID))
      _ <- sessionIDToTempCatalogs.removeSession(sessionID)
    } yield ()


  // TODO take advantage of common utils pattern?
  override def session(sessionID: SessionID): Try[XDSession] =
    for {
      tempCatalogMap <- sessionIDToTempCatalogs.getSession(sessionID)
      configMap <- checkNotNull(sessionIDToSQLProps.get(sessionID))
      sess <- Try(buildSession(configMap, tempCatalogMap))
    } yield {
      sess
    }


  override def close(): Unit = {
    sessionIDToTempCatalogs.clearAllSessions()
    sessionIDToSQLProps.clear()
    hInstance.shutdown()
  }


  private def buildSession(sqlConf: SQLConf, xDTemporaryCatalogs: Seq[XDTemporaryCatalog]): XDSession = {
    val sessionState = new XDSessionState(sqlConf, xDTemporaryCatalogs)
    new XDSession(sharedState, sessionState)
  }

}
