/*
 * CDDL HEADER START
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.sun.com/cddl/cddl.html and legal/CDDLv1.0.txt
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 * CDDL HEADER END
 */
package com.sun.portal.portletcontainer.driver.admin;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
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
        @Override
        public boolean accept(File pathname) {
          return true;
        }
      };
    } else {
      filter = fileFilter;
    }
  }

  @Override
  public void run() {
    File[] fileArray = new File(directoryToWatch).listFiles(filter);
    if (fileArray != null) {
      List<File> currentFileList = Arrays.asList(fileArray);
      for (File currentFile : currentFileList) {
        listener.fileAdded(currentFile);
      }
    }
  }
}
