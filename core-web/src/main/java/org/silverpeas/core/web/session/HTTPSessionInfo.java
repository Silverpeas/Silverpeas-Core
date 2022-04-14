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
package org.silverpeas.core.web.session;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.security.authentication.AuthenticationProtocol;
import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;

/**
 * A Silverpeas user session built upon the HTTP session created by the underlying web server.
 * <p>
 * It wraps the {@link HttpSession} instance created by the web server for a given user and it
 * delegates all the session attribute management to it. So it can be used instead of an HTTP
 * session.
 * </p>
 */
public class HTTPSessionInfo extends SessionInfo {

  private HttpSession httpSession;

  /**
   * Constructs a new {@link SessionInfo} object from the
   * specified HTTP session.
   * @param session the HTTP session to wrap.
   * @param ip the remote user host address IP.
   * @param ud the detail about the connected user.
   */
  HTTPSessionInfo(HttpSession session, String ip, User ud) {
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
            && !AuthenticationProtocol.PASSWORD_CHANGE_ALLOWED.equals(spName)
            && !AuthenticationProtocol.PASSWORD_IS_ABOUT_TO_EXPIRE.equals(spName)) {
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
