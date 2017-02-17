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
package org.apache.spark.sql.crossdata.session

import com.stratio.crossdata.security.CrossdataSecurityManager
import com.typesafe.config.Config
import org.apache.log4j.Logger
import org.apache.spark.SparkContext
import org.apache.spark.sql.catalyst.catalog.ExternalCatalog
import org.apache.spark.sql.internal.SharedState


final class XDSharedState(
                           @transient val sc: SparkContext,
                           @transient private val userCoreConfig: Option[Config] = None
                           //val sqlConf: SQLConf,
                           //val externalCatalog: XDCatalogCommon,
                           //val streamingCatalog: Option[XDStreamingCatalog],
                           //@transient val securityManager: Option[CrossdataSecurityManager]
                         ) extends SharedState(sc) {

  // TODO XDCatalog Spark2.0 => SPIKE fallback? override val externalCatalog: ExternalCatalog = super.externalCatalog
}
