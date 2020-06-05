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
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.webapi.admin.component;

import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.cache.model.SimpleCache;
import org.silverpeas.core.cache.service.CacheServiceProvider;

import java.util.Optional;

import static org.silverpeas.core.admin.service.OrganizationControllerProvider
    .getOrganisationController;

/**
 * This class provides some methods to get easily user roles on all silverpeas component instances
 * which implementation is one of {@link SilverpeasComponentInstance}).
 */
public class SilverpeasComponentInstanceRoleProvider {

  private static final String HIGHEST_USER_ROLE_PREFIX = "@HIGHEST_COMPONENT_USER_ROLE_PREFIX@";

  /**
   * Hidden constructor.
   */
  private SilverpeasComponentInstanceRoleProvider() {
  }

  /**
   * Gets the highest role on the component represented by the given identifier the current user
   * has.<br>
   * The result is put into request cache in order to improve performances in case of multiple
   * access from a single HTTP request.
   * @param componentInstanceId a component instance identifier.
   * @return a {@link SilverpeasComponentInstanceRoleProvider} instance or null if none.
   */
  public static SilverpeasRole getHighestOfCurrentUserOn(String componentInstanceId) {
    String cacheKey = HIGHEST_USER_ROLE_PREFIX + componentInstanceId;
    SimpleCache cache = CacheServiceProvider.getRequestCacheService().getCache();
    SilverpeasRole highestOfCurrentUser = cache.get(cacheKey, SilverpeasRole.class);
    if (highestOfCurrentUser == null) {
      Optional<SilverpeasComponentInstance> silverpeasComponentInstance =
          getOrganisationController().getComponentInstance(componentInstanceId);
      if (silverpeasComponentInstance.isPresent()) {
        highestOfCurrentUser = silverpeasComponentInstance.get()
            .getHighestSilverpeasRolesFor(User.getCurrentRequester());
      }
      cache.put(cacheKey, highestOfCurrentUser);
    }
    return highestOfCurrentUser;
  }
}
