package org.silverpeas.core.util.csv;

import java.util.ArrayList;
import java.util.stream.IntStream;

/**
 * Represents a line to add to a CSV builder.
 */
public class CSVRow extends ArrayList<Object> {
  private static final long serialVersionUID = 7215299921412543498L;

  public CSVRow() {
    super();
  }

  /**
   * Initializing the row with null values.
   * @param rowSize the number of column to initialize.
   */
  public CSVRow(final int rowSize) {
    super(rowSize);
    IntStream.range(0, rowSize).forEach(i -> add(null));
  }

  public void addCell(Object value) {
    super.add(value);
  }

  public void setCell(int index, Object value) {
    super.set(index, value);
  }
}