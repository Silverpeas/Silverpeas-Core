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
package org.silverpeas.search.indexEngine.model;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.search.searchEngine.model.ParseException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author silveryocha
 */
public class IndexProcessor {
  private static final Object UPDATED_MUTEX = new Object();
  private static final Object MUTEX = new Object();
  private static final List<String> UPDATED_INDEXES = new ArrayList<String>();
  private static int currentSearchProcessing = 0;

  /**
   * Hidden constructor.
   */
  private IndexProcessor() {
  }

  public static <R> R doSearch(SearchIndexProcess<R> searchIndexProcess) throws ParseException {
    try {
      synchronized (MUTEX) {
        currentSearchProcessing += 1;
        SilverTrace.debug("searchEngine", IndexProcessor.class.toString(),
            "starts search processing and there are currently " + currentSearchProcessing +
                " search process(es) performing");
      }
      return searchIndexProcess.process();
    } finally {
      synchronized (MUTEX) {
        currentSearchProcessing -= 1;
        closeIndexReaders();
      }
    }
  }

  public static void doFlush(FlushIndexProcess flushIndexProcess) {
    final List<String> updatedPaths = flushIndexProcess.process();
    SilverTrace.debug("searchEngine", IndexProcessor.class.toString(),
        "flushes " + updatedPaths.size() + " writer(s)");
    synchronized (UPDATED_MUTEX) {
      UPDATED_INDEXES.addAll(updatedPaths);
    }
    synchronized (MUTEX) {
      closeIndexReaders();
    }
  }

  private static void closeIndexReaders() {
    if (currentSearchProcessing == 0) {
      synchronized (UPDATED_MUTEX) {
        SilverTrace.debug("searchEngine", IndexProcessor.class.toString(),
            "no search is currently being performed, so closing readers if any");
        final Iterator<String> it = UPDATED_INDEXES.iterator();
        while (it.hasNext()) {
          final String path = it.next();
          SilverTrace.debug("searchEngine", IndexProcessor.class.toString(),
              "closing reader on path " + path);
          IndexReadersCache.closeIndexReader(path);
          it.remove();
        }
      }
    } else {
      SilverTrace.debug("searchEngine", IndexProcessor.class.toString(),
          "no reader close is performed as " + currentSearchProcessing +
              " search process(es) are currently performed");
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
