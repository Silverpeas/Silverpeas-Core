/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.mvc.controller;

import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.user.model.UserDetail;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.silverpeas.core.security.authentication.Authentication;

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

  private final HttpSession httpSession;

  /**
   * Prevent the class from being instantiate (private)
   *
   * @param session the HTTP session to wrap.
   * @param IP the remote user host address IP.
   * @param ud the detail about the connected user.
   */
  public HTTPSessionInfo(HttpSession session, String IP, UserDetail ud) {
    super(session.getId(), ud);
    httpSession = session;
    setIPAddress(IP);
  }

  @Override
  public void onClosed() {
    if (httpSession != null) {
      cleanSession(httpSession);
      try {
        httpSession.invalidate();
      } catch (IllegalStateException ex) {
        SilverTrace.warn("peasCore", "SessionInfo.onClosed()", null, ex.getMessage());
      }
    }
    super.onClosed();
  }

  @SuppressWarnings("unchecked")
  private void cleanSession(final HttpSession httpSession) {
    try {
      Enumeration<String> attributeNames = httpSession.getAttributeNames();
      List<Object> controllers = new ArrayList<Object>();
      while (attributeNames.hasMoreElements()) {
        String spName = attributeNames.nextElement();
        if ((spName != null) && ((spName.startsWith("Silverpeas_")) || (spName.
            startsWith("WYSIWYG_")))) {
          controllers.add(httpSession.getAttribute(spName));
        }
        if (!spName.startsWith("Redirect") && !"gotoNew".equals(spName)
            && !Authentication.PASSWORD_CHANGE_ALLOWED.equals(spName)
            && !Authentication.PASSWORD_IS_ABOUT_TO_EXPIRE.equals(spName)) {
          httpSession.removeAttribute(spName);
        }
      }
      for (Object element : controllers) {
        String elementName = element.getClass().getSimpleName();
        try {
          if (element instanceof AbstractComponentSessionController) {
            AbstractComponentSessionController controller
                = (AbstractComponentSessionController) element;
            controller.close();
          } else if (element instanceof MainSessionController) {
            MainSessionController controller = (MainSessionController) element;
            controller.clear();
          }
        } catch (Exception ex) {
          SilverTrace.warn("peasCore", "SessionInfo.cleanSession()",
              "root.MSG_GEN_PARAM_VALUE", "ERROR while cleaning " + elementName, ex);
        }
      }
    } catch (Exception e) {
      SilverTrace.warn("peasCore", "SessionInfo.cleanSession()",
          "root.MSG_GEN_PARAM_VALUE", "ERROR !!!", e);
    }
  }

  /**
   * Gets the HTTP session backed by this session information.
   *
   * @return the backed HTTP session.
   */
  public HttpSession getHttpSession() {
    return httpSession;
  }

  @Override
  public <T> void setAttribute(String name, T value) {
    httpSession.setAttribute(name, value);
  }

  @Override
  public void unsetAttribute(String name) {
    httpSession.removeAttribute(name);
  }

  @Override
  public <T> T getAttribute(String name) {
    return (T) httpSession.getAttribute(name);
  }
}
