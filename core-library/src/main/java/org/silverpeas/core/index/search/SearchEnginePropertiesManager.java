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

package org.silverpeas.core.index.search;

import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Class used to realize some treatment with SearchEngineSetting.properties file.
 * @author David Derigent
 */
public class SearchEnginePropertiesManager {

  private static List<String> fieldsNameList = null;

  /**
   *
   */
  private SearchEnginePropertiesManager() {
  }

  /**
   * gets the list of form XML fields name use to sort search result
   * @return a list of fields name
   */
  public synchronized static List<String> getFieldsNameList() {
    if (fieldsNameList == null) {
      fieldsNameList = new ArrayList<>();
      SettingBundle settings =
          ResourceLocator.getSettingBundle("org.silverpeas.index.search.searchEngineSettings");
      String property = settings.getString("sorting.formXML.fields", "");
      if (StringUtil.isDefined(property)) {
        String[] tokens = property.split(",");
        for (String token: tokens) {
          token = token.trim();
          if (!token.isEmpty()) {
            fieldsNameList.add(token);
          }
        }
      }
    }
    return fieldsNameList;
  }
}