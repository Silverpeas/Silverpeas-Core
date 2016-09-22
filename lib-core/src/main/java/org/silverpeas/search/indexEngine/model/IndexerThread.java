/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
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

package org.silverpeas.search.indexEngine.model;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.cache.service.CacheServiceFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * A thread IndexerThread index in the background a batch of index requests. All the public methods
 * are static, so only one thread runs and processes the requests.
 */
public class IndexerThread extends Thread {

  private static final int queueLimit = 200;
  private static final Semaphore queueSemaphore = new Semaphore(queueLimit, true);

  /**
   * Builds and starts the thread which will process all the requests. This method is synchonized on
   * the requests queue in order to guarantee that only one IndexerThread is running.
   * @param indexManager
   */
  static public void start(IndexManager indexManager) {
    synchronized (requestList) {
      if (indexerThread == null) {
        SilverTrace.debug("indexEngine", "IndexerThread", "indexEngine.INFO_STARTS_INDEXER_THREAD");
        indexerThread = new IndexerThread(indexManager);
        indexerThread.start();
      }
    }
  }

  /**
   * Add a request 'add entry index'
   * @param indexEntry
   */
  static public void addIndexEntry(FullIndexEntry indexEntry) {
    try {
      queueSemaphore.acquire();
      synchronized (requestList) {
        SilverTrace.debug("indexEngine", "IndexerThread", "indexEngine.INFO_ADDS_ADD_REQUEST",
            indexEntry.toString());
        requestList.add(new AddIndexEntryRequest(indexEntry));
        requestList.notify();
      }
    } catch (InterruptedException e) {
      SilverTrace
          .error("indexEngine", "IndexerThread", "indexEngine.INFO_STARTS_INDEXER_THREAD", e);
    }
  }

  /**
   * Add a request 'remove entry index'
   */
  static public void removeIndexEntry(IndexEntryPK indexEntry) {
    try {
      queueSemaphore.acquire();
      synchronized (requestList) {
        SilverTrace.debug("indexEngine", "IndexerThread", "indexEngine.INFO_ADDS_REMOVE_REQUEST",
            indexEntry.toString());
        requestList.add(new RemoveIndexEntryRequest(indexEntry));
        requestList.notify();
      }
    } catch (InterruptedException e) {
      SilverTrace
          .error("indexEngine", "IndexerThread", "indexEngine.INFO_STARTS_INDEXER_THREAD", e);
    }
  }

  /**
   * Process all the requests. When the queue is empty : sends an optimize query to the
   * indexManager. This method should be private but is already declared public in the base class
   * Thread.
   */
  @Override
  public void run() {
    Request request;

    while (true) {
      /*
       * First, all the requests are processed until the queue becomes empty.
       */
      do {
        CacheServiceFactory.clearAllThreadCaches();
        request = null;

        synchronized (requestList) {
          SilverTrace.info("indexEngine", "IndexerThread", "root.MSG_GEN_PARAM_VALUE",
              "# of items to index = " + requestList.size() + ", queueSemaphore available: " +
                  queueSemaphore.availablePermits());
          if (!requestList.isEmpty()) {
            request = requestList.remove(0);
            queueSemaphore.release();
          }
        }

        /*
         * Each request is processed out of the synchronized block so the others threads (which put
         * the requests) will not be blocked.
         */
        if (request != null) {
          try {
            request.process(indexManager);
          } catch (Exception e) {
            SilverTrace.error("indexEngine", "IndexerThread", "indexEngine.INFO_PROCESS_ERROR", e);
          }
        }

      } while (request != null);

      /**
       * Then the index writer is flushed.
       */
      indexManager.flush();

      /*
       * Finally, unless a new request has been made while optimisation, we wait the notification of
       * a new request to be processed.
       */
      try {
        synchronized (requestList) {
          if (requestList.isEmpty()) {
            requestList.wait();
          }
        }
      } catch (InterruptedException e) {
        SilverTrace
            .debug("indexEngine", "IndexerThread", "indexEngine.INFO_INTERRUPTED_WHILE_WAITING");
      }
    }
  }

  /**
   * The requests are stored in a shared list of Requests. In order to guarantee serial access, all
   * access will be synchronized on this list. Futhermore this list is used to synchronize the
   * providers and the consumers of the list :
   *
   * <PRE>
   * // provider
   * synchronized(requestList)
   * {
   * requestList.add(...);
   * requestList.notify();
   * }
   *
   * // consumer
   * synchronized(requestList)
   * {
   * requestList.wait();
   * ... = requestList.remove(...);
   * }
   * </PRE>
   */
  static private final List<Request> requestList = new ArrayList<Request>(queueLimit);

  /**
   * All the requests are processed by a single background thread. This thread is built and started
   * by the start method.
   */
  static private IndexerThread indexerThread = null;

  /**
   * All the requests will be sent to the IndexManager indexManager.
   */
  private final IndexManager indexManager;

  /**
   * The constructor is private : only one IndexerThread will be created to process all the request.
   */
  private IndexerThread(IndexManager indexManager) {
    this.indexManager = indexManager;
  }

}

/**
 * Each request must define a method called process which will process the request with a given
 * IndexManager.
 */
interface Request {

  /**
   * Method declaration
   * @param indexManager
   */
  void process(IndexManager indexManager);
}

/**
 * An AddEntryIndex add an entry index.
 */
class AddIndexEntryRequest implements Request {

  /**
   * Constructor declaration
   * @param indexEntry
   */
  public AddIndexEntryRequest(FullIndexEntry indexEntry) {
    this.indexEntry = indexEntry;
  }

  /**
   * Method declaration
   * @param indexManager
   */
  @Override
  public void process(IndexManager indexManager) {
    SilverTrace.info("indexEngine", "AddIndexEntryRequest.process", "root.MSG_GEN_ENTER_METHOD",
        indexEntry.toString());
    indexManager.addIndexEntry(indexEntry);
    SilverTrace.info("indexEngine", "AddIndexEntryRequest.process", "root.MSG_GEN_EXIT_METHOD",
        indexEntry.toString());
  }

  private final FullIndexEntry indexEntry;
}

/**
 * A RemoveEntryIndex remove an entry index.
 */
class RemoveIndexEntryRequest implements Request {

  /**
   * Constructor declaration
   * @param indexEntry
   */
  public RemoveIndexEntryRequest(IndexEntryPK indexEntry) {
    this.indexEntry = indexEntry;
  }

  /**
   * Method declaration
   * @param indexManager
   */
  @Override
  public void process(IndexManager indexManager) {
    SilverTrace.info("indexEngine", "RemoveIndexEntryRequest.process", "root.MSG_GEN_ENTER_METHOD",
        indexEntry.toString());
    indexManager.removeIndexEntry(indexEntry);
    SilverTrace.info("indexEngine", "RemoveIndexEntryRequest.process", "root.MSG_GEN_EXIT_METHOD",
        indexEntry.toString());
  }

  private final IndexEntryPK indexEntry;
}
