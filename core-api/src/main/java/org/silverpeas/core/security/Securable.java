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
package org.silverpeas.core.security;

import org.silverpeas.core.admin.user.model.User;

/**
 * A securable object is an object that provides the necessary to verify the user accessibility on
 * it.
 * @author Yohann Chastagnier
 */
public interface Securable {

  /**
   * Indicates if the given user can access the data managed by the object instance.
   * @param user a user in Silverpeas.
   * @return true if the user can access the data managed by this instance, false otherwise.
   */
  default boolean canBeAccessedBy(User user) {
    return false;
  }

  /**
   * Indicates if the given user can modify the data managed by the object instance.
   * @param user a user in Silverpeas.
   * @return true if the user can modify the data managed by this instance, false otherwise.
   */
  default boolean canBeModifiedBy(User user) {
    return canBeAccessedBy(user);
  }

  /**
   * Indicates if the given user can delete the data managed by the object instance.
   * @param user a user in Silverpeas.
   * @return true if the user can delete the data managed by this instance, false otherwise.
   */
  default boolean canBeDeletedBy(User user) {
    return canBeModifiedBy(user);
  }
}
