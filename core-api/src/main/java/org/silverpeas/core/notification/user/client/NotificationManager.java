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
package org.silverpeas.core.notification.user.client;

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

  ArrayList<Properties> getNotificationAddresses(int aUserId) throws NotificationManagerException;

  Properties getNotificationAddress(int aNotificationAddressId, int aUserId)
      throws NotificationManagerException;

  ArrayList<Properties> getDefaultAddresses(int aUserId) throws NotificationManagerException;

  int getDefaultAddress(int aUserId) throws NotificationManagerException;

  ArrayList<Properties> getNotifPriorities();

  ArrayList<Properties> getNotifUsages();

  ArrayList<Properties> getNotifChannels() throws NotificationManagerException;

  ArrayList<Properties> getNotifPreferences(int aUserId) throws NotificationManagerException;

  Properties getNotifPreference(int aPrefId, int aUserId) throws NotificationManagerException;

  void setDefaultAddress(int aNotificationAddressId, int aUserId)
      throws NotificationManagerException;

  void addAddress(int aNotificationAddressId, int aUserId) throws NotificationManagerException;

  void savePreferences(int aUserId, int aInstanceId, int aMessageType, int aDestinationId)
      throws NotificationManagerException;

  void saveNotifAddress(int aNotificationAddressId, int aUserId, String aNotifName, int aChannelId,
      String aAddress, String aUsage) throws NotificationManagerException;

  void deletePreference(int aPreferenceId) throws NotificationManagerException;

  void deleteNotifAddress(int aNotificationAddressId) throws NotificationManagerException;

  void deleteAllAddress(int userId) throws NotificationManagerException;

  void testNotifAddress(int aNotificationAddressId, int aUserId)
      throws NotificationManagerException;

  void notifyUsers(NotificationParameters params, String[] userIds)
      throws NotificationManagerException;

  void notifyExternals(NotificationParameters params, Collection<ExternalRecipient> externals)
      throws NotificationManagerException;

  Collection<UserRecipient> getUsersFromGroup(String groupId) throws NotificationManagerException;

  String getComponentFullName(String compInst) throws NotificationManagerException;

  String getComponentFullName(String compInst, String separator, boolean isPathToComponent)
      throws NotificationManagerException;

  boolean isMultiChannelNotification();
}
