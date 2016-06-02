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
package com.stratio.crossdata.driver.metadata

/**
 * database can be empty ("")
 */
class JavaTableName(val tableName: java.lang.String, val database: java.lang.String) {

  override def equals(other: Any): Boolean = other match {
    case that: JavaTableName =>
      tableName.equals(that.tableName) && database.equals(that.database)
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(tableName, database)
    state.collect {
      case x if x != null => x.hashCode
    }.foldLeft(0)((a, b) => 31 * a + b)
  }

}
