/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
package org.silverpeas.core.notification.user.client;

import org.silverpeas.core.notification.NotificationException;
import org.silverpeas.core.util.ServiceProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

/**
 * A manager of notification against users. It provides all the plumb to make, set and send
 * the notifications. It is not dedicated to be used directly but by the implementation of the
 * {@link org.silverpeas.core.notification.user.UserNotification} interface.
 * @author mmoquillon
 */
public interface NotificationManager {

  static NotificationManager get() {
    return ServiceProvider.getService(NotificationManager.class);
  }

  NotificationManager forLanguage(String language);

  ArrayList<Properties> getNotificationAddresses(int aUserId) throws NotificationException;

  Properties getNotificationAddress(int aNotificationAddressId, int aUserId)
      throws NotificationException;

  ArrayList<Properties> getDefaultAddresses(int aUserId) throws NotificationException;

  int getDefaultAddress(int aUserId) throws NotificationException;

  ArrayList<Properties> getNotifPriorities();

  ArrayList<Properties> getNotifUsages();

  ArrayList<Properties> getNotifChannels() throws NotificationException;

  ArrayList<Properties> getNotifPreferences(int aUserId) throws NotificationException;

  Properties getNotifPreference(int aPrefId, int aUserId) throws NotificationException;

  void setDefaultAddress(int aNotificationAddressId, int aUserId)
      throws NotificationException;

  void addAddress(int aNotificationAddressId, int aUserId) throws NotificationException;

  void savePreferences(int aUserId, int aInstanceId, int aMessageType, int aDestinationId)
      throws NotificationException;

  void saveNotifAddress(int aNotificationAddressId, int aUserId, String aNotifName, int aChannelId,
      String aAddress, String aUsage) throws NotificationException;

  void deletePreference(int aPreferenceId) throws NotificationException;

  void deleteNotifAddress(int aNotificationAddressId) throws NotificationException;

  void deleteAllAddress(int userId) throws NotificationException;

  void testNotifAddress(int aNotificationAddressId, int aUserId)
      throws NotificationException;

  void notifyUsers(NotificationParameters params, String[] userIds)
      throws NotificationException;

  void notifyExternals(NotificationParameters params, Collection<ExternalRecipient> externals)
      throws NotificationException;

  Collection<UserRecipient> getUsersFromGroup(String groupId) throws NotificationException;

  String getComponentFullName(String compInst) throws NotificationException;

  String getComponentFullName(String compInst, String separator, boolean isPathToComponent)
      throws NotificationException;

  boolean isMultiChannelNotification();
}
