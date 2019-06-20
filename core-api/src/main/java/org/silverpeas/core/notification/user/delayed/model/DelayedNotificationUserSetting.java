/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.notification.user.delayed.model;

import org.silverpeas.core.notification.user.client.constant.NotifChannel;
import org.silverpeas.core.notification.user.delayed.constant.DelayedNotificationFrequency;
import org.silverpeas.core.persistence.datasource.model.identifier.UniqueIntegerIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.BasicJpaEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * The user settings about the delayed notifications. They are about the frequency at which the
 * delayed notifications have to be sent to the user as well as the notification channel to use.
 * @author Yohann Chastagnier
 */
@Entity
@Table(name = "st_delayednotifusersetting")
@NamedQueries({
    @NamedQuery(name = "DelayedNotificationUserSetting.findByUserId",
      query = "SELECT d FROM DelayedNotificationUserSetting d WHERE userId = :userId"),
    @NamedQuery(name = "DelayedNotificationUserSetting.findByUserIdAndChannel",
      query = "SELECT d FROM DelayedNotificationUserSetting d WHERE userId = :userId and channel = :channel")})
public class DelayedNotificationUserSetting
    extends BasicJpaEntity<DelayedNotificationUserSetting, UniqueIntegerIdentifier>
    implements Serializable {
  private static final long serialVersionUID = 3477090528448919931L;

  @Column(name = "userId", nullable = false)
  private Integer userId;

  @Column(name = "channel", nullable = false)
  private Integer channel;

  @Column(name = "frequency", nullable = false)
  private String frequency;

  /**
   * Simple constructor
   */
  public DelayedNotificationUserSetting() {
    // NTD
  }

  public DelayedNotificationUserSetting(final int userId, final NotifChannel channelId,
      final DelayedNotificationFrequency frequency) {
    setUserId(userId);
    setChannel(channelId);
    setFrequency(frequency);
  }

  public Integer getUserId() {
    return userId;
  }

  public void setUserId(Integer userId) {
    this.userId = userId;
  }

  public NotifChannel getChannel() {
    return NotifChannel.decode(channel).orElse(null);
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

}
