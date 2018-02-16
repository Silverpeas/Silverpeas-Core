/*
 * Copyright (C) 2000 - 2018 Silverpeas
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

import org.silverpeas.core.thread.task.AbstractRequestTask;
import org.silverpeas.core.thread.task.RequestTaskManager;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;

/**
 * This task is in charge of processing indexation requests.
 */
public class IndexerTask extends AbstractRequestTask<IndexerTask.IndexerProcessContext> {

  private static final int QUEUE_LIMIT = 200;

  /**
   * This instance must be set with the thread which is pushing a new request and not the one
   * which is processing the requests.
   */
  @Inject
  private IndexManager indexManager;

  private static SilverLogger getLogger() {
    return SilverLogger.getLogger(IndexerTask.class);
  }

  /**
   * Add a request 'add entry index'.
   * @param indexEntry the index entry ro process.
   */
  public static void addIndexEntry(FullIndexEntry indexEntry) {
    RequestTaskManager.push(IndexerTask.class, new AddIndexEntryRequest(indexEntry));
  }

  /**
   * Add a request 'remove entry index'.
   * @param indexEntry the index entry ro process.
   */
  public static void removeIndexEntry(IndexEntryKey indexEntry) {
    RequestTaskManager.push(IndexerTask.class, new RemoveIndexEntryRequest(indexEntry));
  }

  /**
   * Add a request 'remove index entries by scope'.
   * @param scope the scope of index entries to process.
   */
  public static void removeIndexEntriesByScope(String scope) {
    RequestTaskManager.push(IndexerTask.class, new RemoveScopedIndexEntriesRequest(scope));
  }

  @Override
  protected int getRequestQueueLimit() {
    return QUEUE_LIMIT;
  }

  @Override
  protected void afterNoMoreRequest() {
    super.afterNoMoreRequest();
    indexManager.flush();
  }

  @Override
  protected IndexerProcessContext getProcessContext() {
    return new IndexerProcessContext(indexManager);
  }

  static class IndexerProcessContext implements AbstractRequestTask.ProcessContext {
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
  static class AddIndexEntryRequest implements AbstractRequestTask.Request<IndexerProcessContext> {
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
  static class RemoveIndexEntryRequest
      implements AbstractRequestTask.Request<IndexerProcessContext> {
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

  /**
   * A RemoveEntryIndex remove an entry index.
   */
  static class RemoveScopedIndexEntriesRequest
      implements AbstractRequestTask.Request<IndexerProcessContext> {
    private final String scope;

    /**
     * @param scope the scope of index entries to process.
     */
    RemoveScopedIndexEntriesRequest(String scope) {
      this.scope = scope;
    }

    /**
     * @param context process context.
     */
    @Override
    public void process(IndexerProcessContext context) {
      context.getIndexManager().removeIndexEntries(scope);
    }
  }
}