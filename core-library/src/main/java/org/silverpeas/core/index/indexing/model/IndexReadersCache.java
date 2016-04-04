/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.index.indexing.model;

import org.silverpeas.core.silvertrace.SilverTrace;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

public class IndexReadersCache {
  private final static IndexReadersCache instance = new IndexReadersCache();
  private Map<String, IndexReader> indexReaders;

  private IndexReadersCache() {
    indexReaders = new HashMap<>();
  }

  private static IndexReadersCache getInstance() {
    return instance;
  }

  public static synchronized IndexReader getIndexReader(String path) {
    if (!getInstance().indexReaders.containsKey(path)) {
      try {
        getInstance().indexReaders.put(path, IndexReader.open(FSDirectory.open(new File(path))));
      } catch (Exception e) {
        SilverTrace.warn("searchEngine", "IndexManager.getIndexReader()",
            "searchEngine.MSG_CANT_OPEN_INDEX_SEARCHER", e);
      }
    }
    return getInstance().indexReaders.get(path);
  }

  public static synchronized void removeIndexReader(String path) {
    if (getInstance().indexReaders.containsKey(path)) {
      IndexReader indexReader = getInstance().indexReaders.get(path);
      try {
        indexReader.close();
      } catch (IOException e) {
        SilverTrace.warn("indexing", "IndexManager.removeIndexReader",
            "indexing.MSG_CANT_CLOSE_INDEX_SEARCHER", path, e);
      }
      getInstance().indexReaders.remove(path);
    }
  }

}
