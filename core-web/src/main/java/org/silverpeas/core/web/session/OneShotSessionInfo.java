/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.web.session;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.security.session.SessionInfo;

import java.util.UUID;

/**
 * A Silverpeas user session built dedicated for one shot requests.
 * <p>
 *   No HTTP session is created into this context.
 * </p>
 * <p>
 *   The session identifier is built on object instantiation. It is composed of
 *   '{@literal oneshot-}' prefix and an UUID.
 * </p>
 */
public class OneShotSessionInfo extends SessionInfo {

  /**
   * Constructs a new {@link SessionInfo} object from the data.
   * @param ip the remote user host address IP.
   * @param ud the detail about the connected user.
   */
  OneShotSessionInfo(String ip, User ud) {
    super("oneshot-" + UUID.randomUUID(), ud);
    setIPAddress(ip);
    setAsOneShot();
  }
}
