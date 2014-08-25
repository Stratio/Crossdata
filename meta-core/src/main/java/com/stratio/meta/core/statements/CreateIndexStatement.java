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

package com.stratio.meta.core.statements;

import com.datastax.driver.core.ColumnMetadata;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.TableMetadata;
import com.stratio.meta.common.result.QueryResult;
import com.stratio.meta.common.result.Result;
import com.stratio.meta.common.utils.StringUtils;
import com.stratio.meta.core.engine.EngineConfig;
import com.stratio.meta.core.metadata.CustomIndexMetadata;
import com.stratio.meta.core.metadata.MetadataManager;
import com.stratio.meta.core.structures.IndexType;
import com.stratio.meta.core.utils.MetaPath;
import com.stratio.meta.core.utils.MetaStep;
import com.stratio.meta.core.utils.Tree;
import com.stratio.meta2.common.statements.structures.terms.GenericTerm;
import com.stratio.meta2.common.statements.structures.terms.StringTerm;
import com.stratio.meta2.common.statements.structures.terms.Term;
import com.stratio.meta2.core.statements.MetaStatement;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Class that models a {@code CREATE INDEX} statement of the META language. This class recognizes the following syntax:
 * <p>
 * CREATE {@link IndexType} INDEX (IF NOT EXISTS)? {@literal <index_name>} <br>
 * ON {@literal <tableName>} ( {@literal <identifier> , ..., <identifier>}) <br>
 * ( USING {@literal <index_class>} )? ( WITH OPTIONS ( key_1=value_1 AND ... AND key_n=value_n) )?;
 */
public class CreateIndexStatement extends MetaStatement {

  /**
   * The {@link com.stratio.meta.core.structures.IndexType} to be created.
   */
  private IndexType type = null;

  /**
   * Whether the index should be created only if not exists.
   */
  private boolean createIfNotExists = false;

  /**
   * Determine whether the index should be created or not.
   */
  private boolean createIndex = false;

  /**
   * The name of the index.
   */
  private String name = null;

  /**
   * The name of the target table.
   */
  private String tableName = null;

  /**
   * The list of columns covered by the index. Only one column is allowed for {@code DEFAULT} indexes.
   */
  private List<String> targetColumns = null;

  /**
   * The name of the class that implements the secondary index.
   */
  private String usingClass = null;

  /**
   * The map of options passed to the index during its creation.
   */
  private Map<Term, GenericTerm> options = null;

  /**
   * Map of lucene types associated with Cassandra data types.
   */
  private static Map<String, String> luceneTypes = new HashMap<>();

  private String optionsJson = "";

  /**
   * Table metadata cached on the validate function.
   */
  private transient TableMetadata metadata = null;

  static{
    luceneTypes.put(DataType.text().toString(), "{type:\"string\"}");
    luceneTypes.put(DataType.varchar().toString(), "{type:\"string\"}");
    luceneTypes.put(DataType.inet().toString(), "{type:\"string\"}");
    luceneTypes.put(DataType.ascii().toString(), "{type:\"string\"}");
    luceneTypes.put(DataType.bigint().toString(), "{type:\"long\"}");
    luceneTypes.put(DataType.counter().toString(), "{type:\"long\"}");
    luceneTypes.put(DataType.cboolean().toString(), "{type:\"boolean\"}");
    luceneTypes.put(DataType.cdouble().toString(), "{type:\"double\"}");
    luceneTypes.put(DataType.cfloat().toString(), "{type:\"float\"}");
    luceneTypes.put(DataType.cint().toString(), "{type:\"integer\"}");
    luceneTypes.put(DataType.uuid().toString(), "{type:\"uuid\"}");
  }

  /**
   * Class constructor.
   */
  public CreateIndexStatement(){
    this.command = false;
    targetColumns = new ArrayList<>();
    options = new LinkedHashMap<>();
  }

  /**
   * Set the type of index.
   * @param type The type from {@link com.stratio.meta.core.structures.IndexType}.
   */
  public void setIndexType(String type){
    this.type = IndexType.valueOf(type.toUpperCase());
  }

  /**
   * Set that the index should be created if not exists.
   */
  public void setCreateIfNotExists(){
    createIfNotExists = true;
  }

  /**
   * Set the type of index.
   * @param type A {@link com.stratio.meta.core.structures.IndexType}.
   */
  public void setType(IndexType type) {
    this.type = type;
  }

  /**
   * If the IF NOT EXISTS clause has been specified.
   * @return Whether the index should be created only if not exists.
   */
  public boolean isCreateIfNotExists() {
    return createIfNotExists;
  }

  /**
   * Set the value of the IF NOT EXISTS clause.
   * @param ifNotExists If it has been specified or not.
   */
  public void setCreateIfNotExists(boolean ifNotExists) {
    this.createIfNotExists = ifNotExists;
  }

  /**
   * Set the name of the index.
   * @param name The name.
   */
  public void setName(String name){
    if(name.contains(".")){
      String[] ksAndTablename = name.split("\\.");
      catalog = ksAndTablename[0];
      this.name = ksAndTablename[1];
      catalogInc = true;
    }else {
      this.name = name;
    }
  }

  /**
   * Get the index name.
   * @return The name.
   */
  public String getName(){
    return name;
  }

  /**
   * Set the name of the target table.
   * @param tableName The name.
   */
  public void setTableName(String tableName){
    if(tableName.contains(".")){
      String[] ksAndTablename = tableName.split("\\.");
      catalog = ksAndTablename[0];
      this.tableName = ksAndTablename[1];
      catalogInc = true;
    }else {
      this.tableName = tableName;
    }

  }

  /**
   * Add a column to the list of indexed columns.
   * @param column The name of the column.
   */
  public void addColumn(String column){
    targetColumns.add(column);
  }

  /**
   * Set a USING class that implements the custom index.
   * @param using The qualified name of the class.
   */
  public void setUsingClass(String using){
    usingClass = using;
  }

  /**
   * Add an options to the index.
   * @param key The option key.
   * @param value The option value.
   */
  public void addOption(Term key, GenericTerm value){
    options.put(key, value);
  }

  public void setOptionsJson(String optionsJson){
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    JsonFactory factory = mapper.getJsonFactory();
    JsonParser jp;
    try {
      jp = factory.createJsonParser(optionsJson);
      JsonNode root = mapper.readTree(jp);

      Iterator<Entry<String, JsonNode>> iter = root.getFields();
      while(iter.hasNext()){
        Entry<String, JsonNode> entry = iter.next();
        addOption(new StringTerm(entry.getKey()), GenericTerm.CreateGenericTerm(entry.getValue()));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Get the map of options.
   * @return The map of options.
   */
  public Map<Term, GenericTerm> getOptions(){
    return options;
  }

  /**
   * Get the name of the index. If a LUCENE index is to be created, the name of the index
   * is prepended with {@code stratio_lucene_}. If a name for the index is not specified, the index
   * will be named using the concatenation of the target column names.
   * @return The name of the index.
   */
  protected String getIndexName(){
    String result = null;
    if(name == null){
      StringBuilder sb = new StringBuilder();
      if(IndexType.LUCENE.equals(type)){
        sb.append("stratio_lucene_");
        sb.append(tableName);
      }else {
        sb.append(tableName);
        for (String c : targetColumns) {
          sb.append("_");
          sb.append(c);
        }
        sb.append("_idx");
      }
      result = sb.toString();
    }else{
      result = name;
      if(IndexType.LUCENE.equals(type)){
        result = "stratio_lucene_" + name;
      }
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("CREATE ");
    sb.append(type);
    sb.append(" INDEX ");
    if(createIfNotExists){
      sb.append("IF NOT EXISTS ");
    }

    if(name != null){
      sb.append(getIndexName()).append(" ");
    }
    sb.append("ON ");
    if(catalogInc){
      sb.append(catalog).append(".");
    }
    sb.append(tableName);
    sb.append(" (").append(StringUtils.stringList(targetColumns, ", ")).append(")");
    if(usingClass != null){
      sb.append(" USING ");
      sb.append(usingClass);
    }
    if(!options.isEmpty()){
      sb.append(" WITH OPTIONS = {");
      Iterator<Entry<Term, GenericTerm>> entryIt = options.entrySet().iterator();
      Entry<Term, GenericTerm> e;
      while(entryIt.hasNext()){
        e = entryIt.next();
        sb.append(e.getKey()).append(": ").append(e.getValue());
        if(entryIt.hasNext()){
          sb.append(", ");
        }
      }
      sb.append("}");
    }

    return sb.toString();
  }

  /** {@inheritDoc} */
  @Override
  public Result validate(MetadataManager metadata, EngineConfig config) {

    //Validate target table
    Result result = validateKeyspaceAndTable(metadata, sessionCatalog, catalogInc, catalog, tableName);
    String effectiveKeyspace = getEffectiveCatalog();

    TableMetadata tableMetadata = null;
    if(!result.hasError()) {
      tableMetadata = metadata.getTableMetadata(effectiveKeyspace, tableName);
      this.metadata = tableMetadata;
      result = validateOptions(effectiveKeyspace, tableMetadata);
    }

    //Validate index name if not exists
    if(!result.hasError()){
      if(name != null && name.toLowerCase().startsWith("stratio")){
        result = Result.createValidationErrorResult(
            "Internal namespace stratio cannot be use on index name " + name);
      }else {
        result = validateIndexName(metadata, tableMetadata);
      }
    }

    //Validate target columns
    if(!result.hasError()){
      result = validateSelectionColumns(tableMetadata);
    }

    //If the syntax is valid and we are dealing with a Lucene index, complete the missing fields.
    if(!result.hasError()
       && IndexType.LUCENE.equals(type)
       && (options.isEmpty() || usingClass == null)){
      options.clear();
      options.putAll(generateLuceneOptions());
      usingClass = "org.apache.cassandra.db.index.stratio.RowIndex";
    }

    return result;
  }

  /**
   * Validate that the target columns exists in the table.
   * @param tableMetadata The associated {@link com.datastax.driver.core.TableMetadata}.
   * @return A {@link com.stratio.meta.common.result.Result} with the validation result.
   */
  private Result validateSelectionColumns(TableMetadata tableMetadata) {
    Result result = QueryResult.createSuccessQueryResult();

    for(String c : targetColumns){
      if(c.toLowerCase().startsWith("stratio")){
        result = Result.createValidationErrorResult("Internal column " + c + " cannot be part of the WHERE clause.");
      }else if(tableMetadata.getColumn(c) == null){
        result = Result.createValidationErrorResult("Column " + c + " does not exist in table " + tableMetadata.getName());
      }
    }

    return result;
  }

  /**
   * Validate that the index name is valid an has not been previously used.
   * @param metadata The associated {@link com.stratio.meta.core.metadata.MetadataManager}.
   * @param tableMetadata The associated {@link com.datastax.driver.core.TableMetadata}.
   * @return A {@link com.stratio.meta.common.result.Result} with the validation result.
   */
  private Result validateIndexName(MetadataManager metadata, TableMetadata tableMetadata){
    Result result = QueryResult.createSuccessQueryResult();
    String indexName = getIndexName();

    List<CustomIndexMetadata> allIndex = metadata.getTableIndex(tableMetadata);

    boolean found = false;
    for(int index = 0; index < allIndex.size() && !found; index++){
      if(allIndex.get(index).getIndexName().equalsIgnoreCase(indexName)){
        found = true;
      }
    }
    if(found && !createIfNotExists){
      result = Result.createValidationErrorResult("Index " + name + " already exists in table " + tableName);
    }else{
      createIndex = true;
    }
    return result;
  }

  /**
   * Validate the index options.
   * @param effectiveKeyspace The effective keyspace used in the validation process.
   * @param metadata The associated {@link com.datastax.driver.core.TableMetadata}.
   * @return A {@link com.stratio.meta.common.result.Result} with the validation result.
   */
  private Result validateOptions(String effectiveKeyspace, TableMetadata metadata) {
    Result result = QueryResult.createSuccessQueryResult();
    if(!options.isEmpty()){
      result = Result.createValidationErrorResult(
          "WITH OPTIONS clause not supported in index creation.");
    }
    if(!createIfNotExists && IndexType.LUCENE.equals(type)) {
      Iterator<ColumnMetadata> columns = metadata.getColumns().iterator();
      boolean found = false;
      ColumnMetadata column = null;
      while (!found && columns.hasNext()) {
        column = columns.next();
        if (column.getName().startsWith("stratio_lucene")) {
          found = true;
        }
      }
      if (found) {
        result = Result.createValidationErrorResult(
            "Cannot create index: A Lucene index already exists on table "
            + effectiveKeyspace + "."
            + metadata.getName() + ". Use DROP INDEX " + column.getName()
                .replace("stratio_lucene_", "") + "; to remove the index.");
      }
    }
    return result;
  }

  /**
   * Generate the set of Lucene options required to create an index.
   * @return The set of options.
   */
  protected Map<Term, GenericTerm> generateLuceneOptions(){
    Map<Term, GenericTerm> result = new HashMap<>();

    //TODO: Read parameters from default configuration and merge with the user specification.
    result.put(new StringTerm("'refresh_seconds'"), new StringTerm("'1'"));

    result.put(new StringTerm("'num_cached_filters'"), new StringTerm("'1'"));
    result.put(new StringTerm("'ram_buffer_mb'"), new StringTerm("'32'"));
    result.put(new StringTerm("'max_merge_mb'"), new StringTerm("'5'"));
    result.put(new StringTerm("'max_cached_mb'"), new StringTerm("'30'"));
    result.put(new StringTerm("'schema'"), new StringTerm("'" + generateLuceneSchema() + "'"));

    return result;
  }

  /**
   * Generate the Lucene options schema that corresponds with the selected column.
   * @return The JSON representation of the Lucene schema.
   */
  protected String generateLuceneSchema(){
    StringBuilder sb = new StringBuilder();
    sb.append("{default_analyzer:\"org.apache.lucene.analysis.standard.StandardAnalyzer\",");
    sb.append("fields:{");

    //Iterate throught the columns.
    for(String column : targetColumns){
      sb.append(column);
      sb.append(":");
      sb.append(luceneTypes.get(metadata.getColumn(column).getType().toString()));
      sb.append(",");
    }

    sb.append("}}");
    return sb.toString().replace(",}}", "}}");
  }

  @Override
  public String translateToCQL() {

    if(IndexType.LUCENE.equals(type)){
      targetColumns.clear();
      targetColumns.add(getIndexName());
    }

    String cqlString = this.toString().replace(" DEFAULT ", " ");
    if(cqlString.contains(" LUCENE ")){
      cqlString = this.toString().replace("CREATE LUCENE ", "CREATE CUSTOM ");
    }

    if(name == null){
      cqlString = cqlString.replace("INDEX ON", "INDEX " + getIndexName() + " ON");
    }

    if(cqlString.contains("USING")){
      cqlString = cqlString.replace("USING ", "USING '");
      if(cqlString.contains("WITH ")){
        cqlString = cqlString.replace(" WITH OPTIONS", "' WITH OPTIONS");
      }
    }
    return cqlString;
  }

  @Override
  public Tree getPlan(MetadataManager metadataManager, String targetKeyspace) {
    Tree result = new Tree();

    if(createIndex) {
      //Add CREATE INDEX as the root.
      result.setNode(new MetaStep(MetaPath.CASSANDRA, translateToCQL()));
      //Add alter table as leaf if LUCENE index is selected.
      if (IndexType.LUCENE.equals(type)) {
        StringBuilder alterStatement = new StringBuilder("ALTER TABLE ");
        if (catalogInc) {
          alterStatement.append(catalog);
          alterStatement.append(".");
        }
        alterStatement.append(tableName);
        alterStatement.append(" ADD ");
        alterStatement.append(getIndexName());
        alterStatement.append(" TEXT;");

        result.addChild(new Tree(new MetaStep(MetaPath.CASSANDRA, alterStatement.toString())));
      }
    }

    return result;
  }

  public void setCreateIndex(Boolean createIndex){
    this.createIndex = createIndex;
  }

}
