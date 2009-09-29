package com.stratelia.webactiv.util.indexEngine.model;

import java.io.File;
import java.util.Comparator;

public class FilenameComparator implements Comparator {

  static public FilenameComparator comparator = new FilenameComparator();

  public int compare(Object o1, Object o2) {
    File file1 = (File) o1;
    File file2 = (File) o2;

    int compareResult = file1.getName().compareTo(file2.getName());

    return compareResult;
  }

  /**
   * This comparator equals self only.
   * 
   */
  public boolean equals(Object o) {
    return o == this;
  }
}
