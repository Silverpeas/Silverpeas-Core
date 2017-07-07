/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.index.indexing.model;

import org.silverpeas.core.thread.ManagedThreadPool;
import org.silverpeas.core.thread.task.AbstractRequestTask;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * This task is in charge of processing indexation requests.
 */
public class IndexerThread extends AbstractRequestTask<IndexerProcessContext> {

  private static final int QUEUE_LIMIT = 200;
  private static final Semaphore queueSemaphore = new Semaphore(QUEUE_LIMIT, true);

  /**
   * Please consult {@link AbstractRequestTask} documentation.
   */
  private static final List<Request<IndexerProcessContext>> requestList =
      new ArrayList<>(QUEUE_LIMIT);

  /**
   * Indicator that represents the simple state of the task running.
   */
  private static boolean running = false;

  /**
   * This instance must be set with the thread which is pushing a new request and not the one
   * which is processing the requests.
   */
  private final IndexManager indexManager;

  /**
   * Hidden constructor in order to oblige the caller to use static methods.
   * @param indexManager the instance of the manager of indexation.
   */
  private IndexerThread(final IndexManager indexManager) {
    super();
    this.indexManager = indexManager;
  }

  /**
   * Builds and starts the task.
   */
  private static void startIfNotAlreadyDone() {
    if (!running) {
      running = true;
      ManagedThreadPool.getPool().invoke(new IndexerThread(IndexManager.get()));
    }
  }

  private static void stop() {
    running = false;
  }

  /**
   * Add a request 'add entry index'.
   * @param indexEntry the index entry ro process.
   */
  public static void addIndexEntry(FullIndexEntry indexEntry) {
    push(new AddIndexEntryRequest(indexEntry));
  }

  /**
   * Add a request 'remove entry index'.
   * @param indexEntry the index entry ro process.
   */
  public static void removeIndexEntry(IndexEntryKey indexEntry) {
    push(new RemoveIndexEntryRequest(indexEntry));
  }

  private static void push(Request<IndexerProcessContext> request) {
    try {
      getLogger().debug("acquiring queue semaphore ({0} available permits before acquire)",
          queueSemaphore.availablePermits());
      queueSemaphore.acquire();
      synchronized (requestList) {
        getLogger().debug("pushing new request {0} ({1} requests queued before push)",
            request.getClass().getSimpleName(), requestList.size());
        requestList.add(request);
        startIfNotAlreadyDone();
      }
    } catch (InterruptedException e) {
      getLogger().error(e);
    }
  }

  private static SilverLogger getLogger() {
    return SilverLogger.getLogger(IndexerThread.class);
  }

  @Override
  protected List<Request<IndexerProcessContext>> getRequestList() {
    return requestList;
  }

  @Override
  protected void taskIsEnding() {
    stop();
  }

  @Override
  protected void beforeRequestProcessing() {
    super.beforeRequestProcessing();
    getLogger().debug("releasing queue semaphore ({0} available permits before release)",
        queueSemaphore.availablePermits());
    queueSemaphore.release();
  }

  @Override
  protected void afterNoMoreRequest() {
    super.afterNoMoreRequest();
    getLogger().debug("flushing manager of indexation");
    indexManager.flush();
  }

  @Override
  protected IndexerProcessContext getProcessContext() {
    return new IndexerProcessContext(indexManager);
  }
}

class IndexerProcessContext implements AbstractRequestTask.ProcessContext {
  private final IndexManager indexManager;

  IndexerProcessContext(final IndexManager indexManager) {
    this.indexManager = indexManager;
  }

  IndexManager getIndexManager() {
    return indexManager;
  }
}

/**
 * An AddEntryIndex add an entry index.
 */
class AddIndexEntryRequest implements AbstractRequestTask.Request<IndexerProcessContext> {
  private final FullIndexEntry indexEntry;

  /**
   * @param indexEntry the index entry to process.
   */
  AddIndexEntryRequest(FullIndexEntry indexEntry) {
    this.indexEntry = indexEntry;
  }

  /**
   * @param context process context.
   */
  @Override
  public void process(IndexerProcessContext context) {
    context.getIndexManager().addIndexEntry(indexEntry);
  }
}

/**
 * A RemoveEntryIndex remove an entry index.
 */
class RemoveIndexEntryRequest implements AbstractRequestTask.Request<IndexerProcessContext> {
  private final IndexEntryKey indexEntry;

  /**
   * @param indexEntry the index entry to process.
   */
  RemoveIndexEntryRequest(IndexEntryKey indexEntry) {
    this.indexEntry = indexEntry;
  }

  /**
   * @param context process context.
   */
  @Override
  public void process(IndexerProcessContext context) {
    context.getIndexManager().removeIndexEntry(indexEntry);
  }
}
