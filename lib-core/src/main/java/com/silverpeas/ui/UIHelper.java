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

package com.silverpeas.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.stratelia.webactiv.util.ResourceLocator;

public class UIHelper {

  private static List<String> languages = new ArrayList<String>();
  private static String defaultLanguage;

  static {
    ResourceLocator rs =
        new ResourceLocator(
            "com.stratelia.silverpeas.personalizationPeas.settings.personalizationPeasSettings", "");
    
    defaultLanguage = rs.getString("DefaultLanguage");

    StringTokenizer st = new StringTokenizer(rs.getString("languages"), ",");
    while (st.hasMoreTokens()) {
      languages.add(st.nextToken());
    }
  }
  
  /**
   * Returns the default language used to display user interface (UI)
   * @return a String (ie : 'fr', 'en' or another two-letters code)
   */
  public static String getDefaultLanguage() {
    return defaultLanguage;
  }

  /**
   * Returns all languages available to display user interface
   * @return a List of String (ie : 'fr', 'en' or another two-letters code)
   */
  public static List<String> getLanguages() {
    return languages;
  }

}