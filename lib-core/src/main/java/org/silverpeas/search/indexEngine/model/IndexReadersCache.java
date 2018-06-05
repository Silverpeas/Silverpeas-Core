/**
 * Copyright (C) 2000 - 2013 Silverpeas
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.search.indexEngine.model;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class IndexReadersCache {
  private static final Object READER_MUTEX = new Object();
  private static final Map<String, IndexReader> INDEX_READERS = new HashMap<String, IndexReader>();

  /**
   * Hidden constructor
   */
  private IndexReadersCache() {
  }

  /**
   * This method must be called only within a
   * {@link IndexProcessor.SearchIndexProcess#process()} implementation in order to get a
   * right behavior against the concurrent accesses.
   * @param path the index root path.
   * @return the {@link IndexReader} well initialized if necessary.
   */
  public static IndexReader getIndexReader(String path) {
    synchronized (READER_MUTEX) {
      final File rootPath = new File(path);
      IndexReader indexReader = INDEX_READERS.get(path);
      if (indexReader == null && rootPath.exists()) {
        try {
          indexReader = IndexReader.open(FSDirectory.open(rootPath));
          INDEX_READERS.put(path, indexReader);
        } catch (Exception e) {
          SilverTrace.warn("searchEngine", "IndexManager.getIndexReader()",
              "searchEngine.MSG_CANT_OPEN_INDEX_SEARCHER", e);
        }
      } else if (indexReader != null && !rootPath.exists()) {
        SilverTrace.warn("searchEngine", "IndexManager.getIndexReader()",
            "searchEngine.MSG_CANT_OPEN_INDEX_SEARCHER",
            "index reader exists in cache but no index path is existing! (" + path + ")");
        closeIndexReader(path);
        indexReader = null;
      }
      return indexReader;
    }
  }

  static void closeIndexReader(String path) {
    synchronized (READER_MUTEX) {
      final IndexReader indexReader = INDEX_READERS.remove(path);
      if (indexReader != null) {
        try {
          indexReader.close();
        } catch (IOException e) {
          SilverTrace.warn("indexEngine", "IndexManager.removeIndexReader",
              "indexEngine.MSG_CANT_CLOSE_INDEX_SEARCHER", path, e);
        }
      }
    }
  }
}
