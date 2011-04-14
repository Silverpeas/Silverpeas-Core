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
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.silverpeas.pdcPeas.control;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 * @author david derigent
 */
public class SortResultsFactory {

  /**
   * 
   */
  private SortResultsFactory() {
  }

  /**
   * returns an implementation of SortResults interface according to the given keyword
   * @param implementor keyword corresponding to a key in searchEngineSettings.properties. this key
   * allows to gets a class name corresponding to a SortResults implementation
   * @return a SortResults implementation
   */
  public static SortResults getSortResults(String implementor) {

    ResourceLocator settings = new ResourceLocator(
        "com.silverpeas.searchEngine.searchEngineSettings", "");
    String className = settings.getString(implementor,
        "com.stratelia.silverpeas.pdcPeas.control.DefaultSortResults");
    if (StringUtil.isDefined(className)) {
      try {
        return (SortResults) Class.forName(className).newInstance();
      } catch (Exception e) {
        SilverTrace.error("pdcPeas",
            "com.stratelia.silverpeas.pdcPeas.SortResultsFactory.getSortResults(String)",
            "root.EX_CLASS_NOT_INITIALIZED", "", e);
      }
    }
    return new DefaultSortResults();
  }
}
