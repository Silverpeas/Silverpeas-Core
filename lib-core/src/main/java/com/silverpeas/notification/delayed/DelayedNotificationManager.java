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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.icu.util.Calendar;
import com.silverpeas.notification.delayed.constant.DelayedNotificationFrequency;
import com.silverpeas.notification.delayed.model.DelayedNotificationData;
import com.silverpeas.notification.delayed.model.DelayedNotificationUserSetting;
import com.silverpeas.notification.delayed.repository.DelayedNotificationRepository;
import com.silverpeas.notification.delayed.repository.DelayedNotificationUserSettingRepository;
import com.silverpeas.notification.model.NotificationResourceData;
import com.silverpeas.notification.repository.NotificationResourceRepository;
import com.silverpeas.util.MapUtil;
import com.stratelia.silverpeas.notificationManager.constant.NotifChannel;
import com.stratelia.webactiv.util.DateUtil;

/**
 * @author Yohann Chastagnier
 */
@Service
@Transactional
public class DelayedNotificationManager implements DelayedNotification {

  @Inject
  private DelayedNotificationRepository dnRepository;

  @Inject
  private NotificationResourceRepository nrRepository;

  @Inject
  private DelayedNotificationUserSettingRepository dnUserSettingRepository;

  @Override
  @Transactional(readOnly = true)
  public Map<NotifChannel, List<DelayedNotificationData>> findDelayedNotificationByUserIdGroupByChannel(
      final int userId,
      final Set<NotifChannel> aimedChannels) {
    final Map<NotifChannel, List<DelayedNotificationData>> result =
        new LinkedHashMap<NotifChannel, List<DelayedNotificationData>>();
    for (final DelayedNotificationData data : dnRepository.findByUserId(userId,
        NotifChannel.toIds(aimedChannels))) {
      MapUtil.putAddList(result, data.getChannel(), data);
    }
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Integer> findAllUsersToBeNotified(final Set<NotifChannel> aimedChannels) {
    return dnRepository.findAllUsersToBeNotified(NotifChannel.toIds(aimedChannels));
  }

  /*
   * (non-Javadoc)
   * @see
   * com.silverpeas.notification.delayed.DelayedNotification#findUsersToBeNotified(java.util.Date,
   * java.util.Set, com.silverpeas.notification.delayed.constant.DelayedNotificationFrequency)
   */
  @Override
  @Transactional(readOnly = true)
  public List<Integer> findUsersToBeNotified(final Date date,
      final Set<NotifChannel> aimedChannels,
      final DelayedNotificationFrequency defaultDelayedNotificationFrequency) {

    final Date dateOfDay = DateUtil.getBeginOfDay(date);

    // Calculating aimed frequencies
    final Set<DelayedNotificationFrequency> aimedFrequencies =
        new HashSet<DelayedNotificationFrequency>();

    // Daily frequency is logically aimed
    aimedFrequencies.add(DelayedNotificationFrequency.DAILY);

    // Weekly frequency if the date is monday
    if (Calendar.MONDAY == DateUtil.getDayNumberInWeek(dateOfDay)) {
      aimedFrequencies.add(DelayedNotificationFrequency.WEEKLY);
    }

    // Monthly frequency if the date is the first of the month
    if (DateUtil.getFirstDateOfMonth(dateOfDay).compareTo(dateOfDay) == 0) {
      aimedFrequencies.add(DelayedNotificationFrequency.MONTHLY);
    }

    // Searching
    return dnRepository.findUsersToBeNotified(aimedChannels, aimedFrequencies,
        aimedFrequencies.contains(defaultDelayedNotificationFrequency));
  }

  @Override
  public void saveDelayedNotification(final DelayedNotificationData delayedNotificationData) {
    dnRepository.saveAndFlush(delayedNotificationData);
  }

  @Override
  public void deleteDelayedNotification(final int id) {
    dnRepository.delete(id);
    dnRepository.flush();
  }

  @Override
  public void deleteDelayedNotification(final DelayedNotificationData delayedNotificationData) {
    dnRepository.delete(delayedNotificationData);
    dnRepository.flush();
  }

  /*
   * Resource Data
   */

  @Override
  @Transactional(readOnly = true)
  public List<NotificationResourceData> findResource(
      final NotificationResourceData notificationResourceData) {
    return nrRepository.findResource(notificationResourceData);
  }

  @Override
  public int deleteResources() {
    final int nbDeletes = nrRepository.deleteResources();
    nrRepository.flush();
    return nbDeletes;
  }

  /*
   * User settings
   */

  @Override
  @Transactional(readOnly = true)
  public DelayedNotificationUserSetting getDelayedNotificationUserSetting(final int id) {
    return dnUserSettingRepository.findOne(id);
  }

  @Override
  @Transactional(readOnly = true)
  public List<DelayedNotificationUserSetting> findDelayedNotificationUserSettingByUserId(
      final int userId) {
    return dnUserSettingRepository.findByUserId(userId);
  }

  @Override
  public DelayedNotificationUserSetting getDelayedNotificationUserSettingByUserIdAndChannel(
      final int userId, final NotifChannel channel) {
    final List<DelayedNotificationUserSetting> userSettings =
        dnUserSettingRepository.findByUserIdAndChannel(userId, channel.getId());
    DelayedNotificationUserSetting result = null;
    if (!userSettings.isEmpty()) {
      result = userSettings.iterator().next();
    }
    return result;
  }

  @Override
  public void saveDelayedNotificationUserSetting(
      final DelayedNotificationUserSetting delayedNotificationUserSetting) {
    dnUserSettingRepository.saveAndFlush(delayedNotificationUserSetting);
  }

  @Override
  public void deleteDelayedNotificationUserSetting(final int id) {
    dnUserSettingRepository.delete(id);
    dnUserSettingRepository.flush();
  }

  @Override
  public void deleteDelayedNotificationUserSetting(
      final DelayedNotificationUserSetting delayedNotificationUserSetting) {
    dnUserSettingRepository.delete(delayedNotificationUserSetting);
    dnUserSettingRepository.flush();
  }
}
