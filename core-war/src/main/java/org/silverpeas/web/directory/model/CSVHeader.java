package org.silverpeas.web.directory.model;

import org.silverpeas.core.util.csv.CSVRow;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CSVHeader {

  CSVRow csvRow = new CSVRow();
  LinkedHashMap<String, List<String>> colsBySource = new LinkedHashMap<>();
  Map<String, Integer> indexOfSourceCols = new HashMap<>();
  int nbCols = 0;

  public void addStandardCol(String label) {
    csvRow.addCell(label);
  }

  public CSVRow asCSVRow() {
    agregateSources();
    return csvRow;
  }

  public void addSourceCols(String sourceId, List<String> cols) {
    colsBySource.put(sourceId, cols);
  }

  private void agregateSources() {
    nbCols = 0;
    for (Map.Entry<String,List<String>> entry : colsBySource.entrySet()) {
      if (indexOfSourceCols.isEmpty()) {
        indexOfSourceCols.put(entry.getKey(), 0);
        List<String> labels = entry.getValue();
        nbCols = labels.size();
        for (String label : labels) {
          csvRow.addCell(label);
        }
      } else {
        // check if source uses same cols that already indexed source
        String indexedSource = getIndexedSourceWithSameCols(entry.getValue());
        if (indexedSource != null) {
          indexOfSourceCols.put(entry.getKey(), indexOfSourceCols.get(indexedSource));
        } else {
          indexOfSourceCols.put(entry.getKey(), nbCols);
          List<String> labels = entry.getValue();
          nbCols += labels.size();
          for (String label : labels) {
            csvRow.addCell(label);
          }
        }
      }
    }
  }

  private String getIndexedSourceWithSameCols(List<String> cols) {
    for (String indexedSource : indexOfSourceCols.keySet()) {
      if (colsBySource.get(indexedSource).equals(cols)) {
        return indexedSource;
      }
    }
    return null;
  }

  public int getIndexOfSourceCols(String sourceId) {
    return indexOfSourceCols.get(sourceId);
  }

  public int getTotalOfCols() {
    return nbCols;
  }

}