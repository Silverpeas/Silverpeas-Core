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
package org.silverpeas.core.security.session;

import org.silverpeas.core.admin.user.model.User;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

/**
 * It defines the contract the session management implementation in Silverpeas should implement. It
 * should have only one activated implementation in Silverpeas, and it should be managed by an IoC
 * container under the name 'sessionManager'.
 */
public interface SessionManagement {

  /**
   * Gets the session information about all the connected users in the Silverpeas platform, whatever
   * their domain and the applied domain isolation policy.
   * @return Collection of session information.
   */
  Collection<SessionInfo> getConnectedUsersList();

  /**
   * Gets the session information about all the connected users that are accessible to the specified
   * user. According to the domain level isolation, a user can see either all the others connected
   * users or only those in the same domain.
   * @param user a user from whom all the distinct connected users are visible.
   * @return Collection of session information.
   */
  Collection<SessionInfo> getDistinctConnectedUsersList(User user);

  /**
   * Gets the count of users that are connected to Silverpeas. The domain level isolation applied to
   * the running Silverpeas is taken into account in the computation of the connected user count:
   * either all the connected users are taken into account or only those in the same domain.
   * @param user the user from whom all the users to count are visible.
   * @return the count of connected users
   */
  int getNbConnectedUsersList(User user);

  /**
   * Gets information about the specified user session.
   * @param sessionKey the key of the user session.
   * @return the information about the session mapped to the specified key.
   */
  @Nonnull
  SessionInfo getSessionInfo(String sessionKey);

  /**
   * Is the specified user currently connected to Silverpeas?
   * @param user the user for which the connection is checked.
   * @return true if the user is connected, false otherwise.
   */
  boolean isUserConnected(User user);

  /**
   * Calls {@link #validateSession(SessionValidationContext)} method by specifying to it a context
   * that contains only the specified session key.
   * @param sessionKey the key of the user session.
   * @return the result of {@link #validateSession(SessionValidationContext)} execution.
   */
  SessionInfo validateSession(String sessionKey);

  /**
   * Validates the session identified uniquely by the specified key. The validation checks a session
   * exists with the specified identifier and returns information about this session. At each access
   * by the user to Silverpeas, its current session must be validated. The validation updates also
   * useful information about the session like the timestamp of this access so that additional
   * features can be performed (for example, the timeout computation of the session).
   * @param context the context of the validation that contains at least the session key
   * @return information about the session identified by the specified key or null if no such
   * session exists.
   */
  SessionInfo validateSession(SessionValidationContext context);

  /**
   * Opens a new session for the specified user with the specified HTTP request at the origin of the
   * session ask. The opened session is built upon an HTTP one, and it is managed directly by the
   * underlying HTTP server/container. If the specified user is anonymous, then
   * an {@link IllegalArgumentException} is thrown.
   * @param user the user for which an HTTP session with Silverpeas has to be opened.
   * @param request the HTTP request at the origin of the session opening ask.
   * @return a SessionInfo instance representing the current opened session with information about
   * that session.
   */
  SessionInfo openSession(final User user, final HttpServletRequest request);

  /**
   * Opens a new session for the specified user with the specified HTTP request at the origin of the
   * session ask. The opened session is only for the given HTTP request and it will be closed once
   * the request processing is done.
   * @param user the user for which an HTTP session with Silverpeas has to be opened.
   * @param request the HTTP request at the origin of the session opening ask.
   * @return a SessionInfo instance representing the current opened session with information about
   * that session.
   */
  SessionInfo openOneShotSession(final User user, final HttpServletRequest request);

  /**
   * Opens explicitly a new anonymous session. The opened session is built upon an HTTP one managed
   * directly by the underlying HTTP server/container. In Silverpeas, any anonymous access to it is
   * done under the cover of a peculiar and specific user account, the anonymous user account. This
   * requires the anonymous access to be enabled for Silverpeas and this is done by creating a
   * specific account for the anonymous user (see the property <code>anonymousId</code> in the
   * properties file <code>org/silverpeas/lookAndFeel/generalLook.properties</code>). So, any
   * anonymous user are referred by a unique and same user account but have each of them a different
   * and separate user session.
   * @return a SessionInfo instance representing the current opened anonymous session with
   * information about that session.
   */
  SessionInfo openAnonymousSession(final HttpServletRequest request);

  /**
   * Closes the specified user session.
   * @param sessionKey the key of the session to close.
   */
  void closeSession(String sessionKey);

}
