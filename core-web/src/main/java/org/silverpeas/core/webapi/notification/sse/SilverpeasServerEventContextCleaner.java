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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.webapi.notification.sse;

import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.annotation.Technical;
import org.silverpeas.core.web.session.UserSessionEvent;

import javax.enterprise.event.Observes;

import static org.silverpeas.core.notification.sse.ServerEventDispatcherTask.unregisterBySessionId;

/**
 * Listener in charge of server event context cleaning.
 * @author silveryocha
 */
@Technical
@Bean
public class SilverpeasServerEventContextCleaner {

  /**
   * On user session ending, cleaning linked contexts.
   * @param userSessionEvent the user session event.
   */
  public void onEvent(@Observes final UserSessionEvent userSessionEvent) {
    if (userSessionEvent.isClosing()) {
      unregisterBySessionId(userSessionEvent.getSessionInfo().getSessionId());
    }
  }
}
