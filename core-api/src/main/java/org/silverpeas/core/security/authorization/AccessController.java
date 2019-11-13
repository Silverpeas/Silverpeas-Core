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

import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.util.CollectionUtil;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A controller of accesses on the Silverpeas resources by a user..
 * @param <T> The type of object we are checking the access on.
 * @author ehugonnet
 */
public interface AccessController<T> {

  /**
   * Filters the given object list in order to keep those the specified user is authorized on.
   * <p>
   * This aim of this method is to be as efficient as possible on large volume of data.
   * </p>
   * @param objects the objects to filter.
   * @param userId the unique identifier of the user.
   * @return true if access is granted - false otherwise.
   */
  Stream<T> filterAuthorizedByUser(final List<T> objects, final String userId);

  /**
   * Filters the given object list in order to keep those the specified user is authorized on.
   * <p>
   * This aim of this method is to be as efficient as possible on large volume of data.
   * </p>
   * @param objects the objects to filter.
   * @param userId the unique identifier of the user.
   * @param context the context in which the object is accessed.
   * @return true if access is granted - false otherwise.
   */
  Stream<T> filterAuthorizedByUser(final List<T> objects, final String userId, final AccessControlContext context);

  /**
   * Checks user authorization from the given role collection.
   * @param userRoles user roles.
   * @return true if user authorization, false otherwise.
   */
  default boolean isUserAuthorized(Set<SilverpeasRole> userRoles) {
    return CollectionUtil.isNotEmpty(userRoles);
  }

  /**
   * Checks if the specified user may access the specified object.
   * @param userId the unique identifier of the user.
   * @param object the object to be accessed.
   * @return true if access is granted - false otherwise.
   */
  boolean isUserAuthorized(String userId, T object);

  /**
   * Checks if the specified user may access the specified object.
   * @param userId the unique identifier of the user.
   * @param object the object to be accessed.
   * @param context the context in which the object is accessed.
   * @return true if access is granted - false otherwise.
   */
  boolean isUserAuthorized(String userId, T object, AccessControlContext context);

  /**
   * Gets the user roles about the aimed object and by taking in account the context of the access.
   * After a first call, user role are cached (REQUEST live time) in order to increase the
   * performances in case of several call on the same user and object.
   * @param userId the unique identifier of the user.
   * @param object the object to be accessed.
   * @param context the context in which the object is accessed.
   * @return the role the user has about a resource and according to a context.
   */
  Set<SilverpeasRole> getUserRoles(String userId, T object, AccessControlContext context);

  /**
   * Is the specified group authorized to access the given object with at least read
   * privileges? The roles of the group on the object aren't taken into account. The group
   * should have at least the user role to access the object unless the object is
   * public.
   * @param groupId the unique identifier of a group.
   * @param object the unique identifier of the object to be accessed.
   * @return true if the group can access the given object, false otherwise.
   */
  default boolean isGroupAuthorized(final String groupId, final T object) {
    // This method must be overridden if needed
    throw new UnsupportedOperationException();
  }
}
