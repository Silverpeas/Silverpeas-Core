/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.core.web.session;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.notification.sse.CommonServerEvent;
import org.silverpeas.core.notification.sse.behavior.AfterSentToAllContexts;
import org.silverpeas.core.notification.sse.behavior.KeepAlwaysLastStored;
import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.security.session.SessionManagement;
import org.silverpeas.core.security.session.SessionManagementProvider;
import org.silverpeas.core.util.JSONCodec;

/**
 * This server event is sent on successful user session opening and on user session ending.
 * @author Yohann Chastagnier.
 */
public class UserSessionServerEvent extends CommonServerEvent implements KeepAlwaysLastStored,
    AfterSentToAllContexts {

  private static final Object DATA_MUTEX = new Object();
  private static final String IS_OPENING_ATTR_NAME = "isOpening";
  private static final String IS_CLOSING_ATTR_NAME = "isClosing";
  private static final String NB_CONNECTED_USERS_ATTR_NAME = "nbConnectedUsers";

  private static final ServerEventName EVENT_NAME = () -> "USER_SESSION";

  private final SessionInfo emitterSession;
  private final boolean opening;
  private final SessionManagement sessionManagement;
  private String data = null;

  /**
   * Hidden constructor.
   * @param opening true if it concerns an opening, false for a closing.
   * @param emitterSession the user behind the event send.
   */
  private UserSessionServerEvent(final boolean opening, final SessionInfo emitterSession) {
    this.emitterSession = emitterSession;
    this.opening = opening;
    sessionManagement = SessionManagementProvider.getSessionManagement();
  }

  static UserSessionServerEvent anOpeningOneFor(final SessionInfo sessionInfo) {
    return new UserSessionServerEvent(true, sessionInfo).initializeData();
  }

  static UserSessionServerEvent aClosingOneFor(final SessionInfo sessionInfo) {
    return new UserSessionServerEvent(false, sessionInfo).initializeData();
  }

  @Override
  public ServerEventName getName() {
    return EVENT_NAME;
  }

  @Override
  public boolean isConcerned(final String receiverSessionId, final User receiver) {
    User emitter = emitterSession.getUserDetail();
    return (!this.opening || !emitterSession.getSessionId().equals(receiverSessionId)) &&
        (!emitter.isDomainRestricted() || emitter.getDomainId().equals(receiver.getDomainId()));
  }

  @Override
  public String getData(final String receiverSessionId, final User receiver) {
    synchronized (DATA_MUTEX) {
      // data computed one time only
      if (data == null) {
        data = super.getData(receiverSessionId, receiver);
      }
    }
    return data;
  }

  @Override
  public void afterAllContexts() {
    synchronized (DATA_MUTEX) {
      data = null;
    }
  }

  /**
   * Initializing the data providing.
   * @return itself.
   */
  private UserSessionServerEvent initializeData() {
    withData((receiverSessionId, receiver) -> {
      final int nbConnectedUsers = sessionManagement.getNbConnectedUsersList(receiver) - 1;
      return JSONCodec.encodeObject(jsonObject -> jsonObject
          .put(IS_OPENING_ATTR_NAME, this.opening)
          .put(IS_CLOSING_ATTR_NAME, !this.opening)
          .put(NB_CONNECTED_USERS_ATTR_NAME, nbConnectedUsers));
    });
    return this;
  }
}
