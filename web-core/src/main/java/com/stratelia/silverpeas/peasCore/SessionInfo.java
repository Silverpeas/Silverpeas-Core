/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.stratelia.silverpeas.peasCore;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class SessionInfo extends com.silverpeas.session.SessionInfo {
  private static final long millisPerHour = 60L * 60L * 1000L;
  private static final long millisPerMinute = 60000L;

  private HttpSession httpSession = null;
  private String userIPAdress = null;
  private long lastAliveDate = 0L;

  /**
   * Updates the isalive status of the session.
   */
  protected void updateIsAlive() {
    lastAliveDate = System.currentTimeMillis();
  }

  /**
   * Gets the date at which the session is alive.
   * @return the isalive date
   */
  public long getIsAliveDate() {
    return lastAliveDate;
  }

  /**
   * Gets the IP address of the host from which the user is connected and is accessing Silverpeas.
   * @return the session client host address IP.
   */
  public String getUserHostIP() {
    return userIPAdress;
  }

  /**
   * Prevent the class from being instantiate (private)
   * @param session the HTTP session to wrap.
   * @param IP the remote user host address IP.
   * @param ud the detail about the connected user.
   */
  public SessionInfo(HttpSession session, String IP, UserDetail ud) {
    super(session.getId(), ud);
    httpSession = session;
    userIPAdress = IP;
    lastAliveDate = System.currentTimeMillis();
  }

  @SuppressWarnings("unchecked")
  public void cleanSession() {
    if (httpSession != null) {
      try {
        Enumeration<String> spSessionAttNames = httpSession.getAttributeNames();
        List<String> spNames = new ArrayList<String>();
        while (spSessionAttNames.hasMoreElements()) {
          String spName = spSessionAttNames.nextElement();
          if ((spName != null) && ((spName.startsWith("Silverpeas_")) || (spName.startsWith("WYSIWYG_")))) {
            spNames.add(spName);
          }
        }
        for (String spName : spNames) {
          try {
            Object element = httpSession.getAttribute(spName);
            SilverTrace.debug("peasCore", "SessionInfo.cleanSession()", "Remove=" + spName);
            if (element instanceof AbstractComponentSessionController) {
              AbstractComponentSessionController controller = (AbstractComponentSessionController) element;
              controller.close();
              SilverTrace.debug("peasCore", "SessionManager.cleanSession()", controller.getClass().getName());
            } else if (element instanceof MainSessionController) {
              MainSessionController controller = (MainSessionController) element;
              controller.close();
              SilverTrace.debug("peasCore", "SessionManager.cleanSession()",
                  "MainSessionController");
            }
            httpSession.removeAttribute(spName);
          } catch (Exception ex) {
            SilverTrace.warn("peasCore", "SessionInfo.cleanSession()",
                "root.MSG_GEN_PARAM_VALUE", "ERROR for parameter : " + spName, ex);
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
  }

  public void terminateSession() {
    if (httpSession != null) {
      try {
        httpSession.invalidate();
        httpSession = null;
      } catch (Exception e) {
        SilverTrace.warn("peasCore", "SessionInfo.cleanSession()",
            "root.MSG_GEN_PARAM_VALUE", "ERROR !!!", e);
      }
    }
  }

  public String getLog() {
    return getUserDetail().getLogin() + " (" + getUserDetail().getDomainId() + ")";
  }

  /**
   * Transform the milliseconds duration in hours, minutes and seconds.
   * @param duration in milliseconds
   * @return "xxHyymnzzs" where xx=hours, yy=minutes, zz=seconds
   */
  public String formatDuration(long duration) {
    long hourDuration = duration / millisPerHour;
    long minuteDuration = (duration % millisPerHour) / millisPerMinute;
    long secondDuration = ((duration % millisPerHour) % millisPerMinute) / 1000;

    String dHour = Long.toString(hourDuration);
    String dMinute = Long.toString(minuteDuration);
    String dSecond = Long.toString(secondDuration);

    if (hourDuration < 10) {
      dHour = "0" + dHour;
    }
    if (minuteDuration < 10) {
      dMinute = "0" + dMinute;
    }
    if (secondDuration < 10) {
      dSecond = "0" + dSecond;
    }

    return dHour + "h" + dMinute + "m" + dSecond + "s";
  }

  /**
   * Gets the HTTP session refered by this session information.
   * @return the backed HTTP session.
   */
  public HttpSession getHttpSession() {
    return this.httpSession;
  }
}
