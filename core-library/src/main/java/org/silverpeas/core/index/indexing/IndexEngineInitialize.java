/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.index.indexing;

import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;

import java.io.File;

/**
 * Initializes the indexation engine of Silverpeas.
 */
public class IndexEngineInitialize implements Initialization {

  public IndexEngineInitialize() {
  }

  /**
   * Since version 1.3 of Lucene, lock files are stored in the java.io.tmpdir system's property By
   * default on Windows, it's %USER_DIR%\Local Settings\TEMP and /tmp on
   * Unix
   */
  @Override
  public void init() {
    // Remove all remaining *.lock files in index path
    String indexPath = ResourceLocator.getGeneralSettingBundle().getString("uploadsIndexPath");
    String removeLocks =
        ResourceLocator.getGeneralSettingBundle().getString("removeLocksOnInit", "");
    if (StringUtil.getBooleanValue(removeLocks)) {
      String property = System.getProperty("java.io.tmpdir");
      removeLockFiles(new File(property));
      removeLockFiles(new File(indexPath));
    }
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
          SilverTrace.error("indexing",
              "IndexEngineInitialize.removeLockFiles",
              "util.EX_DELETE_FILE_ERROR", theFile.getPath());
        }
      }
    }
  }

  /**
   * Since version 1.3 of Lucene, lock files have names that start with "lucene-" followed by an MD5
   * hash of the index directory path. Since version 2.3 of Lucene, lock files are in index dirs and
   * named "write.lock"
   *
   * @param fileName - the file to test
   * @return true if the file is a lucene's lock file, false otherwise.
   */
  protected boolean isLockFile(String fileName) {
    return fileName.startsWith("lucene-")
        || ("write.lock".equalsIgnoreCase(fileName));
  }
}
