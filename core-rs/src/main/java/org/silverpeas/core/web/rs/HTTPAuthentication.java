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
 * FLOSS exception. You should have received a copy of the text describing
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
package org.silverpeas.core.web.rs;

import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.security.authentication.Authentication;
import org.silverpeas.core.security.authentication.AuthenticationCredential;
import org.silverpeas.core.security.authentication.AuthenticationResponse;
import org.silverpeas.core.security.authentication.exception.AuthenticationException;
import org.silverpeas.core.security.authentication.verifier.AuthenticationUserVerifierFactory;
import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.security.session.SessionManagementProvider;
import org.silverpeas.core.security.token.Token;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.kernel.util.Mutable;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.core.web.token.SynchronizerTokenService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.silverpeas.core.web.rs.UserPrivilegeValidation.*;

/**
 * An HTTP authentication mechanism for Silverpeas to allow users to consume the Silverpeas Web API.
 */
@Service
public class HTTPAuthentication {

  private static final Pattern AUTHORIZATION_PATTERN = Pattern.compile("(?i)^(Basic|Bearer) (.*)");
  private static final Pattern AUTHENTICATION_PATTERN =
      Pattern.compile("(?i)^\\s*(\\S+)\\s*@domain([0-9]+):(.+)$");

  public static final String TWO_FACTOR_REQUIRED =
      "org.silverpeas.core.web.rs.HTTPAuthentication.twoFactorRequired";
  public static final String TWO_FACTOR_LOGIN =
      "org.silverpeas.core.web.rs.HTTPAuthentication.twoFactorLogin";
  public static final String TWO_FACTOR_DOMAIN =
      "org.silverpeas.core.web.rs.HTTPAuthentication.twoFactorDomain";
  public static final String TWO_FACTOR_EXPIRES_AT =
      "org.silverpeas.core.web.rs.HTTPAuthentication.twoFactorExpiresAt";
  public static final String TWO_FACTOR_ATTEMPTS =
      "org.silverpeas.core.web.rs.HTTPAuthentication.twoFactorAttempts";

  private static final int DEFAULT_TWO_FACTOR_CHALLENGE_LIFETIME = 120;
  private static final int DEFAULT_TWO_FACTOR_MAX_ATTEMPTS = 5;

  private static final Map<AuthenticationScheme, Function<AuthenticationContext, SessionInfo>>
      schemeHandlers = new EnumMap<>(AuthenticationScheme.class);

  static {
    schemeHandlers.put(AuthenticationScheme.BASIC, HTTPAuthentication::performBasicAuthentication);
    schemeHandlers.put(AuthenticationScheme.BEARER,
        HTTPAuthentication::performTokenBasedAuthentication);
  }

  protected HTTPAuthentication() {
  }

  public SessionInfo authenticate(final AuthenticationContext context) {
    try {
      final Mutable<SessionInfo> session = Mutable.empty();
      String authorizationValue = context.getHttpServletRequest().getHeader(HTTP_AUTHORIZATION);
      if (StringUtil.isDefined(authorizationValue)) {
        Matcher authorizationMatcher = AUTHORIZATION_PATTERN.matcher(authorizationValue);
        final int authorizationValuePartCount = 2;
        final int schemePart = 1;
        final int credentialsPart = 2;
        if (!authorizationMatcher.matches() ||
            authorizationMatcher.groupCount() != authorizationValuePartCount) {
          throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        Optional<AuthenticationScheme> credentialType =
            AuthenticationScheme.from(authorizationMatcher.group(schemePart));
        String userCredentials = authorizationMatcher.group(credentialsPart);

        credentialType.ifPresent(scheme -> {
          context.setAuthenticationScheme(scheme);
          context.setUserCredentials(userCredentials);
          session.set(schemeHandlers.get(scheme).apply(context));
        });
      } else {
        authorizationValue = context.getHttpServletRequest().getParameter(HTTP_ACCESS_TOKEN);
        if (StringUtil.isDefined(authorizationValue)) {
          context.setAuthenticationScheme(AuthenticationScheme.BEARER);
          context.setUserCredentials(authorizationValue);
          session.set(schemeHandlers.get(AuthenticationScheme.BEARER).apply(context));
        }
      }
      return session.orElseThrow(() -> new WebApplicationException(Response.Status.UNAUTHORIZED));
    } catch (final AuthenticationInternalException ex) {
      throw new WebApplicationException(ex, Response.Status.SERVICE_UNAVAILABLE);
    }
  }

  private static SessionInfo performBasicAuthentication(final AuthenticationContext context) {
    final String decodedCredentials =
        new String(StringUtil.fromBase64(context.getUserCredentials()), Charsets.UTF_8).trim();

    Matcher matcher = AUTHENTICATION_PATTERN.matcher(decodedCredentials);
    final int credentialPartCount = 3;
    final int loginPart = 1;
    final int passwordPart = 3;
    final int domainIdPart = 2;
    if (matcher.matches() && matcher.groupCount() == credentialPartCount) {
      try {
        final String login = matcher.group(loginPart);
        final String password = matcher.group(passwordPart);
        final String domainId = matcher.group(domainIdPart);
        AuthenticationCredential credential =
            AuthenticationCredential.newWithAsLogin(login)
                .withAsPassword(password)
                .withAsDomainId(domainId);
        Authentication authenticator = Authentication.get();
        AuthenticationResponse result = authenticator.authenticate(credential);
        if (result.getStatus() == AuthenticationResponse.Status.TWO_FACTOR_REQUIRED) {
          startTwoFactorChallenge(context.getHttpServletRequest(), login, domainId);
          return SessionInfo.NoneSession;
        }
        if (result.getStatus().succeeded()) {
          User user = authenticator.getUserByAuthToken(result.getToken());
          final SessionInfo session;
          if (!user.isAnonymous()) {
            session = SessionManagementProvider.getSessionManagement()
                .openSession(user, context.getHttpServletRequest());
            context.getHttpServletResponse().setHeader(HTTP_SESSIONKEY, session.getSessionId());
            context.getHttpServletResponse()
                .addHeader("Access-Control-Expose-Headers", UserPrivilegeValidation.HTTP_SESSIONKEY);
            SynchronizerTokenService tokenService = SynchronizerTokenService.getInstance();
            tokenService.setUpSessionTokens(session);
            Token token = tokenService.getSessionToken(session);
            context.getHttpServletResponse()
                .addHeader(SynchronizerTokenService.SESSION_TOKEN_KEY, token.getValue());
          } else {
            session = SessionManagementProvider.getSessionManagement()
                .openAnonymousSession(context.getHttpServletRequest());
          }
          return session;
        }
      } catch (AuthenticationException e) {
        throw new AuthenticationInternalException(e.getMessage(), e);
      }
    }
    return null;
  }

  private static void startTwoFactorChallenge(final HttpServletRequest request,
      final String login, final String domainId) {
    HttpSession session = request.getSession(true);
    String pendingLogin = (String) session.getAttribute(TWO_FACTOR_LOGIN);
    String pendingDomain = (String) session.getAttribute(TWO_FACTOR_DOMAIN);
    Long expiresAt = (Long) session.getAttribute(TWO_FACTOR_EXPIRES_AT);

    if (!isValidTwoFactorChallenge(pendingLogin, pendingDomain, expiresAt) ||
        !login.equals(pendingLogin) || !domainId.equals(pendingDomain)) {
      session.setAttribute(TWO_FACTOR_LOGIN, login);
      session.setAttribute(TWO_FACTOR_DOMAIN, domainId);
      int lifetime = getTwoFactorChallengeLifetime();
      session.setAttribute(TWO_FACTOR_EXPIRES_AT,
          System.currentTimeMillis() + lifetime * 1000L);
      session.setAttribute(TWO_FACTOR_ATTEMPTS, 0);
    }
    request.setAttribute(TWO_FACTOR_REQUIRED, Boolean.TRUE);
  }

  public static boolean isValidTwoFactorChallenge(final String login,
      final String domainId, final Long expiresAt) {
    return StringUtil.isDefined(login) && StringUtil.isDefined(domainId) &&
        expiresAt != null && expiresAt >= System.currentTimeMillis();
  }

  public static void clearTwoFactorChallenge(final HttpSession session) {
    session.removeAttribute(TWO_FACTOR_LOGIN);
    session.removeAttribute(TWO_FACTOR_DOMAIN);
    session.removeAttribute(TWO_FACTOR_EXPIRES_AT);
    session.removeAttribute(TWO_FACTOR_ATTEMPTS);
    session.removeAttribute(TWO_FACTOR_REQUIRED);
  }

  public static int getTwoFactorMaxAttempts() {
    return org.silverpeas.kernel.bundle.ResourceLocator
        .getSettingBundle("org.silverpeas.authentication.settings.authenticationSettings")
        .getInteger("twoFactorTotpMaxAttempts", DEFAULT_TWO_FACTOR_MAX_ATTEMPTS);
  }

  private static int getTwoFactorChallengeLifetime() {
    return org.silverpeas.kernel.bundle.ResourceLocator
        .getSettingBundle("org.silverpeas.authentication.settings.authenticationSettings")
        .getInteger("twoFactorTotpChallengeLifetime", DEFAULT_TWO_FACTOR_CHALLENGE_LIFETIME);
  }

  private static SessionInfo performTokenBasedAuthentication(final AuthenticationContext context) {
    final String token = context.getUserCredentials();
    final User user = UserProvider.get().getUserByToken(token);
    if (user != null) {
      verifyUserCanLogin(user);
      return SessionManagementProvider.getSessionManagement()
          .openOneShotSession(user, context.getHttpServletRequest());
    }
    return null;
  }

  private static void verifyUserCanLogin(final User user) {
    if (user != null) {
      try {
        AuthenticationUserVerifierFactory.getUserCanLoginVerifier(user).verify();
      } catch (AuthenticationException e) {
        SilverLogger.getLogger(HTTPAuthentication.class).error(e);
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }
    }
  }

  public static class AuthenticationContext {
    private String credentials;
    private AuthenticationScheme scheme;
    private final HttpServletResponse response;
    private final HttpServletRequest request;

    public AuthenticationContext(final HttpServletRequest request,
        final HttpServletResponse response) {
      this.request = request;
      this.response = response;
    }

    public String getUserCredentials() {
      return credentials;
    }

    public void setUserCredentials(final String credentials) {
      this.credentials = credentials;
    }

    @SuppressWarnings("unused")
    public AuthenticationScheme getAuthenticationScheme() {
      return this.scheme;
    }

    public void setAuthenticationScheme(final AuthenticationScheme scheme) {
      this.scheme = scheme;
    }

    public HttpServletResponse getHttpServletResponse() {
      return this.response;
    }

    public HttpServletRequest getHttpServletRequest() {
      return this.request;
    }
  }

  private static class AuthenticationInternalException extends SilverpeasRuntimeException {
    public AuthenticationInternalException(final String message, final Throwable cause) {
      super(message, cause);
    }
  }
}
