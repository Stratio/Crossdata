/**
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
package com.stratio.crossdata.driver.querybuilder.dslentities

import com.stratio.crossdata.driver.querybuilder.CrossdataSQLStatement
import com.stratio.crossdata.driver.querybuilder.RunnableQuery


object CombineType extends Enumeration {
  type CombineType = Value
  val UnionAll = Value("UNION ALL")
  val Intersect = Value("INTERSECT")
  val Except = Value("EXCEPT")
  val UnionDistinct = Value("UNION DISTINCT")
}


import com.stratio.crossdata.driver.querybuilder.dslentities.CombineType.CombineType

case class CombinationInfo(combineType: CombineType, runnableQuery: RunnableQuery) extends CrossdataSQLStatement {
  override private[querybuilder] def toXDQL: String = s" ${combineType.toString} ${runnableQuery.toXDQL}"
}