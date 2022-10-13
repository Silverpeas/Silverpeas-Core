/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Lib
 * Open Source Software ("FLOSS") applications as described in Silverpeas
 * FLOSS exception. You should have received a copy of the text describin
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public Licen
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package org.silverpeas.core.web.rs;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.WebApplicationException;

/**
 * Validation of the authorization of a user to access a web endpoint in Silverpeas. This interface
 * requires to be implemented by all of authorization validators in Silverpeas. The validation of
 * the authorization can be only performed against authenticated users.
 * @author mmoquillon
 */
public interface WebAuthorizationValidation {

  /**
   * Gets the context of Silverpeas linked to the current request. This context must be initialized
   * before the functional request processing.
   * @return {@link SilverpeasRequestContext} instance.
   */
  SilverpeasRequestContext getSilverpeasContext();

  /**
   * Validates the authorization of the user to request the web endpoint referred by the Silverpeas
   * context by using the specified user privilege validation service. By default, it checks only
   * the user has a valid account in Silverpeas. Further or more precise authorization treatments
   * are delegated to implementors.
   * <p>
   * This method should be invoked for web services requiring an authorized user to access them.
   * Otherwise, the annotation {@link org.silverpeas.core.web.rs.annotation.Authorized} can be
   * also used instead at class level.
   * </p>
   * @param validation the validation instance to use.
   * @throws WebApplicationException if the authorization isn't valid (no authorized or
   * authorization failure).
   * @see UserPrivilegeValidator
   */
  default void validateUserAuthorization(final UserPrivilegeValidation validation) {
    final SilverpeasRequestContext context = getSilverpeasContext();
    final User user = context.getUser();
    if (user == null) {
      SilverLogger.getLogger(this)
          .warn("Authorization validation invoked against a non authenticated user!");
      throw new NotAuthorizedException("User no authenticated");
    }
    if (!user.isValidState()) {
      throw new ForbiddenException(
          "User account isn't valid: account " + user.getState().getName());
    }
  }
}
