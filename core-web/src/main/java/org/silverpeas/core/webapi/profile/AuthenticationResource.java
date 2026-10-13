/*
 * Copyright (C) 2000 - 2026 Silverpeas
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

import org.silverpeas.core.security.authentication.exception.AuthenticationException;
import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.security.session.SessionManagement;
import org.silverpeas.core.security.token.Token;
import org.silverpeas.core.web.rs.RestTwoFactorChallenge;
import jakarta.ws.rs.WebApplicationException;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.Base64;
import org.silverpeas.core.web.rs.RESTWebService;
import org.silverpeas.core.web.rs.UserPrivilegeValidation;
import org.silverpeas.core.web.token.SynchronizerTokenService;

import jakarta.inject.Inject;
import jakarta.servlet.http.Cookie;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
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
  private AuthenticationService authenticationService;
  @Inject
  private UserProvider userProvider;
  @Inject
  private SessionManagement sessionManagement;
  @Inject
  private SynchronizerTokenService synchronizerTokenService;
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
    } catch (WebApplicationException e) {
      if (e.getResponse().getStatus() != Response.Status.UNAUTHORIZED.getStatusCode() ||
          !RestTwoFactorChallenge.isRequired(getHttpServletRequest())) {
        throw e;
      }
    }

    boolean twoFactorRequired = RestTwoFactorChallenge.isRequired(getHttpServletRequest());
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
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response authenticateTwoFactor(final TwoFactorCodeRequest request)
      throws AuthenticationException {
    if (request == null || request.getCode() == null || request.getCode().isBlank()) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
    final String code = request.getCode();
    final boolean trustDevice = request.isTrustDevice();
    RestTwoFactorChallenge challenge =
        RestTwoFactorChallenge.from(getHttpServletRequest()).orElse(null);
    if (challenge == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    String login = challenge.getLogin();
    String domainId = challenge.getDomainId();

    AuthenticationResponse result = authenticationService.authenticateTwoFactor(login, domainId, code);
    if (!result.getStatus().succeeded()) {
      challenge.recordFailure();
      if (authenticationService.isTwoFactorLocked(login, domainId)) {
        challenge.clear();
      }
      return Response.status(Response.Status.UNAUTHORIZED)
          .entity(AuthenticationChallengeEntity.twoFactorRequired())
          .build();
    }

    challenge.clear();
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
    RestTwoFactorChallenge challenge =
        RestTwoFactorChallenge.from(getHttpServletRequest()).orElse(null);
    if (challenge == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    String login = challenge.getLogin();
    String domainId = challenge.getDomainId();

    String trustedDeviceToken = getTrustedDeviceToken();
    AuthenticationResponse result = authenticationService.authenticateTrustedDevice(
        login, domainId, trustedDeviceToken, getHttpServletRequest().getHeader("User-Agent"));
    if (!result.getStatus().succeeded()) {
      return Response.status(Response.Status.UNAUTHORIZED)
          .entity(AuthenticationChallengeEntity.twoFactorRequired())
          .build();
    }

    challenge.clear();
    final User user;
    try {
      user = authenticationService.getUserByAuthToken(result.getToken());
    } catch (AuthenticationException e) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    openAuthenticatedSession(user);
    try {
      writeTrustedDeviceCookie(result.getTrustedDeviceToken(),
          authenticationService.getTrustedDeviceRemainingLifetime(
              login, domainId, result.getTrustedDeviceToken()));
    } catch (AuthenticationException e) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    return Response.ok(UserProfileEntity.fromUser(user)
        .withAsUri(ProfileResourceBaseURIs.uriOfUser(user.getId())))
        .build();
  }


  /**
   * Returns the user whose password has already been validated for this HTTP session.
   * Never accept a user identifier from the client during enrollment.
   */
  private User getPendingTwoFactorUser() {
    return RestTwoFactorChallenge.from(getHttpServletRequest())
        .map(challenge -> userProvider.getUserByLoginAndDomainId(
            challenge.getLogin(), challenge.getDomainId())).orElse(null);
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
        Base64.getEncoder().encodeToString(qrCodeGenerator.generate(
            totpService.buildOtpAuthUri(pending.getSecret(), user.getLogin()), 256))))
        .header("Cache-Control", "no-store").build();
  }

  @POST
  @Path("enrollment/confirm")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response confirmEnrollment(final TwoFactorCodeRequest request)
      throws AuthenticationException {
    if (request == null || request.getCode() == null || request.getCode().isBlank()) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
    final String code = request.getCode();
    final boolean trustDevice = request.isTrustDevice();
    User user = getPendingTwoFactorUser();
    if (user == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    RestTwoFactorChallenge challenge =
        RestTwoFactorChallenge.from(getHttpServletRequest()).orElse(null);
    if (challenge == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    int userId = Integer.parseInt(user.getId());
    if (!twoFactorService.confirmEnrollment(userId, code)) {
      challenge.recordFailure();
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    List<String> recoveryCodes = twoFactorService.generateRecoveryCodes(userId);
    challenge.clear();
    openAuthenticatedSession(user);
    if (trustDevice) {
      String token = authenticationService.createTrustedDevice(
          user.getLogin(), user.getDomainId(), getHttpServletRequest().getHeader("User-Agent"));
      writeTrustedDeviceCookie(token);
    }
    return Response.ok(new EnrollmentConfirmationEntity(
        UserProfileEntity.fromUser(user).withAsUri(ProfileResourceBaseURIs.uriOfUser(userId + "")),
        recoveryCodes)).header("Cache-Control", "no-store").build();
  }

  public static class TwoFactorCodeRequest {
    private String code;
    private boolean trustDevice;

    public TwoFactorCodeRequest() {
    }

    public String getCode() {
      return code;
    }

    public void setCode(final String code) {
      this.code = code;
    }

    public boolean isTrustDevice() {
      return trustDevice;
    }

    public void setTrustDevice(final boolean trustDevice) {
      this.trustDevice = trustDevice;
    }
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
    writeTrustedDeviceCookie(token,
        AUTHENTICATION_SETTINGS.getInteger("twoFactorTrustedDeviceLifetime", 2592000));
  }

  private void writeTrustedDeviceCookie(final String token, final long maxAge) {
    if (!StringUtil.isDefined(token)) {
      return;
    }
    final String cookieValue = URLEncoder.encode(token, Charsets.UTF_8);
    getHttpServletResponse().addHeader("Set-Cookie", TRUSTED_DEVICE_COOKIE + "=" + cookieValue
        + "; Max-Age=" + Math.max(0, maxAge)
        + "; Path=/; HttpOnly; SameSite=Lax"
        + (getHttpServletRequest().isSecure() ? "; Secure" : ""));
  }

  private void openAuthenticatedSession(final User user) {
    SessionInfo session = sessionManagement
        .openSession(user, getHttpServletRequest());
    getHttpServletResponse().setHeader(UserPrivilegeValidation.HTTP_SESSIONKEY, session.getSessionId());
    getHttpServletResponse().addHeader("Access-Control-Expose-Headers",
        UserPrivilegeValidation.HTTP_SESSIONKEY + ", " +
            SynchronizerTokenService.SESSION_TOKEN_KEY);
    SynchronizerTokenService tokenService = synchronizerTokenService;
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

  @XmlRootElement
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
