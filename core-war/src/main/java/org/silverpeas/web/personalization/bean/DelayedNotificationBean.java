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
package org.silverpeas.web.personalization.bean;

import java.util.Set;

import org.silverpeas.core.notification.user.delayed.DelayedNotificationProvider;
import org.silverpeas.core.notification.user.delayed.constant.DelayedNotificationFrequency;
import org.silverpeas.core.notification.user.delayed.delegate.DelayedNotificationDelegate;
import org.silverpeas.core.notification.user.delayed.model.DelayedNotificationUserSetting;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.notification.user.client.constant.NotifChannel;

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
    return DelayedNotificationProvider.getDelayedNotification()
        .getDefaultDelayedNotificationFrequency();
  }

  public Set<DelayedNotificationFrequency> getFrequencies() {
    return DelayedNotificationProvider.getDelayedNotification().getPossibleFrequencies();
  }

  public String getCurrentUserFrequencyCode() {
    if (userSettings == null) {
      userSettings =
          DelayedNotificationProvider.getDelayedNotification()
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
