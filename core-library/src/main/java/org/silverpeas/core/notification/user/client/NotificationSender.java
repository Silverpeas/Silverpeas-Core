/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.core.notification.user.client;

import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.notification.NotificationException;
import org.silverpeas.core.notification.user.client.constant.BuiltInNotifAddress;
import org.silverpeas.core.notification.user.client.model.SentNotificationInterface;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.StringUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Sender of a notification to both the users in Silverpeas and to external users. The notification
 * is defined by a {@link NotificationMetaData} instance. It uses the service of a
 * {@link NotificationManager} object for doing its job.
 */
public class NotificationSender implements java.io.Serializable {

  private static final long serialVersionUID = 4165938893905145809L;

  private transient NotificationManager notificationManager;
  private int instanceId;

  /**
   * Default constructor
   */
  protected NotificationSender() {
    this(null);
  }

  /**
   * Constructor for a standard component
   * @param instanceId the instance Id of the calling's component
   */
  public NotificationSender(final String instanceId) {
    this.instanceId = ComponentInst.getComponentLocalId(instanceId);
    notificationManager = NotificationManager.get();
  }

  /**
   * Sends the notification as defined by the specified {@link NotificationMetaData} instance.
   * @param metaData the meta data of the notification. It defines the content of the notification
   * as well as the recipients.
   * @throws NotificationException if an error occurs while sending the notification.
   *
   */
  public void notifyUser(final NotificationMetaData metaData)
      throws NotificationException {
    notifyUser(BuiltInNotifAddress.COMPONENT_DEFINED.getId(), metaData);
  }

  /**
   * Sends at the specified address the notification whose definition is given by the specified
   * {@link NotificationMetaData} instance.
   * @param addressId the unique identifier of an address at which the notification has to be sent.
   * @param metaData the meta data of the notification. It defines the content of the notification
   * as well as the recipients.
   * @throws NotificationException if an error occurs while sending the notification.
   */
  public void notifyUser(final int addressId, final NotificationMetaData metaData)
      throws NotificationException {
    CurrentUserNotificationContext.getCurrentUserNotificationContext().checkManualUserNotification(metaData);

    // Getting all the users from the recipients declared in metaData (comes from users and groups)
    final Set<UserRecipient> recipients = metaData.getAllUserRecipients(true);
    final Set<String> languages = metaData.getLanguages();

    // send the notification to the internal recipients
    final Set<String> recipientIds =
        recipients.stream().map(UserRecipient::getUserId).collect(Collectors.toSet());
    if (languages.size() == 1) {
      sendNotification(recipientIds, metaData, addressId, languages.iterator().next());
    } else {
      final String defaultLanguage = getDefaultLanguage(metaData.getSender(), languages);
      final Map<String, Set<String>> usersPerLanguage = new HashMap<>();
      recipientIds.stream().map(User::getById).forEach(u -> {
        final String userLang = u.getUserPreferences().getLanguage();
        if (languages.contains(userLang)) {
          addUserForLanguage(usersPerLanguage, u, userLang);
        } else {
          addUserForLanguage(usersPerLanguage, u, defaultLanguage);
        }
      });
      for (final Map.Entry<String, Set<String>> entry : usersPerLanguage.entrySet()) {
        sendNotification(entry.getValue(), metaData, addressId, entry.getKey());
      }
    }

    // send the notification to the external recipients who are declared in metaData
    sendNotification(Collections.emptySet(), metaData, addressId, metaData.getExternalLanguage());

    if (metaData.isSendByAUser()) {
      // save notification for history
      saveNotification(metaData, recipients);
    }


  }

  private void addUserForLanguage(final Map<String, Set<String>> usersPerLanguage, final User u,
      final String userLang) {
    usersPerLanguage.compute(userLang, (l, s) -> {
      Set<String> users = s;
      if (users == null) {
        users = new HashSet<>();
      }
      users.add(u.getId());
      return users;
    });
  }

  private String getDefaultLanguage(final String senderId, final Collection<String> languages) {
    final String defaultLanguage;
    String senderLanguage = "";
    if (StringUtil.isDefined(senderId)) {
      senderLanguage = User.getById(senderId).getUserPreferences().getLanguage();
    }
    if (languages.contains(senderLanguage)) {
      defaultLanguage = senderLanguage;
    } else if (languages.contains(I18NHelper.defaultLanguage)) {
      defaultLanguage = I18NHelper.defaultLanguage;
    } else {
      defaultLanguage = languages.iterator().next();
    }
    return defaultLanguage;
  }

  /**
   * Sends to the specified users the notification described by the {@link NotificationMetaData}
   * by using the given media type and in the specified language.
   * @param userIds a collection of user identifiers. If the collection is empty, then the
   * notification will be sent to the external recipients declared within the
   * {@link NotificationMetaData} object.
   * @param metaData a {@link NotificationMetaData} instance that describes the notification to
   * send.
   * @param addressId the unique identifier of the address at which the notification has to be sent.
   * @param language the language in which the notification content will be written.
   * @throws NotificationException if an error occurs while sending the notification.
   */
  private void sendNotification(final Collection<String> userIds,
      final NotificationMetaData metaData, final int addressId, final String language)
      throws NotificationException {
    final NotificationParameters params = getNotificationParameters(addressId, metaData);
    params.setTitle(metaData.getTitle(language))
        .setLink(metaData.getLink(language))
        .setMessage(metaData.getContent(language))
        .setLanguage(language);
    if (!userIds.isEmpty()) {
      params.setNotificationResourceData(metaData.getNotificationResourceData(language));
      notificationManager.notifyUsers(params, userIds);
    } else if (CollectionUtil.isNotEmpty(metaData.getExternalRecipients())) {
      notificationManager.notifyExternals(params, metaData.getExternalRecipients());
      // indicating the sending to external emails has been managed by clearing the collection
      metaData.setExternalRecipients(null);
    }
  }

  /**
   * Saves the notification into the history of the sent notifications.
   * @param metaData the meta data that defines the notification that has been sent.
   * @param usersSet the recipients that have received the notification.
   * @throws NotificationException if an error occurs while saving the notification
   * information.
   */
  private void saveNotification(NotificationMetaData metaData, Set<UserRecipient> usersSet)
      throws NotificationException {
    if (!usersSet.isEmpty()) {
      getNotificationInterface().saveNotifUser(metaData, usersSet);
    }
  }

  private SentNotificationInterface getNotificationInterface() {
    return SentNotificationInterface.get();
  }

  private NotificationParameters getNotificationParameters(int aMediaType,
      NotificationMetaData metaData) {
    final NotificationParameters params = new NotificationParameters();
    params.setMessagePriority(metaData.getMessageType())
        .setDate(metaData.getDate())
        .setTitle(metaData.getTitle())
        .setMessage(metaData.getContent())
        .setSource(metaData.getSource())
        .setSessionId(metaData.getSessionId())
        .setOriginalExtraMessage(metaData.getOriginalExtraMessage())
        .setSendImmediately(metaData.isSendImmediately())
        .setAddressId(aMediaType)
        .setAnswerAllowed(metaData.isAnswerAllowed())
        .setAction(metaData.getAction());
    if (instanceId != -1) {
      params.setComponentInstance(instanceId);
    } else {
      params.setComponentInstance(ComponentInst.getComponentLocalId(metaData.getComponentId()));
    }
    String sender = metaData.getSender();
    if (aMediaType == BuiltInNotifAddress.BASIC_POPUP.getId()) {
      if (metaData.isAnswerAllowed() && StringUtil.isInteger(sender)) {
        params.setFromUserId(Integer.parseInt(metaData.getSender()));
      }
    } else if (StringUtil.isInteger(sender)) {
      params.setFromUserId(Integer.parseInt(metaData.getSender()));
    } else {
      params.setSenderName(sender);
    }
    return params;
  }
}
