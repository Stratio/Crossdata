/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.crossdata.connector.mongodb


import com.mongodb.{BasicDBObject, QueryBuilder}
import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.commons.MongoDBObject
import com.stratio.common.utils.components.logger.impl.SparkLoggerComponent
import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.crossdata.test.SharedXDContextWithDataTest
import org.apache.spark.sql.crossdata.test.SharedXDContextWithDataTest.SparkTable
import org.scalatest.Suite

import scala.util.Try

trait MongoWithSharedContext extends SharedXDContextWithDataTest with MongoDefaultConstants with SparkLoggerComponent {
  this: Suite =>

  override type ClientParams = MongoClient
  override val provider: String = SourceProvider
  override def defaultOptions = Map(
    "host" -> s"$MongoHost:$MongoPort",
    "database" -> s"$Database",
    "collection" -> s"$Collection"
  )

  override protected def saveTestData: Unit = {
    val client = this.client.get

    val collection = client(Database)(Collection)
    for (a <- 1 to 10) {
      collection.insert {
        MongoDBObject("id" -> a,
          "age" -> (10 + a),
          "description" -> s"description$a",
          "enrolled" -> (a % 2 == 0),
          "name" -> s"Name $a"
        )
      }
      collection.update(QueryBuilder.start("id").greaterThan(4).get, MongoDBObject(("$set", MongoDBObject(("optionalField", true)))), multi = true)
    }

  }

  override protected def terminateClient: Unit = client.foreach(_.close)

  override protected def cleanTestData: Unit = {
    val client = this.client.get
    client(Database).dropDatabase()
  }

  override protected def prepareClient: Option[ClientParams] = Try {
    MongoClient(MongoHost, MongoPort)
  } toOption

  override def sparkRegisterTableSQL: Seq[SparkTable] = super.sparkRegisterTableSQL :+
    str2sparkTableDesc(s"CREATE TEMPORARY TABLE $Collection (id BIGINT, age INT, description STRING, enrolled BOOLEAN, name STRING, optionalField BOOLEAN)")

  override val runningError: String = "MongoDB and Spark must be up and running"

  val UnregisteredCollection = "unregistered"

}

sealed trait MongoDefaultConstants {
  val Database = "highschool"

  //Collections
  val Collection = "students"
  val DataTypesCollection = "studentsTestDataTypes"

  //Config
  val MongoHost: String = {
    Try(ConfigFactory.load().getStringList("mongo.hosts")).map(_.get(0)).getOrElse("127.0.0.1")
  }
  val MongoPort = 27017
  val SourceProvider = "com.stratio.crossdata.connector.mongodb"

  // Types supported in MongoDB for write decimal
  val decimalInt = 10
  val decimalLong = 10l
  val decimalDouble = 10.0
  val decimalFloat = 10f

  // Numeric types
  val float = 1.5f
  val tinyint= 127 // Mongo store it like Byte
  val smallint = 32767

  val byte = Byte.MaxValue
  val binary = Array(byte, byte)
  val date = new java.sql.Date(100000000)

  // Arrays
  val arrayint = Seq(1,2,3)
  val arraystring = Seq("a","b","c")
  val arraystruct = Seq(MongoDBObject("field1" -> 1, "field2" -> 2))
  val arraystructwithdate = Seq(MongoDBObject("field1" -> date ,"field2" -> 3))

  // Map
  val mapintint = new BasicDBObject("1",1).append("2",2)
  val mapstringint = new BasicDBObject("1",1).append("2",2)
  val mapstringstring = new BasicDBObject("1","1").append("2","2")
  val mapstruct = new BasicDBObject("mapstruct", MongoDBObject("structField1" -> date ,"structField2" -> 3))

  // Struct
  val struct = MongoDBObject("field1" -> 2 ,"field2" -> 3)
  val structofstruct = MongoDBObject("field1" -> date ,"field2" -> 3, "struct1" -> MongoDBObject("structField1"-> "structfield1", "structField2" -> 2))

  // Complex compositions
  val arraystructarraystruct = Seq(
    MongoDBObject(
      "stringfield" -> "aa",
      "arrayfield" -> Seq(MongoDBObject("field1" -> 1, "field2" -> 2), MongoDBObject("field1" -> -1, "field2" -> -2))
    ),
    MongoDBObject(
      "stringfield" -> "bb",
      "arrayfield" -> Seq(MongoDBObject("field1" -> 11, "field2" -> 22)
      )
    )
  )

}