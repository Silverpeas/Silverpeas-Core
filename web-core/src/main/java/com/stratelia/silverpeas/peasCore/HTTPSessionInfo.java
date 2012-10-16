/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.stratelia.silverpeas.peasCore;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.servlet.http.HttpSession;

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
public class HTTPSessionInfo extends com.silverpeas.session.SessionInfo {

  private static final long millisPerHour = 60L * 60L * 1000L;
  private static final long millisPerMinute = 60000L;
  private HttpSession httpSession;
  private long lastAliveTimestamp;

  /**
   * Updates the isalive status of the session.
   */
  protected void updateIsAlive() {
    lastAliveTimestamp = System.currentTimeMillis();
  }

  /**
   * Gets the date at which the session is alive.
   *
   * @return the isalive date
   */
  public long getIsAliveDate() {
    return lastAliveTimestamp;
  }

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
    lastAliveTimestamp = System.currentTimeMillis();
    setIPAddress(IP);
  }

  @Override
  public void onClosed() {
    if (httpSession != null) {
      cleanSession(httpSession);
      httpSession.invalidate();
    }
    super.onClosed();
  }

  @SuppressWarnings("unchecked")
  private void cleanSession(final HttpSession httpSession) {
    try {
      Enumeration<String> attributeNames = httpSession.getAttributeNames();
      List<String> names = new ArrayList<String>();
      while (attributeNames.hasMoreElements()) {
        String spName = attributeNames.nextElement();
        if ((spName != null) && ((spName.startsWith("Silverpeas_")) || (spName.
            startsWith("WYSIWYG_")))) {
          names.add(spName);
        }
      }
      for (String attributeName : names) {
        try {
          Object element = httpSession.getAttribute(attributeName);
          SilverTrace.debug("peasCore", "SessionInfo.cleanSession()", "Remove=" + attributeName);
          if (element instanceof AbstractComponentSessionController) {
            AbstractComponentSessionController controller =
                (AbstractComponentSessionController) element;
            controller.close();
            SilverTrace.debug("peasCore", "SessionManager.cleanSession()", controller.getClass().
                getName());
          } else if (element instanceof MainSessionController) {
            MainSessionController controller = (MainSessionController) element;
            controller.close();
            SilverTrace.debug("peasCore", "SessionManager.cleanSession()", "MainSessionController");
          }
          httpSession.removeAttribute(attributeName);
        } catch (Exception ex) {
          SilverTrace.warn("peasCore", "SessionInfo.cleanSession()",
              "root.MSG_GEN_PARAM_VALUE", "ERROR for parameter : " + attributeName, ex);
        }
      }
      httpSession.removeAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
      httpSession.removeAttribute("SessionGraphicElementFactory");
      httpSession.removeAttribute("spaceModel"); // For Portlets
      httpSession.removeAttribute("quizzUnderConstruction"); // For Quizz
      httpSession.removeAttribute("questionsVector"); // For Quizz
      httpSession.removeAttribute("currentQuizzId"); // For Quizz
      httpSession.removeAttribute("questionsResponses"); // For Quizz
      httpSession.removeAttribute("currentParticipationId"); // For Quizz

      httpSession.removeAttribute("DomainsBarUtil");
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
