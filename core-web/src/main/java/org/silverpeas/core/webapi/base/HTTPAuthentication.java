/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.webapi.base;

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.user.UserReference;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.security.authentication.AuthenticationCredential;
import org.silverpeas.core.security.authentication.AuthenticationService;
import org.silverpeas.core.security.authentication.AuthenticationServiceProvider;
import org.silverpeas.core.security.authentication.exception.AuthenticationException;
import org.silverpeas.core.security.authentication.verifier.AuthenticationUserVerifierFactory;
import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.security.token.Token;
import org.silverpeas.core.security.token.persistent.PersistentResourceToken;
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

import static org.silverpeas.core.security.session.SessionManagementProvider.getSessionManagement;
import static org.silverpeas.core.util.StringUtil.fromBase64;
import static org.silverpeas.core.webapi.base.UserPrivilegeValidation.*;

/**
 * An HTTP authentication mechanism for Silverpeas. It implements the authentication mechanism
 * in Silverpeas from an incoming HTTP request. This HTTP request can be as well an explicit
 * authentication call as a Silverpeas API consume. The HTTP request is expected either to contain
 * the HTTP header {@code Authorization} valued with the authentication scheme and the user
 * credentials as expected by the IETF RFC 2617 or to target an URI with the query parameter
 * {@code access_token} (see IETF RFC 6750).
 * <p>
 * Actually, Silverpeas supports two HTTP authentication schemes: the {@code Basic} one
 * (covered by the IETF RFC 2617) and the Bearer one (covered by the IETF RFC 6750). The API token
 * of the users must be passed with the {@code Bearer} scheme to access the REST API of
 * Silverpeas.
 * </p>
 * <p>
 * The authentication opens a new session when succeeded, otherwise a
 * {@link WebApplicationException} exception is thrown with the status
 * {@link Response.Status#UNAUTHORIZED}.
 * </p>
 * @author mmoquillon
 */
@Service
public class HTTPAuthentication {

  private static final Pattern AUTHORIZATION_PATTERN = Pattern.compile("(?i)^(Basic|Bearer) (.*)");

  // the first ':' character is the separator according to the RFC 2617 in basic digest
  private static final Pattern AUTHENTICATION_PATTERN =
      Pattern.compile("(?i)^[\\s]*([^\\s]+)[\\s]*@domain([0-9]+):(.+)$");

  private static Map<AuthenticationScheme, Function<AuthenticationContext, SessionInfo>>
      schemeHandlers = new EnumMap<>(AuthenticationScheme.class);

  static {
    schemeHandlers.put(AuthenticationScheme.BASIC, HTTPAuthentication::performBasicAuthentication);
    schemeHandlers.put(AuthenticationScheme.BEARER,
        HTTPAuthentication::performTokenBasedAuthentication);
  }

  protected HTTPAuthentication() {
  }

  /**
   * Authenticates the user behind the incoming HTTP request according to the specified
   * authentication context.
   * <p>
   * The context is defined for the incoming HTTP request and for the HTTP response to send. The
   * HTTP request contains the elements required to authenticate the user at the source of the
   * request. The mandatory element is either the {@code Authorization} HTTP header that must be
   * valued with an authentication scheme and with the credentials of the user or the
   * {@code access_token} URI query parameter or the {@code access_token} form-encoded body
   * parameter.
   * </p>
   * <p>
   * A {@link WebApplicationException} is thrown with the status
   * {@link Response.Status#UNAUTHORIZED} in the following case:
   * </p>
   * <ul>
   *   <li>No {@code Authentication} header and no {@code access_token} parameter</li>
   *   <li>The authentication scheme isn't supported</li>
   *   <li>the credentials passed in the {@code Authentication} header are invalid</li>
   *   <li>the user API token passed in the {@code access_token} parameter is invalid</li>
   *   <li>the user account in Silverpeas isn't valid (blocked, deactivated, ...)</li>
   * </ul>
   * <p>
   * If the authentication process succeeds, then a session is created and returned. For a basic
   * authentication scheme, the session comes from a session opening in Silverpeas by the
   * {@link org.silverpeas.core.security.session.SessionManagement} subsystem and its unique
   * identifier is set in the {@link UserPrivilegeValidation#HTTP_SESSIONKEY} header of the
   * HTTP response; the session life will span over several HTTP requests and it will be closed
   * either explicitly or by the default session timeout. For a bearer authentication scheme and for
   * an authentication from the {@code access_token} parameter, the
   * session is just created for the specific incoming request and will expire at the end of it.
   * </p>
   * <p>
   * At the end of the authentication, the context is alimented with the user credentials and with
   * the authentication scheme that were fetched from the HTTP request. They can then be retrieved
   * for further operation by the invoker of this method. In the case of an authentication from
   * the {@code access_token} parameter, the authentication scheme is in the context is set as
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
        new String(fromBase64(context.getUserCredentials()), Charsets.UTF_8).trim();

    // Getting expected parts of credentials
    Matcher matcher = AUTHENTICATION_PATTERN.matcher(decodedCredentials);
    final int credentialPartCount = 3;
    final int loginPart = 1;
    final int passwordPart = 3;
    final int domainIdPart = 2;
    if (matcher.matches() && matcher.groupCount() == credentialPartCount) {

      // All expected parts detected, so getting an authentication key
      AuthenticationCredential credential =
          AuthenticationCredential.newWithAsLogin(matcher.group(loginPart))
              .withAsPassword(matcher.group(passwordPart))
              .withAsDomainId(matcher.group(domainIdPart));
      AuthenticationService authenticator = AuthenticationServiceProvider.getService();
      String key = authenticator.authenticate(credential);
      if (!authenticator.isInError(key)) {
        try {
          String userId = Administration.get().getUserIdByAuthenticationKey(key);
          UserDetail user = UserDetail.getById(userId);
          verifyUserCanLogin(user);
          final SessionInfo session;
          if (!UserDetail.isAnonymousUser(userId)) {
            session = getSessionManagement().openSession(user, context.getHttpServletRequest());
            context.getHttpServletResponse().setHeader(HTTP_SESSIONKEY, session.getSessionId());
            context.getHttpServletResponse()
                .addHeader("Access-Control-Expose-Headers", UserPrivilegeValidation.HTTP_SESSIONKEY);
            SynchronizerTokenService tokenService = SynchronizerTokenService.getInstance();
            tokenService.setUpSessionTokens(session);
            Token token = tokenService.getSessionToken(session);
            context.getHttpServletResponse()
                .addHeader(SynchronizerTokenService.SESSION_TOKEN_KEY, token.getValue());
          } else {
            session = getSessionManagement().openAnonymousSession(context.getHttpServletRequest());
          }
          return session;
        } catch (AdminException e) {
          throw new AuthenticationInternalException(e.getMessage(), e);
        }
      }
    }
    return null;
  }

  private static SessionInfo performTokenBasedAuthentication(final AuthenticationContext context) {
    final String token = context.getUserCredentials();
    final PersistentResourceToken userToken = PersistentResourceToken.getToken(token);
    final UserReference userRef = userToken.getResource(UserReference.class);
    if (userRef != null) {
      final UserDetail user = userRef.getEntity();
      verifyUserCanLogin(user);
      final SessionInfo session =
          getSessionManagement().openSession(user, context.getHttpServletRequest());
      context.getHttpServletResponse().setHeader(HTTP_SESSIONKEY, session.getSessionId());
      return session;
    }
    return null;
  }

  private static void verifyUserCanLogin(final UserDetail user) {
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
    private HttpServletResponse response;
    private HttpServletRequest request;

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
