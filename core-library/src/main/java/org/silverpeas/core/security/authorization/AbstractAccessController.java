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

import org.silverpeas.core.WAPrimaryKey;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.kernel.util.StringUtil;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This abstract class provides common implementation about the access controller :
 * <ul>
 *   <li>- cache provider</li>
 *   <li>- user roles</li>
 * </ul>
 */
public abstract class AbstractAccessController<T> implements AccessController<T> {

  @Override
  public Stream<T> filterAuthorizedByUser(final Collection<T> objects, final String userId) {
    return filterAuthorizedByUser(objects, userId, AccessControlContext.init());
  }

  @Override
  public Stream<T> filterAuthorizedByUser(final Collection<T> objects, final String userId,
      final AccessControlContext context) {
    throw new UnsupportedOperationException();
  }

  @Override
  public final boolean isUserAuthorized(String userId, T object) {
    return isUserAuthorized(userId, object, AccessControlContext.init());
  }

  @Override
  @SuppressWarnings("unchecked")
  public final Set<SilverpeasRole> getUserRoles(String userId, T object,
      AccessControlContext context) {
    String cacheKey = buildUserRoleCacheKey(context, userId, object);
    Set<SilverpeasRole> userRoles = context.get(cacheKey, Set.class);
    if (userRoles == null) {
      userRoles = EnumSet.noneOf(SilverpeasRole.class);
      fillUserRoles(userRoles, context, userId, object);
      context.put(cacheKey, userRoles);
    }
    return userRoles;
  }

  /**
   * Fills in the specified set the roles the user plays for the given resource in Silverpeas
   * according to the specified access context.
   * @param userRoles the set to fill in.
   * @param context the context defining the type of access with some additional parameters.
   * @param userId the unique identifier of the user.
   * @param object the resource in Silverpeas accessed by the user.
   */
  protected void fillUserRoles(Set<SilverpeasRole> userRoles, AccessControlContext context,
      String userId, T object) {
    // This method must be overridden if needed
    throw new UnsupportedOperationException();
  }

  private String buildUserRoleCacheKey(AccessControlContext context, String userId, T object) {
    StringBuilder cacheKey = new StringBuilder(getClass().getName()).append("@#@");
    cacheKey.append("USERID").append(userId).append("@#@");
    cacheKey.append("OBJECTID");
    if (object instanceof String) {
      cacheKey.append(object);
    } else if (object instanceof WAPrimaryKey) {
      WAPrimaryKey pk = (WAPrimaryKey) object;
      cacheKey.append(pk.getId()).append("|").append(pk.getInstanceId());
    } else if (object != null) {
      throw new UnsupportedOperationException();
    }
    EnumSet<AccessControlOperation> orderedOperations = EnumSet.copyOf(context.getOperations());
    cacheKey.append("@#@").append("OPERATIONS")
        .append(String.join("|", orderedOperations.stream()
            .map(o -> Objects.toString(o, StringUtil.EMPTY))
            .collect(Collectors.toList())));
    return cacheKey.toString();
  }
}
