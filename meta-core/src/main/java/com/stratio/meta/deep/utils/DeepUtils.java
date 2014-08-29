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

package com.stratio.meta.deep.utils;

import com.stratio.deep.entity.Cells;
import com.stratio.meta.common.data.CassandraResultSet;
import com.stratio.meta.common.data.Cell;
import com.stratio.meta.common.data.ResultSet;
import com.stratio.meta.common.data.Row;
import com.stratio.meta.common.metadata.structures.ColumnMetadata;
import com.stratio.meta.common.metadata.structures.ColumnType;
import com.stratio.meta.common.statements.structures.selectors.GroupByFunction;
import com.stratio.meta.common.statements.structures.selectors.SelectorGroupBy;
import com.stratio.meta.common.statements.structures.selectors.SelectorIdentifier;
import com.stratio.meta.common.statements.structures.selectors.SelectorMeta;
import com.stratio.meta.core.metadata.AbstractMetadataHelper;
import com.stratio.meta.core.metadata.CassandraMetadataHelper;
import com.stratio.meta2.core.statements.SelectStatement;
import com.stratio.meta.core.structures.Selection;
import com.stratio.meta.core.structures.SelectionList;
import com.stratio.meta.core.structures.SelectionSelectors;

import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaRDD;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class DeepUtils {

  /**
   * Class logger.
   */
  private static final Logger LOG = Logger.getLogger(DeepUtils.class);

  /**
   * Private class constructor as all methods are static.
   */
  private DeepUtils() {

  }

  /**
   * Build ResultSet from list of Cells.
   * 
   * @param cells list of Cells
   * @param selectedCols List of fields selected in the SelectStatement.
   * @return ResultSet
   */
  public static ResultSet buildResultSet(List<Cells> cells, List<String> selectedCols) {
    CassandraResultSet rs = new CassandraResultSet();

    rs.setColumnMetadata(retrieveColumnMetadata(cells, selectedCols));

    if (!cells.isEmpty()) {
      if (selectedCols.isEmpty()) {
        for (Cells deepRow : cells) {
          Row metaRow = new Row();
          for (com.stratio.deep.entity.Cell deepCell : deepRow.getCells()) {

            if (deepCell.getCellName().toLowerCase().startsWith("stratio")) {
              continue;
            }

            Cell metaCell = new Cell(deepCell.getCellValue());
            metaRow.addCell(deepCell.getCellName(), metaCell);
          }

          rs.add(metaRow);
        }
      } else {
        List<Integer> fieldPositions = retrieveFieldsPositionsList(cells.get(0), selectedCols);

        for (Cells deepRow : cells) {
          Row metaRow = new Row();
          for (int fieldPosition : fieldPositions) {
            com.stratio.deep.entity.Cell deepCell = deepRow.getCellByIdx(fieldPosition);

            Cell metaCell = new Cell(deepCell.getCellValue());
            metaRow.addCell(deepCell.getCellName(), metaCell);
          }
          rs.add(metaRow);
        }
      }
    }

    StringBuilder logResult = new StringBuilder("Deep Result: ").append(rs.size());
    if (!rs.isEmpty()) {
      logResult.append(" rows & ").append(rs.iterator().next().size()).append(" columns");
    }
    LOG.info(logResult);

    if (LOG.isDebugEnabled()) {
      printDeepResult(rs.getRows());
    }

    return rs;
  }

  private static List<ColumnMetadata> retrieveColumnMetadata(List<Cells> cells,
      List<String> selectedCols) {

    // CellValidator
    AbstractMetadataHelper helper = new CassandraMetadataHelper();

    List<ColumnMetadata> columnList = new ArrayList<>();
    if (!cells.isEmpty()) {
      if (selectedCols.isEmpty()) {
        // Obtain the metadata associated with the columns.
        for (com.stratio.deep.entity.Cell def : cells.get(0).getCells()) {

          // skip internal columns
          if (def.getCellName().toLowerCase().startsWith("stratio")) {
            continue;
          }
          ColumnMetadata columnMetadata = new ColumnMetadata("deep", def.getCellName());
          ColumnType type = helper.toColumnType(def);
          columnMetadata.setType(type);
          columnList.add(columnMetadata);
        }

      } else {
        Cells firstRowCells = cells.get(0);
        for (String selectedCol : selectedCols) {
          ColumnMetadata columnMetadata = new ColumnMetadata("deep", selectedCol);
          if (selectedCol.equalsIgnoreCase("COUNT(*)")) {
            columnMetadata.setType(ColumnType.BIGINT);
          } else {
            com.stratio.deep.entity.Cell cell = firstRowCells.getCellByName(selectedCol);

            ColumnType type = helper.toColumnType(cell);
            columnMetadata.setType(type);
          }
          columnList.add(columnMetadata);
        }
      }
    } else {
      for (String selectedCol : selectedCols) {
        ColumnMetadata columnMetadata = new ColumnMetadata("deep", selectedCol);
        columnMetadata.setType(ColumnType.VARCHAR);
        columnList.add(columnMetadata);
      }
    }

    return columnList;
  }

  private static List<Integer> retrieveFieldsPositionsList(Cells firstRow, List<String> selectedCols) {

    List<Integer> fieldPositions = new ArrayList<>();
    for (String selectCol : selectedCols) {
      Integer position = 0;
      boolean fieldFound = false;

      Iterator<com.stratio.deep.entity.Cell> cellsIt = firstRow.getCells().iterator();
      while (!fieldFound && cellsIt.hasNext()) {
        com.stratio.deep.entity.Cell cell = cellsIt.next();

        if (cell.getCellName().equalsIgnoreCase(selectCol)) {
          fieldPositions.add(position);
          fieldFound = true;
        }

        position++;
      }
    }

    return fieldPositions;
  }

  /**
   * Create a result with a count.
   * 
   * @param rdd rdd to be counted
   * @return ResultSet Result set with only a cell containing the a number of rows
   */
  public static ResultSet buildCountResult(JavaRDD<?> rdd) {
    CassandraResultSet rs = new CassandraResultSet();

    int numberOfRows = (int) rdd.count();

    Row metaRow = new Row();

    Cell metaCell = new Cell(numberOfRows);

    List<ColumnMetadata> columns = new ArrayList<>();
    ColumnMetadata metadata = new ColumnMetadata("count", "COUNT");
    ColumnType type = ColumnType.INT;
    type.setDBMapping("int", Integer.class);
    metadata.setType(type);
    rs.setColumnMetadata(columns);

    metaRow.addCell("COUNT", metaCell);
    rs.add(metaRow);

    return rs;
  }

  /**
   * Print a List of {@link com.stratio.meta.common.data.Row}.
   * 
   * @param rows List of Rows
   */
  protected static void printDeepResult(List<Row> rows) {
    StringBuilder sb = new StringBuilder(System.lineSeparator());
    boolean firstRow = true;
    for (Row row : rows) {
      if (firstRow) {
        for (String colName : row.getCells().keySet()) {
          sb.append(colName).append(" | ");
        }
        sb.append(System.lineSeparator());
        sb.append("---------------------------------------------------------------------");
        sb.append(System.lineSeparator());
      }
      firstRow = false;
      for (Map.Entry<String, Cell> entry : row.getCells().entrySet()) {
        sb.append(String.valueOf(entry.getValue())).append(" - ");
      }
      sb.append(System.lineSeparator());
    }
    sb.append(System.lineSeparator());
    LOG.debug(sb.toString());
  }

  /**
   * Retrieve fields in selection clause.
   * 
   * @param ss SelectStatement of the query
   * @return Array of fields in selection clause or null if all fields has been selected
   */
  public static String[] retrieveSelectorFields(SelectStatement ss) {
    throw new UnsupportedOperationException();
    /*
    // Retrieve selected column names
    SelectionList sList = (SelectionList) ss.getSelectionClause();
    Selection selection = sList.getSelection();
    List<String> columnsSet = new ArrayList<>();
    if (selection instanceof SelectionSelectors) {
      SelectionSelectors sSelectors = (SelectionSelectors) selection;
      for (int i = 0; i < sSelectors.getSelectors().size(); ++i) {
        SelectorMeta selectorMeta = sSelectors.getSelectors().get(i).getSelector();
        if (selectorMeta instanceof SelectorIdentifier) {
          SelectorIdentifier selId = (SelectorIdentifier) selectorMeta;
          columnsSet.add(selId.getField());
        } else if (selectorMeta instanceof SelectorGroupBy) {
          SelectorGroupBy selectorGroupBy = (SelectorGroupBy) selectorMeta;
          if (selectorGroupBy.getGbFunction() != GroupByFunction.COUNT) {
            SelectorIdentifier selId = (SelectorIdentifier) selectorGroupBy.getParam();
            columnsSet.add(selId.getField());
          }
        }
      }
    }
    return columnsSet.toArray(new String[columnsSet.size()]);
    */
  }

  /**
   * Retrieve fields in selection clause.
   * 
   * @param selection SelectStatement of the query
   * @return List of fields in selection clause or null if all fields has been selected
   */
  public static List<String> retrieveSelectors(Selection selection) {

    // Retrieve aggretation function column names
    List<String> columnsSet = new ArrayList<>();
    if (selection instanceof SelectionSelectors) {
      SelectionSelectors sSelectors = (SelectionSelectors) selection;
      for (int i = 0; i < sSelectors.getSelectors().size(); ++i) {
        SelectorMeta selectorMeta = sSelectors.getSelectors().get(i).getSelector();
        if (selectorMeta instanceof SelectorIdentifier) {
          SelectorIdentifier selId = (SelectorIdentifier) selectorMeta;
          columnsSet.add(selId.getField());
        } else if (selectorMeta instanceof SelectorGroupBy) {
          SelectorGroupBy selGroup = (SelectorGroupBy) selectorMeta;
          columnsSet.add(selGroup.getGbFunction().name() + "("
              + ((SelectorIdentifier) selGroup.getParam()).getField() + ")");
        }
      }
    }
    return columnsSet;
  }

  /**
   * Retrieve fields in selection clause.
   * 
   * @param selection SelectStatement of the query
   * @return Array of fields in selection clause or null if all fields has been selected
   */
  public static List<String> retrieveSelectorAggegationFunctions(Selection selection) {

    // Retrieve aggretation function column names
    List<String> columnsSet = new ArrayList<>();
    if (selection instanceof SelectionSelectors) {
      SelectionSelectors sSelectors = (SelectionSelectors) selection;
      for (int i = 0; i < sSelectors.getSelectors().size(); ++i) {
        SelectorMeta selectorMeta = sSelectors.getSelectors().get(i).getSelector();
        if (selectorMeta instanceof SelectorGroupBy) {
          SelectorGroupBy selGroup = (SelectorGroupBy) selectorMeta;
          columnsSet.add(selGroup.getGbFunction().name() + "("
              + ((SelectorIdentifier) selGroup.getParam()).getField() + ")");
        }
      }
    }
    return columnsSet;
  }
}
