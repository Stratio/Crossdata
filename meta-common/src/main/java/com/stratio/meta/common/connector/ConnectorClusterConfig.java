/*
 * Licensed to STRATIO (C) under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.  The STRATIO (C) licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.stratio.meta.common.connector;

import com.stratio.meta2.common.data.ClusterName;

import java.util.Map;

/**
 * Configuration used by a connector to establish a connection to a specific cluster.
 */
public class ConnectorClusterConfig {

  /**
   * Name of the target cluster.
   */
  private final ClusterName name;

  /**
   * Map of options required by a connector in order to be able to establish a connection
   * to an existing datastore cluster.
   */
  private final Map<String, Object> options;

  /**
   * Class constructor.
   * @param name Name of the target cluster.
   * @param options Map of options.
   */
  public ConnectorClusterConfig(ClusterName name, Map<String, Object> options) {
    this.name = name;
    this.options = options;
  }

  /**
   * Get the cluster options.
   * @return A map of options.
   */
  public Map<String, Object> getOptions() {
    return options;
  }

  /**
   * Get the name of the target cluster.
   * @return A {@link com.stratio.meta2.common.data.ClusterName}.
   */
  public ClusterName getName() {
    return name;
  }

}
