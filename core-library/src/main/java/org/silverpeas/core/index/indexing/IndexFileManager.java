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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.index.indexing;

import org.silverpeas.core.util.ResourceLocator;

import static java.io.File.separatorChar;

/**
 * Utility class to manage index files.
 */
public class IndexFileManager {

  private static String indexUpLoadPath = ResourceLocator.getGeneralSettingBundle()
      .getString("uploadsIndexPath");

  private IndexFileManager() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * For test purpose only
   *
   * @param indexPath the path to configure.
   */
  public static void configure(String indexPath) {
    indexUpLoadPath = indexPath;
  }

  public static String getAbsoluteIndexPath(String componentId) {
    final String componentPath = extractComponentPath(componentId);
    return getIndexUpLoadPath() + componentPath + separatorChar + "index";
  }

  public static String extractComponentPath(final String componentId) {
    final int originalLength = componentId.length();
    final StringBuilder sb = new StringBuilder(originalLength);
    for (int i = 0; i < originalLength; i++) {
      final char current = componentId.charAt(i);
      if (Character.isDigit(current)) {
        break;
      }
      sb.append(current);
    }
    if (sb.length() == 0) {
      // If path cannot be extracted, using unknown path
      sb.append("unknown");
    }
    return sb.toString();
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
