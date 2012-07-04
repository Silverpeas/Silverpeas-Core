/**
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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.stratelia.silverpeas.notificationManager.model;

public class NotifPreferenceRow {
  private int id;
  private int notifAddressId;
  private int componentInstanceId;
  private int userId;
  private int messageType;

  public int getId() {
    return id;
  }

  public void setId(int aId) {
    id = aId;
  }

  public int getNotifAddressId() {
    return notifAddressId;
  }

  public void setNotifAddressId(int aNotifAddressId) {
    notifAddressId = aNotifAddressId;
  }

  public int getComponentInstanceId() {
    return componentInstanceId;
  }

  public void setComponentInstanceId(int aComponentInstanceId) {
    componentInstanceId = aComponentInstanceId;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int aUserId) {
    userId = aUserId;
  }

  public int getMessageType() {
    return messageType;
  }

  public void setMessageType(int aMessageType) {
    messageType = aMessageType;
  }

  public NotifPreferenceRow(int aId, int aNotifAddressId,
      int aComponentInstanceId, int aUserId, int aMessageType) {
    id = aId;
    notifAddressId = aNotifAddressId;
    componentInstanceId = aComponentInstanceId;
    userId = aUserId;
    messageType = aMessageType;
  }
}
