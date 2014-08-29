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
import com.stratio.meta2.common.data.TableName;
import com.stratio.meta2.core.statements.InsertIntoStatement;
import com.stratio.meta.core.structures.Option;
import com.stratio.meta2.common.statements.structures.terms.BooleanTerm;
import com.stratio.meta2.common.statements.structures.terms.GenericTerm;
import com.stratio.meta2.common.statements.structures.terms.LongTerm;
import com.stratio.meta2.common.statements.structures.terms.StringTerm;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InsertIntoStatementTest extends BasicPlannerTest {

	@Test
	public void testPlanForInsert() {
		String inputText = "INSERT INTO demo.users (name, gender, email, age, bool, phrase) VALUES ('name_0', 'male', 'name_0@domain.com', 10, true, '');";
		List<String> ids = Arrays.asList("name", "gender", "email", "age",
				"bool", "phrase");
		List<GenericTerm> list = new ArrayList<>();
		list.add(new StringTerm("name_0"));
		list.add(new StringTerm("male"));
		list.add(new LongTerm("10"));
		list.add(new BooleanTerm("false"));
		list.add(new StringTerm(""));
		stmt = new InsertIntoStatement(new TableName("demo", "users"), ids, list, false,
				new ArrayList<Option>());
		validateCassandraPath("testPlanForInsert");
	}
}
