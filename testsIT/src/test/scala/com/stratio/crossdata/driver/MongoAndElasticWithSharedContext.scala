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
package com.stratio.crossdata.driver

import com.mongodb.casbah.MongoClient
import com.sksamuel.elastic4s.ElasticClient
import com.stratio.common.utils.components.logger.impl.SparkLoggerComponent
import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.crossdata.test.SharedXDContextTest
import org.elasticsearch.common.settings.ImmutableSettings
import org.scalatest.BeforeAndAfterAll

import scala.util.Try

class MongoAndElasticWithSharedContext extends SharedXDContextTest with Constants with BeforeAndAfterAll with SparkLoggerComponent {

  lazy val mongoClient: MongoClient = MongoClient(MongoHost, MongoPort)

  lazy val elasticClient: ElasticClient = Try {
    logInfo(s"Connection to elastic search, ElasticHost: $ElasticHost, ElasticNativePort:$ElasticNativePort, ElasticClusterName $ElasticClusterName")
    val settings = ImmutableSettings.settingsBuilder().put("cluster.name", ElasticClusterName).build()
    val elasticClient = ElasticClient.remote(settings, ElasticHost, ElasticNativePort)
    elasticClient
  } get

  override protected def afterAll(): Unit = {
    Try(elasticClient.close())
    _xdContext.dropAllTables()
    super.afterAll()
  }


}

sealed trait Constants {

  //Config
  private lazy val config = ConfigFactory.load()

  //Mongo
  val MongoHost: String = {
    Try(config.getStringList("mongo.hosts")).map(_.get(0)).getOrElse("127.0.0.1")
  }
  val MongoPort = 27017
  val MongoSourceProvider = "com.stratio.crossdata.connector.mongodb"

  //Elastic
  val ElasticHost: String = Try(config.getStringList("elasticsearch.hosts")).map(_.get(0)).getOrElse("127.0.0.1")
  val ElasticRestPort = 9200
  val ElasticNativePort = 9300
  val ElasticSourceProvider = "com.stratio.crossdata.connector.elasticsearch"
  val ElasticClusterName: String = Try(config.getString("elasticsearch.cluster")).getOrElse("esCluster")


}