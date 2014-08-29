/*
 * Licensed to STRATIO (C) under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.  The STRATIO (C) licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.stratio.meta.common.logicalplan;

import com.stratio.meta.common.connector.Operations;
import com.stratio.meta.common.statements.structures.relationships.Operator;
import com.stratio.meta.common.statements.structures.relationships.Relation;

/**
 * Filter the results retrieved through a Project operation.
 */
public class Filter extends TransformationStep{

  /**
   * Type of operation to be executed.
   */
  private final Operations operation;

  /**
   * Relationship.
   */
  private final Relation relation;

  /**
   * Create filter operation to be executed over a existing dataset.
   * @param operation The operation to be executed.
   * @param relation The relationship.
   */
  public Filter(Operations operation, Relation relation) {
    this.operation = operation;
    this.relation = relation;
  }

  /**
   * Get the type of operation associated with this filter.
   * @return A {@link com.stratio.meta.common.connector.Operations}.
   */
  public Operations getOperation(){
    return operation;
  }

  /**
   * Get the relationship.
   * @return A {@link com.stratio.meta.common.statements.structures.relationships.Relation}
   */
  public Relation getRelation() {
    return relation;
  }

}
