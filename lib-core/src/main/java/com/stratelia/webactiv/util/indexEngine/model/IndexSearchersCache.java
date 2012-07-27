/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.util.indexEngine.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.search.IndexSearcher;

import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class IndexSearchersCache {
  private final static IndexSearchersCache instance = new IndexSearchersCache();
  private Map<String, IndexSearcher> indexSearchers;

  private IndexSearchersCache() {
    indexSearchers = new HashMap<String, IndexSearcher>();
  }

  private static IndexSearchersCache getInstance() {
    return instance;
  }

  public static synchronized IndexSearcher getIndexSearcher(String path) {
    if (!getInstance().indexSearchers.containsKey(path)) {
      try {
        getInstance().indexSearchers.put(path, new IndexSearcher(path));
      } catch (Exception e) {
        SilverTrace.warn("searchEngine", "IndexManager.getIndexSearcher()",
            "searchEngine.MSG_CANT_OPEN_INDEX_SEARCHER", e);
      }
    }
    return getInstance().indexSearchers.get(path);
  }

  public static synchronized void removeIndexSearcher(String path) {
    if (getInstance().indexSearchers.containsKey(path)) {
      IndexSearcher indexSearcher = getInstance().indexSearchers.get(path);
      try {
        indexSearcher.close();
      } catch (IOException e) {
        SilverTrace.warn("indexEngine", "IndexManager.removeIndexSearcher",
            "indexEngine.MSG_CANT_CLOSE_INDEX_SEARCHER", path, e);
      }
      getInstance().indexSearchers.remove(path);
    }
  }

}
