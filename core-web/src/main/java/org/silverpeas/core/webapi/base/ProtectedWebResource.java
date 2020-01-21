/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.core.webapi.base;

import org.silverpeas.core.web.SilverpeasWebResource;

import javax.ws.rs.WebApplicationException;

/**
 * A protected Web resource is a Web resource in Silverpeas that can require the user to be
 * authenticated or authorized to access the instances of the resource. To be authorized the user
 * requires to be authenticated. The web resource validates itself the user accessing it is
 * well authenticated and, optionally, if he's authorized to handle it.
 * @author mmoquillon
 */
public interface ProtectedWebResource extends SilverpeasWebResource, WebAuthenticationValidation {

  /**
   * Validates the authorization of the user to request this web service. For doing, the user must
   * have the rights to access the component instance that manages this web resource. If no such
   * component instance exists, a Not Found HTTP error is thrown (status code 404). Otherwise the
   * validation is delegated to the validation service by passing it the required information.
   * <p>
   * This method should be invoked for web service requiring an authorized access. For doing, the
   * authentication of the user must be first valdiated. Otherwise, the annotation Authorized can be
   * also used instead at class level for both authentication and authorization.
   * </p>
   * @see UserPrivilegeValidator
   * @param validation the validation instance to use.
   * @throws WebApplicationException if the rights of the user are not enough to access this web
   * resource.
   */
  default void validateUserAuthorization(final UserPrivilegeValidation validation) {
    validation.validateUserAuthorizationOnComponentInstance(getSilverpeasContext().getUser(),
        getComponentId());
  }
}
