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
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.silverpeas.notification.delayed.DelayedNotificationFactory;
import com.silverpeas.notification.model.NotificationResourceData;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.constant.NotifAction;
import com.stratelia.silverpeas.notificationManager.constant.NotifChannel;
import com.stratelia.silverpeas.notificationserver.NotificationData;

/**
 * @author Yohann Chastagnier
 */
@Entity
@Table(name = "sp_delayednotification")
public class DelayedNotificationData implements Serializable {
  private static final long serialVersionUID = 3477090528448919931L;

  @Id
  @TableGenerator(name = "UNIQUE_ID_GEN", table = "uniqueId", pkColumnName = "tablename",
      valueColumnName = "maxId", pkColumnValue = "sp_delayednotification", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "UNIQUE_ID_GEN")
  @Column(name = "id")
  private Integer id;

  @Column(name = "userId", nullable = false)
  private Integer userId;

  @Column(name = "fromUserId", nullable = false)
  private Integer fromUserId;

  @Column(name = "channel", nullable = false)
  private Integer channel;

  @Column(name = "action", nullable = false)
  private Integer action;

  @Column(name = "componentInstanceId", nullable = false)
  private int componentInstanceId = -1;

  @Column(name = "language", nullable = false)
  private String language;

  @Column(name = "creationDate", nullable = false)
  @Temporal(value = TemporalType.TIMESTAMP)
  private Date creationDate;

  @Column(name = "message", nullable = true)
  private String message;

  @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
  @JoinColumn(name = "notificationResourceId", referencedColumnName = "id", nullable = false)
  private NotificationResourceData resource;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumns({
      @JoinColumn(name = "userId", referencedColumnName = "userId", insertable = false, updatable = false),
      @JoinColumn(name = "channel", referencedColumnName = "channel", insertable = false, updatable = false) })
  private DelayedNotificationUserSetting delayedNotificationUserSetting;

  @Transient
  private NotificationData notificationData;

  @Transient
  private NotificationParameters notificationParameters;

  /**
   * Simple constructor
   */
  public DelayedNotificationData() {
    // NTD
  }

  /**
   * Checks the data integrity
   * @return
   */
  public boolean isValid() {
    return getUserId() != null && getFromUserId() != null && getChannel() != null &&
        getAction() != null && isDefined(getLanguage()) && getResource() != null &&
        getResource().isValid();
  }

  private static boolean isDefined(final String string) {
    return string != null && StringUtil.isDefined(string.trim());
  }

  @PrePersist
  public void beforePersist() {
    loadResourceIfExists();
    creationDate = new Date();
    if (!isDefined(language)) {
      language = I18NHelper.defaultLanguage;
    }
  }

  @PreUpdate
  public void beforeUpdate() {
    loadResourceIfExists();
  }

  /**
   * Centralizes the resource loading before save
   */
  private void loadResourceIfExists() {
    if (getResource() != null && getResource().getId() == null) {
      final List<NotificationResourceData> resources =
          DelayedNotificationFactory.getDelayedNotification().findResource(getResource());
      if (resources.size() == 1) {
        setResource(resources.get(0));
      }
    }
  }

  public Integer getId() {
    return id;
  }

  public void setId(final Integer id) {
    this.id = id;
  }

  public Integer getUserId() {
    return userId;
  }

  public void setUserId(final Integer userId) {
    this.userId = userId;
  }

  public void setUserId(final String userId) {
    this.userId = new Integer(userId);
  }

  public Integer getFromUserId() {
    return fromUserId;
  }

  public void setFromUserId(final Integer fromUserId) {
    this.fromUserId = fromUserId;
  }

  public NotifChannel getChannel() {
    return NotifChannel.decode(channel);
  }

  public void setChannel(final NotifChannel channel) {
    if (channel != null) {
      this.channel = channel.getId();
    } else {
      this.channel = null;
    }
  }

  public NotifAction getAction() {
    return NotifAction.decode(action);
  }

  public void setAction(final NotifAction action) {
    if (action != null) {
      this.action = action.getId();
    } else {
      this.action = null;
    }
  }

  public int getComponentInstanceId() {
    return componentInstanceId;
  }

  public void setComponentInstanceId(final int componentInstanceId) {
    this.componentInstanceId = componentInstanceId;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(final Date creationDate) {
    this.creationDate = creationDate;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(final String language) {
    this.language = language;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(final String message) {
    this.message = message;
  }

  public NotificationResourceData getResource() {
    return resource;
  }

  public void setResource(final NotificationResourceData resource) {
    this.resource = resource;
  }

  public DelayedNotificationUserSetting getDelayedNotificationUserSetting() {
    return delayedNotificationUserSetting;
  }

  public void setDelayedNotificationUserSetting(
      final DelayedNotificationUserSetting delayedNotificationUserSetting) {
    this.delayedNotificationUserSetting = delayedNotificationUserSetting;
  }

  public NotificationData getNotificationData() {
    return notificationData;
  }

  public void setNotificationData(final NotificationData notificationData) {
    this.notificationData = notificationData;
  }

  public NotificationParameters getNotificationParameters() {
    return notificationParameters;
  }

  public void setNotificationParameters(final NotificationParameters notificationParameters) {
    this.notificationParameters = notificationParameters;
  }
}
