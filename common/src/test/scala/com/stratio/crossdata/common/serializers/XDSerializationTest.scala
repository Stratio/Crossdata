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
package com.stratio.crossdata.common.serializers

import com.stratio.crossdata.test.BaseXDTest
import org.json4s.jackson.JsonMethods._
import org.json4s.{Extraction, Formats}

import scala.reflect.ClassTag

object XDSerializationTest {
  case class TestCase(description: String, obj: Any)
}

//TODO: Use the template to fully test all interchange messages' serialization (CommandEnvelope, ...)
abstract class XDSerializationTest[T : ClassTag : Manifest] extends BaseXDTest {

  import XDSerializationTest._

  def testCases: Seq[TestCase]
  implicit val formats: Formats

  private val classTag: ClassTag[T] = implicitly[ClassTag[T]]

  testCases foreach {
    case TestCase(description, obj) =>

    s"A ${classTag.toString().split('.').last} serializer" should description in {

      val serialized = compact(render(Extraction.decompose(obj)))
      val extracted = parse(serialized, false).extract[T]

      extracted shouldEqual obj

    }

  }

}
