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
package com.stratio.crossdata.driver.shell

import com.stratio.crossdata.test.BaseXDTest
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ShellArgsReaderSpec extends BaseXDTest {

  "ShellArgsReader" should "parse boolean options adding a flag" in {
    val opts = new ShellArgsReader(List("--http")).options

    opts("http") shouldBe true
    opts.get("async") shouldBe empty

    val options = new ShellArgsReader(List("--http", "--async")).options
    options("http") shouldBe true
    options("async") shouldBe true

  }

  it should "parse boolean options indicating the value" in {
    val opts = new ShellArgsReader(List("--http", "true")).options
    opts("http") shouldBe true

    val options = new ShellArgsReader(List("--http", "false")).options
    options("http") shouldBe false
  }

}
