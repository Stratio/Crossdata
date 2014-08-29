/*
 * Licensed to STRATIO (C) under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. The STRATIO
 * (C) licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.stratio.meta2.core.metadata;


import java.util.Map;
import java.util.concurrent.locks.Lock;

import com.stratio.meta.common.ask.Connect;
import com.stratio.meta2.common.data.*;
import com.stratio.meta2.common.metadata.*;
import joptsimple.internal.Column;



public enum MetadataManager {
  MANAGER;

  private boolean isInit = false;

  private Map<FirstLevelName, IMetadata> metadata;
  private Lock writeLock;


  private void shouldBeInit() {
    if (!isInit) {
      throw new MetadataManagerException("Metadata is not initialized yet.");
    }
  }





  public boolean exists(Name name){
    boolean result=false;
    switch(name.getType()){
      case Catalog:
        result=exists((CatalogName)name);
        break;
      case Cluster:
        result=exists((ClusterName)name);
        break;
      case Column:
        result=exists((ColumnName)name);
        break;
      case Connector:
        result=exists((ConnectorName)name);
        break;
      case DataStore:
        result=exists((DataStoreName)name);
        break;
      case Table:
        result=exists((TableName)name);
        break;
    }
    return result;
  }

  private void shouldBeUnique(Name name) {
    if (exists(name)) {
      throw new MetadataManagerException("[" + name + "] already exists");
    }
  }

  private void shouldExist(Name name) {
    if (!exists(name)) {
      throw new MetadataManagerException("[" + name + "] doesn't exist yet");
    }
  }

  private boolean exists(FirstLevelName name) {
    return metadata.containsKey(name);
  }
  public boolean exists(TableName name) {
    boolean result = false;
    if (exists(name.getCatalogName())) {
      CatalogMetadata catalogMetadata = this.getCatalog(name.getCatalogName());
      result = catalogMetadata.getTables().containsKey(name);
    }
    return result;
  }




  public boolean exists(ColumnName name){
    boolean result = false;
    if (exists(name.getTableName())) {
      TableMetadata catalogMetadata = this.getTable(name.getTableName());
      result = catalogMetadata.getColumns().containsKey(name);
    }
    return result;
  }






  public synchronized void init(Map<FirstLevelName, IMetadata> metadata, Lock writeLock) {
    if (metadata != null && writeLock != null) {
      this.metadata = metadata;
      this.writeLock = writeLock;
      this.isInit = true;
    } else {
      throw new NullPointerException("Any parameter can't be NULL");
    }
  }

  public void createCatalog(CatalogMetadata catalogMetadata) {
    shouldBeInit();
    try {
      writeLock.lock();
      shouldBeUnique(catalogMetadata.getName());
      metadata.put(catalogMetadata.getName(), catalogMetadata);
    } catch (MetadataManagerException mex) {
      throw mex;
    } catch (Exception ex) {
      throw new MetadataManagerException(ex.getMessage(), ex.getCause());
    } finally {
      writeLock.unlock();
    }
  }

  public CatalogMetadata getCatalog(CatalogName name) {
    shouldBeInit();
    shouldExist(name);
    return (CatalogMetadata) metadata.get(name);
  }


  public void createTable(TableMetadata tableMetadata) {
    shouldBeInit();
    try {
      writeLock.lock();
      shouldExist(tableMetadata.getName().getCatalogName());
      shouldExist(tableMetadata.getClusterRef());
      shouldBeUnique(tableMetadata.getName());
      CatalogMetadata catalogMetadata =
          ((CatalogMetadata) metadata.get(tableMetadata.getName().getCatalogName()));

      if (catalogMetadata.getTables().containsKey(tableMetadata.getName())) {
        throw new MetadataManagerException("Table [" + tableMetadata.getName()
            + "] already exists");
      }

      catalogMetadata.getTables().put(tableMetadata.getName(), tableMetadata);
      metadata.put(tableMetadata.getName().getCatalogName(), catalogMetadata);
    } catch (Exception ex) {
      throw new MetadataManagerException(ex.getMessage(), ex.getCause());
    } finally {
      writeLock.unlock();
    }
  }

  public TableMetadata getTable(TableName name) {
    shouldBeInit();
    shouldExist(name);
    CatalogMetadata catalogMetadata = this.getCatalog(name.getCatalogName());
    return catalogMetadata.getTables().get(name);
  }

  public void createCluster(ClusterMetadata clusterMetadata) {
    shouldBeInit();
    try {
      writeLock.lock();
      shouldExist(clusterMetadata.getDataStoreRef());
      shouldBeUnique(clusterMetadata.getName());
      for (ConnectorAttachedMetadata connectorRef : clusterMetadata.getConnectorAttachedRefs()
          .values()) {
        shouldExist(connectorRef.getConnectorRef());
      }
      metadata.put(clusterMetadata.getName(),clusterMetadata);
    } catch (MetadataManagerException mex) {
      throw mex;
    } catch (Exception ex) {
      throw new MetadataManagerException(ex.getMessage(), ex.getCause());
    } finally {
      writeLock.unlock();
    }
  }

  public ClusterMetadata getCluster(ClusterName name) {
    shouldBeInit();
    shouldExist(name);
    return (ClusterMetadata) metadata.get(name);
  }

  public void createDataStore(DataStoreMetadata dataStoreMetadata) {
    shouldBeInit();
    try {
      writeLock.lock();
      shouldBeUnique(dataStoreMetadata.getName());
      metadata.put(dataStoreMetadata.getName(), dataStoreMetadata);
    } catch (MetadataManagerException mex) {
      throw mex;
    } catch (Exception ex) {
      throw new MetadataManagerException(ex.getMessage(), ex.getCause());
    } finally {
      writeLock.unlock();
    }
  }

  public DataStoreMetadata getDataStore(DataStoreName name) {
    shouldBeInit();
    shouldExist(name);
    return (DataStoreMetadata) metadata.get(name);
  }

  public void createConnector(ConnectorMetadata connectorMetadata) {
    shouldBeInit();
    try {
      writeLock.lock();
      shouldBeUnique(connectorMetadata.getName());
      metadata.put(connectorMetadata.getName(), connectorMetadata);
    } catch (MetadataManagerException mex) {
      throw mex;
    } catch (Exception ex) {
      throw new MetadataManagerException(ex.getMessage(), ex.getCause());
    } finally {
      writeLock.unlock();
    }
  }

  public ConnectorMetadata getConnector(ConnectorName name) {
    shouldBeInit();
    shouldExist(name);
    return (ConnectorMetadata) metadata.get(name);
  }
  
  

}
