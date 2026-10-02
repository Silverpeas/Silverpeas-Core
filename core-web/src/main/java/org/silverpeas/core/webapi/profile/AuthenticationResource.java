/*
 * Copyright (C) 2000 - 2026 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Lib
 * Open Source Software ("FLOSS") applications as described in Silverpeas
 * FLOSS exception.  You should have received a copy of the text describing
 * Silverpeas FLOSS exception, and it is also available here:
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
package org.silverpeas.core.webapi.profile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.security.authentication.AuthenticationResponse;
import org.silverpeas.core.security.authentication.AuthenticationServiceProvider;
import org.silverpeas.core.security.authentication.AuthenticationService;
import org.silverpeas.core.security.authentication.AuthenticationResponse.Status;
import org.silverpeas.core.security.authentication.exception.AuthenticationException;
import org.silverpeas.core.security.session.SessionManagementProvider;
import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.security.token.Token;
import org.silverpeas.core.web.rs.HTTPAuthentication;
import org.silverpeas.core.web.rs.RESTWebService;
import org.silverpeas.core.web.rs.SynchronizerTokenService;
import org.silverpeas.core.web.rs.UserPrivilegeValidation;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * A REST-based Web service to authenticate a user in Silverpeas.
 */
@WebService
@Path(AuthenticationResource.PATH)
public class AuthenticationResource extends RESTWebService {

  static final String PATH = "authentication";

  @Inject
  private UserPrivilegeValidation privilegeValidation;

  @Operation(summary = "Authenticates the user from his credentials passed through the " +
      "Authorization HTTP header, opens a new HTTP session in Silverpeas in the case of a Basic " +
      "authentication scheme, or asks for the second authentication factor.")
  @ApiResponse(responseCode = "200", description = "The profile of the user once authenticated.",
      content = @Content(schema = @Schema(implementation = UserProfileEntity.class)))
  @ApiResponse(responseCode = "401", description = "Authentication failed or a second factor is required.")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  public Response authenticate() {
    if (isTwoFactorRequired()) {
      return Response.status(Response.Status.UNAUTHORIZED)
          .entity(AuthenticationChallengeEntity.twoFactorRequired())
          .build();
    }

    validateUserAuthentication(privilegeValidation);
    User user = getUser();
    return Response.ok(UserProfileEntity.fromUser(user)
        .withAsUri(ProfileResourceBaseURIs.uriOfUser(user.getId())))
        .build();
  }

  /**
   * Completes a REST authentication after the password has been validated and Silverpeas has
   * requested a TOTP/recovery code.
   *
   * <p>The pending authentication is kept server-side in the HTTP session. The caller therefore
   * cannot authenticate with a TOTP code alone.</p>
   *
   * @param code the TOTP or recovery code.
   * @return the authenticated user's profile, or a 401 response when the code is invalid.
   */
  @Operation(summary = "Completes a pending REST authentication with a TOTP or recovery code.")
  @ApiResponse(responseCode = "200", description = "The profile of the authenticated user.",
      content = @Content(schema = @Schema(implementation = UserProfileEntity.class)))
  @ApiResponse(responseCode = "401", description = "No pending authentication or invalid second factor.")
  @POST
  @Path("two-factor")
  @Produces(MediaType.APPLICATION_JSON)
  public Response authenticateTwoFactor(@QueryParam("code") final String code) {
    HttpSession session = getHttpServletRequest().getSession(false);
    if (session == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    String login = (String) session.getAttribute(HTTPAuthentication.TWO_FACTOR_LOGIN);
    String domainId = (String) session.getAttribute(HTTPAuthentication.TWO_FACTOR_DOMAIN);
    Long expiresAt = (Long) session.getAttribute(HTTPAuthentication.TWO_FACTOR_EXPIRES_AT);
    if (!HTTPAuthentication.isValidTwoFactorChallenge(login, domainId, expiresAt)) {
      HTTPAuthentication.clearTwoFactorChallenge(session);
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    AuthenticationService authenticationService = AuthenticationServiceProvider.getService();
    AuthenticationResponse result = authenticationService.authenticateTwoFactor(login, domainId, code);
    if (!result.getStatus().succeeded()) {
      int attempts = getTwoFactorAttempts(session) + 1;
      session.setAttribute(HTTPAuthentication.TWO_FACTOR_ATTEMPTS, attempts);
      int maxAttempts = HTTPAuthentication.getTwoFactorMaxAttempts();
      if (attempts >= maxAttempts || authenticationService.isTwoFactorLocked(login, domainId)) {
        HTTPAuthentication.clearTwoFactorChallenge(session);
      }
      return Response.status(Response.Status.UNAUTHORIZED)
          .entity(AuthenticationChallengeEntity.twoFactorRequired())
          .build();
    }

    HTTPAuthentication.clearTwoFactorChallenge(session);
    openAuthenticatedSession(result.getToken());
    User user = authenticationService.getUserByAuthToken(result.getToken());
    return Response.ok(UserProfileEntity.fromUser(user)
        .withAsUri(ProfileResourceBaseURIs.uriOfUser(user.getId())))
        .build();
  }

  private boolean isTwoFactorRequired() {
    return Boolean.TRUE.equals(
        getHttpServletRequest().getAttribute(HTTPAuthentication.TWO_FACTOR_REQUIRED));
  }

  private int getTwoFactorAttempts(final HttpSession session) {
    Object attempts = session.getAttribute(HTTPAuthentication.TWO_FACTOR_ATTEMPTS);
    return attempts instanceof Integer ? (Integer) attempts : 0;
  }

  private void openAuthenticatedSession(final String authToken) {
    AuthenticationService authenticationService = AuthenticationServiceProvider.getService();
    User user = authenticationService.getUserByAuthToken(authToken);
    SessionInfo session = SessionManagementProvider.getSessionManagement()
        .openSession(user, getHttpServletRequest());
    getHttpServletResponse().setHeader(UserPrivilegeValidation.HTTP_SESSIONKEY, session.getSessionId());
    getHttpServletResponse().addHeader("Access-Control-Expose-Headers",
        UserPrivilegeValidation.HTTP_SESSIONKEY);
    SynchronizerTokenService tokenService = SynchronizerTokenService.getInstance();
    tokenService.setUpSessionTokens(session);
    Token token = tokenService.getSessionToken(session);
    getHttpServletResponse().addHeader(SynchronizerTokenService.SESSION_TOKEN_KEY, token.getValue());
  }

  @Override
  protected String getResourceBasePath() {
    return PATH;
  }

  @Override
  public String getComponentId() {
    return null;
  }

  @jakarta.xml.bind.annotation.XmlRootElement
  public static class AuthenticationChallengeEntity {
    private String status;

    public static AuthenticationChallengeEntity twoFactorRequired() {
      AuthenticationChallengeEntity entity = new AuthenticationChallengeEntity();
      entity.status = Status.TWO_FACTOR_REQUIRED.name();
      return entity;
    }

    public String getStatus() {
      return status;
    }
  }
}
