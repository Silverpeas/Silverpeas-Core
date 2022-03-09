/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
 * @t.leroi
 */
public class GroupNavigationStock extends NavigationStock {
  Group m_NavGroup = null;
  String m_GroupId = null;
  List<String> manageableGroupIds = null;

  public GroupNavigationStock(String navGroup, AdminController adc,
      List<String> manageableGroupIds) {
    super(adc);
    m_GroupId = navGroup;
    this.manageableGroupIds = manageableGroupIds;
    refresh();
  }

  public void refresh() {
    String[] subUsersIds;
    m_NavGroup = m_adc.getGroupById(m_GroupId);
    subUsersIds = m_NavGroup.getUserIds();
    if (subUsersIds == null) {
      m_SubUsers = new UserDetail[0];
    } else {
      m_SubUsers = m_adc.getUserDetails(subUsersIds);
    }
    JobDomainSettings.sortUsers(m_SubUsers);

    m_SubGroups = m_adc.getAllSubGroups(m_NavGroup.getId());
    if (manageableGroupIds != null) {
      m_SubGroups = filterGroupsToGroupManager(manageableGroupIds, m_SubGroups);
    }

    JobDomainSettings.sortGroups(m_SubGroups);
    verifIndexes();
  }

  public boolean isThisGroup(String grId) {
    if (StringUtil.isDefined(grId)) {
      return (grId.equals(m_NavGroup.getId()));
    } else {
      return (isGroupValid(m_NavGroup) == false);
    }
  }

  public Group getThisGroup() {
    return m_NavGroup;
  }

  static public boolean isGroupValid(Group gr) {
    return gr != null && StringUtil.isDefined(gr.getId());
  }
}
