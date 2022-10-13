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
package org.silverpeas.core.notification.sse;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.notification.sse.behavior.IgnoreStoring;

/**
 * @author Yohann Chastagnier
 */
class HeartbeatServerEvent extends AbstractServerEvent implements IgnoreStoring {

  private static final ServerEventName EVENT_NAME = () -> "HEARTBEAT_EVENT_SOURCE";

  private final String emitterSessionId;

  /**
   * Hidden constructor.
   * @param emitterSessionId the emitter session id of the event.
   */
  private HeartbeatServerEvent(final String emitterSessionId) {
    this.emitterSessionId = emitterSessionId;
    withData("Event source heartbeat.");
  }

  static HeartbeatServerEvent createFor(final String emitterSessionId) {
    return new HeartbeatServerEvent(emitterSessionId);
  }

  @Override
  public ServerEventName getName() {
    return EVENT_NAME;
  }

  @Override
  public boolean isConcerned(final String receiverSessionId, final User receiver) {
    return emitterSessionId.equals(receiverSessionId);
  }
}
