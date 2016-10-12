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
package org.apache.spark.sql.crossdata

import org.apache.spark.sql.SQLConf

trait XDSQLConf extends SQLConf {
  def enableCacheInvalidation(enable: Boolean): XDSQLConf
}


object XDSQLConf {

  val UserIdPropertyKey = "crossdata.security.user"

  implicit def fromSQLConf(conf: SQLConf): XDSQLConf = new XDSQLConf {

    override def enableCacheInvalidation(enable: Boolean): XDSQLConf = this
    override protected[spark] val settings: java.util.Map[String, String] = conf.settings
  }
}

