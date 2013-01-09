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

package com.silverpeas.web.mock;

import com.silverpeas.session.SessionInfo;
import com.silverpeas.session.SessionManagement;
import com.stratelia.webactiv.beans.admin.UserDetail;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * A mock of a session manager for testing purpose.
 */
@Named("sessionManagement")
public class SessionManagerMock implements SessionManagement {

  private Map<String, SessionInfo> sessions = new HashMap<String, SessionInfo>();

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
    return sessions.get(sessionKey);
  }

  @Override
  public SessionInfo openSession(UserDetail user) {
    String key = UUID.randomUUID().toString();
    SessionInfo session = new SessionInfo(key, user);
    sessions.put(key, session);
    return session;
  }

  @Override
  public SessionInfo openSession(UserDetail user, HttpServletRequest request) {
    HttpSession session = request.getSession();
    SessionInfo sessionInfo = new SessionInfo(session.getId(), user);
    sessions.put(sessionInfo.getSessionId(), sessionInfo);
    return sessionInfo;
    }

  @Override
  public void closeSession(String sessionKey) {
    sessions.remove(sessionKey);
  }

  @Override
  public boolean isUserConnected(UserDetail user) {
    for (SessionInfo session : sessions.values()) {
      if (user.getId().equals(session.getUserDetail().getId())) {
        return true;
      }
    }
    return false;
  }

  /**
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

}
