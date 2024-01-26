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

import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.user.constant.GroupState;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.kernel.logging.SilverLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class manage the information needed for groups navigation and browsing.
 * REQUIREMENT: the Group passed in the constructor MUST BE A {@link GroupState#VALID} GROUP
 * (with Id, etc...)
 * @author t.leroi
 */
public class NavigationStock {
  protected Group[] subGroups = null;
  protected UserDetail[] subUsers = null;
  protected AdminController adminController;
  protected UserState userStateFilter;

  public NavigationStock(AdminController adc) {
    adminController = adc;
  }

  public static Group[] filterGroupsToGroupManager(final List<String> manageableGroupIds,
      final Group[] groups) {
    List<Group> manageableGroups = new ArrayList<>(groups.length);
    for (Group group: groups) {
      if (manageableGroupIds.contains(group.getId())) {
        manageableGroups.add(group);
      } else {
        Group[] subGroups = new Group[0];
        try {
          subGroups = Administration.get().getRecursivelyAllSubGroups(group.getId());
        } catch (AdminException e) {
          SilverLogger.getLogger(NavigationStock.class).error(e.getMessage(), e);
        }
        for (Group subGroup : subGroups) {
          if (manageableGroupIds.contains(subGroup.getId())) {
            manageableGroups.add(group);
            break;
          }
        }
      }
    }
    return manageableGroups.toArray(new Group[0]);
  }

  public void filterOnUserState(final UserState userState) {
    if (userState != UserState.UNKNOWN) {
      this.userStateFilter = userState;
    }
  }

  public void unsetFilterOnUserState() {
    filterOnUserState(null);
  }

  public boolean isUserStateFiltered() {
    return this.userStateFilter != null;
  }

  public final UserDetail[] getUserPage() {
    return userStateFilter == null ? subUsers :
        Arrays.stream(subUsers)
            .filter(u -> u.getState() == userStateFilter)
            .toArray(UserDetail[]::new);
  }

  public final Group[] getGroupPage() {
    return subGroups;
  }

}