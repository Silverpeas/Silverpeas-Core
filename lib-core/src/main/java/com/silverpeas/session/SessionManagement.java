/*
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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

package com.silverpeas.session;

import java.util.Collection;

import com.stratelia.webactiv.beans.admin.UserDetail;

/**
 * It defines the contract the session management implementation in Silverpeas should implement.
 * It should have only one activated implementation in Sivlerpeas and it should be managed by an
 * IoC container under the name 'sessionManager'.
 */
public interface SessionManagement {

  /**
   * Gets all the connected users and the duration of their session.
   * @return Collection of SessionInfo
   */
  Collection<SessionInfo> getDistinctConnectedUsersList(UserDetail user);

  /**
   * Gets the count of connected users.
   * @return the count of connected users
   */
  int getNbConnectedUsersList(UserDetail user);

  /**
   * Gets information about the specified user session.
   * @param sessionKey the key of the user session.
   * @return the information about the session mapped to the specified key.
   */
  SessionInfo getSessionInfo(String sessionKey);

  /**
   * Opens a new session for a user with the specified information.
   * @param sessionInfo the information about the session to open.
   * @return the key of the opened session.
   */
  String openSession(final SessionInfo sessionInfo);

  /**
   * Closes the specified user session.
   * @param sessionKey the key of the session to close.
   */
  void closeSession(String sessionKey);

}
