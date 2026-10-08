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
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.security.authentication.twofactor.TwoFactorAuthenticationService;
import org.silverpeas.core.security.authentication.twofactor.model.TwoFactorAuthentication;
import org.silverpeas.core.security.totp.TotpService;
import org.silverpeas.core.webapi.twofactor.QrCodeGenerator;
import java.util.List;
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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;
import org.silverpeas.kernel.util.StringUtil;

import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * A REST-based Web service to authenticate a user in Silverpeas.
 */
@WebService
@Path(AuthenticationResource.PATH)
public class AuthenticationResource extends RESTWebService {

  static final String PATH = "authentication";
  static final String TRUSTED_DEVICE_COOKIE = "Silverpeas_TrustedDevice";
  private static final SettingBundle AUTHENTICATION_SETTINGS = ResourceLocator.getSettingBundle(
      "org.silverpeas.authentication.settings.authenticationSettings");
  @Inject
  private UserPrivilegeValidation privilegeValidation;
  @Inject
  private TwoFactorAuthenticationService twoFactorService;
  @Inject
  private TotpService totpService;
  @Inject
  private QrCodeGenerator qrCodeGenerator;

  @Operation(summary = "Authenticates the user from his credentials passed through the " +
      "Authorization HTTP header, opens a new HTTP session in Silverpeas in the case of a Basic " +
      "authentication scheme, or asks for the second authentication factor.")
  @ApiResponse(responseCode = "200", description = "The profile of the user once authenticated.",
      content = @Content(schema = @Schema(implementation = UserProfileEntity.class)))
  @ApiResponse(responseCode = "401", description = "Authentication failed or a second factor is required.")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  public Response authenticate() {
    try {
      validateUserAuthentication(privilegeValidation);
    } catch (RuntimeException e) {
      throw e;
    }

    boolean twoFactorRequired = Boolean.TRUE.equals(
        getHttpServletRequest().getAttribute(HTTPAuthentication.TWO_FACTOR_REQUIRED));
    if (twoFactorRequired) {
      User pendingUser = getPendingTwoFactorUser();
      if (pendingUser != null && !twoFactorService.getAuthentication(
          Integer.parseInt(pendingUser.getId())).filter(TwoFactorAuthentication::isEnabled)
          .isPresent()) {
        return Response.status(Response.Status.UNAUTHORIZED)
            .header("X-Silverpeas-2FA-Enrollment-Required", "true")
            .header("Access-Control-Expose-Headers", "X-Silverpeas-2FA-Enrollment-Required")
            .entity(AuthenticationChallengeEntity.twoFactorRequired()).build();
      }
      return Response.status(Response.Status.UNAUTHORIZED)
          .header("X-Silverpeas-2FA-Required", "true")
          .header("Access-Control-Expose-Headers", "X-Silverpeas-2FA-Required")
          .entity(AuthenticationChallengeEntity.twoFactorRequired())
          .build();
    }

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
  public Response authenticateTwoFactor(@QueryParam("code") final String code,
      @QueryParam("trustDevice") final boolean trustDevice) throws AuthenticationException {
    HttpSession session = getHttpServletRequest().getSession(false);
    if (session == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    String login = (String) session.getAttribute(HTTPAuthentication.TWO_FACTOR_LOGIN);
    String domainId = (String) session.getAttribute(HTTPAuthentication.TWO_FACTOR_DOMAIN);
    Long expiresAt = (Long) session.getAttribute(HTTPAuthentication.TWO_FACTOR_EXPIRES_AT);
    if (!HTTPAuthentication.isValidTwoFactorChallenge(login, domainId, expiresAt)) {
      clearRestTwoFactorChallenge(session);
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    AuthenticationService authenticationService = AuthenticationServiceProvider.getService();
    AuthenticationResponse result = authenticationService.authenticateTwoFactor(login, domainId, code);
    if (!result.getStatus().succeeded()) {
      int attempts = getTwoFactorAttempts(session) + 1;
      session.setAttribute(HTTPAuthentication.TWO_FACTOR_ATTEMPTS, attempts);
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
    final User user;
    try {
      user = authenticationService.getUserByAuthToken(result.getToken());
    } catch (AuthenticationException e) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    openAuthenticatedSession(user);
    Response.ResponseBuilder response = Response.ok(UserProfileEntity.fromUser(user)
        .withAsUri(ProfileResourceBaseURIs.uriOfUser(user.getId())));
    if (trustDevice) {
      String trustedDeviceToken = authenticationService.createTrustedDevice(
          user.getLogin(), user.getDomainId(), getHttpServletRequest().getHeader("User-Agent"));
      writeTrustedDeviceCookie(trustedDeviceToken);
    }
    return response.build();
  }

  @Operation(summary = "Completes a pending REST authentication with a trusted device.")
  @ApiResponse(responseCode = "200", description = "The profile of the authenticated user.",
      content = @Content(schema = @Schema(implementation = UserProfileEntity.class)))
  @ApiResponse(responseCode = "401", description = "No pending authentication or invalid trusted device.")
  @POST
  @Path("trusted-device")
  @Produces(MediaType.APPLICATION_JSON)
  public Response authenticateTrustedDevice() {
    HttpSession session = getHttpServletRequest().getSession(false);
    if (session == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    String login = (String) session.getAttribute(HTTPAuthentication.TWO_FACTOR_LOGIN);
    String domainId = (String) session.getAttribute(HTTPAuthentication.TWO_FACTOR_DOMAIN);
    Long expiresAt = (Long) session.getAttribute(HTTPAuthentication.TWO_FACTOR_EXPIRES_AT);
    if (!HTTPAuthentication.isValidTwoFactorChallenge(login, domainId, expiresAt)) {
      clearRestTwoFactorChallenge(session);
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    String trustedDeviceToken = getTrustedDeviceToken();
    AuthenticationService authenticationService = AuthenticationServiceProvider.getService();
    AuthenticationResponse result = authenticationService.authenticateTrustedDevice(
        login, domainId, trustedDeviceToken, getHttpServletRequest().getHeader("User-Agent"));
    if (!result.getStatus().succeeded()) {
      return Response.status(Response.Status.UNAUTHORIZED)
          .entity(AuthenticationChallengeEntity.twoFactorRequired())
          .build();
    }

    clearRestTwoFactorChallenge(session);
    HTTPAuthentication.clearTwoFactorChallenge(session);
    final User user;
    try {
      user = authenticationService.getUserByAuthToken(result.getToken());
    } catch (AuthenticationException e) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    openAuthenticatedSession(user);
    writeTrustedDeviceCookie(result.getTrustedDeviceToken());
    return Response.ok(UserProfileEntity.fromUser(user)
        .withAsUri(ProfileResourceBaseURIs.uriOfUser(user.getId())))
        .build();
  }


  /**
   * Returns the user whose password has already been validated for this HTTP session.
   * Never accept a user identifier from the client during enrollment.
   */
  private User getPendingTwoFactorUser() {
    HttpSession session = getHttpServletRequest().getSession(false);
    if (session == null) {
      return null;
    }
    String login = (String) session.getAttribute(HTTPAuthentication.TWO_FACTOR_LOGIN);
    String domain = (String) session.getAttribute(HTTPAuthentication.TWO_FACTOR_DOMAIN);
    Long expires = (Long) session.getAttribute(HTTPAuthentication.TWO_FACTOR_EXPIRES_AT);
    if (!HTTPAuthentication.isValidTwoFactorChallenge(login, domain, expires)) {
      return null;
    }
    return UserProvider.get().getUserByLoginAndDomainId(login, domain);
  }

  @POST
  @Path("enrollment")
  @Produces(MediaType.APPLICATION_JSON)
  public Response startEnrollment() {
    User user = getPendingTwoFactorUser();
    if (user == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    int userId = Integer.parseInt(user.getId());
    TwoFactorAuthentication pending = twoFactorService.getAuthentication(userId).orElse(null);
    if (pending != null && pending.isEnabled()) {
      return Response.status(Response.Status.CONFLICT).build();
    }
    // Reuse an existing pending secret when the mobile screen is reopened.
    if (pending == null) {
      pending = twoFactorService.startEnrollment(userId);
    }
    return Response.ok(new EnrollmentEntity(pending.getSecret(),
        java.util.Base64.getEncoder().encodeToString(qrCodeGenerator.generate(
            totpService.buildOtpAuthUri(pending.getSecret(), user.getLogin()), 256))))
        .header("Cache-Control", "no-store").build();
  }

  @POST
  @Path("enrollment/confirm")
  @Produces(MediaType.APPLICATION_JSON)
  public Response confirmEnrollment(@QueryParam("code") String code,
      @QueryParam("trustDevice") boolean trustDevice) throws AuthenticationException {
    User user = getPendingTwoFactorUser();
    if (user == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    HttpSession session = getHttpServletRequest().getSession(false);
    int attempts = getTwoFactorAttempts(session);
    if (attempts >= HTTPAuthentication.getTwoFactorMaxAttempts()) {
      clearRestTwoFactorChallenge(session);
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    int userId = Integer.parseInt(user.getId());
    if (!twoFactorService.confirmEnrollment(userId, code)) {
      session.setAttribute(HTTPAuthentication.TWO_FACTOR_ATTEMPTS, attempts + 1);
      if (attempts + 1 >= HTTPAuthentication.getTwoFactorMaxAttempts()) {
        clearRestTwoFactorChallenge(session);
      }
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    List<String> recoveryCodes = twoFactorService.generateRecoveryCodes(userId);
    clearRestTwoFactorChallenge(session);
    openAuthenticatedSession(user);
    if (trustDevice) {
      String token = AuthenticationServiceProvider.getService().createTrustedDevice(
          user.getLogin(), user.getDomainId(), getHttpServletRequest().getHeader("User-Agent"));
      writeTrustedDeviceCookie(token);
    }
    return Response.ok(new EnrollmentConfirmationEntity(
        UserProfileEntity.fromUser(user).withAsUri(ProfileResourceBaseURIs.uriOfUser(userId + "")),
        recoveryCodes)).header("Cache-Control", "no-store").build();
  }

  public static class EnrollmentEntity {
    private final String secret;
    private final String qrCode;

    public EnrollmentEntity(String secret, String qrCode) {
      this.secret = secret;
      this.qrCode = qrCode;
    }

    public String getSecret() { return secret; }
    public String getQrCode() { return qrCode; }
  }

  public static class EnrollmentConfirmationEntity {
    private final UserProfileEntity profile;
    private final List<String> recoveryCodes;

    public EnrollmentConfirmationEntity(UserProfileEntity profile, List<String> recoveryCodes) {
      this.profile = profile;
      this.recoveryCodes = recoveryCodes;
    }

    public UserProfileEntity getProfile() { return profile; }
    public List<String> getRecoveryCodes() { return recoveryCodes; }
  }

  private String getTrustedDeviceToken() {
    if (getHttpServletRequest().getCookies() == null) {
      return null;
    }
    for (Cookie cookie : getHttpServletRequest().getCookies()) {
      if (TRUSTED_DEVICE_COOKIE.equals(cookie.getName())) {
        return URLDecoder.decode(cookie.getValue(), Charsets.UTF_8);
      }
    }
    return null;
  }

  private void writeTrustedDeviceCookie(final String token) {
    if (!StringUtil.isDefined(token)) {
      return;
    }
    final String cookieValue = URLEncoder.encode(token, Charsets.UTF_8);
    getHttpServletResponse().addHeader("Set-Cookie", TRUSTED_DEVICE_COOKIE + "=" + cookieValue
        + "; Max-Age=" + AUTHENTICATION_SETTINGS.getInteger(
            "twoFactorTrustedDeviceLifetime", 2592000)
        + "; Path=/; HttpOnly; SameSite=Lax"
        + (getHttpServletRequest().isSecure() ? "; Secure" : ""));
  }

  private int getTwoFactorAttempts(final HttpSession session) {
    Object attempts = session.getAttribute(HTTPAuthentication.TWO_FACTOR_ATTEMPTS);
    return attempts instanceof Integer ? (Integer) attempts : 0;
  }

  private void clearRestTwoFactorChallenge(final HttpSession session) {
    HTTPAuthentication.clearTwoFactorChallenge(session);
  }

  private void openAuthenticatedSession(final User user) {
    SessionInfo session = SessionManagementProvider.getSessionManagement()
        .openSession(user, getHttpServletRequest());
    getHttpServletResponse().setHeader(UserPrivilegeValidation.HTTP_SESSIONKEY, session.getSessionId());
    getHttpServletResponse().addHeader("Access-Control-Expose-Headers",
        UserPrivilegeValidation.HTTP_SESSIONKEY + ", " +
            SynchronizerTokenService.SESSION_TOKEN_KEY);
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
