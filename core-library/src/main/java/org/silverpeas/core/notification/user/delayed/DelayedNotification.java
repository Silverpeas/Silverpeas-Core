/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.core.notification.user.delayed;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.silverpeas.core.notification.user.delayed.constant.DelayedNotificationFrequency;
import org.silverpeas.core.notification.user.delayed.model.DelayedNotificationData;
import org.silverpeas.core.notification.user.delayed.model.DelayedNotificationUserSetting;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.notification.user.client.constant.NotifChannel;

/**
 * @author Yohann Chastagnier
 */
public interface DelayedNotification {

  /*
   * Delayed Notification
   */

  List<Integer> findAllUsersToBeNotified(Set<NotifChannel> aimedChannels);

  List<Integer> findUsersToBeNotified(Date date, Set<NotifChannel> aimedChannels,
      DelayedNotificationFrequency defaultDelayedNotificationFrequency);

  Map<NotifChannel, List<DelayedNotificationData>> findDelayedNotificationByUserIdGroupByChannel(int userId,
      Set<NotifChannel> aimedChannels);

  void saveDelayedNotification(DelayedNotificationData delayedNotificationData);

  int deleteDelayedNotifications(Collection<Long> ids);

  /*
   * Resource Data
   */

  NotificationResourceData getExistingResource(String resourceId, String resourceType, String componentInstanceId);

  /*
   * User settings
   */

  DelayedNotificationUserSetting getDelayedNotificationUserSetting(int id);

  List<DelayedNotificationUserSetting> findDelayedNotificationUserSettingByUserId(int userId);

  DelayedNotificationUserSetting getDelayedNotificationUserSettingByUserIdAndChannel(int userId, NotifChannel channel);

  DelayedNotificationUserSetting saveDelayedNotificationUserSetting(final int userId, final NotifChannel channel,
      final DelayedNotificationFrequency frequency);

  void deleteDelayedNotificationUserSetting(int id);

  void deleteDelayedNotificationUserSetting(DelayedNotificationUserSetting delayedNotificationUserSetting);

  /*
   * Commons
   */

  /**
   * For now, only the SMTP channel can be delayed (mail)
   * @return
   */
  Set<NotifChannel> getWiredChannels();

  /**
   * Gets the possible frequencies. At least, the default frequency.
   * @return
   */
  Set<DelayedNotificationFrequency> getPossibleFrequencies();

  /**
   * Gets the default frequency from the file of notification manager settings. If that not exists,
   * DelayedNotificationFrequency.NONE is returned.
   * @return
   */
  DelayedNotificationFrequency getDefaultDelayedNotificationFrequency();

  /**
   * Gets the user notification frequency
   * @param userId
   * @return
   */
  DelayedNotificationFrequency getUserFrequency(final Integer userId, final NotifChannel channel);
}
