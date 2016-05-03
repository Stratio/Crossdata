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
package com.stratio.crossdata.connector.mongodb

import com.stratio.common.utils.components.logger.impl.SparkLoggerComponent
import com.stratio.crossdata.connector.NativeScan
import com.stratio.datasource.mongodb.MongodbRelation
import com.stratio.datasource.util.Config
import org.apache.spark.sql.catalyst.plans.logical.{Filter, LeafNode, Limit, LogicalPlan, Project, UnaryNode}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{Row, SQLContext}

/**
 * A MongoDB baseRelation that can eliminate unneeded columns
 * and filter using selected predicates before producing
 * an RDD containing all matching tuples as Row objects.
 * @param config A Deep configuration with needed properties for MongoDB
 * @param schemaProvided The optionally provided schema. If not provided,
 *                       it will be inferred from the whole field projection
 *                       of the specified table in Spark SQL statement using
 *                       a sample ratio (as JSON Data Source does).
 * @param sqlContext An existing Spark SQL context.
 */
case class MongodbXDRelation(config: Config,
                             schemaProvided: Option[StructType] = None)(
                            @transient sqlContext: SQLContext)
  extends MongodbRelation(config, schemaProvided)(sqlContext) with NativeScan with SparkLoggerComponent{


  override def buildScan(optimizedLogicalPlan: LogicalPlan): Option[Array[Row]] = {
    logDebug(s"Processing ${optimizedLogicalPlan.toString()}")
    val queryExecutor = MongoQueryProcessor(optimizedLogicalPlan, config, schemaProvided)
    queryExecutor.execute()
  }

  override def isSupported(logicalStep: LogicalPlan, wholeLogicalPlan: LogicalPlan): Boolean = logicalStep match {
    case ln: LeafNode => true // TODO leafNode == LogicalRelation(xdSourceRelation)
    case un: UnaryNode => un match {
      case Limit(_, _) | Project(_, _) | Filter(_, _) => true
      case _ => false

    }
    case unsupportedLogicalPlan =>logDebug(s"LogicalPlan $unsupportedLogicalPlan cannot be executed natively"); false
  }


}