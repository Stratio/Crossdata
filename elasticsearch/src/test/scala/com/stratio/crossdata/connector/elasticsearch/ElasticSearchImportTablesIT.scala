/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.crossdata.connector.elasticsearch

import com.sksamuel.elastic4s.ElasticDsl._
import org.apache.spark.sql.types._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ElasticSearchImportTablesIT extends ElasticWithSharedContext {


  // IMPORT OPERATIONS

  it should "import all tables from an index" in {
    assumeEnvironmentIsUpAndRunning
    def tableCountInHighschool: Long = sql("SHOW TABLES").count
    val initialLength = tableCountInHighschool
    xdContext.dropAllTables()

    val importQuery =
      s"""
         |IMPORT TABLES
         |USING $SourceProvider
          |OPTIONS (
          |es.nodes '$ElasticHost',
          |es.port '$ElasticRestPort',
          |es.nativePort '$ElasticNativePort',
          |es.cluster '$ElasticClusterName',
          |es.index '$Index'
          |)
      """.stripMargin
    //Experimentation
    sql(importQuery)

    //Expectations
    tableCountInHighschool should be (1)
    xdContext.tableNames() should contain (s"$Index.$Type")
  }

  it should "infer schema after import all tables from an Index" in {
    assumeEnvironmentIsUpAndRunning
    xdContext.dropAllTables()
    val importQuery =
      s"""
         |IMPORT TABLES
         |USING $SourceProvider
          |OPTIONS (
          |es.nodes '$ElasticHost',
          |es.port '$ElasticRestPort',
          |es.nativePort '$ElasticNativePort',
          |es.cluster '$ElasticClusterName',
          |es.index '$Index'
          |)
      """.stripMargin

    //Experimentation
    sql(importQuery)

    //Expectations
    xdContext.tableNames() should contain (s"$Index.$Type")
    val schema = xdContext.table(s"$Index.$Type").schema

    schema should have length 9

    schema("age").dataType shouldBe IntegerType
    schema("description").dataType shouldBe StringType
    schema("enrolled").dataType shouldBe BooleanType
    schema("salary").dataType shouldBe DoubleType
    schema("ageInMillis").dataType shouldBe LongType
    schema("birthday").dataType shouldBe TimestampType
    schema("team").dataType shouldBe a [StructType]

    val teamSchema = schema("team").dataType.asInstanceOf[StructType]
    teamSchema.fields should have length 2
    teamSchema("id").dataType shouldBe IntegerType
    teamSchema("name").dataType shouldBe StringType



  }

  it should "infer schema after import One table from an Index" in {
    assumeEnvironmentIsUpAndRunning
    xdContext.dropAllTables()

    ElasticSearchConnectionUtils.withClientDo(connectionOptions){ client =>
      client.execute { index into Index -> "NewMapping" fields {
        "name" -> "luis"
      }}

      val importQuery =
        s"""
           |IMPORT TABLES
           |USING $SourceProvider
           |OPTIONS (
           |es.nodes '$ElasticHost',
           |es.port '$ElasticRestPort',
           |es.nativePort '$ElasticNativePort',
           |es.cluster '$ElasticClusterName',
           |es.resource '$Index/$Type'
           |)
      """.stripMargin

      //Experimentation
      sql(importQuery)

      //Expectations
      xdContext.tableNames() should contain (s"$Index.$Type")
      xdContext.tableNames() should not contain s"$Index.NewMapping"
    }
  }

  it should "fail when infer schema with bad es.resource" in {
    assumeEnvironmentIsUpAndRunning
    xdContext.dropAllTables()

    ElasticSearchConnectionUtils.withClientDo(connectionOptions){ client =>
      client.execute { index into Index -> "NewMapping" fields {
        "name" -> "luis"
      }}

      val importQuery =
        s"""
           |IMPORT TABLES
           |USING $SourceProvider
           |OPTIONS (
           |es.nodes '$ElasticHost',
           |es.port '$ElasticRestPort',
           |es.nativePort '$ElasticNativePort',
           |es.cluster '$ElasticClusterName',
           |es.resource '$Type'
           |)
      """.stripMargin

      //Experimentation
      an [IllegalArgumentException] should be thrownBy sql(importQuery)
    }

  }

  it should "infer schema after import all tables from a Cluster" in {
    assumeEnvironmentIsUpAndRunning
    xdContext.dropAllTables()

    ElasticSearchConnectionUtils.withClientDo(connectionOptions){ client =>
      createIndex(client,"index_test", typeMapping())
      try {
        val importQuery =
          s"""
             |IMPORT TABLES
             |USING $SourceProvider
             |OPTIONS (
             |es.nodes '$ElasticHost',
             |es.port '$ElasticRestPort',
             |es.nativePort '$ElasticNativePort',
             |es.cluster '$ElasticClusterName'
             |)
      """.stripMargin

        //Experimentation:
        sql(importQuery)

        //Expectations
        sql("SHOW TABLES").count should be > 1l
        xdContext.tableNames().length should be > 1

      } finally {
        cleanTestData(client, "index_test")
      }
    }
  }

  it should "Fail when mess an attribute" in {
    assumeEnvironmentIsUpAndRunning
    xdContext.dropAllTables()

    ElasticSearchConnectionUtils.withClientDo(connectionOptions){ client =>
      client.execute { index into Index -> "NewMapping" fields {
        "name" -> "luis"
      }}

      val importQuery =
        s"""
           |IMPORT TABLES
           |USING $SourceProvider
           |OPTIONS (
           |es.nodes '$ElasticHost',
           |es.port '$ElasticRestPort',
           |es.nativePort '$ElasticNativePort',
           |es.resource '$Index/$Type'
           |)
      """.stripMargin

      //Experimentation
      an [RuntimeException] should be thrownBy sql(importQuery)

    }
  }


  lazy val connectionOptions: Map[String, String] = Map(
    "es.nodes" -> s"$ElasticHost",
    "es.port" -> s"$ElasticRestPort",
    "es.nativePort" -> s"$ElasticNativePort",
    "es.cluster" -> s"$ElasticClusterName"
  )
}
