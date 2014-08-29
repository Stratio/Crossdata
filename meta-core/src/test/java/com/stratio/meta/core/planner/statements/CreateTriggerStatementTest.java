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
import com.stratio.meta2.core.statements.CreateTriggerStatement;
import org.testng.annotations.Test;

public class CreateTriggerStatementTest  extends BasicPlannerTest {

    @Test
    public void planificationNotSupported(){
        String inputText = "CREATE TRIGGER trigger1 ON table1 USING triggerClassName;";
        stmt = new CreateTriggerStatement("trigger1", "table1", "triggerClassName");
        validateNotSupported();
    }
}
