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
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public Licence
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package org.silverpeas.core.web.rs;

import org.silverpeas.core.SilverpeasRuntimeException;
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
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.token.SynchronizerTokenService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.silverpeas.core.web.rs.UserPrivilegeValidation.*;

/**
 * An HTTP authentication mechanism for Silverpeas to allow users to consume the Silverpeas Web API.
 * It implements the authentication process for any incoming HTTPs requests targeting a web resource
 * of the Silverpeas Web API. This HTTP request can be as well an explicit
 * authentication ask as a Silverpeas API consume. The HTTP request is expected either to contain
 * the HTTP header {@code Authorization} valued with the authentication scheme and the user
 * credentials as expected by the IETF RFC 2617 or to target a web resource URI with the query
 * parameter {@code access_token} (see IETF RFC 6750).
 * <p>
 * Actually, Silverpeas supports for its web resources two HTTP authentication schemes: the
 * {@code Basic} one (covered by the IETF RFC 2617) and the Bearer one (covered by the IETF RFC
 * 6750). The API token of the users must be passed with the {@code Bearer} scheme to access the
 * REST API of Silverpeas. Any other authentication schemes throws a {@link WebApplicationException}
 * exception with the status {@link Response.Status#UNAUTHORIZED}.
 * </p>
 * <p>
 * The two ways to authenticate with Silverpeas are for different purposes:
 * </p>
 * <ul>
 *   <li>The authentication by credentials (carried by the {@code Basic} authentication scheme)
 *   is for opening a session in Silverpeas in order to perform one or several Web API invocations.
 *   The user behind will be then counted as a connected user.</li>
 *   <li>The authentication by the API token (carried either by the query parameter
 *   {@code access_token} or by the {@code Bearer} authentication scheme) is for a one-shot API
 *   call and for doing it doesn't require a session to be opened. It is usually used by external
 *   tools interacting with Silverpeas in the behalf of the user.</li>
 * </ul>
 * <p>
 * The failure of the authentication throws a {@link WebApplicationException} exception with the
 * status {@link Response.Status#UNAUTHORIZED}.
 * </p>
 * @author mmoquillon
 */
@Service
public class HTTPAuthentication {

  private static final Pattern AUTHORIZATION_PATTERN = Pattern.compile("(?i)^(Basic|Bearer) (.*)");

  // the first ':' character is the separator according to the RFC 2617 in basic digest
  private static final Pattern AUTHENTICATION_PATTERN =
      Pattern.compile("(?i)^\\s*(\\S+)\\s*@domain([0-9]+):(.+)$");

  private static final Map<AuthenticationScheme, Function<AuthenticationContext, SessionInfo>>
      schemeHandlers = new EnumMap<>(AuthenticationScheme.class);

  static {
    schemeHandlers.put(AuthenticationScheme.BASIC, HTTPAuthentication::performBasicAuthentication);
    schemeHandlers.put(AuthenticationScheme.BEARER,
        HTTPAuthentication::performTokenBasedAuthentication);
  }

  protected HTTPAuthentication() {
  }

  /**
   * Authenticates the user that sent the incoming HTTP request according to the specified
   * authentication context.
   * <p>
   * The context is defined for the incoming HTTP request and for the HTTP response to send. The
   * HTTP request contains the elements required to authenticate the user at the source of the
   * request. The mandatory element is either the {@code Authorization} HTTP header that must be
   * valued with an authentication scheme and with the credentials of the user, or the
   * {@code access_token} URI query parameter, or the {@code access_token} form-encoded body
   * parameter.
   * </p>
   * <p>
   * A {@link WebApplicationException} is thrown with the status
   * {@link Response.Status#UNAUTHORIZED} in the following cases:
   * </p>
   * <ul>
   *   <li>No {@code Authentication} header and no {@code access_token} parameter;</li>
   *   <li>The authentication scheme isn't supported;</li>
   *   <li>The credentials passed in the {@code Authentication} header are invalid;</li>
   *   <li>The user API token passed in the {@code access_token} parameter is invalid;</li>
   *   <li>The user account in Silverpeas isn't in a valid state (blocked, deactivated, ...).</li>
   * </ul>
   * <p>
   * If the authentication process succeeds, then a session is created and returned. For a basic
   * authentication scheme, the session comes from a session opening in Silverpeas by the
   * {@link org.silverpeas.core.security.session.SessionManagement} subsystem and its unique
   * identifier is set in the {@link UserPrivilegeValidation#HTTP_SESSIONKEY} header of the
   * HTTP response; the session life will span over several HTTP requests and it will be closed
   * either explicitly or by the default session timeout. For a bearer authentication scheme and for
   * an authentication from the {@code access_token} parameter, the
   * session is just created for the specific incoming request and will expire at the end of it;
   * this is why the session identifier is not sent back to the user with the HTTP response.
   * </p>
   * <p>
   * At the end of the authentication, the context is alimented with the user credentials and with
   * the authentication scheme that were fetched from the HTTP request. They can then be retrieved
   * for further operation by the invoker of this method. In the case of an authentication from
   * the {@code access_token} parameter, the authentication scheme in the context is set as
   * a bearer authentication scheme.
   * </p>
   * @param context the context of the authentication with the HTTP request and with the HTTP
   * response.
   * @return the created session for the request if the authentication succeeds or throws a
   * {@link WebApplicationException} with as status {@link Response.Status#UNAUTHORIZED}.
   */
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

    // Getting expected parts of credentials
    Matcher matcher = AUTHENTICATION_PATTERN.matcher(decodedCredentials);
    final int credentialPartCount = 3;
    final int loginPart = 1;
    final int passwordPart = 3;
    final int domainIdPart = 2;
    if (matcher.matches() && matcher.groupCount() == credentialPartCount) {
      // All expected parts detected, so getting an authentication key
      try {
        AuthenticationCredential credential =
            AuthenticationCredential.newWithAsLogin(matcher.group(loginPart))
                .withAsPassword(matcher.group(passwordPart))
                .withAsDomainId(matcher.group(domainIdPart));
        Authentication authenticator = Authentication.get();
        AuthenticationResponse result = authenticator.authenticate(credential);
        if (result.getStatus().succeeded()) {
          User user = authenticator.getUserByAuthToken(result.getToken());
          final SessionInfo session;
          if (!user.isAnonymous()) {
            session = SessionManagementProvider.getSessionManagement()
                .openSession(user, context.getHttpServletRequest());
            context.getHttpServletResponse().setHeader(HTTP_SESSIONKEY, session.getSessionId());
            context.getHttpServletResponse()
                .addHeader("Access-Control-Expose-Headers",
                    UserPrivilegeValidation.HTTP_SESSIONKEY);
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
