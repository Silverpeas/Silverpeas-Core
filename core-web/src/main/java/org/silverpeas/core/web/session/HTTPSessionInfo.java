/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.session;

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.security.authentication.Authentication;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;

/**
 * Information on the HTTP session opened by a Silverpeas user to access the Silverpeas Web pages.
 *
 * It wraps the HttpSession instance created by Silverpeas for a given user and it delegates all the
 * session attribute setting to the wrapped HttpSession instance. So it can be used in a such
 * context as an HTTP session itself.
 *
 * The HTTPSessionInfo objects are mainly used for the users accessing Silverpeas with their WEB
 * browser. It is not yet used in the management of sessions for REST-based web service clients.
 */
public class HTTPSessionInfo extends org.silverpeas.core.security.session.SessionInfo {

  private HttpSession httpSession;

  /**
   * Prevent the class from being instantiate (private)
   *
   * @param session the HTTP session to wrap.
   * @param ip the remote user host address IP.
   * @param ud the detail about the connected user.
   */
  HTTPSessionInfo(HttpSession session, String ip, UserDetail ud) {
    super(session.getId(), ud);
    httpSession = session;
    setIPAddress(ip);
  }

  public void setHttpSession(final HttpSession httpSession) {
    this.httpSession = httpSession;
  }

  @Override
  public void onClosed() {
    if (httpSession != null) {
      cleanSession(httpSession);
      try {
        httpSession.invalidate();
      } catch (IllegalStateException ex) {
        SilverLogger.getLogger(this).info(ex.getMessage(), ex);
      }
    }
    super.onClosed();
  }

  @SuppressWarnings("unchecked")
  private void cleanSession(final HttpSession httpSession) {
    try {
      Enumeration<String> attributeNames = httpSession.getAttributeNames();
      List<Object> controllers = new ArrayList<>();
      while (attributeNames.hasMoreElements()) {
        String spName = defaultStringIfNotDefined(attributeNames.nextElement());
        if (spName.startsWith("Silverpeas_") || spName.startsWith("WYSIWYG_")) {
          controllers.add(httpSession.getAttribute(spName));
        }
        if (!spName.startsWith("Redirect") && !"gotoNew".equals(spName)
            && !Authentication.PASSWORD_CHANGE_ALLOWED.equals(spName)
            && !Authentication.PASSWORD_IS_ABOUT_TO_EXPIRE.equals(spName)) {
          httpSession.removeAttribute(spName);
        }
      }
      cleanSessionControllers(controllers);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error("Error while cleaning the HTTP session", e);
    }
  }

  /**
   * Cleans from the session each controller contained into the given list.
   * @param controllers the controllers to clean.
   */
  private void cleanSessionControllers(final List<Object> controllers) {
    for (Object element : controllers) {
      try {
        if (element instanceof SessionCloseable) {
          ((SessionCloseable) element).close();
        }
      } catch (Exception ex) {
        SilverLogger.getLogger(this)
            .error("Error while cleaning the HTTP session in " + element.getClass().getSimpleName(),
                ex);
      }
    }
  }

  @Override
  public <T> void setAttribute(String name, T value) {
    httpSession.setAttribute(name, value);
  }

  @Override
  public void unsetAttribute(String name) {
    httpSession.removeAttribute(name);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getAttribute(String name) {
    return (T) httpSession.getAttribute(name);
  }
}
