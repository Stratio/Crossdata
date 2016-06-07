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

import java.sql.{Connection, DriverManager, ResultSet}

import com.stratio.common.utils.components.logger.impl.SparkLoggerComponent
import org.apache.spark.sql.catalyst.{CatalystConf, SimpleCatalystConf, TableIdentifier}
import org.apache.spark.sql.crossdata._

import scala.annotation.tailrec


object DerbyCatalog {
  // TableMetadataFields
  val DatabaseField = "db"
  val TableNameField = "tableName"
  val SchemaField = "tableSchema"
  val DatasourceField = "datasource"
  val PartitionColumnField = "partitionColumn"
  val OptionsField = "options"
  val CrossdataVersionField = "crossdataVersion"
  // ViewMetadataFields (databaseField, tableNameField, sqlViewField, CrossdataVersionField
  val SqlViewField = "sqlView"
  //App values
  val JarPath = "jarPath"
  val AppAlias = "alias"
  val AppClass = "class"
}

/**
  * Default implementation of the [[catalog.XDCatalog]] with persistence using
  * Derby.
  *
  * @param conf An implementation of the [[CatalystConf]].
  */
class DerbyCatalog(override val conf: CatalystConf = new SimpleCatalystConf(true), xdContext: XDContext)
  extends XDCatalog(conf, xdContext) with SparkLoggerComponent {

  import DerbyCatalog._
  import XDCatalog._

  private val db = "CROSSDATA"
  private val tableWithTableMetadata = "xdtables"
  private val tableWithViewMetadata = "xdviews"
  private val tableWithAppJars = "appJars"

  @transient lazy val connection: Connection = {

    val driver = "org.apache.derby.jdbc.EmbeddedDriver"
    val url = "jdbc:derby:sampledb/crossdata;create=true"

    Class.forName(driver)
    val jdbcConnection = DriverManager.getConnection(url)

    // CREATE PERSISTENT METADATA TABLE

    if (!schemaExists(db, jdbcConnection)) {
      jdbcConnection.createStatement().executeUpdate(s"CREATE SCHEMA $db")


      jdbcConnection.createStatement().executeUpdate(
        s"""|CREATE TABLE $db.$tableWithTableMetadata (
            |$DatabaseField VARCHAR(50),
            |$TableNameField VARCHAR(50),
            |$SchemaField LONG VARCHAR,
            |$DatasourceField LONG VARCHAR,
            |$PartitionColumnField LONG VARCHAR,
            |$OptionsField LONG VARCHAR,
            |$CrossdataVersionField LONG VARCHAR,
            |PRIMARY KEY ($DatabaseField,$TableNameField))""".stripMargin)

      jdbcConnection.createStatement().executeUpdate(
        s"""|CREATE TABLE $db.$tableWithViewMetadata (
            |$DatabaseField VARCHAR(50),
            |$TableNameField VARCHAR(50),
            |$SqlViewField LONG VARCHAR,
            |$CrossdataVersionField VARCHAR(30),
            |PRIMARY KEY ($DatabaseField,$TableNameField))""".stripMargin)

      jdbcConnection.createStatement().executeUpdate(
        s"""|CREATE TABLE $db.$tableWithAppJars (
            |$JarPath VARCHAR(100),
            |$AppAlias VARCHAR(50),
            |$AppClass VARCHAR(100),
            |PRIMARY KEY ($AppAlias))""".stripMargin)
    }

    jdbcConnection
  }


  override def lookupTable(tableIdentifier: TableIdentifier): Option[CrossdataTable] = {

    val resultSet = selectMetadata(tableWithTableMetadata, tableIdentifier)

    if (!resultSet.next) {
      None
    } else {

      val database = resultSet.getString(DatabaseField)
      val table = resultSet.getString(TableNameField)
      val schemaJSON = resultSet.getString(SchemaField)
      val partitionColumn = resultSet.getString(PartitionColumnField)
      val datasource = resultSet.getString(DatasourceField)
      val optsJSON = resultSet.getString(OptionsField)
      val version = resultSet.getString(CrossdataVersionField)

      Some(
        CrossdataTable(table, Some(database), Option(deserializeUserSpecifiedSchema(schemaJSON)), datasource,
          deserializePartitionColumn(partitionColumn), deserializeOptions(optsJSON), version)
      )
    }
  }

  override def lookupApp(alias: String): Option[CrossdataApp] = {

    val preparedStatement = connection.prepareStatement(s"SELECT * FROM $db.$tableWithAppJars WHERE $AppAlias= ?")
    preparedStatement.setString(1, alias)
    val resultSet: ResultSet = preparedStatement.executeQuery()

    if (!resultSet.next) {
      closeJDBCStatements(preparedStatement, resultSet)
      None
    } else {
      val jar = resultSet.getString(JarPath)
      val alias = resultSet.getString(AppAlias)
      val clss = resultSet.getString(AppClass)

      closeJDBCStatements(preparedStatement, resultSet)
      Some(
        CrossdataApp(jar, alias, clss)
      )
    }
  }

  override def listPersistedTables(databaseName: Option[String]): Seq[(String, Boolean)] = {
    @tailrec
    def getSequenceAux(resultset: ResultSet, next: Boolean, set: Set[String] = Set()): Set[String] = {
      if (next) {
        val database = resultset.getString(DatabaseField)
        val table = resultset.getString(TableNameField)
        val tableId = if (database.trim.isEmpty) table else s"$database.$table"
        getSequenceAux(resultset, resultset.next(), set + tableId)
      } else {
        resultset.close()
        set
      }
    }

    val statement = connection.createStatement
    val dbFilter = databaseName.fold("")(dbName => s"WHERE $DatabaseField ='$dbName'")
    val resultSet = statement.executeQuery(s"SELECT $DatabaseField, $TableNameField FROM $db.$tableWithTableMetadata $dbFilter")

    getSequenceAux(resultSet, resultSet.next).map(tableId => (tableId, false)).toSeq

  }

  override def persistTableMetadata(crossdataTable: CrossdataTable): Unit =
    try {

      val tableSchema = serializeSchema(crossdataTable.schema.getOrElse(requireSchema()))
      val tableOptions = serializeOptions(crossdataTable.opts)
      val partitionColumn = serializePartitionColumn(crossdataTable.partitionColumn)

      connection.setAutoCommit(false)

      // check if the database-table exist in the persisted catalog
      val resultSet = selectMetadata(tableWithTableMetadata, TableIdentifier(crossdataTable.tableName, crossdataTable.dbName))

      if (!resultSet.next()) {
        val prepped = connection.prepareStatement(
          s"""|INSERT INTO $db.$tableWithTableMetadata (
              | $DatabaseField, $TableNameField, $SchemaField, $DatasourceField, $PartitionColumnField, $OptionsField, $CrossdataVersionField
              |) VALUES (?,?,?,?,?,?,?)
       """.stripMargin)
        prepped.setString(1, crossdataTable.dbName.getOrElse(""))
        prepped.setString(2, crossdataTable.tableName)
        prepped.setString(3, tableSchema)
        prepped.setString(4, crossdataTable.datasource)
        prepped.setString(5, partitionColumn)
        prepped.setString(6, tableOptions)
        prepped.setString(7, CrossdataVersion)
        prepped.execute()

        closeJDBCStatements(prepped, resultSet)
      }
      else {
        val prepped = connection.prepareStatement(
          s"""|UPDATE $db.$tableWithTableMetadata SET $SchemaField=?, $DatasourceField=?,$PartitionColumnField=?,$OptionsField=?,$CrossdataVersionField=?
              |WHERE $DatabaseField='${crossdataTable.dbName.getOrElse("")}' AND $TableNameField='${crossdataTable.tableName}'
       """.stripMargin)
        prepped.setString(1, tableSchema)
        prepped.setString(2, crossdataTable.datasource)
        prepped.setString(3, partitionColumn)
        prepped.setString(4, tableOptions)
        prepped.setString(5, CrossdataVersion)
        prepped.execute()

        closeJDBCStatements(prepped, resultSet)
      }
      connection.commit()

    } finally {
      connection.setAutoCommit(true)
    }

  override def dropPersistedTable(tableIdentifier: TableIdentifier): Unit = {

    connection.createStatement.executeUpdate(
      s"DELETE FROM $db.$tableWithTableMetadata WHERE tableName='${tableIdentifier.table}' AND db='${tableIdentifier.database.getOrElse("")}'")
  }

  override def dropAllPersistedTables(): Unit = {
    connection.createStatement.executeUpdate(s"DELETE FROM $db.$tableWithTableMetadata")
    connection.createStatement.executeUpdate(s"DELETE FROM $db.$tableWithViewMetadata")
  }

  private def schemaExists(schema: String, connection: Connection): Boolean = {
    val preparedStatement = connection.prepareStatement(s"SELECT * FROM SYS.SYSSCHEMAS WHERE schemaname='$db'")
    val resultSet = preparedStatement.executeQuery()

    resultSet.next()

  }

  override protected def lookupView(tableIdentifier: TableIdentifier): Option[String] = {

    val resultSet = selectMetadata(tableWithViewMetadata, tableIdentifier)

    if (!resultSet.next) {
      resultSet.close()
      None
    } else {
      val res=Option(resultSet.getString(SqlViewField))
      resultSet.close()
      res
    }

  }


  override protected[crossdata] def persistViewMetadata(tableIdentifier: TableIdentifier, sqlText: String): Unit =
    try {
      connection.setAutoCommit(false)

      val resultSet = selectMetadata(tableWithViewMetadata, tableIdentifier)

      if (!resultSet.next()) {
        val prepped = connection.prepareStatement(
          s"""|INSERT INTO $db.$tableWithViewMetadata (
              | $DatabaseField, $TableNameField, $SqlViewField, $CrossdataVersionField
              |) VALUES (?,?,?,?)
         """.stripMargin)
        prepped.setString(1, tableIdentifier.database.getOrElse(""))
        prepped.setString(2, tableIdentifier.table)
        prepped.setString(3, sqlText)
        prepped.setString(4, CrossdataVersion)
        prepped.execute()
        closeJDBCStatements(prepped, resultSet)
      } else {
        val prepped = connection.prepareStatement(
          s"""|UPDATE $db.$tableWithViewMetadata SET $SqlViewField=?
              |WHERE $DatabaseField='${tableIdentifier.database.getOrElse("")}' AND $TableNameField='${tableIdentifier.table}'
         """.stripMargin)
        prepped.setString(1, sqlText)
        prepped.execute()
        closeJDBCStatements(prepped, resultSet)
      }
      connection.commit()
    } finally {
      connection.setAutoCommit(true)
    }


  override def persistAppMetadata(crossdataApp: CrossdataApp): Unit =
    try {
      connection.setAutoCommit(false)

      val preparedStatement = connection.prepareStatement(s"SELECT * FROM $db.$tableWithAppJars WHERE $AppAlias= ?")
      preparedStatement.setString(1, crossdataApp.appAlias)
      val resultSet = preparedStatement.executeQuery()

      if (!resultSet.next()) {
        val prepped = connection.prepareStatement(
          s"""|INSERT INTO $db.$tableWithAppJars (
              | $JarPath, $AppAlias, $AppClass
              |) VALUES (?,?,?)
         """.stripMargin)
        prepped.setString(1, crossdataApp.jar)
        prepped.setString(2, crossdataApp.appAlias)
        prepped.setString(3, crossdataApp.appClass)
        prepped.execute()
      } else {
        val prepped = connection.prepareStatement(
          s"""|UPDATE $db.$tableWithAppJars SET $JarPath=?, $AppClass=?
              |WHERE $AppAlias='${crossdataApp.appAlias}'
         """.stripMargin)
        prepped.setString(1, crossdataApp.jar)
        prepped.setString(2, crossdataApp.appClass)
        prepped.execute()

        closeJDBCStatements(prepped, resultSet)
      }
      connection.commit()
    } finally {
      connection.setAutoCommit(true)
    }

  private def selectMetadata(targetTable: String, tableIdentifier: TableIdentifier): ResultSet = {

    val preparedStatement = connection.prepareStatement(s"SELECT * FROM $db.$targetTable WHERE $DatabaseField= ? AND $TableNameField= ?")
    preparedStatement.setString(1, tableIdentifier.database.getOrElse(""))
    preparedStatement.setString(2, tableIdentifier.table)
    preparedStatement.executeQuery()


  }

  override protected def dropPersistedView(viewIdentifier: ViewIdentifier): Unit = {
    connection.createStatement.executeUpdate(
      s"DELETE FROM $db.$tableWithViewMetadata WHERE tableName='${viewIdentifier.table}' AND db='${viewIdentifier.database.getOrElse("")}'")
  }

  override protected def dropAllPersistedViews(): Unit = {
    connection.createStatement.executeUpdate(s"DELETE FROM $db.$tableWithViewMetadata")
  }

  override def checkConnectivity: Boolean = true


}