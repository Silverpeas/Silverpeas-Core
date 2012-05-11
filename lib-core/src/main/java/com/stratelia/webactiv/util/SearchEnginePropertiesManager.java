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

package com.stratelia.webactiv.util;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Class used to realize some treatment with SearchEngineSetting.properties file.
 * @author David Derigent
 */
public class SearchEnginePropertiesManager {

  private static ArrayList<String> fieldsNameList = null;

  /**
   * 
   */
  private SearchEnginePropertiesManager() {
  }

  /**
   * gets the list of form XML fields name use to sort search result
   * @return a list of fields name
   */
  public synchronized static ArrayList<String> getFieldsNameList() {
    if (fieldsNameList == null) {
      fieldsNameList = new ArrayList<String>();
      ResourceLocator resource = new ResourceLocator(
          "com.silverpeas.searchEngine.searchEngineSettings", "");
      String property = resource.getString("sorting.formXML.fields");
      if (com.silverpeas.util.StringUtil.isDefined(property)) {
        StringTokenizer tokens = new StringTokenizer(property, ",");
        while (tokens.hasMoreTokens()) {
          fieldsNameList.add(tokens.nextToken());
        }
      }
    }
    return fieldsNameList;
  }
}