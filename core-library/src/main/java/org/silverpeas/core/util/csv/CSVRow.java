package org.silverpeas.core.util.csv;

import java.util.ArrayList;

public class CSVRow extends ArrayList<Object> {
  private static final long serialVersionUID = 7215299921412543498L;

  public CSVRow() {
    super();
  }

  public void addCell(Object value) {
    super.add(value);
  }
}