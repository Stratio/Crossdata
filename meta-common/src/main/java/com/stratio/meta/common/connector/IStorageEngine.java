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

package com.stratio.meta.common.connector;

import com.stratio.meta.common.data.Row;
import com.stratio.meta.common.exceptions.ExecutionException;
import com.stratio.meta.common.exceptions.UnsupportedException;
import com.stratio.meta2.common.data.ClusterName;
import com.stratio.meta2.common.data.TableName;

import java.util.Collection;

/**
 * Interface provided by a connector to access storage related operations such as inserting new
 * data.
 */
public interface IStorageEngine {

  /**
   * Insert a single row in a table.
   *
   * @param targetCluster Target cluster.
   * @param targetTable   Target table fully qualified including catalog.
   * @param row           The row to be inserted.
   * @throws UnsupportedException If the operation is not supported.
   * @throws ExecutionException   If the execution fails.
   */
  public void insert(ClusterName targetCluster, TableName targetTable, Row row)
      throws UnsupportedException,
             ExecutionException;

  /**
   * Insert a collection of rows in a table.
   *
   * @param targetCluster Target cluster.
   * @param targetTable   Target table fully qualified including catalog.
   * @param rows          Collection of rows to be inserted.
   * @throws UnsupportedException If the operation is not supported.
   * @throws ExecutionException   If the execution fails.
   */
  public void insert(ClusterName targetCluster, TableName targetTable, Collection<Row> rows)
      throws UnsupportedException,
             ExecutionException;

}
