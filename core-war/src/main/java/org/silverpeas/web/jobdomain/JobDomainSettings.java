/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.jobdomain;

import org.silverpeas.core.admin.user.constant.GroupState;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;

import java.util.Arrays;

/**
 * This class manage the information needed for groups navigation and browse PRE-REQUIRED : the
 * Group passed in the constructor MUST BE A {@link GroupState#VALID} GROUP (with Id, etc...)
 * @author t.leroi
 */
public class JobDomainSettings {

  private static final int USERS_PER_PAGE;
  private static final int GROUPS_PER_PAGE;
  private static final int LOGIN_MIN_LENGTH;
  private static final boolean USER_ADDING_FOR_GROUP_MANAGERS;
  private static final boolean USE_COMMUNITY_MANAGEMENT;
  private static final boolean USERS_IN_DOMAIN_QUOTA_ACTIVATED;
  private static final boolean LAST_CONNECTION_COLUMN_ENABLED;

  static {
    SettingBundle rs = ResourceLocator.getSettingBundle(
        "org.silverpeas.jobDomainPeas.settings.jobDomainPeasSettings");

    USERS_PER_PAGE = rs.getInteger("UsersByPage", 10);
    GROUPS_PER_PAGE = rs.getInteger("GroupsByPage", 10);
    LOGIN_MIN_LENGTH = rs.getInteger("MinLengthLogin", 5);
    USER_ADDING_FOR_GROUP_MANAGERS = rs.getBoolean("UserAddingAllowedForGroupManagers", false);
    USE_COMMUNITY_MANAGEMENT = rs.getBoolean("UseCommunityManagement", false);
    USERS_IN_DOMAIN_QUOTA_ACTIVATED = rs.getBoolean("quota.domain.users.activated", false);
    LAST_CONNECTION_COLUMN_ENABLED = rs.getBoolean("domain.users.columns.lastconnection", true);
  }

  static public void sortGroups(Group[] toSort) {
    Arrays.sort(toSort, Comparable::compareTo);
  }

  static public void sortUsers(UserDetail[] toSort) {
    Arrays.sort(toSort, UserDetail::compareTo);
  }

  public static int getUsersCountPerPage() {
    return USERS_PER_PAGE;
  }

  public static int getGroupsNbPerPage() {
    return GROUPS_PER_PAGE;
  }

  public static int getLoginMinLength() {
    return LOGIN_MIN_LENGTH;
  }

  public static boolean isUserAddingAllowedForGroupManagers() {
    return USER_ADDING_FOR_GROUP_MANAGERS;
  }

  public static boolean isCommunityManagementEnabled() {
    return USE_COMMUNITY_MANAGEMENT;
  }

  public static boolean isUsersInDomainQuotaEnabled() {
    return USERS_IN_DOMAIN_QUOTA_ACTIVATED;
  }

  public static boolean isLastConnectionInfoEnabled() {
    return LAST_CONNECTION_COLUMN_ENABLED;
  }
}
