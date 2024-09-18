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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.security;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.kernel.cache.model.SimpleCache;
import org.silverpeas.core.cache.service.CacheAccessorProvider;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * A common tool to handle authorization on resources with a request cache in order to avoid
 * multiple authorization computations for a given user within the same request flow.
 * <p>
 * Indeed, the computation of access grant can be costly in performances and using a request cache
 * is useful when the access authorization to a given resource and for a given user is computed
 * several times within the context of the same user request.
 * </p>
 *
 * @author Yohann Chastagnier
 */
public class AuthorizationRequestCache {

  private static final String CAN_BE = "canBe";
  static final String CAN_BE_ACCESSED_BY_KEY_SUFFIX = CAN_BE + "AccessedBy";
  static final String CAN_BE_MODIFIED_BY_KEY_SUFFIX = CAN_BE + "ModifiedBy";
  static final String CAN_BE_DELETED_BY_KEY_SUFFIX = CAN_BE + "DeletedBy";
  private static final Object ALL_KEYS = "AuthorizationRequestCache@@@allKeys";
  private static final int DEFAULT_NB_HANDLED_KEYS = 100;

  /**
   * Hidden constructor.
   */
  private AuthorizationRequestCache() {
  }

  /**
   * Applies the specified access authorization mechanism to the given resource for the specified
   * user and caches the result.
   *
   * @param user the user behind the current incoming request and wanting to access the resource.
   * @param uniqueIdentifier the unique identifier of the resource for which the access grant is
   * computed.
   * @param authorization the authorization computer.
   * @param keySuffix the key suffix to distinguish the different types of accessibility.
   * @return the authorization result. True if the user is granted to access the resource, false
   * otherwise.
   */
  private static boolean handle(final User user, final String uniqueIdentifier,
      Predicate<User> authorization, final String keySuffix) {
    SimpleCache cache = CacheAccessorProvider.getThreadCacheAccessor().getCache();
    String cacheKey = getCacheKey(user, uniqueIdentifier, keySuffix);
    synchronized (ALL_KEYS) {
      Boolean result = cache.get(cacheKey, Boolean.class);
      if (result == null) {
        result = authorization.test(user);
        cache.put(cacheKey, result);
        getHandledKeys().add(cacheKey);
      }
      return result;
    }
  }

  @SuppressWarnings("unchecked")
  private static Set<String> getHandledKeys() {
    synchronized (ALL_KEYS) {
      SimpleCache cache = CacheAccessorProvider.getThreadCacheAccessor().getCache();
      Set<String> handledKeys = cache.get(ALL_KEYS, Set.class);
      if (handledKeys == null) {
        handledKeys = new HashSet<>(DEFAULT_NB_HANDLED_KEYS);
        cache.put(ALL_KEYS, handledKeys);
      }
      return handledKeys;
    }
  }

  /**
   * Is the given user can access the specified resource?
   *
   * @param user a user in Silverpeas.
   * @param uniqueIdentifier the unique identifier of the accessed resource.
   * @param canBeAccessedBy the access grant computation.
   * @return true if the user is granted to access the specified resource, false otherwise.
   */
  public static boolean canBeAccessedBy(User user, String uniqueIdentifier,
      Predicate<User> canBeAccessedBy) {
    return handle(user, uniqueIdentifier, canBeAccessedBy, CAN_BE_ACCESSED_BY_KEY_SUFFIX);
  }

  /**
   * Is the given user can modify the specified resource or the data managed by the specified
   * resource.
   *
   * @param user a user in Silverpeas.
   * @param uniqueIdentifier the unique identifier of the resource.
   * @param canBeModifiedBy the modification grant computation.
   * @return true if the user is granted to modify the resource (or the data managed by the
   * resource), false otherwise.
   */
  public static boolean canBeModifiedBy(User user, String uniqueIdentifier,
      Predicate<User> canBeModifiedBy) {
    return handle(user, uniqueIdentifier, canBeModifiedBy, CAN_BE_MODIFIED_BY_KEY_SUFFIX);
  }

  /**
   * Is the the given user can delete the specified resource or the data managed by the specified
   * resource.
   *
   * @param user a user in Silverpeas.
   * @param uniqueIdentifier the unique identifier of the resource.
   * @param canBeDeletedBy the deletion grant computation.
   * @return true if the user is granted to delete the resource (or the data managed by the
   * resource), false otherwise.
   */
  public static boolean canBeDeletedBy(User user, String uniqueIdentifier,
      Predicate<User> canBeDeletedBy) {
    return handle(user, uniqueIdentifier, canBeDeletedBy, CAN_BE_DELETED_BY_KEY_SUFFIX);
  }

  /**
   * Clears the authorization caches for the specified resource.
   *
   * @param uniqueIdentifier the unique identifier of a resource for which an authorization has
   * been computed.
   */
  public static void clear(String uniqueIdentifier) {
    final String keyPart = "@@@" + uniqueIdentifier + "@@@" + CAN_BE;
    final SimpleCache cache = CacheAccessorProvider.getThreadCacheAccessor().getCache();
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
   * Computes the base of the cache key by the user accessing the specified resource.
   *
   * @param user a user in Silverpeas.
   * @param uniqueIdentifier the unique identifier of a resource.
   * @param keySuffix the key suffix to distinguish the different types of accessibility.
   * @return the base of cache key.
   */
  static String getCacheKey(final User user, final String uniqueIdentifier,
      final String keySuffix) {
    return "AuthorizationRequestCache@@@user_" + user.getId() + "@@@" + uniqueIdentifier + "@@@" +
        keySuffix;
  }
}
