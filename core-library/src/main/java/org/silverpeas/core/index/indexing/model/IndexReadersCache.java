/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.index.indexing.model;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static org.silverpeas.core.index.indexing.IndexingLogger.indexingLogger;

public class IndexReadersCache {
  private static final Object READER_MUTEX = new Object();
  private static final Map<String, IndexReader> INDEX_READERS = new HashMap<>();
  private static final BiConsumer<String, IndexReader> CLOSE_INDEX_CONSUMER = (s, r) -> {
    final SilverLogger logger = indexingLogger();
    try {
      logger.debug("closing reader of path {0}", s);
      r.close();
    } catch (IOException e) {
      logger.warn(e);
    }
  };

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
      final boolean validRootPath = ArrayUtil.isNotEmpty(rootPath.list());
      IndexReader indexReader = INDEX_READERS.get(path);
      if (indexReader == null && validRootPath) {
        try {
          indexReader = DirectoryReader.open(FSDirectory.open(rootPath.toPath()));
          INDEX_READERS.put(path, indexReader);
        } catch (Exception e) {
          indexingLogger().warn(e);
        }
      } else if (indexReader != null && !validRootPath) {
        indexingLogger().warn("index reader exists in cache but no index path is existing! ({0})", path);
        closeIndexReader(path);
        indexReader = null;
      } else if (!validRootPath) {
        indexingLogger().debug("index reader for path {0} can not be open as there is no index data", path);
      }
      return indexReader;
    }
  }

  static void closeIndexReader(String path) {
    synchronized (READER_MUTEX) {
      final IndexReader indexReader = INDEX_READERS.remove(path);
      if (indexReader != null) {
        CLOSE_INDEX_CONSUMER.accept(path, indexReader);
      }
    }
  }

  static void closeAllIndexReaders() {
    synchronized (READER_MUTEX) {
      INDEX_READERS.forEach(CLOSE_INDEX_CONSUMER);
      INDEX_READERS.clear();
    }
  }
}
