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
package com.stratelia.silverpeas.personalizationPeas.bean;

import java.util.Set;

import com.silverpeas.notification.delayed.DelayedNotificationFactory;
import com.silverpeas.notification.delayed.constant.DelayedNotificationFrequency;
import com.silverpeas.notification.delayed.delegate.DelayedNotificationDelegate;
import com.silverpeas.notification.delayed.model.DelayedNotificationUserSetting;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.notificationManager.constant.NotifChannel;

/**
 * @author Yohann Chastagnier
 */
public class DelayedNotificationBean {

  private final Integer userId;
  private DelayedNotificationUserSetting userSettings = null;

  public DelayedNotificationBean(final String userId) {
    this(Integer.valueOf(userId));
  }

  public DelayedNotificationBean(final Integer userId) {
    this.userId = userId;
  }

  public DelayedNotificationFrequency getDefaultFrequency() {
    return DelayedNotificationFactory.getDelayedNotification()
        .getDefaultDelayedNotificationFrequency();
  }

  public Set<DelayedNotificationFrequency> getFrequencies() {
    return DelayedNotificationFactory.getDelayedNotification().getPossibleFrequencies();
  }

  public String getCurrentUserFrequencyCode() {
    if (userSettings == null) {
      userSettings =
          DelayedNotificationFactory.getDelayedNotification()
              .getDelayedNotificationUserSettingByUserIdAndChannel(userId, NotifChannel.SMTP);
    }
    if (userSettings != null) {
      return userSettings.getFrequency().getCode();
    }
    return null;
  }

  public void saveFrequency(final String frequencyCode) throws Exception {
    DelayedNotificationFrequency newUserFrequency = null;
    if (StringUtil.isDefined(frequencyCode)) {
      newUserFrequency = DelayedNotificationFrequency.decode(frequencyCode);
    }
    userSettings =
        DelayedNotificationDelegate.executeUserSettingsUpdating(userId, NotifChannel.SMTP,
            newUserFrequency);
  }
}
