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
package org.silverpeas.core.security.authorization;

import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.UserSpaceAvailabilityChecker;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;

import javax.inject.Singleton;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static org.silverpeas.core.admin.space.SpaceInst.SPACE_KEY_PREFIX;
import static org.silverpeas.core.admin.user.model.SilverpeasRole.MANAGER;
import static org.silverpeas.core.security.authorization.AccessControlOperation.MODIFICATION;
import static org.silverpeas.core.util.StringUtil.EMPTY;

/**
 * Check the access to a space for a user.
 * @author Yohann Chastagnier
 */
@Service
@Singleton
public class SpaceAccessController extends AbstractAccessController<String>
    implements SpaceAccessControl {

  private static final String LIGHT_ROLE_COMPUTING = "light_role_computing";
  private static final String DATA_MANAGER_CONTEXT_KEY = "SpaceAccessControllerDataManager";

  SpaceAccessController() {
    // Instance by IoC only.
  }

  @Override
  public boolean isUserAuthorized(final String userId, final String spaceId,
      final AccessControlContext context) {
    context.put(LIGHT_ROLE_COMPUTING, true);
    try {
      return isUserAuthorized(getUserRoles(userId, spaceId, context));
    } finally {
      context.put(LIGHT_ROLE_COMPUTING, null);
    }
  }

  @Override
  public Stream<String> filterAuthorizedByUser(final Collection<String> spaceIds,
      final String userId, final AccessControlContext context) {
    getDataManager(context).loadCaches(userId, spaceIds);
    return spaceIds.stream().filter(s -> isUserAuthorized(userId, s, context));
  }

  @Override
  protected void fillUserRoles(final Set<SilverpeasRole> userRoles,
      final AccessControlContext context, final String userId, final String spaceId) {
    final DataManager dataManager = getDataManager(context);
    if (!AccessControlOperation.isPersistActionFrom(context.getOperations()) &&
        dataManager.isSpaceAvailableToUser(spaceId, userId)) {
      // in that case, it means that the user can access the space
      if (Boolean.TRUE.equals(context.get(LIGHT_ROLE_COMPUTING, Boolean.class))) {
        userRoles.add(SilverpeasRole.USER);
      } else {
        userRoles.addAll(dataManager.getSpaceUserRoles(spaceId, userId));
        userRoles.remove(SilverpeasRole.MANAGER);
        if (userRoles.isEmpty()) {
          // If empty, it means that the space contains only public component instances
          userRoles.add(SilverpeasRole.USER);
        }
      }
    } else if (context.getOperations().contains(MODIFICATION)
        && (dataManager.getUser(userId).isAccessAdmin()
            || dataManager.getManageableSpaceIds(userId)
                  .contains(spaceId.replace(SPACE_KEY_PREFIX, EMPTY)))) {
      // in that case, it means that the user is administrator of the space, but it does not mean
      // that he can access it
      userRoles.add(SilverpeasRole.MANAGER);
    }
  }

  @Override
  public boolean hasUserSpaceManagementAuthorization(final String userId, final String spaceId,
      final AccessControlContext context) {
    boolean hasAddedModificationOperation = !context.getOperations().contains(MODIFICATION);
    context.onOperationsOf(MODIFICATION);
    try {
      return getUserRoles(userId, spaceId, context).contains(MANAGER);
    } finally {
      if (hasAddedModificationOperation) {
        context.removeOperationsOf(MODIFICATION);
      }
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

  /**
   * Data manager.
   */
  public static class DataManager {
    private final OrganizationController controller;
    private UserSpaceAvailabilityChecker userSpaceAvailabilityChecker;
    private final Map<String, Set<String>> manageableSpaceIdsCache = new HashMap<>(1);
    Map<String, Set<String>> spaceUserProfiles = null;

    DataManager() {
      controller = OrganizationController.get();
    }

    void loadCaches(final String userId, final Collection<String> spaceIds) {
      if (spaceIds.isEmpty()) {
        return;
      }
      completeCaches(userId, spaceIds);
    }

    void completeCaches(final String userId, final Collection<String> spaceIds) {
      final boolean firstLoad = spaceUserProfiles == null;
      if (firstLoad) {
        spaceUserProfiles = controller.getSpaceUserProfilesBySpaceIds(userId, spaceIds);
      } else {
        spaceUserProfiles.putAll(controller.getSpaceUserProfilesBySpaceIds(userId, spaceIds));
      }
    }

    User getUser(final String userId) {
      return User.getById(userId);
    }

    Collection<SilverpeasRole> getSpaceUserRoles(final String spaceId, final String userId) {
      final Collection<String> profiles;
      if (spaceUserProfiles != null) {
        profiles = spaceUserProfiles.getOrDefault(spaceId, emptySet());
      } else {
        profiles = controller.getSpaceUserProfilesBySpaceId(userId, spaceId);
      }
      return profiles.stream()
          .map(SilverpeasRole::fromString)
          .collect(Collectors.toList());
    }

    boolean isSpaceAvailableToUser(final String spaceId, final String userId) {
      if (userSpaceAvailabilityChecker == null) {
        userSpaceAvailabilityChecker = controller.getUserSpaceAvailabilityChecker(userId);
      }
      return userSpaceAvailabilityChecker.isAvailable(spaceId);
    }

    public Set<String> getManageableSpaceIds(final String userId) {
      return manageableSpaceIdsCache.computeIfAbsent(userId,
          s -> Stream.of(controller.getUserManageableSpaceIds(s)).collect(Collectors.toSet()));
    }
  }
}
