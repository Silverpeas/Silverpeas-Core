package com.stratelia.webactiv.util.indexEngine.model;

import java.io.File;
import java.io.FileFilter;

public class DirectorySPFilter implements FileFilter {

  private static DirectorySPFilter INSTANCE = null;

  private DirectorySPFilter() {
  }

  public synchronized static DirectorySPFilter getInstance() {
    if (INSTANCE == null)
      INSTANCE = new DirectorySPFilter();
    return INSTANCE;
  }

  public boolean accept(File file) {
    return file != null && file.isDirectory();
  }

}
