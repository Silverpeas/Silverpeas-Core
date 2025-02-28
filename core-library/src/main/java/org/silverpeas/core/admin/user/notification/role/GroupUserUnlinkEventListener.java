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

import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.user.notification.GroupUserLink;
import org.silverpeas.core.admin.user.notification.GroupUserLinkEvent;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.notification.system.CDIAfterSuccessfulTransactionResourceEventListener;
import org.silverpeas.core.notification.system.ResourceEvent;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

/**
 * Listens for events about the removing of a user from a given group, and for each such removal,
 * find all the roles in the different component instances the user doesn't play anymore. For each
 * found role, a notification about that is sent.
 *
 * @author mmoquillon
 */
@Service
class GroupUserUnlinkEventListener
    extends CDIAfterSuccessfulTransactionResourceEventListener<GroupUserLinkEvent> {

  @Inject
  private UserRoleChangeNotifier notifier;

  @Inject
  private AdminController admin;

  @Override
  public void onDeletion(GroupUserLinkEvent event) {
    GroupUserLink link = event.getTransition().getBefore();
    Map<String, Set<String>> profiles =
        findComponentInstancesGroupedByNotAnymorePlayedRole(link.getUserId(), link.getGroupId());
    profiles.forEach((key, value) -> {
      UserRoleEvent evt = UserRoleEvent.builderFor(ResourceEvent.Type.DELETION)
          .role(key)
          .userId(link.getUserId())
          .instanceIds(value)
          .build();
      notifier.notify(evt);
    });
  }

  /**
   * Finds all the component instances in which the specified group or its parent groups play a role
   * and for which the specified user doesn't play anymore. The component instances are grouped by
   * role name.
   *
   * @param userId the unique identifier of the user.
   * @param groupId the unique identifier of the group.
   * @return a dictionary with as key the role name and as value a set with the unique identifiers
   * of the component instances in which the user doesn't play anymore a role.
   */
  private Map<String, Set<String>> findComponentInstancesGroupedByNotAnymorePlayedRole(String userId,
      String groupId) {
    Set<ProfileInst> userProfiles = Stream.of(admin.getProfileIds(userId))
        .map(admin::getProfileInst)
        .collect(toSet());
    return Stream.concat(Stream.of(groupId),
            admin.getPathToGroup(groupId).stream())
        .map(admin::getGroupById)
        .filter(g -> !List.of(g.getUserIds()).contains(userId))
        .flatMap(g -> Stream.of(admin.getProfileIdsOfGroup(g.getId()))
            .map(admin::getProfileInst)
            .filter(p -> isNotInUserProfiles(p, userProfiles))
            .filter(p -> isNotInOthersGroups(userId, g.getId(), p.getAllGroups())))
        .collect(
            groupingBy(ProfileInst::getName,
                mapping(this::getComponentInst, toSet())));
  }

  private boolean isNotInUserProfiles(ProfileInst actualProfile, Set<ProfileInst> userProfiles) {
    return userProfiles.stream()
        .filter(p -> p.getComponentFatherId() == actualProfile.getComponentFatherId())
        .noneMatch(p -> p.getName().equals(actualProfile.getName()));
  }

  private boolean isNotInOthersGroups(String userId, String groupId, List<String> groups) {
    return groups.stream()
        .filter(g -> !g.equalsIgnoreCase(groupId))
        .map(g -> admin.getGroupById(g))
        .noneMatch(g -> List.of(g.getUserIds()).contains(userId));
  }

  private String getComponentInst(ProfileInst profileInst) {
    return admin.getComponentInst(String.valueOf(profileInst.getComponentFatherId())).getId();
  }
}
  