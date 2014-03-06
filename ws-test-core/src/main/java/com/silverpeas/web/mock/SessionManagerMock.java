/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.silverpeas.web.mock;

import com.silverpeas.session.SessionInfo;
import com.silverpeas.session.SessionManagement;
import com.stratelia.webactiv.beans.admin.UserDetail;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.silverpeas.authentication.Authentication;

/**
 * A mock of a session manager for testing purpose.
 */
@Named("sessionManagement")
public class SessionManagerMock implements SessionManagement {

  private boolean noSession = false;

  private final Map<String, HttpSessionInfo> sessions = new HashMap<String, HttpSessionInfo>();

  @Override
  public Collection<SessionInfo> getConnectedUsersList() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Collection<SessionInfo> getDistinctConnectedUsersList(UserDetail user) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int getNbConnectedUsersList(UserDetail user) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public SessionInfo getSessionInfo(String sessionKey) {
    if (noSession) {
      return null;
    }
    return sessions.get(sessionKey);
  }

  @Override
  public SessionInfo openSession(UserDetail user) {
    String key = UUID.randomUUID().toString();
    HttpSessionInfo session = new HttpSessionInfo(key, user);
    sessions.put(key, session);
    return session;
  }

  @Override
  public SessionInfo openSession(UserDetail user, HttpServletRequest request) {
    HttpSession session = request.getSession(true);
    HttpSessionInfo sessionInfo = new HttpSessionInfo(session.getId(), user, session);
    sessions.put(sessionInfo.getSessionId(), sessionInfo);
    return sessionInfo;
  }

  @Override
  public void closeSession(String sessionKey) {
    HttpSessionInfo sessionInfo = sessions.remove(sessionKey);
    if (sessionInfo != null) {
      HttpSession httpSession = sessionInfo.getHttpSession();
      if (httpSession != null) {
        Enumeration<String> attributeNames = httpSession.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
          String spName = attributeNames.nextElement();
          if (!spName.startsWith("Redirect") && !"gotoNew".equals(spName)
              && !Authentication.PASSWORD_CHANGE_ALLOWED.equals(spName)
              && !Authentication.PASSWORD_IS_ABOUT_TO_EXPIRE.equals(spName)) {
            httpSession.removeAttribute(spName);
          }
        }
      }
    }
  }

  @Override
  public boolean isUserConnected(UserDetail user) {
    if (noSession) {
      return false;
    }
    for (SessionInfo session : sessions.values()) {
      if (user.getId().equals(session.getUserDetail().getId())) {
        return true;
      }
    }
    return false;
  }

  /**
   * @return the noSession
   */
  public boolean isNoSession() {
    return noSession;
  }

  /**
   * @param noSession the noSession to set
   */
  public void setNoSession(boolean noSession) {
    this.noSession = noSession;
  }

  /*
   * Validates the session identified uniquely by the specified key. The validation checks a session
   * exists with the specified identifier and returns information about this session.
   * At each access by the user to Silverpeas, its current session must be validated. The validation
   * updates also useful information about the session like the timestamp of this access
   * so that additional features can be performed (for example, the timeout computation of the
   * session).
   *
   * @param sessionKey the key of the user session.
   * @return information about the session identified by the specified key or null if no such session
   *         exists.
   */
  @Override
  public SessionInfo validateSession(String sessionKey) {
    SessionInfo sessionInfo = getSessionInfo(sessionKey);
    if (sessionInfo != null) {
      sessionInfo.updateLastAccess();
    }
    return sessionInfo;
  }

  @Override
  public long getNextSessionTimeOut(String sessionKey) {
    return System.currentTimeMillis();
  }
}
