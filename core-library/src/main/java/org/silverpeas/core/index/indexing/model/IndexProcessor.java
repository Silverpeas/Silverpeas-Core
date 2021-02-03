/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

import org.silverpeas.core.index.search.model.ParseException;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.logging.SilverLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Supplier;

import static org.silverpeas.core.index.indexing.IndexingLogger.indexingLogger;

/**
 * @author silveryocha
 */
public class IndexProcessor {
  private static final SettingBundle searchSettings =
      ResourceLocator.getSettingBundle("org.silverpeas.index.search.searchEngineSettings");
  private static final StampedLock SEARCH_LOCK = new StampedLock();
  private static final Object UPDATED_MUTEX = new Object();
  private static final Object MUTEX = new Object();
  private static final List<String> UPDATED_INDEXES = new ArrayList<>();

  /**
   * Hidden constructor.
   */
  private IndexProcessor() {
  }

  public static <R> R doSearch(SearchIndexProcess<R> searchIndexProcess, Supplier<R> defaultReturn) throws ParseException {
    final SilverLogger logger = indexingLogger();
    final long stamp;
    synchronized (MUTEX) {
      stamp = SEARCH_LOCK.tryReadLock();
      if (stamp == 0) {
        logger.debug("starting and ending directly search processing because of index removing");
      } else {
        logger.debug(
            "starting search processing and there are currently {0} search process(es) performing",
            SEARCH_LOCK.getReadLockCount());
      }
    }
    if (stamp == 0) {
      return defaultReturn.get();
    }
    try {
      return searchIndexProcess.process();
    } finally {
      synchronized (MUTEX) {
        SEARCH_LOCK.unlockRead(stamp);
        logger.debug(
            "ending search processing and there are currently {0} search process(es) performing",
            SEARCH_LOCK.getReadLockCount());
        closeIndexReaders();
      }
    }
  }

  public static void doFlush(FlushIndexProcess flushIndexProcess) {
    final List<String> updatedPaths = flushIndexProcess.process();
    synchronized (UPDATED_MUTEX) {
      UPDATED_INDEXES.addAll(updatedPaths);
    }
    synchronized (MUTEX) {
      closeIndexReaders();
    }
  }

  static void doRemoveAll(RemoveAllIndexesProcess removeAllIndexesProcess) {
    final long stamp = SEARCH_LOCK.writeLock();
    final SilverLogger logger = indexingLogger();
    logger.debug("starting remove of all index entries");
    try {
      logger.debug("closing all index readers");
      IndexReadersCache.closeAllIndexReaders();
      logger.debug("removing indexes");
      removeAllIndexesProcess.process();
    } catch (IOException e) {
      logger.error(e);
    } finally {
      SEARCH_LOCK.unlockWrite(stamp);
    }
  }

  private static void closeIndexReaders() {
    final SilverLogger logger = indexingLogger();
    if (SEARCH_LOCK.getReadLockCount() == 0) {
      synchronized (UPDATED_MUTEX) {
        if (searchSettings.getBoolean("index.reader.closeAfterLastSearch", false)) {
          logger.debug("no search is currently being performed, so closing all readers");
          IndexReadersCache.closeAllIndexReaders();
          UPDATED_INDEXES.clear();
        } else {
          logger.debug("no search is currently being performed, so closing readers if any");
          final Iterator<String> it = UPDATED_INDEXES.iterator();
          while (it.hasNext()) {
            final String path = it.next();
            IndexReadersCache.closeIndexReader(path);
            it.remove();
          }
        }
      }
    } else {
      logger.debug("no reader close is performed as {0} search process(es) are currently performed",
          SEARCH_LOCK.getReadLockCount());
    }
  }

  /**
   * A search process.
   * @param <R> the type of result of processing.
   */
  public interface SearchIndexProcess<R> {
    R process() throws ParseException;
  }

  /**
   * A flush process.
   */
  public interface FlushIndexProcess {
    List<String> process();
  }

  /**
   * A flush process.
   */
  public interface RemoveAllIndexesProcess {
    void process() throws IOException;
  }
}
