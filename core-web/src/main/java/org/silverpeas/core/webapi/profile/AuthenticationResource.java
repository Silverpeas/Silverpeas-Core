/*
 * Copyright (C) 2000 - 2026 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 */
package org.silverpeas.core.webapi.profile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.security.authentication.AuthenticationResponse;
import org.silverpeas.core.security.authentication.AuthenticationService;
import org.silverpeas.core.security.authentication.AuthenticationServiceProvider;
import org.silverpeas.core.security.authentication.exception.AuthenticationException;
import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.security.session.SessionManagementProvider;
import org.silverpeas.core.security.token.Token;
import org.silverpeas.core.web.rs.HTTPAuthentication;
import org.silverpeas.core.web.rs.RESTWebService;
import org.silverpeas.core.web.rs.UserPrivilegeValidation;
import org.silverpeas.core.web.token.SynchronizerTokenService;

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
  private static final String REST_TWO_FACTOR_LOGIN =
      "org.silverpeas.core.webapi.profile.AuthenticationResource.twoFactorLogin";
  private static final String REST_TWO_FACTOR_DOMAIN =
      "org.silverpeas.core.webapi.profile.AuthenticationResource.twoFactorDomain";
  private static final String REST_TWO_FACTOR_EXPIRES_AT =
      "org.silverpeas.core.webapi.profile.AuthenticationResource.twoFactorExpiresAt";
  private static final String REST_TWO_FACTOR_ATTEMPTS =
      "org.silverpeas.core.webapi.profile.AuthenticationResource.twoFactorAttempts";

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
    if (Boolean.TRUE.equals(getHttpServletRequest().getAttribute(HTTPAuthentication.TWO_FACTOR_REQUIRED))) {
      HttpSession session = getHttpServletRequest().getSession(true);
      if (session.getAttribute(REST_TWO_FACTOR_LOGIN) == null) {
        session.setAttribute(REST_TWO_FACTOR_LOGIN,
            session.getAttribute(HTTPAuthentication.TWO_FACTOR_LOGIN));
        session.setAttribute(REST_TWO_FACTOR_DOMAIN,
            session.getAttribute(HTTPAuthentication.TWO_FACTOR_DOMAIN));
        session.setAttribute(REST_TWO_FACTOR_EXPIRES_AT,
            session.getAttribute(HTTPAuthentication.TWO_FACTOR_EXPIRES_AT));
        session.setAttribute(REST_TWO_FACTOR_ATTEMPTS, 0);
      }
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

  @Operation(summary = "Completes a pending REST authentication with a TOTP or recovery code.")
  @ApiResponse(responseCode = "200", description = "The profile of the authenticated user.",
      content = @Content(schema = @Schema(implementation = UserProfileEntity.class)))
  @ApiResponse(responseCode = "401", description = "No pending authentication or invalid second factor.")
  @POST
  @Path("two-factor")
  @Produces(MediaType.APPLICATION_JSON)
  public Response authenticateTwoFactor(@QueryParam("code") final String code)
      throws AuthenticationException {
    HttpSession session = getHttpServletRequest().getSession(false);
    if (session == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    String login = (String) session.getAttribute(REST_TWO_FACTOR_LOGIN);
    String domainId = (String) session.getAttribute(REST_TWO_FACTOR_DOMAIN);
    Long expiresAt = (Long) session.getAttribute(REST_TWO_FACTOR_EXPIRES_AT);
    if (!HTTPAuthentication.isValidTwoFactorChallenge(login, domainId, expiresAt)) {
      clearRestTwoFactorChallenge(session);
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    AuthenticationService authenticationService = AuthenticationServiceProvider.getService();
    AuthenticationResponse result = authenticationService.authenticateTwoFactor(login, domainId, code);
    if (!result.getStatus().succeeded()) {
      int attempts = getTwoFactorAttempts(session) + 1;
      session.setAttribute(REST_TWO_FACTOR_ATTEMPTS, attempts);
      if (attempts >= HTTPAuthentication.getTwoFactorMaxAttempts() ||
          authenticationService.isTwoFactorLocked(login, domainId)) {
        clearRestTwoFactorChallenge(session);
      }
      return Response.status(Response.Status.UNAUTHORIZED)
          .entity(AuthenticationChallengeEntity.twoFactorRequired())
          .build();
    }

    clearRestTwoFactorChallenge(session);
    HTTPAuthentication.clearTwoFactorChallenge(session);
    User user = authenticationService.getUserByAuthToken(result.getToken());
    openAuthenticatedSession(user);
    return Response.ok(UserProfileEntity.fromUser(user)
        .withAsUri(ProfileResourceBaseURIs.uriOfUser(user.getId())))
        .build();
  }

  private int getTwoFactorAttempts(final HttpSession session) {
    Object attempts = session.getAttribute(REST_TWO_FACTOR_ATTEMPTS);
    return attempts instanceof Integer ? (Integer) attempts : 0;
  }

  private void clearRestTwoFactorChallenge(final HttpSession session) {
    session.removeAttribute(REST_TWO_FACTOR_LOGIN);
    session.removeAttribute(REST_TWO_FACTOR_DOMAIN);
    session.removeAttribute(REST_TWO_FACTOR_EXPIRES_AT);
    session.removeAttribute(REST_TWO_FACTOR_ATTEMPTS);
    HTTPAuthentication.clearTwoFactorChallenge(session);
  }

  private void openAuthenticatedSession(final User user) {
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
      entity.status = AuthenticationResponse.Status.TWO_FACTOR_REQUIRED.name();
      return entity;
    }

    public String getStatus() {
      return status;
    }
  }
}
