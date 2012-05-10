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
package com.silverpeas.notification.delayed;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.silverpeas.notification.delayed.constant.DelayedNotificationFrequency;
import com.silverpeas.notification.delayed.model.DelayedNotificationData;
import com.silverpeas.notification.delayed.model.DelayedNotificationUserSetting;
import com.silverpeas.notification.model.NotificationResourceData;
import com.stratelia.silverpeas.notificationManager.constant.NotifChannel;

/**
 * @author Yohann Chastagnier
 */
public interface DelayedNotification {

  public List<Integer> findAllUsersToBeNotified(Set<NotifChannel> aimedChannels);

  public List<Integer> findUsersToBeNotified(Date date, Set<NotifChannel> aimedChannels,
      DelayedNotificationFrequency defaultDelayedNotificationFrequency);

  public Map<NotifChannel, List<DelayedNotificationData>> findDelayedNotificationByUserIdGroupByChannel(
      int userId, Set<NotifChannel> aimedChannels);

  public void saveDelayedNotification(DelayedNotificationData delayedNotificationData);

  public void deleteDelayedNotification(int id);

  public void deleteDelayedNotification(DelayedNotificationData delayedNotificationData);

  /*
   * Resource Data
   */

  public List<NotificationResourceData> findResource(
      NotificationResourceData notificationResourceData);

  public int deleteResources();

  /*
   * User settings
   */

  public DelayedNotificationUserSetting getDelayedNotificationUserSetting(int id);

  public List<DelayedNotificationUserSetting> findDelayedNotificationUserSettingByUserId(
      int userId);

  public DelayedNotificationUserSetting getDelayedNotificationUserSettingByUserIdAndChannel(
      int userId, NotifChannel channel);

  public void saveDelayedNotificationUserSetting(
      DelayedNotificationUserSetting delayedNotificationUserSetting);

  public void deleteDelayedNotificationUserSetting(int id);

  public void deleteDelayedNotificationUserSetting(
      DelayedNotificationUserSetting delayedNotificationUserSetting);
}
