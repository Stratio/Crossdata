/*
 * Licensed to STRATIO (C) under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.  The STRATIO (C) licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.stratio.meta.core.planner;

import com.datastax.driver.core.Session;
import com.stratio.meta.core.metadata.MetadataManager;
import com.stratio.meta.core.utils.MetaQuery;
import com.stratio.streaming.api.IStratioStreamingAPI;
import com.stratio.meta.common.result.QueryStatus;

public class Planner {

    /**
     * A {@link com.stratio.meta.core.metadata.MetadataManager}.
     */
    private final MetadataManager metadata;

    /**
     * Planner constructor.
     *
     * @param session Cassandra datastax java driver session.
     */
    public Planner(Session session, IStratioStreamingAPI stratioStreamingAPI){
        metadata = new MetadataManager(session, stratioStreamingAPI);
        metadata.loadMetadata();
    }

    /**
     * Plan a {@link com.stratio.meta.core.utils.MetaQuery}.
     *
     * @param metaQuery Query to plan.
     * @return same {@link com.stratio.meta.core.utils.MetaQuery} planned.
     */
    public MetaQuery planQuery(MetaQuery metaQuery) {
        metaQuery.setStatus(QueryStatus.PLANNED);
        //metaQuery.setPlan(metaQuery.getStatement().getPlan(metadata, metaQuery.getSessionCatalog()));
      throw new UnsupportedOperationException();
        //return metaQuery;
    }
    
}
