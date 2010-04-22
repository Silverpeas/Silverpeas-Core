/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
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

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;

/**
 * The IndexEngineProxy class encapsulates the calls to the index engine server.
 */
public final class IndexEngineProxy {
  /**
   * The IndexEngine class is only used via static methods and no IndexEngine object will ever be
   * constructed.
   */
  private IndexEngineProxy() {
  }

  /**
   * Add an entry index.
   */
  static public void addIndexEntry(FullIndexEntry indexEntry) {
    init();
    if (indexEngine != null) {
      IndexerThread.addIndexEntry(indexEntry);
    } else {
      SilverTrace.error("indexEngine", "IndexEngineProxy",
          "indexEngine.MSG_ADD_REQUEST_IGNORED");
    }
  }

  /**
   * Remove an entry index.
   */
  static public void removeIndexEntry(IndexEntryPK indexEntry) {
    init();
    if (indexEngine != null) {
      IndexerThread.removeIndexEntry(indexEntry);
    } else {
      SilverTrace.error("indexEngine", "IndexEngineProxy",
          "indexEngine.MSG_REMOVE_REQUEST_IGNORED");
    }
  }

  /**
   * Initialize the class, if this is not already done.
   */
  static private void init() {
    String rootPath = FileRepositoryManager.getAbsoluteIndexPath("x", "x");

    if (rootPath == null) {
      SilverTrace.fatal("indexEngine", "IndexEngineEJB",
          "indexEngine.MSG_INDEX_FILES_UNFOUND");
      indexEngine = null;
      return;
    }

    IndexerThread.start(new IndexManager());
    indexEngine = "indexEngine";
  }

  /**
   * The indexEngineBm to which all the requests are forwarded.
   */
  static private String indexEngine = null;
}
