/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package org.elasticsearch.spark.sql

import com.stratio.crossdata.test.BaseXDTest
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.catalyst.plans.logical.{Filter, LeafNode, LogicalPlan, Project}
import org.apache.spark.sql.types.StructType
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar

@RunWith(classOf[JUnitRunner])
class ElasticSearchXDRelationSpec extends BaseXDTest with MockitoSugar {


  "An ElasticSearchXDRelation " should "support Limit Node" in {
    //Fixture
    val logicalStep = mock[Filter]
    val wholeLogicalPlan = mock[LogicalPlan]

    val esRelation = buildElasticSearchRelation()

    //Experimentation
    val result = esRelation.isSupported(logicalStep, wholeLogicalPlan)

    //Expectations
    result should be (true)
  }

  it should "support LeafNode Node" in {
    //Fixture
    val logicalStep = mock[LeafNode]
    val wholeLogicalPlan = mock[LogicalPlan]

    val esRelation = buildElasticSearchRelation()

    //Experimentation
    val result = esRelation.isSupported(logicalStep, wholeLogicalPlan)

    //Expectations
    result should be (true)
  }

  it should "support Project Node" in {
    //Fixture
    val logicalStep = mock[Project]
    val wholeLogicalPlan = mock[LogicalPlan]

    val esRelation = buildElasticSearchRelation()

    //Experimentation
    val result = esRelation.isSupported(logicalStep, wholeLogicalPlan)

    //Expectations
    result should be (true)
  }

  def buildElasticSearchRelation(): ElasticsearchXDRelation = {
    val parameters: Map[String, String] = mock[Map[String, String]]
    val sqlContext: SQLContext = mock[SQLContext]
    val schemaProvided: Option[StructType] = None

    new ElasticsearchXDRelation(parameters, sqlContext, schemaProvided)
  }

}
