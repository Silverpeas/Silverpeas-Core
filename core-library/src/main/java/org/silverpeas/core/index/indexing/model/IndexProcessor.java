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

import org.silverpeas.core.index.search.model.ParseException;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author silveryocha
 */
public class IndexProcessor {
  private static final Object UPDATED_MUTEX = new Object();
  private static final Object MUTEX = new Object();
  private static final List<String> UPDATED_INDEXES = new ArrayList<>();
  private static int currentSearchProcessing = 0;

  /**
   * Hidden constructor.
   */
  private IndexProcessor() {
  }

  public static <R> R doSearch(SearchIndexProcess<R> searchIndexProcess) throws ParseException {
    final SilverLogger logger = SilverLogger.getLogger(IndexProcessor.class);
    try {
      synchronized (MUTEX) {
        currentSearchProcessing += 1;
        logger.debug(
            "starts search processing and there are currently {0} search process(es) performing",
            currentSearchProcessing);
      }
      return searchIndexProcess.process();
    } finally {
      synchronized (MUTEX) {
        currentSearchProcessing -= 1;
        logger.debug(
            "ends search processing and there are currently {0} search process(es) performing",
            currentSearchProcessing);
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

  private static void closeIndexReaders() {
    final SilverLogger logger = SilverLogger.getLogger(IndexProcessor.class);
    if (currentSearchProcessing == 0) {
      synchronized (UPDATED_MUTEX) {
        logger.debug("no search is currently being performed, so closing readers if any");
        final Iterator<String> it = UPDATED_INDEXES.iterator();
        while (it.hasNext()) {
          final String path = it.next();
          IndexReadersCache.closeIndexReader(path);
          it.remove();
        }
      }
    } else {
      logger.debug("no reader close is performed as {0} search process(es) are currently performed",
          currentSearchProcessing);
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
}
