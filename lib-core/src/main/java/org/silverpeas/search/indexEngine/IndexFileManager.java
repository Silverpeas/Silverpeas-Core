/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.search.indexEngine;

import com.silverpeas.util.ComponentHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.GeneralPropertiesManager;

import static java.io.File.separatorChar;

/**
 * Utility class to manage index files.
 */
public class IndexFileManager {

  /**
   * For test purposde only
   *
   * @param indexPath
   */
  public static void configure(String indexPath) {
    indexUpLoadPath = indexPath;
  }
  private static String indexUpLoadPath = GeneralPropertiesManager.getString("uploadsIndexPath");

  static public String getAbsoluteIndexPath(String particularSpace, String componentId) {
    final String componentName = ComponentHelper.getInstance().extractComponentName(componentId);
    SilverTrace.debug("util", "FileRepositoryManager.getAbsoluteIndexPath",
            "particularSpace = " + particularSpace + " componentName= " + componentName);
    if (particularSpace != null &&
        (particularSpace.startsWith("user@") || "transverse".equals(particularSpace))) {
      return getIndexUpLoadPath() + particularSpace + separatorChar + componentName + separatorChar
              + "index";
    }
    return getIndexUpLoadPath() + componentName + separatorChar + "index";
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
