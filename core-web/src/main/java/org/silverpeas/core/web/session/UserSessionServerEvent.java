/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

package org.silverpeas.core.web.session;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.notification.sse.CommonServerEvent;
import org.silverpeas.core.notification.sse.behavior.KeepAlwaysStoring;
import org.silverpeas.core.security.session.SessionManagement;
import org.silverpeas.core.security.session.SessionManagementProvider;
import org.silverpeas.core.util.JSONCodec;

/**
 * This server event is sent on successful user session opening and on user session ending.
 * @author Yohann Chastagnier.
 */
public class UserSessionServerEvent extends CommonServerEvent implements KeepAlwaysStoring {

  private static ServerEventName EVENT_NAME = () -> "USER_SESSION";

  private final User emitter;

  /**
   * Hidden constructor.
   * @param opening true if it concerns an opening, false for a closing.
   * @param emitter the user behind the event send.
   */
  private UserSessionServerEvent(final boolean opening, final User emitter) {
    this.emitter = emitter;
    withData(receiver -> {
      SessionManagement sessionManagement = SessionManagementProvider.getSessionManagement();
      int nbConnectedUsers = sessionManagement.getNbConnectedUsersList(receiver) - 1;
      return JSONCodec.encodeObject(
          jsonObject -> jsonObject.put("isOpening", opening).put("isClosing", !opening)
              .put("nbConnectedUsers", nbConnectedUsers));
    });
  }

  public static UserSessionServerEvent anOpeningOneFor(final UserDetail sessionUser) {
    return new UserSessionServerEvent(true, sessionUser);
  }

  public static UserSessionServerEvent aClosingOneFor(final UserDetail sessionUser) {
    return new UserSessionServerEvent(false, sessionUser);
  }

  @Override
  public ServerEventName getName() {
    return EVENT_NAME;
  }

  @Override
  public boolean isConcerned(final User receiver) {
    return !emitter.equals(receiver) &&
        (!emitter.isDomainRestricted() || emitter.getDomainId().equals(receiver.getId()));
  }
}
