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

import org.silverpeas.core.ResourceIdentifier;
import org.silverpeas.core.util.ServiceProvider;

/**
 * This interface extends access controller for a Space resource.
 * @author Yohann Chastagnier
 */
public interface SpaceAccessControl extends AccessController<String> {

  static SpaceAccessControl get() {
    return ServiceProvider.getService(SpaceAccessControl.class);
  }

  @Override
  default boolean isUserAuthorized(String userId, ResourceIdentifier id) {
    return isUserAuthorized(userId, id.asString());
  }

  /**
   * Checks if the specified user may manage the specified space represented by its identifier.
   * @param userId the unique identifier of the user.
   * @param spaceId the full identifier of a space.
   * @return true if space management is granted, false otherwise.
   */
  default boolean hasUserSpaceManagementAuthorization(String userId, String spaceId) {
    return hasUserSpaceManagementAuthorization(userId, spaceId, AccessControlContext.init());
  }

  /**
   * Checks if the specified user may manage the specified space represented by its identifier.
   * @param userId the unique identifier of the user.
   * @param spaceId the full identifier of a space.
   * @param context the context in which the space is accessed.
   * @return true if space management is granted, false otherwise.
   */
  boolean hasUserSpaceManagementAuthorization(String userId, String spaceId, AccessControlContext context);
}
