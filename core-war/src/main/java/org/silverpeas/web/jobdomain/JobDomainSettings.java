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

package org.silverpeas.web.jobdomain;

import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;

import java.util.Arrays;
import java.util.Comparator;

/**
 * This class manage the informations needed for groups navigation and browse PRE-REQUIRED : the
 * Group passed in the constructor MUST BE A VALID GROUP (with Id, etc...)
 * @t.leroi
 */
public class JobDomainSettings {

  public static int m_UsersByPage = 10;
  public static int m_GroupsByPage = 10;
  public static int m_MinLengthLogin = 5;
  public static boolean m_UserAddingAllowedForGroupManagers = false;
  public static boolean m_UseCommunityManagement = false;
  public static boolean usersInDomainQuotaActivated = false;
  public static boolean lastConnectionColumnEnabled = true;

  static {
    SettingBundle rs = ResourceLocator.getSettingBundle(
        "org.silverpeas.jobDomainPeas.settings.jobDomainPeasSettings");

    m_UsersByPage = rs.getInteger("UsersByPage", 10);
    m_GroupsByPage = rs.getInteger("GroupsByPage", 10);
    m_MinLengthLogin = rs.getInteger("MinLengthLogin", 5);
    m_UserAddingAllowedForGroupManagers = rs.getBoolean("UserAddingAllowedForGroupManagers", false);
    m_UseCommunityManagement = rs.getBoolean("UseCommunityManagement", false);
    usersInDomainQuotaActivated = rs.getBoolean("quota.domain.users.activated", false);
    lastConnectionColumnEnabled = rs.getBoolean("domain.users.columns.lastconnection", true);
  }

  static public void sortGroups(Group[] toSort) {
    Arrays.sort(toSort, new Comparator<Group>() {

      public int compare(Group o1, Group o2) {
        return o1.compareTo(o2);
        }
            });
  }

  static public void sortUsers(UserDetail[] toSort) {
    Arrays.sort(toSort, new Comparator<UserDetail>() {

      public int compare(UserDetail o1, UserDetail o2) {
        return o1.compareTo(o2);
        }

    });
  }
}
