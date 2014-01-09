/*
 * Copyright (C) 2000-2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.token;

import com.silverpeas.calendar.DateTime;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.ResourceLocator;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.silverpeas.servlet.HttpRequest;
import org.silverpeas.token.Token;
import org.silverpeas.token.TokenGenerator;
import org.silverpeas.token.TokenGeneratorProvider;
import org.silverpeas.token.exception.TokenValidationException;
import org.silverpeas.token.synchronizer.SynchronizerToken;

/**
 * A service to manage the synchronizer tokens used in Silverpeas to protect the user sessions or
 * the web resources published by Silverpeas.
 *
 * Each resource in Silverpeas and accessible through the Web can be protected by one or more
 * security tokens. These tokens are named synchronizer token as they are transmitted within each
 * request and must match the ones expected by Silverpeas to access the asked resource. This service
 * provides the functions to generate, to validate and to set such tokens for the Web resource in
 * Silverpeas to protect (not all resources require to be protected in Silverpeas).
 *
 * @author mmoquillon
 */
public class SynchronizerTokenService {

  protected static final String SESSION_TOKEN_KEY = "X-STKN";
  private static final String DEFAULT_RULE
      = "^/(?!(util/)|(images/)|(Main/)|(Rclipboard/)|(LinkFile/)|(repository/)|.*DragAndDrop/)\\w+/.*(?<!(.gif)|(.png)|(.jpg)|(.js)|(.css)|(.jar)|(.swf)|(.properties)|(.html))$";
  private static final String RULE_PREFIX = "security.web.protection.rule";
  private static final String SECURITY_ACTIVATION_KEY = "security.web.protection";
  private static final Logger logger = Logger.getLogger(SynchronizerTokenService.class.getName());
  private static final ResourceLocator settings
      = new ResourceLocator("org.silverpeas.util.security", "");

  protected SynchronizerTokenService() {

  }

  /**
   * Sets a session token for the specified HTTP session. A session token is a token used to
   * validate that any requests to a protected web resource are well done within an open and valid
   * user session. The setting occurs only if the security mechanism by token is enabled.
   *
   * @param session the user session to protect with a synchronizer token.
   */
  public void setSessionTokens(HttpSession session) {
    if (isWebSecurityByTokensEnabled()) {
      MainSessionController controller = (MainSessionController) session.getAttribute(
          MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
      String userId = "anonymous";
      if (controller != null) {
        UserDetail user = controller.getCurrentUserDetail();
        userId = user.getId() + " (" + user.getDisplayedName() + ")";
      }
      TokenGenerator generator = TokenGeneratorProvider.getTokenGenerator(SynchronizerToken.class);
      Token token = (Token) session.getAttribute(SESSION_TOKEN_KEY);
      if (token != null) {
        logger.log(Level.INFO, "Generate new session token for user {0}", userId);
        token = generator.renew(token);
      } else {
        logger.log(Level.INFO, "Renew the session token for user {0}", userId);
        token = generator.generate();
      }
      session.setAttribute(SESSION_TOKEN_KEY, token);
    }
  }

  /**
   * Validates the request to a Silverpeas web resource can be trusted. The request is validated
   * only if both the security mechanism by token is enabled and the request targets a protected web
   * resource.
   *
   * The access to a protected web resource is considered as trusted if and only if it is stamped
   * with the expected security tokens for the requested resource. Otherwise, the request isn't
   * considered as trusted and should be rejected. A request is stamped at least with the session
   * token, that is to say with the token that is set with the user session.
   *
   * @param request the HTTP request to check.
   * @throws TokenValidationException if the specified request cannot be trusted.
   */
  public void validate(HttpServletRequest request) throws TokenValidationException {
    if (isWebSecurityByTokensEnabled() && isAProtectedResource(request)) {
      logger.log(Level.INFO, "Validate the request for path {0}", getRequestPath(request));
      boolean isOk = false;
      Token expectedToken = getSessionToken(request);
      if (expectedToken.isDefined()) {
        String actualToken = request.getHeader(SESSION_TOKEN_KEY);
        if (StringUtil.isNotDefined(actualToken)) {
          actualToken = request.getParameter(SESSION_TOKEN_KEY);
          if (StringUtil.isNotDefined(actualToken) && !request.getMethod().equals("POST")) {
            // use cookie only for other HTTP method than POST; the cookie should be avoided to
            // carry a synchronizer token for security reason.
            logger.log(Level.WARNING, "Validation of the request for path {0} by cookie",
                getRequestPath(request));
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
              for (int i = 0; i < cookies.length && StringUtil.isNotDefined(actualToken); i++) {
                if (cookies[i].getName().equals(SESSION_TOKEN_KEY)) {
                  actualToken = cookies[i].getValue();
                }
              }
            }
          }
        }
        isOk = expectedToken.getValue().equals(actualToken);
      }
      if (!isOk) {
        DateTime now = DateTime.now();
        throw new TokenValidationException("Attempt of a CSRF attack detected at " + now.toISO8601());
      }
    }
  }

  /**
   * Applies the specified template with the tokens in the specified requests.
   *
   * Some peculiar codes are generated from a template and then executed in the behalf of a web page
   * in order to set a security context. Such codes are for example to set the tokens in each form
   * of a web page so that the request will be validated by the token validator.
   *
   * @param template a security template to use.
   * @param request the request containing the data required to apply the template.
   * @return the result of the template application.
   */
  public String applyTemplate(TokenSettingTemplate template, HttpServletRequest request) {
    String result = "";
    Token token = getSessionToken(request);
    if (token != null && token.isDefined()) {
      TokenSettingTemplate.Parameter tokenKey = new TokenSettingTemplate.Parameter(
          TokenSettingTemplate.TOKEN_NAME_PARAMETER, SESSION_TOKEN_KEY);
      TokenSettingTemplate.Parameter tokenValue = new TokenSettingTemplate.Parameter(
          TokenSettingTemplate.TOKEN_VALUE_PARAMETER, token.getValue());
      result = template.apply(tokenKey, tokenValue);
    }
    return result;
  }

  /**
   * Creates a cookie valued with the synchronizer token used to protect the user session. This
   * method is useful for web pages using relocation to load some contents and with which the usual
   * way to set the token within the browser request or the AJAX request cannot work.
   *
   * If the security mechanism based on the tokens is disabled or if there is no token protecting
   * the HTTP session, then null is returned.
   *
   * @param request the HTTP request.
   * @param force a boolean indicating if the cookie should be created even it already exists in the
   * request.
   * @return the cookie if the security mechanism based on the tokens is enabled and there is a
   * token that protects the current HTTP session.
   */
  public Cookie createCookieWithSessionToken(HttpServletRequest request, boolean force) {
    HttpRequest httpRequest = HttpRequest.decorate(request);
    if (isWebSecurityByTokensEnabled() && (!httpRequest.hasCookie(SESSION_TOKEN_KEY) || force)) {
      Token token = getSessionToken(httpRequest);
      if (token.isDefined()) {
        Cookie cookie = new Cookie(SESSION_TOKEN_KEY, token.getValue());
        cookie.setHttpOnly(true);
        cookie.setMaxAge(-1);
        cookie.setSecure(httpRequest.isSecure());
        return cookie;
      }
    }
    return null;
  }

  protected boolean isWebSecurityByTokensEnabled() {
    return settings.getBoolean(SECURITY_ACTIVATION_KEY, false);
  }

  /**
   * Is the resource targeted by the specified request must be protected by a synchronizer token?
   *
   * @param request the request to a possibly protected resource.
   * @return true if the requested resource is a protected one and then the request should be
   * validate.
   */
  protected boolean isAProtectedResource(HttpServletRequest request) {
    String path = getRequestPath(request);
    boolean isProtected = path.matches(DEFAULT_RULE);
    Enumeration<String> properties = settings.getKeys();
    for (; properties.hasMoreElements() && isProtected;) {
      String property = properties.nextElement();
      if (property.startsWith(RULE_PREFIX)) {
        String rule = settings.getString(property);
        isProtected &= path.matches(rule);
      }
    }
    return isProtected;
  }

  protected Token getSessionToken(HttpServletRequest request) {
    Token token = null;
    HttpSession session = request.getSession(false);
    if (session != null) {
      token = (Token) session.getAttribute(SESSION_TOKEN_KEY);
    }
    return (token == null ? SynchronizerToken.NoneToken : token);
  }

  private String getRequestPath(HttpServletRequest request) {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    String path = httpRequest.getRequestURI();
    if (path.startsWith(httpRequest.getContextPath())) {
      path = path.substring(httpRequest.getContextPath().length());
    }
    return path;
  }
}
