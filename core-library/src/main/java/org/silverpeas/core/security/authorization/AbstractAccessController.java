/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.core.security.authorization;

import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.WAPrimaryKey;

import java.util.EnumSet;
import java.util.Set;

/**
 * This abstract class provides common implementation about the access controller :
 * <ul>
 *   <li>- cache provider</li>
 *   <li>- user roles</li>
 * </ul>
 */
public abstract class AbstractAccessController<T> implements AccessController<T> {

  @Override
  public final boolean isUserAuthorized(String userId, T object) {
    return isUserAuthorized(userId, object, AccessControlContext.init());
  }

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
   * This method must fill user roles into the given container by taking in account the other
   * parameters.
   * @param userRoles
   * @param context
   * @param userId
   * @param object
   */
  protected void fillUserRoles(Set<SilverpeasRole> userRoles, AccessControlContext context,
      String userId, T object) {
    // This method must be overridden if needed
    throw new UnsupportedOperationException();
  }

  /**
   * Build a unique key for user role cache.
   * @param userId
   * @param object
   * @return
   */
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
        .append(StringUtil.join(orderedOperations.toArray(), "|"));
    return cacheKey.toString();
  }
}
