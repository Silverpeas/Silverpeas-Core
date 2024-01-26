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

package org.silverpeas.core.webapi.session;

import org.silverpeas.core.annotation.Bean;
import org.silverpeas.kernel.annotation.Technical;
import org.silverpeas.core.web.session.UserSessionEvent;
import org.silverpeas.core.web.token.SilverpeasWebTokenService;

import javax.enterprise.event.Observes;

import static java.util.Optional.of;

/**
 * Listener in charge of session token cleaning.
 * @author silveryocha
 */
@Technical
@Bean
public class SilverpeasUserSessionTokenCleaner {

  /**
   * On user session ending, revoking all linked tokens generated from
   * {@link SilverpeasUserSessionTokenResource}.
   * @param userSessionEvent the user session event.
   */
  public void onEvent(@Observes final UserSessionEvent userSessionEvent) {
    if (userSessionEvent.isClosing()) {
      of(userSessionEvent.getSessionInfo().getSessionId())
          .ifPresent(SilverpeasWebTokenService.get()::revokeById);
    }
  }
}
