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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.selectionPeas;

import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.util.SilverpeasSettings;
import com.stratelia.webactiv.util.ResourceLocator;

public class SelectionPeasSettings extends SilverpeasSettings {
  public static int m_SetBySearchPage = 14;
  public static int m_ElementBySearchPage = 14;
  public static int m_SetByBrowsePage = 5;
  public static int m_ElementByBrowsePage = 8;
  public static String m_FirstPage = Selection.FIRST_PAGE_DEFAULT;
  public static String m_DefaultPage = Selection.FIRST_PAGE_SEARCH_ELEMENT;
  public static boolean m_DisplayGroupsUsers = false;
  public static boolean m_DisplayUsersGroups = true;
  public static boolean m_DisplayNbUsersByGroup = true;
  public static boolean m_DisplayAllSearchByDefault = true;
  public static boolean m_DisplayDomains = true;

  static {
    ResourceLocator rs = new ResourceLocator(
        "com.stratelia.silverpeas.selectionPeas.settings.selectionPeasSettings",
        "");

    m_SetBySearchPage = readInt(rs, "SetBySearchPage", m_SetBySearchPage);
    m_ElementBySearchPage = readInt(rs, "ElementBySearchPage",
        m_ElementBySearchPage);
    m_SetByBrowsePage = readInt(rs, "SetByBrowsePage", m_SetByBrowsePage);
    m_ElementByBrowsePage = readInt(rs, "ElementByBrowsePage",
        m_ElementByBrowsePage);
    m_FirstPage = readString(rs, "FirstPage", m_FirstPage);
    m_DefaultPage = readString(rs, "DefaultPage", m_DefaultPage);
    m_DisplayGroupsUsers = readBoolean(rs, "DisplayGroupsUsers",
        m_DisplayGroupsUsers);
    m_DisplayUsersGroups = readBoolean(rs, "DisplayUsersGroups",
        m_DisplayUsersGroups);
    m_DisplayNbUsersByGroup = readBoolean(rs, "DisplayNbUsersByGroup",
        m_DisplayNbUsersByGroup);
    m_DisplayAllSearchByDefault = readBoolean(rs, "DisplayAllSearchByDefault",
        m_DisplayAllSearchByDefault);
    m_DisplayDomains = readBoolean(rs, "DisplayDomains", m_DisplayDomains);
  }
}
