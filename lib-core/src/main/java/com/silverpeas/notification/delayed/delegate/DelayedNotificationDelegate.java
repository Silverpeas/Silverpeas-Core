/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.notification.delayed.delegate;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.silverpeas.notification.delayed.DelayedNotificationFactory;
import com.silverpeas.notification.delayed.constant.DelayedNotificationFrequency;
import com.silverpeas.notification.delayed.model.DelayedNotificationData;
import com.silverpeas.notification.delayed.model.DelayedNotificationUserSetting;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.constant.NotifChannel;
import com.stratelia.silverpeas.notificationserver.NotificationData;
import com.stratelia.silverpeas.notificationserver.NotificationServer;
import com.stratelia.silverpeas.notificationserver.NotificationServerException;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 * @author Yohann Chastagnier
 */
public class DelayedNotificationDelegate {

  /** For now, only the SMTP channel can be delayed (mail) */
  private static final Set<NotifChannel> WIRED_CHANNELS = new HashSet<NotifChannel>();
  static {
    WIRED_CHANNELS.add(NotifChannel.SMTP);
  }

  /** Global settings */
  private final ResourceLocator settings = new ResourceLocator(
      "com.stratelia.silverpeas.notificationManager.settings.notificationManagerSettings", "");

  /** Notification server instance */
  private final NotificationServer notificationServer = new NotificationServer();

  /** User details cache */
  private final Map<Integer, UserDetail> userDetailCache = new LinkedHashMap<Integer, UserDetail>(
      100);

  /** StringTemplate header cache */
  private final Map<String, SilverpeasTemplate> langHeaderCache =
      new HashMap<String, SilverpeasTemplate>(10);

  /** StringTemplate summary cache */
  private final Map<String, SilverpeasTemplate> langSummaryCache =
      new HashMap<String, SilverpeasTemplate>(20);

  /** StringTemplate footer cache */
  private final Map<String, SilverpeasTemplate> langFooterCache =
      new HashMap<String, SilverpeasTemplate>(10);

  /**
   * Default constructor
   */
  protected DelayedNotificationDelegate() {
    // Nothing to do
  }

  /*
   * New notification
   */

  /**
   * Easy call of new notification process
   * @param delayedNotificationData
   * @throws NotificationServerException
   */
  public static void executeNewNotification(final DelayedNotificationData delayedNotificationData)
      throws NotificationServerException {
    new DelayedNotificationDelegate().performNewNotificationSending(delayedNotificationData);
  }

  /**
   * Handling a new notification
   * @param delayedNotificationData
   * @throws NotificationServerException
   */
  protected void performNewNotificationSending(final DelayedNotificationData delayedNotificationData)
      throws NotificationServerException {
    if (!isThatToBeDelayed(delayedNotificationData)) {
      sendNotification(delayedNotificationData.getNotificationData());
    } else {
      DelayedNotificationFactory.getDelayedNotification().saveDelayedNotification(
          delayedNotificationData);
    }
  }

  /**
   * Checks if the notification has to be delayed or not
   * @param delayedNotificationData
   * @return
   */
  private boolean isThatToBeDelayed(final DelayedNotificationData delayedNotificationData) {

    // The notification action has to be defined
    if (delayedNotificationData.getAction() == null) {
      return false;
    }

    // The notification priority has to be different from URGENT or ERROR and the action type has to
    // be setted
    if (NotificationParameters.NORMAL != delayedNotificationData.getNotificationParameters().iMessagePriority) {
      return false;
    }

    // The user frequency has to be different from NONE
    if (DelayedNotificationFrequency.NONE.equals(getUserFrequency(delayedNotificationData
        .getUserId(), delayedNotificationData.getChannel()))) {
      return false;
    }

    // The last conditions
    return delayedNotificationData.isValid();
  }

  /*
   * Delayed notifications
   */

  /**
   * Easy call of delayed notifications process
   * @param date
   */
  public static void executeDelayedNotifications(final Date date) {
    new DelayedNotificationDelegate().performDelayedNotificationsSending(date);
  }

  /**
   * Forces the sending of all the saved delayed notifications
   * @param date
   */
  protected void forceDelayedNotificationsSending() {

    // Searching all the users from delayed notifications
    final List<Integer> usersToBeNotified =
        DelayedNotificationFactory.getDelayedNotification()
            .findAllUsersToBeNotified(WIRED_CHANNELS);

    // Performing all users to notify
    performUsersDelayedNotifications(usersToBeNotified);
  }

  /**
   * Handling the saved delayed notifications
   * @param date
   */
  protected void performDelayedNotificationsSending(final Date date) {

    // Searching all the users that have to be notify from a given date and given channels
    final List<Integer> usersToBeNotified =
        DelayedNotificationFactory.getDelayedNotification().findUsersToBeNotified(date,
            WIRED_CHANNELS, getDefaultDelayedNotificationFrequency());

    // Performing all users to notify
    performUsersDelayedNotifications(usersToBeNotified);
  }

  /**
   * Performing delayed notifications for given users
   * @param usersToBeNotified
   */
  private void performUsersDelayedNotifications(final List<Integer> usersToBeNotified) {

    // Stopping if no users to notify
    if (usersToBeNotified.isEmpty()) {
      return;
    }

    // Performing all users to notify
    Map<NotifChannel, List<DelayedNotificationData>> delayedNotifications;
    for (final Integer userIdToNotify : usersToBeNotified) {

      // Searching current user notifications, group by channels
      delayedNotifications =
          DelayedNotificationFactory.getDelayedNotification()
              .findDelayedNotificationByUserIdGroupByChannel(
                  userIdToNotify, WIRED_CHANNELS);

      // Browse channel notifications
      for (final Map.Entry<NotifChannel, List<DelayedNotificationData>> mapEntry : delayedNotifications
          .entrySet()) {
        performUserDelayedNotificationsOnChannel(mapEntry.getKey(), mapEntry.getValue());
      }
    }

    // Deleting massively notification resource data
    DelayedNotificationFactory.getDelayedNotification().deleteResources();
  }

  /**
   * Performing delayed notifications for a given user and a given channel
   * @param channel
   * @param delayedNotifications
   */
  private void performUserDelayedNotificationsOnChannel(final NotifChannel channel,
      final List<DelayedNotificationData> delayedNotifications) {
    // TODO
  }

  /*
   * Commons
   */

  /**
   * Gets the user notification frequency
   * @param userId
   * @return
   */
  private DelayedNotificationFrequency getUserFrequency(final Integer userId,
      final NotifChannel channel) {
    DelayedNotificationFrequency result = DelayedNotificationFrequency.NONE;

    // For now, only the SMTP channel can be delayed (mail)
    if (WIRED_CHANNELS.contains(channel)) {

      // Search in the database the user's setting
      final DelayedNotificationUserSetting dnus =
          (userId != null) ? DelayedNotificationFactory.getDelayedNotification()
              .getDelayedNotificationUserSettingByUserIdAndChannel(userId, channel) : null;

      // If no user setting data, the default frequency is retrieved
      if (dnus == null) {
        result = getDefaultDelayedNotificationFrequency();
      } else {
        result = dnus.getFrequency();
      }
    }

    return result;
  }

  /**
   * Gets the default frequency from the file of notification manager settings. If that not exists,
   * DelayedNotificationFrequency.NONE is returned.
   * @return
   */
  private DelayedNotificationFrequency getDefaultDelayedNotificationFrequency() {
    return DelayedNotificationFrequency.decode(settings
        .getString("DEFAULT_DELAYED_NOTIFICATION_FREQUENCY"));
  }

  /**
   * Centralizes notification sending
   * @param notificationData
   * @throws NotificationServerException
   */
  protected void sendNotification(final NotificationData notificationData)
      throws NotificationServerException {
    notificationServer.addNotification(notificationData);
  }
}
