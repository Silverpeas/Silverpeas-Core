/*
 * Copyright (C) 2000 - 2019 Silverpeas
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

package org.silverpeas.core.security;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.cache.model.SimpleCache;
import org.silverpeas.core.cache.service.CacheServiceProvider;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * A common tool to handled securable implementation with a request cache.
 * <p>Indeed, the computing about secured access can be costly about performances and using a
 * request cache could be interesting when access authorization is computed several times for a same
 * entity into the context of a same request.</p>
 * @author Yohann Chastagnier
 */
public class SecurableRequestCache {

  private static final String CAN_BE = "canBe";
  static final String CAN_BE_ACCESSED_BY_KEY_SUFFIX = CAN_BE + "AccessedBy";
  static final String CAN_BE_MODIFIED_BY_KEY_SUFFIX = CAN_BE + "ModifiedBy";
  static final String CAN_BE_DELETED_BY_KEY_SUFFIX = CAN_BE + "DeletedBy";
  private static final String ALL_KEYS = "SecurableRequestCache@@@allKeys";
  private static final int DEFAULT_NB_HANDLED_KEYS = 100;

  /**
   * Hidden constructor.
   */
  private SecurableRequestCache() {
  }

  /**
   * @param user a user in Silverpeas.
   * @param uniqueIdentifier a unique identifier.
   * @param authorization the supplier of the result of access authorization computing.
   * @param keySuffix the key suffix to distinguish the different types of accessibility.
   * @return the authorization
   */
  private static boolean handle(final User user, final String uniqueIdentifier,
      Function<User, Boolean> authorization, final String keySuffix) {
    SimpleCache cache = CacheServiceProvider.getRequestCacheService().getCache();
    String cacheKey = getCacheKey(user, uniqueIdentifier, keySuffix);
    synchronized (ALL_KEYS) {
      Boolean result = cache.get(cacheKey, Boolean.class);
      if (result == null) {
        result = authorization.apply(user);
        cache.put(cacheKey, result);
        getHandledKeys().add(cacheKey);
      }
      return result;
    }
  }

  @SuppressWarnings("unchecked")
  private static Set<String> getHandledKeys() {
    synchronized (ALL_KEYS) {
      SimpleCache cache = CacheServiceProvider.getRequestCacheService().getCache();
      Set<String> handledKeys = (Set) cache.get(ALL_KEYS, Set.class);
      if (handledKeys == null) {
        handledKeys = new HashSet<>(DEFAULT_NB_HANDLED_KEYS);
        cache.put(ALL_KEYS, handledKeys);
      }
      return handledKeys;
    }
  }

  /**
   * Indicates if the given user can access the data managed by the object instance.
   * @param user a user in Silverpeas.
   * @param uniqueIdentifier a unique identifier.
   * @param canBeAccessedBy the supplier of the result of access authorization computing.
   * @return true if the user can access the data managed by this instance, false otherwise.
   */
  public static boolean canBeAccessedBy(User user, String uniqueIdentifier,
      Function<User, Boolean> canBeAccessedBy) {
    return handle(user, uniqueIdentifier, canBeAccessedBy, CAN_BE_ACCESSED_BY_KEY_SUFFIX);
  }

  /**
   * Indicates if the given user can modify the data managed by the object instance.
   * @param user a user in Silverpeas.
   * @param uniqueIdentifier a unique identifier.
   * @param canBeModifiedBy the supplier of the result of access authorization computing.
   * @return true if the user can modify the data managed by this instance, false otherwise.
   */
  public static boolean canBeModifiedBy(User user, String uniqueIdentifier,
      Function<User, Boolean> canBeModifiedBy) {
    return handle(user, uniqueIdentifier, canBeModifiedBy, CAN_BE_MODIFIED_BY_KEY_SUFFIX);
  }

  /**
   * Indicates if the given user can delete the data managed by the object instance.
   * @param user a user in Silverpeas.
   * @param uniqueIdentifier a unique identifier.
   * @param canBeDeletedBy the supplier of the result of access authorization computing.
   * @return true if the user can delete the data managed by this instance, false otherwise.
   */
  public static boolean canBeDeletedBy(User user, String uniqueIdentifier,
      Function<User, Boolean> canBeDeletedBy) {
    return handle(user, uniqueIdentifier, canBeDeletedBy, CAN_BE_DELETED_BY_KEY_SUFFIX);
  }

  /**
   * Clears the cache linked to an entity represented by the given unique identifier.
   * @param uniqueIdentifier an identifier which represents a unique entity.
   */
  public static void clear(String uniqueIdentifier) {
    final String keyPart = "@@@" + uniqueIdentifier + "@@@" + CAN_BE;
    final SimpleCache cache = CacheServiceProvider.getRequestCacheService().getCache();
    synchronized (ALL_KEYS) {
      getHandledKeys().removeIf(handledKey -> {
        boolean hasToBeRemoved = handledKey.contains(keyPart);
        if (hasToBeRemoved) {
          cache.remove(handledKey);
        }
        return hasToBeRemoved;
      });
    }
  }

  /**
   * Computes the base of the cache key from given data.
   * @param user a user in Silverpeas.
   * @param uniqueIdentifier a unique identifier.
   * @param keySuffix the key suffix to distinguish the different types of accessibility.
   * @return the base of cache key.
   */
  static String getCacheKey(final User user, final String uniqueIdentifier,
      final String keySuffix) {
    return "SecurableRequestCache@@@user_" + user.getId() + "@@@" + uniqueIdentifier + "@@@" +
        keySuffix;
  }
}
