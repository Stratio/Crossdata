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
package com.stratio.crossdata.server

import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConversions._


object CrossdataApplication extends App {

  val keysValuesTuple = args.toList.partition(_.startsWith("--"))
  val argsConfig = keysValuesTuple._1.zip(keysValuesTuple._2).toMap

  val mapConfig = ConfigFactory.parseMap(argsConfig.map{
    e => (e._1.replace("--", ""), e._2)
  })

  val params = mapConfig match {
    case m: Config if !m.isEmpty => Some(m)
    case _ => None
  }

  val crossdataServer = new CrossdataServer(params)
  crossdataServer.start()

}