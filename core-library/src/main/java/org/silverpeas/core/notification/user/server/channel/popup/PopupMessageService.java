/*
 * Copyright (C) 2000 - 2017 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.notification.user.server.channel.popup;

import org.silverpeas.core.notification.user.server.NotificationData;
import org.silverpeas.core.util.ServiceProvider;

import javax.transaction.Transactional;

/**
 * @author Yohann Chastagnier
 */
public interface PopupMessageService {

  static PopupMessageService get() {
    return ServiceProvider.getService(PopupMessageService.class);
  }

  /**
   * Read the first message (the oldest in other words) about a user and a session.
   * @param userId the identifier of the user.
   * @return an instance of {@link PopupMsg} which represents the Silverpeas message.
   */
  PopupMsg read(String userId);

  /**
   * Deletes the message which is referenced by the given identifier.
   * @param msgId the identifier of the message to delete.
   */
  @Transactional
  void deleteById(String msgId);

  /**
   * Deletes all the messages linked to the user and the session represented by given identifiers.
   * @param userId the identifier of the user.
   */
  @Transactional
  void deleteAll(String userId);

  /**
   * Pushes a new message into the context.
   * @param userId the identifier of the user.
   * @param notifMsg the notification meta data.
   */
  @Transactional
  void push(String userId, NotificationData notifMsg);
}
