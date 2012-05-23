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

import java.util.Collection;
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
import com.silverpeas.util.CollectionUtil;
import com.silverpeas.util.MapUtil;
import com.stratelia.silverpeas.notificationManager.constant.NotifChannel;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 * @author Yohann Chastagnier
 */
@Service
@Transactional
public class DelayedNotificationManager implements DelayedNotification {

  /** Settings location */
  private final ResourceLocator settings = new ResourceLocator(
      "com.stratelia.silverpeas.notificationManager.settings.notificationManagerSettings", "");

  /** For now, only the SMTP channel can be delayed (mail) */
  private static final Set<NotifChannel> WIRED_CHANNELS = new HashSet<NotifChannel>();
  static {
    WIRED_CHANNELS.add(NotifChannel.SMTP);
  }

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
    if (delayedNotificationData.getResource().getId() == null) {
      final List<NotificationResourceData> resources =
          findResource(delayedNotificationData.getResource());
      if (resources.size() == 1) {
        delayedNotificationData.setResource(resources.get(0));
        if (delayedNotificationData.getId() == null) {
          final List<DelayedNotificationData> exists =
              dnRepository.findDelayedNotification(delayedNotificationData);
          if (exists.size() == 1) {
            delayedNotificationData.setId(exists.get(0).getId());
          } else {
            dnRepository.saveAndFlush(delayedNotificationData);
          }
        } else {
          dnRepository.saveAndFlush(delayedNotificationData);
        }
      } else {
        nrRepository.save(delayedNotificationData.getResource());
        dnRepository.saveAndFlush(delayedNotificationData);
      }
    } else {
      dnRepository.saveAndFlush(delayedNotificationData);
    }
  }

  @Override
  public int deleteDelayedNotifications(final Collection<Integer> ids) {
    int nbDeletes = 0;
    if (CollectionUtil.isNotEmpty(ids)) {
      for (final Collection<Integer> idLot : CollectionUtil.split(ids)) {
        nbDeletes += dnRepository.deleteByIds(idLot);
      }
      nrRepository.deleteResources();
      dnRepository.flush();
    }
    return nbDeletes;
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
  public DelayedNotificationUserSetting saveDelayedNotificationUserSetting(final int userId,
      final NotifChannel channel, final DelayedNotificationFrequency frequency) {
    DelayedNotificationUserSetting userSettings =
        getDelayedNotificationUserSettingByUserIdAndChannel(userId, channel);
    if (userSettings == null) {
      userSettings = new DelayedNotificationUserSetting();
      userSettings.setUserId(userId);
      userSettings.setChannel(channel);
    }
    userSettings.setFrequency(frequency);
    dnUserSettingRepository.saveAndFlush(userSettings);
    return userSettings;
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

  /*
   * Commons
   */

  @Override
  public Set<NotifChannel> getWiredChannels() {
    return WIRED_CHANNELS;
  }

  @Override
  public DelayedNotificationFrequency getDefaultDelayedNotificationFrequency() {
    return DelayedNotificationFrequency.decode(settings.getString(
        "DEFAULT_DELAYED_NOTIFICATION_FREQUENCY"));
  }

  @Override
  public DelayedNotificationFrequency getUserFrequency(final Integer userId,
      final NotifChannel channel) {
    DelayedNotificationFrequency result = DelayedNotificationFrequency.NONE;

    // For now, only the SMTP channel can be delayed (mail)
    if (getWiredChannels().contains(channel)) {

      // Search in the database the user's setting
      final DelayedNotificationUserSetting dnus =
          (userId != null) ? getDelayedNotificationUserSettingByUserIdAndChannel(userId, channel)
              : null;

      // If no user setting data, the default frequency is retrieved
      if (dnus == null) {
        result = getDefaultDelayedNotificationFrequency();
      } else {
        result = dnus.getFrequency();
      }
    }

    return result;
  }
}
