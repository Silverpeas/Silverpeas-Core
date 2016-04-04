/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.notification.user.client.model;

public class NotifAddressRow {
  private int id;
  private int userId;
  private String notifName;
  private int notifChannelId;
  private String address;
  private String usage;
  private int priority;

  public int getId() {
    return id;
  }

  public void setId(int aId) {
    id = aId;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int aUserId) {
    userId = aUserId;
  }

  public String getNotifName() {
    return notifName;
  }

  public void setNotifName(String aNotifName) {
    notifName = aNotifName;
  }

  public int getNotifChannelId() {
    return notifChannelId;
  }

  public void setNotifChannelId(int aNotifChannelId) {
    notifChannelId = aNotifChannelId;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String aAddress) {
    address = aAddress;
  }

  public String getUsage() {
    return usage;
  }

  public void setUsage(String aUsage) {
    usage = aUsage;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int aPriority) {
    priority = aPriority;
  }

  public NotifAddressRow(int aId, int aUserId, String aNotifName,
      int aNotifChannelId, String aAddress, String aUsage, int aPriority) {
    id = aId;
    userId = aUserId;
    notifName = aNotifName;
    notifChannelId = aNotifChannelId;
    address = aAddress;
    usage = aUsage;
    priority = aPriority;
  }
}
