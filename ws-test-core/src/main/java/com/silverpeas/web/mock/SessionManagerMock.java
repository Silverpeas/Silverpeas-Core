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

/**
 * A mock of a session manager for testing purpose.
 */
@Named("sessionManager")
public class SessionManagerMock implements SessionManagement {

  private boolean noSession = false;

  private Map<String, SessionInfo> sessions = new HashMap<String, SessionInfo>();

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
    SessionInfo session = new SessionInfo(key, user);
    sessions.put(key, session);
    return session;
  }

  @Override
  public void closeSession(String sessionKey) {
    sessions.remove(sessionKey);
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
}
