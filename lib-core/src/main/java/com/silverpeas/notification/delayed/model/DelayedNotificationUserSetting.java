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
package com.silverpeas.notification.delayed.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import com.silverpeas.notification.delayed.constant.DelayedNotificationFrequency;
import com.stratelia.silverpeas.notificationManager.constant.NotifChannel;

/**
 * @author Yohann Chastagnier
 */
@Entity
@Table(name = "st_delayednotifusersetting")
public class DelayedNotificationUserSetting implements Serializable {
  private static final long serialVersionUID = 3477090528448919931L;

  @Id
  @TableGenerator(name = "UNIQUE_ID_GEN", table = "uniqueId", pkColumnName = "tablename",
      valueColumnName = "maxId", pkColumnValue = "st_delayednotifusersetting", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "UNIQUE_ID_GEN")
  @Column(name = "id")
  private Integer id;

  @Column(name = "userId", nullable = false)
  private Integer userId;

  @Column(name = "channel", nullable = false)
  private Integer channel;

  @Column(name = "frequency", nullable = false)
  private String frequency;

  @OneToMany(mappedBy = "delayedNotificationUserSetting", fetch = FetchType.LAZY)
  private List<DelayedNotificationData> delayedNotifications;

  /**
   * Simple constructor
   */
  public DelayedNotificationUserSetting() {
    // NTD
  }

  /**
   * Default constructor
   * @param userId
   * @param channelId
   * @param frequency
   */
  public DelayedNotificationUserSetting(final int userId, final NotifChannel channelId,
      final DelayedNotificationFrequency frequency) {
    setUserId(userId);
    setChannel(channelId);
    setFrequency(frequency);
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getUserId() {
    return userId;
  }

  public void setUserId(Integer userId) {
    this.userId = userId;
  }

  public NotifChannel getChannel() {
    return NotifChannel.decode(channel);
  }

  public void setChannel(NotifChannel channelId) {
    this.channel = channelId.getId();
  }

  public DelayedNotificationFrequency getFrequency() {
    return DelayedNotificationFrequency.decode(frequency);
  }

  public void setFrequency(DelayedNotificationFrequency frequency) {
    this.frequency = frequency.getCode();
  }

  public List<DelayedNotificationData> getDelayedNotifications() {
    return delayedNotifications;
  }

  public void setDelayedNotifications(List<DelayedNotificationData> delayedNotifications) {
    this.delayedNotifications = delayedNotifications;
  }
}
