/**
 * Copyright (C) 2000 - 2011 Silverpeas
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

package com.stratelia.webactiv.util.indexEngine;

import com.silverpeas.util.StringUtil;
import java.io.File;

import com.stratelia.silverpeas.silverpeasinitialize.IInitialize;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.GeneralPropertiesManager;

/**
 * Class declaration
 * @author
 */
public class IndexEngineInitialize implements IInitialize {

  public IndexEngineInitialize() {
  }

  /*
   * (non-Javadoc)
   * @see com.stratelia.silverpeas.silverpeasinitialize.IInitialize#Initialize()
   */
  /**
   * Since version 1.3 of Lucene, lock files are stored in the java.io.tmpdir system's property By
   * default on Windows, it's C:\Documents and Settings\neysseri\Local Settings\TEMP and /tmp on
   * Unix
   */
  public boolean Initialize() {
    // Remove all remaining *.lock files in index path
    String indexPath = GeneralPropertiesManager.getString("uploadsIndexPath");
    String removeLocks = GeneralPropertiesManager.getString("removeLocksOnInit", "");
    if (StringUtil.getBooleanValue(removeLocks)) {
      String property = System.getProperty("java.io.tmpdir");
      SilverTrace.debug("indexEngine", "IndexEngineInitialize.Initialize()",
          "Removing Locks...(" + property + ")");
      removeLockFiles(new File(property));
      removeLockFiles(new File(indexPath));
      SilverTrace.debug("indexEngine", "IndexEngineInitialize.Initialize()",
          "Locks removed !");
    }
    return true;
  }

  protected void removeLockFiles(File theFile) {
    if (theFile.isDirectory()) {
      File[] list = theFile.listFiles();
      int i = 0;

      while (list != null && i < list.length) {
        removeLockFiles(list[i++]);
      }
    } else {
      if (theFile.isFile() && isLockFile(theFile.getName())) {
        if (!theFile.delete()) {
          SilverTrace.error("indexEngine",
              "IndexEngineInitialize.removeLockFiles",
              "util.EX_DELETE_FILE_ERROR", theFile.getPath());
        } else {
          SilverTrace.debug("indexEngine",
              "IndexEngineInitialize.removeLockFiles", "Lock "
              + theFile.getPath() + " removed.");
        }
      }
    }
  }

  /**
   * Since version 1.3 of Lucene, lock files have names that start with "lucene-" followed by an MD5
   * hash of the index directory path. Since version 2.3 of Lucene, lock files are in index dirs and
   * named "write.lock"
   * @param fileName - the file to test
   * @return true if the file is a lucene's lock file, false otherwise.
   */
  protected boolean isLockFile(String fileName) {
    // return (("commit.lock".equalsIgnoreCase(fileName)) ||
    // ("write.lock".equalsIgnoreCase(fileName)));
    return fileName.startsWith("lucene-")
        || ("write.lock".equalsIgnoreCase(fileName));
  }
}