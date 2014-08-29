package com.stratio.meta2.core.statements;

import com.stratio.meta.common.utils.StringUtils;
import com.stratio.meta.core.metadata.MetadataManager;
import com.stratio.meta.core.utils.Tree;
import com.stratio.meta2.common.statements.structures.terms.GenericTerm;
import com.stratio.meta2.common.statements.structures.terms.Term;

import java.util.Map;

public class AttachConnectorStatement extends MetaStatement {

  private String connectorName;
  private String clusterName;

  /**
   * The map of options passed to the connector during its attachment.
   */
  private Map<Term, GenericTerm> options = null;

  public AttachConnectorStatement(String connectorName, String clusterName, String json){
    this.connectorName = connectorName;
    this.clusterName = clusterName;
    this.options = StringUtils.convertJsonToOptions(json);
  }

  @Override
  public String toString() {
    return "ATTACH CONNECTOR" + connectorName + " TO "+ clusterName + "WITH OPTIONS " + StringUtils.getStringFromOptions(options);
  }

  @Override
  public String translateToCQL() {
    return null;
  }

  @Override
  public Tree getPlan(MetadataManager metadataManager, String targetKeyspace) {
    return null;
  }
}
