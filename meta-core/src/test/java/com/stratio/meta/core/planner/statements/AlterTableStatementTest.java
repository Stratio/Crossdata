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
import com.stratio.meta2.common.data.ColumnName;
import com.stratio.meta2.common.data.TableName;
import com.stratio.meta2.core.statements.AlterTableStatement;
import com.stratio.meta2.core.structures.Property;

import org.testng.annotations.Test;

import java.util.ArrayList;

public class AlterTableStatementTest extends BasicPlannerTest {
    @Test
    public void testPlanForAlter(){
        String inputText = "ALTER TABLE table1 ADD column1 INT;";
        stmt = new AlterTableStatement(new TableName("demo", "table1"), new ColumnName("demo", "table1", "column1"), "int", new ArrayList<Property>(), 1);
        validateCassandraPath("testPlanForAlter");
    }
}
