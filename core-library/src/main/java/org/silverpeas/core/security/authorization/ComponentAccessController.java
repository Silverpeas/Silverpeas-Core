/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.core.security.authorization;

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.silverpeas.core.util.StringUtil.getBooleanValue;

/**
 * It controls the access of a user to a given Silverpeas component. A Silverpeas component can be
 * either a Silverpeas application instance (like a Kmelia instance for example) or a user
 * personal tool or an administrative tool.
 * @author ehugonnet
 */
@Service
@Singleton
public class ComponentAccessController extends AbstractAccessController<String>
    implements ComponentAccessControl {

  private static final String DATA_MANAGER_CONTEXT_KEY = "ComponentAccessControllerDataManager";
  private static final String RIGHTS_ON_TOPICS_PARAM_NAME = "rightsOnTopics";

  private OrganizationController controller;

  @Inject
  ComponentAccessController(OrganizationController controller) {
    // Instance by IoC only.
    this.controller = controller;
  }

  @Override
  public boolean isGroupAuthorized(final String groupId, final String instanceId) {
    try {
      return controller.isComponentAvailableToGroup(instanceId, groupId);
    } catch (Exception e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  static DataManager getDataManager(final AccessControlContext context) {
    DataManager manager = context.get(DATA_MANAGER_CONTEXT_KEY, DataManager.class);
    if (manager == null) {
      manager = new DataManager();
      context.put(DATA_MANAGER_CONTEXT_KEY, manager);
    }
    return manager;
  }

  @Override
  public boolean isRightOnTopicsEnabled(final String instanceId) {
    return isTopicTrackerSupported(instanceId) &&
        getBooleanValue(controller.getComponentParameterValue(instanceId, RIGHTS_ON_TOPICS_PARAM_NAME));
  }

  private boolean isTopicTrackerSupported(String componentId) {
    return controller.getComponentInstance(componentId)
        .filter(SilverpeasComponentInstance::isTopicTracker).isPresent();
  }

  @Override
  public Stream<String> filterAuthorizedByUser(final Collection<String> instanceIds, final String userId,
      final AccessControlContext context) {
    getDataManager(context).loadCaches(userId, instanceIds);
    return instanceIds.stream().filter(p -> isUserAuthorized(userId, p, context));
  }

  @Override
  public boolean isUserAuthorized(String userId, String componentId,
      final AccessControlContext context) {
    return isUserAuthorized(getUserRoles(userId, componentId, context));
  }

  @Override
  protected void fillUserRoles(final Set<SilverpeasRole> userRoles,
      final AccessControlContext context, final String userId, final String componentId) {
    try {
      final DataManager dataManager = getDataManager(context);
      final Predicate<User> isUserNotValidState = u -> u == null || (!u.isActivatedState() && !u.isAnonymous());
      final Predicate<String> isTool = c -> c == null || dataManager.isToolAvailable(c);

      // If userId corresponds to nothing or to a deleted or deactivated user, then no role is
      // retrieved.
      User user = User.getById(userId);
      if (isUserNotValidState.test(user)) {
        return;
      }

      // Personal space or user tool
      if (isTool.test(componentId)) {
        userRoles.add(SilverpeasRole.admin);
        return;
      }
      if (Administration.Constants.ADMIN_COMPONENT_ID.equals(componentId)) {
        if (user.isAccessAdmin()) {
          userRoles.add(SilverpeasRole.admin);
        }
        return;
      }

      if (fillUserRolesFromComponentInstance(userId, componentId, context, userRoles)) {
        return;
      }

      if (dataManager.isComponentAvailableToUser(componentId, userId)) {
        final String[] userProfiles = dataManager.getUserProfiles(componentId, userId);
        userRoles.addAll(SilverpeasRole.from(userProfiles));
        if (userRoles.isEmpty() && userProfiles != null && userProfiles.length > 0) {
          // Taking into account the case where the user has only specific profiles.
          // In that case, even the user is an admin one (indicated by a specific profile)
          // it is considered as a simple user.
          userRoles.add(SilverpeasRole.user);
        }
      }
    } finally {
      if (AccessControlOperation.isPersistActionFrom(context.getOperations())) {
        userRoles.remove(SilverpeasRole.user);
      }
    }
  }

  private boolean fillUserRolesFromComponentInstance(final String userId, final String componentId,
      final AccessControlContext context, final Set<SilverpeasRole> userRoles) {
    final DataManager dataManager = getDataManager(context);
    final Optional<SilverpeasComponentInstance> optionalInstance = dataManager.getComponentInstance(componentId);
    if (!optionalInstance.isPresent()) {
      return true;
    }

    final Set<AccessControlOperation> operations = context.getOperations();
    final SilverpeasComponentInstance componentInstance = optionalInstance.get();
    if (componentInstance.isPersonal()) {
      userRoles.addAll(componentInstance.getSilverpeasRolesFor(User.getById(userId)));
      if (AccessControlOperation.isPersistActionFrom(operations) ||
          AccessControlOperation.isDownloadActionFrom(operations) ||
          AccessControlOperation.isSharingActionFrom(operations)) {
        userRoles.remove(SilverpeasRole.user);
      }
      return true;
    }

    if (componentInstance.isPublic() || dataManager.isPublicFilesEnabled(componentId)) {
      userRoles.add(SilverpeasRole.user);
    }
    return false;
  }

  /**
   * Data manager.
   */
  static class DataManager {
    private static final List<String> HANDLED_PARAM_NAMES = Arrays
        .asList(RIGHTS_ON_TOPICS_PARAM_NAME, "usePublicationSharing", "useFileSharing",
            "useFolderSharing", "coWriting", "publicFiles");
    private OrganizationController controller;
    private Map<String, Optional<SilverpeasComponentInstance>> componentInstancesCache = new HashMap<>(1);
    private Map<String, Boolean> isPublicationSharingEnabledForRoleCache = new HashMap<>(1);
    private Map<String, Boolean> isFileSharingEnabledForRoleCache = new HashMap<>(1);
    private Map<String, Boolean> isFolderSharingEnabledForRoleCache = new HashMap<>(1);
    private Map<String, Boolean> isCoWritingEnabledCache = new HashMap<>(1);
    private Map<String, Boolean> isTopicTrackerSupportedCache = new HashMap<>(1);
    Map<String, Boolean> isRightOnTopicsEnabledCache = new HashMap<>(1);
    Set<String> availableComponentCache = null;
    Map<String, Set<String>> userProfiles = null;
    Map<String, Map<String, String>> componentParameterValueCache = null;

    DataManager() {
      controller = OrganizationController.get();
    }

    void loadCaches(final String userId, final Collection<String> instanceIds) {
      if (availableComponentCache != null || instanceIds.isEmpty()) {
        return;
      }
      final int nbElements = instanceIds.size();
      componentInstancesCache = new HashMap<>(nbElements);
      isRightOnTopicsEnabledCache = new HashMap<>(nbElements);
      isPublicationSharingEnabledForRoleCache = new HashMap<>(nbElements);
      isFileSharingEnabledForRoleCache = new HashMap<>(nbElements);
      isFolderSharingEnabledForRoleCache = new HashMap<>(nbElements);
      isCoWritingEnabledCache = new HashMap<>(nbElements);
      isTopicTrackerSupportedCache = new HashMap<>(nbElements);
      availableComponentCache = new HashSet<>(controller.getAvailableComponentsByUser(userId));
      completeCaches(userId, instanceIds);
    }

    void completeCaches(final String userId, final Collection<String> instanceIds) {
      final boolean firstLoad = userProfiles == null;
      if (firstLoad) {
        userProfiles = controller.getUserProfilesByComponentId(userId, instanceIds);
      } else {
        controller.getUserProfilesByComponentId(userId, instanceIds)
            .forEach((k, v) -> userProfiles.put(k, v));
      }
      if (firstLoad) {
        componentParameterValueCache = controller
            .getParameterValuesByComponentIdThenByParamName(instanceIds, HANDLED_PARAM_NAMES);
      } else {
        controller.getParameterValuesByComponentIdThenByParamName(instanceIds, HANDLED_PARAM_NAMES)
            .forEach((k, v) -> componentParameterValueCache.put(k, v));
      }
    }

    boolean isRightOnTopicsEnabled(final String instanceId) {
      return isRightOnTopicsEnabledCache.computeIfAbsent(instanceId, s ->
          isTopicTrackerSupported(instanceId) && getBooleanValue(getComponentParameterValue(instanceId, RIGHTS_ON_TOPICS_PARAM_NAME)));
    }

    boolean isPublicationSharingEnabledForRole(final String componentId, final SilverpeasRole greatestUserRole) {
      return isPublicationSharingEnabledForRoleCache
          .computeIfAbsent(componentId + "@" + greatestUserRole,
              s -> isSharingEnabledForRole(componentId, greatestUserRole, "usePublicationSharing"));
    }

    boolean isFileSharingEnabledForRole(String componentId, SilverpeasRole greatestUserRole) {
      return isFileSharingEnabledForRoleCache.computeIfAbsent(componentId + "@" + greatestUserRole,
          s -> isSharingEnabledForRole(componentId, greatestUserRole, "useFileSharing"));
    }

    boolean  isFolderSharingEnabledForRole(String componentId, SilverpeasRole greatestUserRole) {
      return isFolderSharingEnabledForRoleCache.computeIfAbsent(componentId + "@" + greatestUserRole,
          s -> isSharingEnabledForRole(componentId, greatestUserRole, "useFolderSharing"));
    }

    boolean isCoWritingEnabled(String componentId) {
      return isCoWritingEnabledCache.computeIfAbsent(componentId, s ->
          isTopicTrackerSupported(s) && getBooleanValue(getComponentParameterValue(s, "coWriting")));
    }

    boolean isTopicTrackerSupported(String componentId) {
      return isTopicTrackerSupportedCache.computeIfAbsent(componentId, s ->
          getComponentInstance(s).filter(SilverpeasComponentInstance::isTopicTracker).isPresent());
    }

    boolean isPublicFilesEnabled(String componentId) {
      return getBooleanValue(getComponentParameterValue(componentId, "publicFiles"));
    }

    boolean isToolAvailable(final String componentId) {
      return controller.isToolAvailable(componentId);
    }

    Optional<SilverpeasComponentInstance> getComponentInstance(final String componentId) {
      return componentInstancesCache.computeIfAbsent(componentId, s -> controller.getComponentInstance(componentId));
    }

    boolean isComponentAvailableToUser(final String componentId, final String userId) {
      if (availableComponentCache != null) {
        return availableComponentCache.contains(componentId);
      }
      return controller.isComponentAvailableToUser(componentId, userId);
    }

    String[] getUserProfiles(final String componentId, final String userId) {
      if (userProfiles != null) {
        return userProfiles.getOrDefault(componentId, emptySet()).toArray(new String[0]);
      }
      return controller.getUserProfiles(userId, componentId);
    }

    private boolean isSharingEnabledForRole(String componentId, SilverpeasRole greatestUserRole,
        String parameterName) {
      if (!isTopicTrackerSupported(componentId)) {
        return false;
      }
      final String value = getComponentParameterValue(componentId, parameterName);
      if ("1".equals(value)) {
        return greatestUserRole.isGreaterThanOrEquals(SilverpeasRole.admin);
      } else if ("2".equals(value)) {
        return greatestUserRole.isGreaterThanOrEquals(SilverpeasRole.writer);
      }
      return "3".equals(value);
    }

    private String getComponentParameterValue(final String componentId,
        final String parameterName) {
      if (componentParameterValueCache != null) {
        return componentParameterValueCache.getOrDefault(componentId, emptyMap()).getOrDefault(parameterName, EMPTY);
      }
      return controller.getComponentParameterValue(componentId, parameterName);
    }
  }
}
