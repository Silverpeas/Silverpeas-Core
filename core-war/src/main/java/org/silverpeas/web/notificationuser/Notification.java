/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.web.notificationuser;

import org.silverpeas.core.notification.NotificationException;
import org.silverpeas.core.notification.user.DefaultUserNotification;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.NotificationSender;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

/**
 * User notification defined in the Notification User personal component.
 */
public class Notification extends DefaultUserNotification {

  private final NotificationMetaData metaData;
  private String addressId;

  /**
   * Constructs an empty user notification.
   */
  public Notification() {
    metaData = new NotificationMetaData();
  }

  /**
   * Sets the priority of this notification.
   * @param priority the priority level.
   */
  public void setPriority(final int priority) {
    metaData.setMessageType(priority);
  }

  /**
   * Sets the source of this notification. The source is the component from which this notification
   * is sent.
   * @param source the name of the source of this notification.
   */
  public void setSource(final String source) {
    this.metaData.setSource(source);
  }

  @Override
  public NotificationMetaData getNotificationMetaData() {
    return metaData;
  }

  /**
   * Sets the identifier of a notification address to which this notification has to be sent.
   * @param addressId the unique identifier of a notification address.
   */
  public void setAddressId(final String addressId) {
    this.addressId = addressId;
  }

  @Override
  public void send() {
    try {
      final NotificationSender sender = new NotificationSender(metaData.getComponentId());
      if (StringUtil.isDefined(addressId) && StringUtil.isInteger(addressId)) {
        sender.notifyUser(Integer.parseInt(addressId), metaData);
      } else {
        sender.notifyUser(metaData);
      }
    } catch (final NotificationException e) {
      SilverLogger.getLogger(this).warn(e);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
  }

}
