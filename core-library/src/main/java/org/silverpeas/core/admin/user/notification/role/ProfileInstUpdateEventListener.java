/*
 * Copyright (C) 2000 - 2025 Silverpeas
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

package org.silverpeas.core.admin.user.notification.role;

import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.user.notification.ProfileInstEvent;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.notification.system.CDIResourceEventListener;
import org.silverpeas.core.notification.system.ResourceEvent;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Listens for events about a change in the lists of users and of groups of a given access right
 * profile instance. For instance, the user groups removed from an access right profile instance
 * aren't taken in charge, only the users. For all the users removed from the given profile
 * instance, a notification about the fact these users don't play anymore the role is sent. The
 * removed users shouldn't play the role in the concerned component instance neither directly nor by
 * a group or a subgroup (in both the specific and inherited profile instances for the same role).
 *
 * @author mmoquillon
 */
@Service
class ProfileInstUpdateEventListener extends CDIResourceEventListener<ProfileInstEvent> {

  @Inject
  private OrganizationController organization;

  @Inject
  private UserRoleChangeNotifier notifier;

  @Override
  public void onUpdate(ProfileInstEvent event) {
    ProfileInst before = event.getTransition().getBefore();
    ProfileInst after = event.getTransition().getAfter();
    ComponentInst componentInst = getComponentInstanceId(before.getComponentFatherId());
    Set<String> removedUsers = findRemovedUsersId(componentInst, before, after);
    if (!removedUsers.isEmpty()) {
      UserRoleEvent userRoleEvent = UserRoleEvent.builderFor(ResourceEvent.Type.DELETION)
          .role(before.getName())
          .instanceId(componentInst.getId())
          .userIds(removedUsers)
          .build();
      notifier.notify(userRoleEvent);
    }
  }

  /**
   * Finds the users that were removed, either directly or through groups, from the specified access
   * right profile of the underlying component instance. The users or the groups that are removed by
   * the profile instance update are those present in the role profile before the update and that
   * aren't anymore present in the role profile after the update.
   *
   * @param componentInst the component instance for which the right profile has been updated.
   * @param before the state of the role profile before update.
   * @param after the state of the role profile after update.
   * @return a set with the unique identifiers of all of the users that were removed by the profile
   * instance update.
   */
  private Set<String> findRemovedUsersId(ComponentInst componentInst, ProfileInst before,
      ProfileInst after) {
    List<String> usersAfter = after.getAllUsers();
    List<String> groupsAfter = after.getAllGroups();
    String roleName =  before.getName();
    // get all the users directly removed from the profile instance and who don't play anymore
    // the role for the application (they can be play another role)
    Stream<String> removedUsers = before.getAllUsers().stream()
        .filter(user -> !usersAfter.contains(user))
        .filter(u -> Stream.of(organization.getUserProfiles(u, componentInst.getId()))
            .noneMatch(p -> p.equalsIgnoreCase(roleName)));


    // get all the users belonging to the groups removed from the profile instance and who's not
    // anymore play the role for the application (can be play a role in an inherited or
    // specific role other than the updated one)
    Stream<String> removedUsersInGroups = before.getAllGroups().stream()
        .filter(group -> !groupsAfter.contains(group))
        .map(g -> organization.getGroup(g))
        .flatMap(g -> Stream.of(((Group) g).getUserIds()))
        .distinct()
        .filter(u -> Stream.of(organization.getUserProfiles(u, componentInst.getId()))
            .noneMatch(p -> p.equalsIgnoreCase(roleName)));

    return Stream.concat(removedUsers, removedUsersInGroups).collect(Collectors.toSet());
  }

  private ComponentInst getComponentInstanceId(int localComponentId) {
    return organization.getComponentInst(String.valueOf(localComponentId));
  }
}
  