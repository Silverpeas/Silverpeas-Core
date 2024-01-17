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

package org.silverpeas.core.admin.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static org.silverpeas.core.admin.service.RemovedSpaceAndComponentInstanceChecker.create;

/**
 * Centralizing the determination of the space availability to a user.
 * <p>
 * The aim of this implementation is to give the availability of a space taking also into
 * account the status of a space.
 * </p>
 * <p>
 * An other aim of this implementation is also to provide some performances by using some
 * computing caches. These caches MUST be scoped only to the treatment context. Due to this
 * behavior, be careful to not keep an instance reference into a kind of singleton.
 * </p>
 * @author silveryocha
 */
public class UserSpaceAvailabilityChecker {

  private final String userId;
  private final Set<String> userAllowedComponentIds;
  private final Function<String, Integer> toIntSpaceId;
  private final BiPredicate<Set<String>, Integer> isSpaceContainsOneComponent;
  private final RemovedSpaceAndComponentInstanceChecker removedChecker;

  UserSpaceAvailabilityChecker(final String userId) {
    this(userId, emptyList(), null, null);
  }

  UserSpaceAvailabilityChecker(final String userId,
      final Collection<String> userAllowedComponentIds,
      final Function<String, Integer> toIntSpaceId,
      final BiPredicate<Set<String>, Integer> isSpaceContainsOneComponent) {
    this(userId, userAllowedComponentIds, toIntSpaceId, isSpaceContainsOneComponent,
        create().resetWithCacheSizeOf(1));
  }

  UserSpaceAvailabilityChecker(final String userId,
      final Collection<String> userAllowedComponentIds,
      final Function<String, Integer> toIntSpaceId,
      final BiPredicate<Set<String>, Integer> isSpaceContainsOneComponent,
      final RemovedSpaceAndComponentInstanceChecker removedChecker) {
    this.userId = userId;
    this.userAllowedComponentIds = new HashSet<>(userAllowedComponentIds);
    this.toIntSpaceId = toIntSpaceId;
    this.isSpaceContainsOneComponent = isSpaceContainsOneComponent;
    this.removedChecker = removedChecker;
  }

  /**
   * Gets the identifier of the user linked to the current instance.
   * @return a string representing a user identifier.
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Is the given identifier the one of a space available to the user of the instance
   * @param spaceId the identifier or a space as string.
   * @return true if space is available to the user, false otherwise.
   */
  public boolean isAvailable(String spaceId) {
    return !userAllowedComponentIds.isEmpty() && !removedChecker.isRemovedSpaceById(spaceId) &&
        isSpaceContainsOneComponent.test(userAllowedComponentIds, toIntSpaceId.apply(spaceId));
  }
}
