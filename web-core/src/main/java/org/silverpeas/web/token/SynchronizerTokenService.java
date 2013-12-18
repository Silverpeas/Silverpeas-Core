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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
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
  private static final Logger logger = Logger.getLogger(SynchronizerTokenService.class);

  protected SynchronizerTokenService() {

  }

  /**
   * Sets a session token to the specified HTTP session. A session token is a token used to validate
   * that any requests to a protected web resource are well done within an open and valid user
   * session.
   *
   * @param session the user session to protect with a synchronizer token.
   */
  public void setSessionTokens(HttpSession session) {
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
      logger.log(Level.INFO, "Generate new session token for user " + userId);
      token = generator.renew(token);
    } else {
      logger.log(Level.INFO, "Renew the session token for user " + userId);
      token = generator.generate();
    }
    session.setAttribute(SESSION_TOKEN_KEY, token);
  }

  /**
   * Validates the request to a Silverpeas web resource can be trusted.
   *
   * The access to a Silverpeas web resource is considered as trusted if and only if it is stamped
   * with the expected security tokens for the requested resource. Otherwise, the request isn't
   * considered as trusted and should be rejected. A request is stamped at least with the session
   * token, that is to say with the token that is set with the user session.
   *
   * @param request the HTTP request to check.
   * @throws TokenValidationException if the specified request cannot be trusted.
   */
  public void validate(HttpServletRequest request) throws TokenValidationException {
    boolean isOk = false;
    Token expectedToken = getSessionToken(request);
    if (expectedToken != null && expectedToken.isDefined()) {
      String actualToken = request.getHeader(SESSION_TOKEN_KEY);
      if (StringUtil.isNotDefined(actualToken)) {
        actualToken = request.getParameter(SESSION_TOKEN_KEY);
      }
      isOk = expectedToken.getValue().equals(actualToken);
    }
    if (!isOk) {
      DateTime now = DateTime.now();
      throw new TokenValidationException("Attempt of a CSRF attack detected at " + now.toISO8601());
    }
  }

  /**
   * Applies the specified template with the data in the specified requests.
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
   * Stamps the specified URL with the current security tokens so that the request to the resource
   * located at the URL can be validated by Silverpeas. This method is for the JSP pages in
   * Silverpeas to stamp the URL of the frame or other HTML elements.
   *
   * @param url the URL to stamp.
   * @param request the current HTTP request.
   * @return the stamped URL.
   */
  public String stampsResourceURL(String url, HttpServletRequest request) {
    Token token = getSessionToken(request);
    String stamp = (url.contains("?") ? "&" : "?") + SESSION_TOKEN_KEY + "=" + token.getValue();
    return url + stamp;
  }

  private Token getSessionToken(HttpServletRequest request) {
    Token token = SynchronizerToken.NoneToken;
    HttpSession session = request.getSession(false);
    if (session != null) {
      token = (Token) session.getAttribute(SESSION_TOKEN_KEY);
    }
    return token;
  }
}
