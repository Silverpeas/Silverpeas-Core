package org.silverpeas.web.directory.model;

import org.silverpeas.core.util.csv.CSVRow;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CSVHeader {
  private final CSVRow csvRow = new CSVRow();
  private final LinkedHashMap<String, List<String>> colsBySource = new LinkedHashMap<>();
  private final Map<String, Integer> indexOfSourceCols = new HashMap<>();

  public void addStandardCol(final String label) {
    csvRow.addCell(label);
  }

  public CSVRow asCSVRow() {
    aggregateSources();
    return csvRow;
  }

  public void addSourceCols(final String sourceId, final List<String> cols) {
    colsBySource.put(sourceId, cols);
  }

  private void aggregateSources() {
    colsBySource.forEach((s, l) -> {
      // check if source uses same cols that already indexed source
      final String indexedSource = getIndexedSourceWithSameCols(l);
      if (indexedSource != null) {
        indexOfSourceCols.put(s, indexOfSourceCols.get(indexedSource));
      } else {
        indexOfSourceCols.put(s, csvRow.size());
        l.forEach(csvRow::addCell);
      }
    });
  }

  private String getIndexedSourceWithSameCols(final List<String> cols) {
    for (final String indexedSource : indexOfSourceCols.keySet()) {
      if (colsBySource.get(indexedSource).equals(cols)) {
        return indexedSource;
      }
    }
    return null;
  }

  public Optional<Integer> getIndexOfSourceCols(final String sourceId) {
    return Optional.ofNullable(indexOfSourceCols.get(sourceId));
  }

  public int getTotalOfCols() {
    return csvRow.size();
  }
}