package org.silverpeas.core.util.csv;

import java.util.ArrayList;

public class CSVRow extends ArrayList<Object> {

  public CSVRow() {
    super();
  }

  public void addCell(Object value) {
    super.add(value);
  }

  public Object getCell(int i) {
    return super.get(i);
  }

}