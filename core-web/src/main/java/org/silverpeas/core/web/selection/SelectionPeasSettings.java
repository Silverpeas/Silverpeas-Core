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

package org.silverpeas.core.web.selection;

import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;

public class SelectionPeasSettings {

  public static int setBySearchPage;
  public static int elementBySearchPage;
  public static int setByBrowsePage;
  public static int elementByBrowsePage;
  public static String firstPage;
  public static String defaultPage;
  public static boolean displayGroupsUsers;
  public static boolean displayUsersGroups;
  public static boolean displayNbUsersByGroup;
  public static boolean displayAllSearchByDefault;
  public static boolean displayDomains;

  static {
    SettingBundle rs = ResourceLocator.getSettingBundle(
        "org.silverpeas.selectionPeas.settings.selectionPeasSettings");
    setBySearchPage = rs.getInteger("SetBySearchPage", 14);
    elementBySearchPage = rs.getInteger("ElementBySearchPage", 14);
    setByBrowsePage = rs.getInteger("SetByBrowsePage", 5);
    elementByBrowsePage = rs.getInteger("ElementByBrowsePage", 8);
    firstPage = rs.getString("FirstPage", Selection.FIRST_PAGE_DEFAULT);
    defaultPage = rs.getString("DefaultPage", Selection.FIRST_PAGE_SEARCH_ELEMENT);
    displayGroupsUsers = rs.getBoolean("DisplayGroupsUsers", false);
    displayUsersGroups = rs.getBoolean("DisplayUsersGroups", true);
    displayNbUsersByGroup = rs.getBoolean("DisplayNbUsersByGroup", true);
    displayAllSearchByDefault = rs.getBoolean("DisplayAllSearchByDefault", true);
    displayDomains = rs.getBoolean("DisplayDomains", true);
  }
}
