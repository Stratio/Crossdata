/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.crossdata.connector.cassandra

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CassandraCreateExternalTableIT extends CassandraWithSharedContext {


  "The Cassandra connector" should "execute natively create a External Table" in {

    val tableName = "newtable"

    val createTableQueryString =
      s"""|CREATE EXTERNAL TABLE $tableName (
          |id Integer,
          |name String,
          |booleanFile boolean,
          |timeTime Timestamp,
          |binaryType Binary,
          |arrayType ARRAY<STRING>,
          |mapType MAP<INT, INT>,
          |decimalType DECIMAL
          |)
          |USING $SourceProvider
          |OPTIONS (
          |keyspace '$Catalog',
          |table '$tableName',
          |cluster '$ClusterName',
          |pushdown "true",
          |spark_cassandra_connection_host '$CassandraHost',
          |primary_key_string 'id'
          |)
      """.stripMargin.replaceAll("\n", " ")
    //Experimentation
    val result = sql(createTableQueryString).collect()

    //Expectations
    val table = xdContext.table(tableName)
    table should not be null
    table.schema.fieldNames should contain ("name")

    // In case that the table didn't exist, then this operation would throw a InvalidQueryException
    val resultSet = client.get._2.execute(s"SELECT * FROM $Catalog.$tableName")

    import scala.collection.JavaConversions._

    resultSet.getColumnDefinitions.asList.map(cd => cd.getName) should contain ("name")
  }

  it should "execute natively create a External Table with no existing Keyspace" in {
    val createTableQueryString =
      s"""|CREATE EXTERNAL TABLE newkeyspace.othertable (id Integer, name String)
          |USING $SourceProvider
          |OPTIONS (
          |keyspace 'newkeyspace',
          |cluster '$ClusterName',
          |pushdown "true",
          |spark_cassandra_connection_host '$CassandraHost',
          |primary_key_string 'id',
          |with_replication "{'class' : 'SimpleStrategy', 'replication_factor' : 3}"
          |)
      """.stripMargin.replaceAll("\n", " ")

    try {
      //Experimentation
      val result = sql(createTableQueryString).collect()

      //Expectations
      val table = xdContext.table(s"newkeyspace.othertable")
      table should not be null
      table.schema.fieldNames should contain("name")
    }finally {
      //AFTER
      client.get._2.execute(s"DROP KEYSPACE newkeyspace")
    }
  }

  it should "fail execute natively create a External Table with no existing Keyspace without with_replication" in {
    val createTableQueryString =
      s"""|CREATE EXTERNAL TABLE NoKeyspaceCreatedBefore.newTable (id Integer, name String)
          |USING $SourceProvider
          |OPTIONS (
          |keyspace 'NoKeyspaceCreatedBefore',
          |cluster '$ClusterName',
          |pushdown "true",
          |spark_cassandra_connection_host '$CassandraHost',
          |primary_key_string 'id'
          |)
      """.stripMargin.replaceAll("\n", " ")
    //Experimentation

    the [IllegalArgumentException] thrownBy {
      sql(createTableQueryString).collect()
    }  should have message "requirement failed: with_replication required when use CREATE EXTERNAL TABLE command"

  }

  
}
