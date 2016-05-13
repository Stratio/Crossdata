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
package org.apache.spark.sql.crossdata.security

import org.apache.spark.sql.crossdata.test.SharedXDContextTest

class SecurityManagerIT extends SharedXDContextTest {

  "A Dummy Security Manager" should "authorize all the operations" in {
    val securityManager = xdContext.securityManager
    val reply = securityManager.authorize("Tricky operation")
    assert(reply.authorizated === true)
    assert(reply.info === Some(DummySecurityManager.UniqueReply))
  }

  "Credentials of the configuration file" should "override empty credentials from XDContext constructor" in {
    val securityManager = xdContext.securityManager
    assert(securityManager.credentials.user === Some("tester"))
    assert(securityManager.credentials.password === Some("secret"))
    assert(securityManager.credentials.sessionId === Some("1234"))
  }


}
