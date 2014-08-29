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

package com.stratio.meta.core.planner.statements;

import com.stratio.meta.core.planner.BasicPlannerTest;
import com.stratio.meta2.common.data.ClusterName;
import com.stratio.meta2.common.data.ColumnName;
import com.stratio.meta2.common.data.TableName;
import com.stratio.meta2.common.statements.structures.terms.BooleanTerm;
import com.stratio.meta2.core.statements.CreateTableStatement;
import com.stratio.meta2.core.statements.MetaStatement;
import com.stratio.meta2.core.structures.Property;
import com.stratio.meta2.core.structures.PropertyNameValue;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CreateTableStatementTest extends BasicPlannerTest {

  /**
   * Class logger.
   */
  private static final Logger logger = Logger.getLogger(CreateTableStatementTest.class);

  @Test
  public void testPlanForCreateTable() {
    String inputText =
        "CREATE TABLE demo.new_table ON CLUSTER clusterDemo (id INT, name VARCHAR, check BOOLEAN, PRIMARY KEY (id, name));";
    Map<ColumnName, String> columns = new HashMap();
    columns.put(new ColumnName("demo", "new_table", "id"), "INT");
    columns.put(new ColumnName("demo", "new_table", "name"), "VARCHAR");
    columns.put(new ColumnName("demo", "new_table", "check"), "BOOLEAN");
    stmt =
        new CreateTableStatement(new TableName("demo", "new_table"), new ClusterName("clusterDemo"), columns, Arrays.asList(new ColumnName("demo", "new_table", "id")),
            Arrays.asList(new ColumnName("demo", "new_table", "name")), 1, 1);
    stmt.setSessionCatalog("demo");

    try {
      Class<? extends MetaStatement> clazz = stmt.getClass();
      Field field = clazz.getDeclaredField("createTable");
      field.setAccessible(true);
      field.setBoolean(stmt, true);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      logger.error(e.getStackTrace());
    }

    validateCassandraPath("testPlanForCreateTable");
  }

  @Test
  public void testPlanForEphemeralCreateTable() {
    String inputText =
        "CREATE TABLE demo.table_temporal (id INT, name VARCHAR, check BOOLEAN, PRIMARY KEY (id)) WITH ephemeral = true;";
    Map<ColumnName, String> columns = new HashMap();
    columns.put(new ColumnName("demo", "table_temporal", "id"), "INT");
    columns.put(new ColumnName("demo", "table_temporal", "name"), "VARCHAR");
    columns.put(new ColumnName("demo", "table_temporal", "check"), "BOOLEAN");
    stmt =
        new CreateTableStatement(new TableName("demo", "new_table"), new ClusterName("clusterDemo"), columns, Arrays.asList(new ColumnName("demo", "table_temporal", "id")),
            Arrays.asList(new ColumnName("demo", "table_temporal", "name")), 1, 1);
    stmt.setSessionCatalog("demo");

    Property prop = new PropertyNameValue("ephemeral", new BooleanTerm("true"));

    ((CreateTableStatement) stmt).setProperties(Collections.singletonList(prop));

    try {
      Class<? extends MetaStatement> clazz = stmt.getClass();
      Field field = clazz.getDeclaredField("createTable");
      field.setAccessible(true);
      field.setBoolean(stmt, true);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      logger.error(e.getStackTrace());
    }

    validateStreamingPath("testPlanForEphemeralCreateTable");
  }

}
