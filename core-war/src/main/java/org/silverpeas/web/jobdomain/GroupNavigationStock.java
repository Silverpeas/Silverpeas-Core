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
/*
 * GroupNavigationStock.java
 */

package org.silverpeas.web.jobdomain;

import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.user.constant.GroupState;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.util.StringUtil;

import java.util.List;

/**
 * This class manage the information needed for groups navigation and browse PRE-REQUIRED : the
 * Group passed in the constructor MUST BE A {@link GroupState#VALID} GROUP (with Id, etc...)
 * @author t.leroi
 */
public class GroupNavigationStock extends NavigationStock {
  private Group group = null;
  private final String groupId;
  private final List<String> manageableGroupIds;

  public GroupNavigationStock(String navGroup, AdminController adc,
      List<String> manageableGroupIds) {
    super(adc);
    groupId = navGroup;
    this.manageableGroupIds = manageableGroupIds;
    refresh();
  }

  public void refresh() {
    String[] subUsersIds;
    userStateFilter = null;
    group = adminController.getGroupById(groupId);
    subUsersIds = group.getUserIds();
    if (subUsersIds == null) {
      subUsers = new UserDetail[0];
    } else {
      subUsers = adminController.getUserDetails(subUsersIds);
    }
    JobDomainSettings.sortUsers(subUsers);

    subGroups = adminController.getAllSubGroups(group.getId());
    if (manageableGroupIds != null) {
      subGroups = filterGroupsToGroupManager(manageableGroupIds, subGroups);
    }

    JobDomainSettings.sortGroups(subGroups);
  }

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  public boolean isThisGroup(String grId) {
    if (StringUtil.isDefined(grId)) {
      return (grId.equals(group.getId()));
    } else {
      return !isGroupValid(group);
    }
  }

  public Group getThisGroup() {
    return group;
  }

  public static boolean isGroupValid(Group gr) {
    return gr != null && StringUtil.isDefined(gr.getId());
  }
}
