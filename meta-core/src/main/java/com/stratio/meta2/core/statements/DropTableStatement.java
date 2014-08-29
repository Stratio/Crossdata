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

package com.stratio.meta2.core.statements;

import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.TableMetadata;
import com.stratio.meta.common.result.QueryResult;
import com.stratio.meta.common.result.Result;
import com.stratio.meta.core.engine.EngineConfig;
import com.stratio.meta2.core.engine.validator.Validation;
import com.stratio.meta2.core.engine.validator.ValidationRequirements;
import com.stratio.meta2.core.metadata.MetadataManager;
import com.stratio.meta2.core.statements.MetaStatement;
import com.stratio.meta2.common.data.TableName;


/**
 * Class that models a {@code DROP TABLE} statement from the META language.
 */
public class DropTableStatement extends MetaStatement {

  /**
   * The name of the target table.
   */
  private TableName tableName;

  /**
   * Whether the table should be dropped only if exists.
   */
  private boolean ifExists;

  /**
   * Class constructor.
   *
   * @param tableName The name of the table.
   * @param ifExists Whether it should be dropped only if exists.
   */
  public DropTableStatement(TableName tableName, boolean ifExists) {
    this.tableName = tableName;
    this.ifExists = ifExists;
  }

  /**
   * Get the name of the table.
   * 
   * @return The name.
   */
  public TableName getTableName() {
    return tableName;
  }

  /**
   * Set the name of the table.
   * 
   * @param tableName The name of the table.
   */
  public void setTableName(TableName tableName) {
    this.tableName = tableName;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("DROP TABLE ");
    if (ifExists) {
      sb.append("IF EXISTS ");
    }
    if (catalogInc) {
      sb.append(catalog).append(".");
    }
    sb.append(tableName);
    return sb.toString();
  }

  @Override
  public String translateToCQL() {
    return this.toString();
  }

  public ValidationRequirements getValidationRequirements(){
    return new ValidationRequirements().add(Validation.MUST_EXIST_CATALOG).add(Validation.MUST_EXIST_TABLE);
  }

}
