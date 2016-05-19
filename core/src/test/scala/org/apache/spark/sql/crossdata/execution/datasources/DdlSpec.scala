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
package org.apache.spark.sql.crossdata.execution.datasources

import java.sql.{Date, Timestamp}
import java.text.SimpleDateFormat

import com.stratio.crossdata.test.BaseXDTest
import org.apache.spark.sql.types._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar

import scala.util.Success

@RunWith(classOf[JUnitRunner])
class DdlSpec extends BaseXDTest with MockitoSugar{


  "Ddl" should  "successfully convert from ByteType to Byte" in {

    DDLUtils.convertSparkDatatypeToScala("4", ByteType) shouldBe Success(4 : Byte)

  }

  "Ddl" should  "successfully convert from ShortType to Short" in {

    DDLUtils.convertSparkDatatypeToScala("6", ShortType) shouldBe Success(6 : Short)

  }

  "Ddl" should  "successfully convert from IntegerType to Integer" in {

    DDLUtils.convertSparkDatatypeToScala("25", IntegerType) shouldBe Success(25 : Int)

  }

  "Ddl" should  "successfully convert from LongType to Long" in {

    DDLUtils.convertSparkDatatypeToScala("-127", LongType) shouldBe Success(-127 : Long)

  }

  "Ddl" should  "successfully convert from FloatType to Float" in {

    DDLUtils.convertSparkDatatypeToScala("-1.01", FloatType) shouldBe Success(-1.01f : Float)

  }

  "Ddl" should  "successfully convert from DoubleType to Double" in {

    DDLUtils.convertSparkDatatypeToScala("3.75", DoubleType) shouldBe Success(3.75 : Double)

  }

  "Ddl" should  "successfully convert from DecimalType to BigDecimal" in {

    DDLUtils.convertSparkDatatypeToScala("-106.75", DecimalType.SYSTEM_DEFAULT) shouldBe Success(BigDecimal(-106.75))

  }

  "Ddl" should  "successfully convert from StringType to String" in {

    DDLUtils.convertSparkDatatypeToScala("abcde", StringType) shouldBe Success("abcde")

  }

  "Ddl" should  "successfully convert from BooleanType to Boolean" in {

    DDLUtils.convertSparkDatatypeToScala("false", BooleanType) shouldBe Success(false : Boolean)

  }

  "Ddl" should  "successfully convert from DateType to Date" in {

    DDLUtils.convertSparkDatatypeToScala("2015-01-01", DateType) shouldBe Success(Date.valueOf("2015-01-01"))

  }

  "Ddl" should  "successfully convert from TimestampType to Timestamp" in {

    DDLUtils.convertSparkDatatypeToScala("1988-08-11 11:12:13", TimestampType) shouldBe Success(Timestamp.valueOf("1988-08-11 11:12:13"))

  }


}
