/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Lib
 * Open Source Software ("FLOSS") applications as described in Silverpeas
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public Licence
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package org.silverpeas.web.test.stub;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.security.session.SessionManagement;
import org.silverpeas.core.security.session.SessionValidationContext;

import javax.annotation.Nonnull;
import javax.enterprise.inject.Alternative;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Service
@Singleton
@Alternative
public class SessionManagementStub implements SessionManagement {

  private final ConcurrentMap<String, SessionInfo> userDataSessions = new ConcurrentHashMap<>(100);
  private final ConcurrentMap<String, SessionInfo> anonymousSessions = new ConcurrentHashMap<>(100);

  @Override
  public Collection<SessionInfo> getConnectedUsersList() {
    return userDataSessions.values();
  }

  @Override
  public Collection<SessionInfo> getDistinctConnectedUsersList(final User user) {
    return userDataSessions.values().stream()
        .filter(s -> s.getUser().getId().equals(user.getId()))
        .collect(Collectors.toSet());
  }

  @Override
  public int getNbConnectedUsersList(final User user) {
    return userDataSessions.size();
  }

  @Override
  @Nonnull
  @SuppressWarnings("Duplicates")
  public SessionInfo getSessionInfo(final String sessionId) {
    SessionInfo session = userDataSessions.get(sessionId);
    if (session == null) {
      if (UserDetail.getCurrentRequester() != null && UserDetail.getCurrentRequester()
          .isAnonymous()) {
        session = anonymousSessions.get(sessionId);
      } else {
        session = SessionInfo.NoneSession;
      }
    }
    return session;
  }

  @Override
  public boolean isUserConnected(final User userDetail) {
    return userDataSessions.values().stream()
        .anyMatch(s -> s.getUser().getId().equals(userDetail.getId()));
  }

  @Override
  public SessionInfo validateSession(final String sessionKey) {
    return validateSession(SessionValidationContext.withSessionKey(sessionKey));
  }

  @Override
  public SessionInfo validateSession(final SessionValidationContext context) {
    String sessionKey = context.getSessionKey();
    return getSessionInfo(sessionKey);
  }

  private SessionInfo openSession(final SessionInfo session) {
    userDataSessions.put(session.getSessionId(), session);
    return session;
  }

  @Override
  public SessionInfo openSession(final User user, final HttpServletRequest request) {
    HttpSession httpSession = request.getSession();
    SessionInfo session = new SessionInfoForTest(httpSession.getId(), user);
    return openSession(session);
  }

  @Override
  public SessionInfo openAnonymousSession(final HttpServletRequest httpServletRequest) {
    UserDetail anonymousUser = UserDetail.getAnonymousUser();
    if (anonymousUser != null) {
      HttpSession httpSession = httpServletRequest.getSession();
      SessionInfo sessionInfo =
          new SessionInfoForTest(httpSession.getId(), UserDetail.getAnonymousUser());
      anonymousSessions.put(sessionInfo.getId(), sessionInfo);
      return sessionInfo;
    }
    return SessionInfo.NoneSession;
  }

  @Override
  public void closeSession(final String sessionId) {
    SessionInfo si = userDataSessions.get(sessionId);
    if (si != null) {
      if (si.isAnonymous()) {
        anonymousSessions.remove(si.getSessionId());
      } else {
        userDataSessions.remove(si.getSessionId());
        si.onClosed();
      }
    }
  }

  private static class SessionInfoForTest extends SessionInfo {

    /**
     * Constructs a new instance about a given opened user session.
     * @param sessionId the identifier of the opened session.
     * @param user the user for which a session was opened.
     */
    public SessionInfoForTest(final String sessionId, final User user) {
      super(sessionId, user);
    }
  }
}

