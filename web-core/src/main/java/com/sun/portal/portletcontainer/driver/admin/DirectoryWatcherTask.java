package com.sun.portal.portletcontainer.driver.admin;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;

public class DirectoryWatcherTask extends TimerTask {

  private String directoryToWatch;

  private FileFilter filter;

  private DirectoryChangedListener listener;

  public DirectoryWatcherTask(String directoryToWatch,
      DirectoryChangedListener listener) {
    this(directoryToWatch, null, listener);
  }

  // Currently only notifies if files matching the filter are added to the
  // directory being watched.

  public DirectoryWatcherTask(String dirToWatch, FileFilter fileFilter,
      DirectoryChangedListener dirChangeListener) {
    directoryToWatch = dirToWatch;
    listener = dirChangeListener;

    if (fileFilter == null) {
      filter = new FileFilter() {
        // Default FileFilter accepts all files.
        public boolean accept(File pathname) {
          return true;
        }
      };
    } else {
      filter = fileFilter;
    }
  }

  public void run() {
    File[] fileArray = new File(directoryToWatch).listFiles(filter);
    if (fileArray != null) {
      List currentFileList = Arrays.asList(fileArray);

      Iterator iterator = currentFileList.iterator();

      while (iterator.hasNext()) {
        File currentFile = (File) iterator.next();
        listener.fileAdded(currentFile);
      }
    }
  }
}
