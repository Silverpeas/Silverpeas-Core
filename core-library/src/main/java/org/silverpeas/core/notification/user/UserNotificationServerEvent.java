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
package org.silverpeas.core.notification.user;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.notification.sse.CommonServerEvent;
import org.silverpeas.core.util.JSONCodec;

import static org.silverpeas.core.notification.user.server.channel.silvermail.SILVERMAILPersistence.countNotReadMessagesOfFolder;

/**
 * This server event is sent on the reception of a user notification.
 * @author Yohann Chastagnier.
 */
public class UserNotificationServerEvent extends CommonServerEvent {

  private static final String ID_ATTR_NAME = "id";
  private static final String SUBJECT_ATTR_NAME = "subject";
  private static final String SENDER_ATTR_NAME = "sender";
  private static final String NB_UNREAD_ATTR_NAME = "nbUnread";
  private static final String IS_CREATION_ATTR_NAME = "isCreation";
  private static final String IS_DELETION_ATTR_NAME = "isDeletion";
  private static final String IS_READ_ATTR_NAME = "isRead";
  private static final String IS_CLEAR_ATTR_NAME = "isClear";

  private static final ServerEventName EVENT_NAME = () -> "USER_NOTIFICATION";

  private final String emitterUserId;

  /**
   * Hidden constructor.
   * @param emitterUserId the emitter of the notification.
   */
  private UserNotificationServerEvent(final String emitterUserId) {
    this.emitterUserId = emitterUserId;
  }

  public static UserNotificationServerEvent creationOf(final String emitterUserId,
      final String notificationId, final String subject, final String sender) {
    return new UserNotificationServerEvent(emitterUserId).withData(JSONCodec.encodeObject(
        jsonObject -> jsonObject
            .put(ID_ATTR_NAME, notificationId)
            .put(SUBJECT_ATTR_NAME, subject)
            .put(SENDER_ATTR_NAME, sender)
            .put(NB_UNREAD_ATTR_NAME, getNbUnreadFor(emitterUserId))
            .put(IS_CREATION_ATTR_NAME, true)
            .put(IS_DELETION_ATTR_NAME, false)
            .put(IS_READ_ATTR_NAME, false)
            .put(IS_CLEAR_ATTR_NAME, false)));
  }

  public static UserNotificationServerEvent readOf(final String emitterUserId,
      final String notificationId, final String subject, final String sender) {
    return new UserNotificationServerEvent(emitterUserId).withData(JSONCodec.encodeObject(
        jsonObject -> jsonObject
            .put(ID_ATTR_NAME, notificationId)
            .put(SUBJECT_ATTR_NAME, subject)
            .put(SENDER_ATTR_NAME, sender)
            .put(NB_UNREAD_ATTR_NAME, getNbUnreadFor(emitterUserId))
            .put(IS_CREATION_ATTR_NAME, false)
            .put(IS_DELETION_ATTR_NAME, false)
            .put(IS_READ_ATTR_NAME, true)
            .put(IS_CLEAR_ATTR_NAME, false)));
  }

  public static UserNotificationServerEvent deletionOf(final String emitterUserId,
      final String notificationId) {
    return new UserNotificationServerEvent(emitterUserId).withData(JSONCodec.encodeObject(
        jsonObject -> jsonObject
            .put(ID_ATTR_NAME, notificationId)
            .put(NB_UNREAD_ATTR_NAME, getNbUnreadFor(emitterUserId))
            .put(IS_CREATION_ATTR_NAME, false)
            .put(IS_DELETION_ATTR_NAME, true)
            .put(IS_READ_ATTR_NAME, false)
            .put(IS_CLEAR_ATTR_NAME, false)));
  }

  public static UserNotificationServerEvent clear(final String emitterUserId) {
    return new UserNotificationServerEvent(emitterUserId).withData(JSONCodec.encodeObject(
        jsonObject -> jsonObject
            .put(NB_UNREAD_ATTR_NAME, getNbUnreadFor(emitterUserId))
            .put(IS_CREATION_ATTR_NAME, false)
            .put(IS_DELETION_ATTR_NAME, false)
            .put(IS_READ_ATTR_NAME, false)
            .put(IS_CLEAR_ATTR_NAME, true)));
  }

  /**
   * Gets the number of unread message of user represented by the given identifier.
   * @param userId the identifier of a user.
   * @return a number of unread message.
   */
  public static int getNbUnreadFor(String userId) {
    return (int) countNotReadMessagesOfFolder(userId, "INBOX");
  }

  @Override
  public ServerEventName getName() {
    return EVENT_NAME;
  }

  /**
   * Gets the identifier of the emitter of the notification.
   * <p>
   * Please verify the use of this method by mobile services before modifying or deleting it.
   * </p>
   * @return an identifier as string.
   */
  @SuppressWarnings("unused")
  public String getEmitterUserId() {
    return emitterUserId;
  }

  @Override
  public boolean isConcerned(final String receiverSessionId, final User receiver) {
    return receiver.getId().equals(emitterUserId);
  }
}
