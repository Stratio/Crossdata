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

package com.stratio.meta.core.grammar.statements;

import com.stratio.meta.core.grammar.ParsingTest;
import org.testng.annotations.Test;

public class DropTableStatementTest extends ParsingTest {

  @Test
  public void dropTable() {
    String inputText = "DROP TABLE IF EXISTS lastTable;";
    testRegularStatement(inputText, "dropTable");
  }

  @Test
  public void dropNotMissing(){
    String inputText = "DROP TABLE IF EXISTS _lastTable;";
    testRecoverableError(inputText, "dropNotMissing");
  }

  @Test
  public void dropTableWithCatalog() {
    String inputText = "[ oldcatalog ], DROP TABLE lastTable;";
    String expectedText = inputText.replace("[ oldcatalog ], ", "");
    testRegularStatement(inputText, expectedText, "dropTableWithCatalog");
  }

  @Test
  public void dropTableWithEmptyCatalog() {
    String inputText = "[ ], DROP TABLE lastTable;";
    String expectedText = inputText.replace("[ ], ", "");
    testRegularStatement(inputText, expectedText, "dropTableWithEmptyCatalog");
  }

}
