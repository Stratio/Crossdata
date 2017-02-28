package org.apache.spark.sql.crossdata.catalyst.catalog.persistent.zookeeper.daos

import java.util.UUID

object DAOConstants {

  val PrefixPermantCatalogsConfig = "prefix" //crossdata-core.catalog.prefix
  val PrefixStreamingCatalogsConfigForActors = "prefix" //crossdata-core.streaming.catalog.zookeeper.prefix
  val PrefixStreamingCatalogsConfig = "zookeeper.prefix" //crossdata-core.streaming.catalog.zookeeper.prefix

  val BaseZKPath = "stratio/crossdata"
  val DatabasesPath = "databases"
  val TablesViewsPath = s"tablesviews"
  val PartitionsPath = s"partitions"
  val FuntionsPath = s"funtions"
  val AppsPath = "apps"
  val IndexesPath = "indexes"
  val EphemeralTablesPath = "ephemeraltables"
  val EphemeralTableStatusPath = "ephemeraltablestatus"
  val EphemeralQueriesPath = "ephemeralqueries"

  def createId: String = UUID.randomUUID.toString
}