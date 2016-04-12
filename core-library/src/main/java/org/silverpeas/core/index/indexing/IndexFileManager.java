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

import org.apache.commons.io.FileUtils;
import org.silverpeas.core.util.ResourceLocator;

import java.io.File;

import static java.io.File.separatorChar;

/**
 * Utility class to manage index files.
 */
public class IndexFileManager {

  /**
   * For test purpose only
   *
   * @param indexPath
   */
  public static void configure(String indexPath) {
    indexUpLoadPath = indexPath;
  }

  private static String indexUpLoadPath =
      ResourceLocator.getGeneralSettingBundle().getString("uploadsIndexPath");

  static public String getAbsoluteIndexPath(String particularSpace, String sComponentId) {
    if (particularSpace != null && (particularSpace.startsWith("user@")
            || "transverse".equals(particularSpace))) {
      return getIndexUpLoadPath() + particularSpace + separatorChar + sComponentId + separatorChar
              + "index";
    }
    return getIndexUpLoadPath() + sComponentId + separatorChar + "index";
  }

  public static void deleteComponentIndexFolder(String componentId) {
    File folder = new File(getIndexUpLoadPath(), componentId);
    FileUtils.deleteQuietly(folder);
  }

  /**
   * get the base directory of index upload path
   *
   * @return
   */
  public static String getIndexUpLoadPath() {
    return indexUpLoadPath + separatorChar;
  }
}
