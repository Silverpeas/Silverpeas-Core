/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.webapi.base;

import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.util.ServiceProvider;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;

/**
 * @author mmoquillon
 */
public interface UserPrivilegeValidation {

  static UserPrivilegeValidation get() {
    return ServiceProvider.getService(UserPrivilegeValidation.class);
  }

  /**
   * The HTTP header paremeter in an incoming request that carries the user session key. By the user
   * session key could be passed a user token to perform a HTTP request without opening a session.
   * This parameter isn't mandatory as the session key can be found from an active HTTP session. If
   * neither HTTP session nor session key is available for the incoming request, user credentials
   * must be passed in the standard HTTP header parameter Authorization.
   */
  String HTTP_SESSIONKEY = "X-Silverpeas-Session";
  /**
   * The standard HTTP header parameter in an incoming request that carries user credentials
   * information in order to open an authorized connexion with the web service that backs the
   * refered resource. This parameter must be used when requests aren't sent through an opened HTTP
   * session. It should be the prefered way for a REST client to access resources in Silverpeas as
   * it offers better scalability.
   */
  String HTTP_AUTHORIZATION = "Authorization";

  /**
   * Validates the authentication of the user at the origin of a web request.
   *
   * The validation checks first the user is already authenticated and then it has a valid opened
   * session in Silverpeas. Otherwise it attempts to open a new session for the user by using its
   * credentials passed through the request (as an HTTP header). Once the authentication succeed,
   * the identification of the user is done and detail about it can then be got. A runtime exception
   * is thrown with an HTTP status code UNAUTHORIZED (401) at validation failure. The validation
   * fails when one of the belowed situation is occuring: <ul> <li>The user session key is
   * invalid;</li> <li>The user isn't authenticated and no credentials are passed with the
   * request;</li> <li>The user authentication failed.</li> </ul>
   *
   * @param request the HTTP request from which the authentication of the caller can be done.
   * @return the opened session of the user at the origin of the specified request.
   * @throws WebApplicationException exception if the validation failed.
   */
  SessionInfo validateUserAuthentication(HttpServletRequest request) throws WebApplicationException;

  /**
   * Sets into the request attributes the {@link
   * UserPrivilegeValidator#SKIP_LAST_USER_ACCESS_TIME_REGISTERING} attribute to true.
   * @param request the current request performed.
   * @return itself.
   */
  UserPrivilegeValidation skipLastUserAccessTimeRegistering(HttpServletRequest request);

  /**
   * Validates the authorization of the specified user to access the component instance with the
   * specified unique identifier.
   *
   * @param user the user for whom the authorization has to be validated.
   * @param instanceId the unique identifier of the accessed component instance.
   * @throws WebApplicationException exception if the validation failed.
   */
  void validateUserAuthorizationOnComponentInstance(UserDetail user, String instanceId)
              throws WebApplicationException;

  /**
   * Validates the authorization of the specified user to access the specified attachment.
   *
   * @param request the HTTP request from which the authentication of the caller can be done.
   * @param user the user for whom the authorization has to be validated.
   * @param doc the document accessed.
   * @throws WebApplicationException exception if the validation failed.
   */
  void validateUserAuthorizationOnAttachment(HttpServletRequest request, UserDetail user,
      SimpleDocument doc) throws WebApplicationException;
}
