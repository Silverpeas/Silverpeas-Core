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

package org.silverpeas.core.admin.service.visitors;

import org.silverpeas.core.admin.ProfiledObjectId;
import org.silverpeas.core.admin.ProfiledObjectType;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.component.model.SilverpeasPersonalComponentInstance;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.user.GroupManager;
import org.silverpeas.core.admin.user.model.*;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.kernel.annotation.NonNull;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.kernel.util.StringUtil;

import javax.inject.Inject;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;
import static org.silverpeas.core.SilverpeasExceptionMessages.failureOnGetting;
import static org.silverpeas.core.util.ArrayUtil.contains;
import static org.silverpeas.core.util.CollectionUtil.intersection;
import static org.silverpeas.kernel.util.StringUtil.EMPTY;

/**
 * Visitor of {@link SearchCriteria} objects to complete them with the criteria on the users and the
 * groups that have access a given component instance with the roles specified by one of the
 * criterion of the criteria . If there is no criterion on a component instance, nothing is done.
 *
 * @author mmoquillon
 */
public class SearchCriteriaOnUsersAndGroupsForComponentInstance implements SearchCriteriaVisitor {

  private final Administration admin;
  private final GroupManager groupManager;

  @Inject
  public SearchCriteriaOnUsersAndGroupsForComponentInstance(Administration admin,
      GroupManager groupManager) {
    this.admin = admin;
    this.groupManager = groupManager;
  }

  @Override
  public void visit(UserDetailsSearchCriteria searchCriteria) throws SilverpeasRuntimeException {
    try {
      List<String> roleNames = getRoleNames(searchCriteria);
      if (searchCriteria.isCriterionOnComponentInstanceIdSet()) {
        sethUsersOrGroupsPlayingRoles(searchCriteria, roleNames);
      } else if (!roleNames.isEmpty()) {
        SilverLogger.getLogger(this)
            .warn("searching users on role name(s) {0} without specifying a component instance " +
                "identifier. No role name filtering is performed.");
      }
    } catch (AdminException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  @Override
  public void visit(GroupsSearchCriteria searchCriteria) throws SilverpeasRuntimeException {
    try {
      final List<String> roleNames = getRoleNames(searchCriteria);
      if (searchCriteria.isCriterionOnComponentInstanceIdSet()) {
        // ok, replace role names and component instance by role ids.
        setRoleIdsMatchingRoles(searchCriteria, roleNames);
      } else if (!roleNames.isEmpty()) {
        SilverLogger.getLogger(this)
            .warn("searching groups on role name(s) {0} without specifying a component instance " +
                "identifier. No role name filtering is performed.");
      }
    } catch (AdminException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  @NonNull
  private static List<String> getRoleNames(AbstractSearchCriteria searchCriteria) {
    return Optional.ofNullable(searchCriteria.getCriterionOnRoleNames())
        .filter(ArrayUtil::isNotEmpty)
        .stream()
        .flatMap(Stream::of)
        .collect(toList());
  }

  private void setRoleIdsMatchingRoles(GroupsSearchCriteria searchCriteria,
      List<String> roleNames) throws AdminException {
    final String instanceId = searchCriteria.getCriterionOnComponentInstanceId();
    final SilverpeasComponentInstance instance = admin.getComponentInstance(instanceId);
    if (instance == null) {
      // instance does not exist, stopping the search
      SilverLogger.getLogger(this).warn(failureOnGetting("component instance", instanceId));
      searchCriteria.clear();
    } else if (!roleNames.isEmpty() || !instance.isPublic()) {
      final List<String> profileIds = new ArrayList<>();
      if (!instance.isPersonal()) {
        final List<ProfileInst> profiles;
        if (searchCriteria.isCriterionOnResourceIdSet()) {
          profiles =
              getProfileInstancesFor(searchCriteria.getCriterionOnResourceId(), instance.getId());
        } else {
          profiles = admin.getComponentInst(instance.getId()).getAllProfilesInst();
        }
        profiles.stream()
            .filter(p -> roleNames.isEmpty() || roleNames.contains(p.getName()))
            .map(ProfileInst::getId)
            .forEach(profileIds::add);
        // if empty, given criteria are not consistent. A dummy role id is set in order to get an
        // empty result
        Optional.of(profileIds).filter(List::isEmpty).ifPresent(r -> r.add("-1000"));
      }
      searchCriteria.onProfileIds(profileIds.toArray(new String[0]));
    }
  }

  private void sethUsersOrGroupsPlayingRoles(UserDetailsSearchCriteria searchCriteria,
      List<String> roleNames) throws AdminException {
    final String instanceId = searchCriteria.getCriterionOnComponentInstanceId();
    final SilverpeasComponentInstance instance = admin.getComponentInstance(instanceId);
    if (instance == null) {
      // instance does not exist, stopping the search
      SilverLogger.getLogger(this).warn(failureOnGetting("component instance", instanceId));
      searchCriteria.clear();
    } else if (!roleNames.isEmpty() || !instance.isPublic()) {
      if (!instance.isPersonal()) {
        setGroupsPlayingRole(roleNames, instance, searchCriteria);
      } else {
        final String currentUserId =
            getCurrentUserPlayingRole(roleNames, instance, searchCriteria);
        if (StringUtil.isDefined(currentUserId)) {
          searchCriteria.onUserIds(currentUserId);
        } else {
          searchCriteria.clear();
        }
      }
    }
  }

  private String getCurrentUserPlayingRole(final List<String> listOfRoleNames,
      final SilverpeasComponentInstance instance, final UserDetailsSearchCriteria searchCriteria) {
    final User user = ((SilverpeasPersonalComponentInstance) instance).getUser();
    return Optional.of(instance.getSilverpeasRolesFor(user)
            .stream()
            .map(SilverpeasRole::getName)
            .collect(toList()))
        .filter(r -> listOfRoleNames.isEmpty() || !intersection(r, listOfRoleNames).isEmpty())
        .filter(r -> !searchCriteria.isCriterionOnUserIdsSet() ||
            contains(searchCriteria.getCriterionOnUserIds(), user.getId()))
        .map(r -> user.getId())
        .orElse(EMPTY);
  }

  private void setGroupsPlayingRole(final List<String> roleNames,
      final SilverpeasComponentInstance instance, final UserDetailsSearchCriteria searchCriteria)
      throws AdminException {
    getRecursivelyValidGroupsIdPlaying(roleNames, instance,
        searchCriteria.getCriterionOnResourceId())
        .ifPresent(m -> {
      searchCriteria.withGroupsByRoles(m);
      if (searchCriteria.isCriterionOnAnyGroupSet()) {
        searchCriteria.onGroupIds(m.values()
            .stream()
            .flatMap(Collection::stream)
            .distinct()
            .toArray(String[]::new));
      }
    });
  }

  private Optional<Map<String, Set<String>>> getRecursivelyValidGroupsIdPlaying(
      final List<String> roleNames, final SilverpeasComponentInstance instance,
      final String resourceId) throws AdminException {
    final List<ProfileInst> profiles;
    if (StringUtil.isDefined(resourceId)) {
      profiles = getProfileInstancesFor(resourceId, instance.getId());
    } else {
      profiles = admin.getComponentInst(instance.getId()).getAllProfilesInst();
    }
    final Map<String, Set<String>> groupIdsByRole = new HashMap<>(profiles.size());
    for (ProfileInst aProfile : profiles) {
      if (roleNames.isEmpty() || roleNames.contains(aProfile.getName())) {
        final String roleName = aProfile.getName();
        final Set<String> allGroupIdsOfCurrentRole = Optional
            .ofNullable(groupIdsByRole.get(roleName))
            .orElseGet(HashSet::new);
        // groups (and recursively their subgroups) playing the role
        final List<String> groupIds = aProfile.getAllGroups();
        for (String aGroupId : groupIds) {
          allGroupIdsOfCurrentRole.add(aGroupId);
          allGroupIdsOfCurrentRole.addAll(groupManager.getAllSubGroupIdsRecursively(aGroupId));
        }
        if (!allGroupIdsOfCurrentRole.isEmpty()) {
          groupIdsByRole.put(roleName, allGroupIdsOfCurrentRole);
        }
      }
    }
    return Optional.of(groupIdsByRole).filter(not(Map::isEmpty));
  }

  private List<ProfileInst> getProfileInstancesFor(String resourceId, String instanceId)
      throws AdminException {
    Pattern objectIdPattern = Pattern.compile("([a-zA-Z]+)(\\d+)");
    Matcher matcher = objectIdPattern.matcher(resourceId);
    if (matcher.matches() && matcher.groupCount() == 2) {
      String type = matcher.group(1);
      String id = matcher.group(2);
      ProfiledObjectId objectId = new ProfiledObjectId(ProfiledObjectType.fromCode(type), id);
      return admin.getProfilesByObject(objectId, instanceId);
    }
    throw new AdminException(
        failureOnGetting("profiles on resource " + resourceId, "of component " + instanceId));
  }
}
  