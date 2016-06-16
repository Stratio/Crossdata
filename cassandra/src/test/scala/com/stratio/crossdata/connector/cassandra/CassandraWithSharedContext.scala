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
package com.stratio.crossdata.connector.cassandra

import com.datastax.driver.core.{Cluster, Session}
import com.stratio.common.utils.components.logger.impl.SparkLoggerComponent
import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.crossdata.test.SharedXDContextWithDataTest
import org.apache.spark.sql.crossdata.test.SharedXDContextWithDataTest.SparkTable
import org.scalatest.Suite

import scala.collection.immutable.ListMap
import scala.util.Try

trait CassandraWithSharedContext extends SharedXDContextWithDataTest
  with CassandraDefaultTestConstants
  with SparkLoggerComponent {

  this: Suite =>

  override type ClientParams = (Cluster, Session)
  override val provider: String = SourceProvider
  override val defaultOptions = Map(
    "table"    -> Table,
    "keyspace" -> Catalog,
    "cluster"  -> ClusterName,
    "pushdown" -> "true",
    "spark_cassandra_connection_host" -> CassandraHost
  )

  abstract override def saveTestData: Unit = {
    val session = client.get._2

    def stringifySchema(schema: Map[String, String]): String = schema.map(p => s"${p._1} ${p._2}").mkString(", ")

    session.execute(
      s"""CREATE KEYSPACE $Catalog WITH replication = {'class':'SimpleStrategy', 'replication_factor':1}
         |AND durable_writes = true;""".stripMargin.replaceAll("\n", " "))
    session.execute(
      s"""CREATE TABLE $Catalog.$Table (${stringifySchema(schema)},
         |PRIMARY KEY (${pk.mkString(", ")}))""".stripMargin.replaceAll("\n", " "))

    if(indexedColumn.nonEmpty){
      session.execute(s"""
                         |CREATE CUSTOM INDEX student_index ON $Catalog.$Table (name)
                         |USING 'com.stratio.cassandra.lucene.Index'
                         |WITH OPTIONS = {
                         | 'refresh_seconds' : '1',
                         | 'schema' : '{ fields : {comment  : {type : "text", analyzer : "english"}} }'
                         |}
      """.stripMargin.replaceAll("\n", " "))
    }

    /*for (a <- 1 to 10) {
      session.execute("INSERT INTO " + Catalog + "." + Table + s" (${schema.map(p => p._1).mkString(", ")}) VALUES " +
        "(" + a + ", " + (10 + a) + ", 'Comment " + a + "', " + (a % 2 == 0) + ", 'Name " + a + "')")
    }*/

    def insertRow(row: List[Any]): Unit = {
      session.execute(
        s"""INSERT INTO $Catalog.$Table(${schema.map(p => p._1).mkString(", ")})
           | VALUES (${row.mkString(", ")})""".stripMargin.replaceAll("\n", ""))
    }

    testData.foreach(insertRow(_))

    //This creates a new table in the keyspace which will not be initially registered at the Spark
    if(UnregisteredTable.nonEmpty){
      session.execute(
        s"""CREATE TABLE $Catalog.$UnregisteredTable (${stringifySchema(schema)},
            |PRIMARY KEY (${pk.mkString(", ")}))""".stripMargin.replaceAll("\n", " "))
    }

    super.saveTestData
  }

  override protected def terminateClient: Unit = {
    val (cluster, session) = client.get

    session.close()
    cluster.close()
  }

  override protected def cleanTestData: Unit = client.get._2.execute(s"DROP KEYSPACE $Catalog")

  override protected def prepareClient: Option[ClientParams] = Try {
    val cluster = Cluster.builder().addContactPoint(CassandraHost).build()
    (cluster, cluster.connect())
  } toOption

  abstract override def sparkRegisterTableSQL: Seq[SparkTable] = super.sparkRegisterTableSQL :+
    str2sparkTableDesc(s"CREATE TEMPORARY TABLE $Table")

  override val runningError: String = "Cassandra and Spark must be up and running"

}

sealed trait CassandraDefaultTestConstants {
  val ClusterName = "Test Cluster"
  val Catalog = "highschool"
  val Table = "students"
  val TypesTable = "datatypestablename"
  val UnregisteredTable = "teachers"
  val schema = ListMap("id" -> "int", "age" -> "int", "comment" -> "text", "enrolled" -> "boolean", "name" -> "text")
  val pk = "(id)" :: "age" :: "comment" :: Nil
  val indexedColumn = "name"

  val testData = (for (a <- 1 to 10) yield {
    a :: (10 + a) :: s"'Comment $a'" :: (a % 2 == 0) :: s"'Name $a'" :: Nil
  }).toList

  val CassandraHost: String = {
    Try(ConfigFactory.load().getStringList("cassandra.hosts")).map(_.get(0)).getOrElse("127.0.0.1")
  }
  val SourceProvider = "com.stratio.crossdata.connector.cassandra"
}