package com.stratelia.webactiv.util.indexEngine.model;

import java.io.File;
import java.io.FileFilter;

public class FileSPFilter implements FileFilter {

  private static FileSPFilter INSTANCE = null;

  private FileSPFilter() {
  }

  public synchronized static FileSPFilter getInstance() {
    if (INSTANCE == null)
      INSTANCE = new FileSPFilter();
    return INSTANCE;
  }

  public boolean accept(File file) {
    return file != null && file.isFile();
  }

}
