/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.crossdata.connector.postgresql

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PostgresqlCreateExternalTableIT extends PostgresqlWithSharedContext {

  "The Postgresql connector" should "execute natively create a External Table" in {

    val tableName = "newtable"

    val createTableQueryString =
      s"""|CREATE EXTERNAL TABLE $postgresqlSchema.$tableName (
          |id Integer,
          |name String,
          |booleanFile Boolean,
          |timeTime Timestamp
          |)
          |USING $SourceProvider
          |OPTIONS (
          |url '$url',
          |primary_key_string 'id'
          |)
      """.stripMargin.replaceAll("\n", " ")

    try {
      //Experimentation
      sql(createTableQueryString).collect()

      //Expectations
      val table = xdContext.table(s" $postgresqlSchema.$tableName")
      table should not be null
      table.schema.fieldNames should contain("name")

      val resultSet = client.get._2.executeQuery(s"SELECT * FROM $postgresqlSchema.$tableName")
      resultSet.getMetaData.getColumnName(2) should be("name")

    } finally {
      client.get._2.execute(s"DROP TABLE $postgresqlSchema.$tableName")
    }
  }

  it should "execute natively create a External Table with no existing schema" in {
    val noExistingSchema = "newschema"
    val newTable = "othertable"

    val createTableQueryString =
      s"""|CREATE EXTERNAL TABLE $noExistingSchema.$newTable(
          |id Integer,
          |name String,
          |booleanFile Boolean,
          |timeTime Timestamp
          |)
          |USING $SourceProvider
          |OPTIONS (
          |url '$url',
          |primary_key 'id'
          |)
      """.stripMargin.replaceAll("\n", " ")

    try {
      //Experimentation
      sql(createTableQueryString).collect()

      //Expectations
      val table = xdContext.table(s"$noExistingSchema.$newTable")
      table should not be null
      table.schema.fieldNames should contain("name")
    }finally {
      //AFTER
      client.get._2.execute(s"DROP TABLE $noExistingSchema.$newTable")
      client.get._2.execute(s"DROP SCHEMA $noExistingSchema")
    }
  }

  it should "throw an exception executing create External Table without specify schema" in {
    val newTable = "othertable"

    val createTableQueryString =
      s"""|CREATE EXTERNAL TABLE $newTable(
          |id Integer,
          |name String,
          |booleanFile Boolean,
          |timeTime Timestamp
          |)
          |USING $SourceProvider
          |OPTIONS (
          |url '$url',
          |primary_key 'id'
          |)
      """.stripMargin.replaceAll("\n", " ")

    an [java.lang.IllegalArgumentException] shouldBe thrownBy(sql(createTableQueryString).collect())
  }

  it should "throw an exception executing create External Table when table already exists in Crossdata or Postgresql" in {
    val newTable = "othertable"

    val createTableQueryString =
      s"""|CREATE EXTERNAL TABLE $postgresqlSchema.$newTable(
          |id Integer,
          |name String,
          |booleanFile Boolean,
          |timeTime Timestamp
          |)
          |USING $SourceProvider
          |OPTIONS (
          |url '$url',
          |primary_key 'id'
          |)
      """.stripMargin.replaceAll("\n", " ")

    sql(createTableQueryString).collect()
    // TABLE exists in crossdata
    an [org.apache.spark.sql.AnalysisException] shouldBe thrownBy(sql(createTableQueryString).collect())

    val newTable2= "anothertable"
    client.get._2.execute(s"CREATE TABLE $postgresqlSchema.$newTable2 (id text, age integer, primary key (id))")
    //TABLE exists in postgresql
    val createTableQueryString2 =
      s"""|CREATE EXTERNAL TABLE $postgresqlSchema.$newTable2(
          |id Integer,
          |name String,
          |booleanFile Boolean,
          |timeTime Timestamp
          |)
          |USING $SourceProvider
          |OPTIONS (
          |url '$url',
          |primary_key 'id'
          |)
      """.stripMargin.replaceAll("\n", " ")

    an [RuntimeException] shouldBe thrownBy(sql(createTableQueryString2).collect())
  }

}
