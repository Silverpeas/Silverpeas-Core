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

package com.silverpeas.jobManagerPeas;

import java.util.Arrays;
import java.util.Comparator;

import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 * This class manage the informations needed for job manager
 * @t.leroi
 */
public class JobManagerSettings {

  public static int m_UsersByPage = 10;
  public static int m_GroupsByPage = 10;
  public static boolean m_IsKMVisible = false;
  public static boolean m_IsToolsVisible = false;
  public static boolean m_IsToolSpecificAuthentVisible = false;
  public static boolean m_IsToolWorkflowDesignerVisible = false;
  public static boolean m_IsTemplateDesignerVisible = false;
  public static boolean m_IsPortletDeployerVisible = false;

  static {
    ResourceLocator rs = new ResourceLocator(
        "com.silverpeas.jobManagerPeas.settings.jobManagerPeasSettings", "");

    m_UsersByPage = readInt(rs, "UsersByPage", 10);
    m_GroupsByPage = readInt(rs, "GroupsByPage", 10);
    m_IsKMVisible = readBoolean(rs, "IsKMVisible", false);
    m_IsToolsVisible = readBoolean(rs, "IsToolsVisible", false);
    m_IsToolSpecificAuthentVisible = readBoolean(rs,
        "IsToolSpecificAuthentVisible", false);
    m_IsToolWorkflowDesignerVisible = readBoolean(rs,
        "IsToolWorkflowDesignerVisible", false);
    m_IsTemplateDesignerVisible = readBoolean(rs, "IsTemplateDesignerVisible",
        false);
    m_IsPortletDeployerVisible = readBoolean(rs, "IsPortletDeployerVisible",
        false);
  }

  static protected int readInt(ResourceLocator rs, String propName,
      int defaultValue) {
    String s = rs.getString(propName, Integer.toString(defaultValue));
    return Integer.parseInt(s);
  }

  static protected boolean readBoolean(ResourceLocator rs, String propName,
      boolean defaultValue) {
    String s = null;
    if (defaultValue) {
      s = rs.getString(propName, "true");
    } else {
      s = rs.getString(propName, "false");
    }

    boolean valret = defaultValue;
    if (defaultValue) {
      if ("false".equalsIgnoreCase(s)) {
        valret = false;
      }
    } else {
      if ("true".equalsIgnoreCase(s)) {
        valret = true;
      }
    }

    return valret;
  }

  static public void sortGroups(Group[] toSort) {
    Arrays.sort(toSort, new Comparator<Group>() {

      public int compare(Group o1, Group o2) {
        return o1.getName().compareTo(o2.getName());
        }
            });
  }

  static public void sortUsers(UserDetail[] toSort) {
    Arrays.sort(toSort, new Comparator<UserDetail>() {

      public int compare(UserDetail o1, UserDetail o2) {
        return o1.getLastName().compareTo(o2.getLastName());
        }
            });
  }
}
