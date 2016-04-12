/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
package org.silverpeas.core.webapi.profile;

import org.silverpeas.core.annotation.RequestScoped;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.security.session.SessionManagement;
import org.silverpeas.core.webapi.base.RESTWebService;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.user.model.UserFull;
import org.apache.commons.codec.binary.Base64;
import org.silverpeas.core.security.authentication.AuthenticationCredential;
import org.silverpeas.core.security.authentication.AuthenticationService;
import org.silverpeas.core.security.authentication.AuthenticationServiceProvider;
import org.silverpeas.core.security.authentication.UserSessionReference;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.security.token.persistent.PersistentResourceToken;
import org.silverpeas.core.util.Charsets;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.silverpeas.core.webapi.base.UserPrivilegeValidation.HTTP_AUTHORIZATION;
import static org.silverpeas.core.webapi.base.UserPrivilegeValidation.HTTP_SESSIONKEY;
import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * A REST-based Web service that handles the authentication of a user to the data server.
 */
@Service
@RequestScoped
@Path("authentication")
public class AuthenticationResource extends RESTWebService {

  private static Pattern AUTHORIZATION_PATTERN = Pattern.compile("(?i)^Basic (.*)");

  // the first ':' character is the separator according to the RFC 2617 in basic digest
  private static Pattern AUTHENTICATION_PATTERN =
      Pattern.compile("(?i)^[\\s]*([^\\s]+)[\\s]*@domain([0-9]+):(.+)$");

  @Inject
  private SessionManagement sessionManagement;

  /**
   * Creates a new instance of UserProfileResource
   */
  public AuthenticationResource() {
  }

  /**
   * @return The user entity corresponding to the token specified in the URI.
   */
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  public UserProfileEntity authenticate() {

    try {
      String authorizationValue = getHttpRequest().getHeader(HTTP_AUTHORIZATION);
      Matcher authorizationMatcher = AUTHORIZATION_PATTERN.matcher(authorizationValue);
      if (!authorizationMatcher.matches() || authorizationMatcher.groupCount() != 1) {
        throw new WebApplicationException(Status.UNAUTHORIZED);
      }
      String userCredentials = authorizationMatcher.group(1);
      if (isDefined(userCredentials)) {
        String decodedCredentials =
            new String(Base64.decodeBase64(userCredentials), Charsets.UTF_8);

        // Getting expected parts of credentials
        Matcher matcher = AUTHENTICATION_PATTERN.matcher(decodedCredentials);
        if (matcher.matches() && matcher.groupCount() == 3) {

          // All expected parts detected, so getting an authentication key
          AuthenticationCredential credential =
              AuthenticationCredential.newWithAsLogin(matcher.group(1))
                  .withAsPassword(matcher.group(3)).withAsDomainId(matcher.group(2));
          AuthenticationService authenticator = AuthenticationServiceProvider.getService();
          String key = authenticator.authenticate(credential);
          if (!authenticator.isInError(key)) {
            String userId = Administration.get().getUserIdByAuthenticationKey(key);
            UserFull user = OrganizationController.get().getUserFull(userId);

            // The key is correct, so computing a new authentication token to return to the user.
            PersistentResourceToken userSessionToken =
                PersistentResourceToken.createToken(UserSessionReference.fromUser(user));
            if (userSessionToken.isDefined()) {

              // Setting the new token into the response headers
              getHttpServletResponse().setHeader("Access-Control-Expose-Headers", HTTP_SESSIONKEY);
              getHttpServletResponse().setHeader(HTTP_SESSIONKEY, userSessionToken.getValue());

              // Returning the user profile
              return UserProfileEntity.fromUser(user).withAsUri(ProfileResourceBaseURIs.uriOfUser(userId));
            }
          }
        }
      }
      throw new WebApplicationException(Status.UNAUTHORIZED);
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  @Override
  public String getComponentId() {
    return null;
  }
}
