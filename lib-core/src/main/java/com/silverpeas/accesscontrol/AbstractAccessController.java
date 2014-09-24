/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.silverpeas.accesscontrol;

import org.silverpeas.util.StringUtil;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.util.WAPrimaryKey;
import org.silverpeas.cache.service.CacheServiceFactory;

import java.util.EnumSet;
import java.util.Set;

/**
 * User: Yohann Chastagnier
 * Date: 30/12/13
 */
public abstract class AbstractAccessController<T> implements AccessController<T> {

  @Override
  public final boolean isUserAuthorized(String userId, T object) {
    return isUserAuthorized(userId, object, AccessControlContext.init());
  }

  /**
   * Gets the user roles about the aimed object and by taking in account the context of the access.
   * After a first call, user role are cached (REQUEST live time) in order to increase the
   * performances in case of several call on the same user and object.
   * @param context
   * @param userId
   * @param object
   * @return
   */
  @SuppressWarnings("unchecked")
  public Set<SilverpeasRole> getUserRoles(AccessControlContext context, String userId, T object) {
    String cacheKey = buildUserRoleCacheKey(context, userId, object);
    Set<SilverpeasRole> userRoles =
        CacheServiceFactory.getRequestCacheService().get(cacheKey, Set.class);
    if (userRoles == null) {
      userRoles = EnumSet.noneOf(SilverpeasRole.class);
      fillUserRoles(userRoles, context, userId, object);
      CacheServiceFactory.getRequestCacheService().put(cacheKey, userRoles);
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
