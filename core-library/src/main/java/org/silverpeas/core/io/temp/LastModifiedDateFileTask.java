/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.io.temp;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.silverpeas.core.thread.ManagedThreadPool;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A task in order to update asynchronously the last modified date of given file and folders.
 */
public class LastModifiedDateFileTask implements Runnable {

  private static final Map<File, Long> requestMap = new LinkedHashMap<File, Long>();

  /**
   * All the requests are processed by a single background thread. This thread is built and
   * started by the start method.
   */
  private static boolean running = false;

  /**
   * Builds and starts the thread which will process all the requests. This method is
   * synchronized on the requests queue in order to guarantee that only one
   * LastModifiedDateFileThread is running.
   */
  private static void startIfNotAlreadyDone() {
    if (!isRunning()) {
      running = true;
      ManagedThreadPool.invoke(new LastModifiedDateFileTask());
    }
  }

  /**
   * Add a file to process.
   * @param file
   */
  public static void addFile(File file) {
    synchronized (requestMap) {
      requestMap.put(file, System.currentTimeMillis());
      startIfNotAlreadyDone();
    }
  }

  /**
   * Indicates if the thread is running.
   * @return true if running, false otherwise.
   */
  public static boolean isRunning() {
    synchronized (requestMap) {
      return running;
    }
  }

  /**
   * The constructor is private : only one MailSenderThread will be created to process all the
   * request.
   */
  private LastModifiedDateFileTask() {
  }

  /**
   * Process all the requests. This method should be private but is already declared public in
   * the
   * base class Thread.
   */
  @Override
  public void run() {
    Pair<File, Long> pair = nextRequest();

    // The loop condition must be verified on a private attribute of run method (not on the static
    // running attribute) in order to avoid concurrent access.
    while (pair != null) {

      /*
       * Each request is processed out of the synchronized block so the others threads (which put
       * the requests) will not be blocked.
       */
      try {
        File currentFile = pair.getKey();
        Long lastModifiedDate = pair.getValue();
        if (currentFile.isFile()) {
          currentFile.setLastModified(lastModifiedDate);
        } else if (currentFile.isDirectory()) {
          for (File file : FileUtils.listFilesAndDirs(currentFile, FileFilterUtils.trueFileFilter(),
              FileFilterUtils.trueFileFilter())) {
            file.setLastModified(lastModifiedDate);
          }
        }
      } catch (Exception ignore) {
      }

      // Getting the next request if any.
      pair = nextRequest();
    }
  }

  /**
   * Gets the next request.
   * @return the next request.
   */
  private Pair<File, Long> nextRequest() {
    synchronized (requestMap) {
      final Pair<File, Long> nextRequest;
      if (!requestMap.isEmpty()) {
        File file = requestMap.keySet().iterator().next();
        Long newLastModifiedTime = requestMap.remove(file);
        nextRequest = Pair.of(file, newLastModifiedTime);
      } else {
        nextRequest = null;
        running = false;
      }
      return nextRequest;
    }
  }
}
