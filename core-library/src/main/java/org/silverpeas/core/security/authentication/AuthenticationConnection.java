/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.security.authentication;

/**
 * A connection with a server of a remote authentication service.
 * It wraps the actual object of type T used to communicate with the server.
 * @param <T> the type of the connector to use when communicating with the remote server. The
 * connector is specific to the authentication service.
 */
public class AuthenticationConnection<T> {

  private final T connector;

  /**
   * Constructs a new connection with an authentication server by using the specified  specific server
   * connector.
   * @param connector a connector specific to the remote authentication server.
   */
  public AuthenticationConnection(final T connector) {
    this.connector = connector;
  }

  /**
   * Gets the connector specific to the remote authentication server used by this connection.
   * @return a connector of the authentication server.
   */
  public T getConnector() {
    return connector;
  }
}
