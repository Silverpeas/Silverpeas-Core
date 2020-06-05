/*
 * Copyright (C) 2000 - 2020 Silverpeas
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

import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * A manager of notification against users. It provides all the plumb to make, set and send
 * the notifications. It is not dedicated to be used directly but by the implementation of the
 * {@link org.silverpeas.core.notification.user.UserNotification} interface.
 * @author mmoquillon
 */
public interface NotificationManager {

  static NotificationManager get() {
    return ServiceProvider.getSingleton(NotificationManager.class);
  }

  NotificationManager forLanguage(final String language);

  /**
   * Gets all the properties about the different notification addresses that were set for the
   * specified user. The notification addresses are made up of both the built-in notification
   * addresses and those that were created by the user himself.
   * @throws NotificationException if an error occurs while building the properties the
   * notification addresses.
   */
  List<Properties> getNotifAddressProperties(final String aUserId) throws NotificationException;

  /**
   * Gets the properties of the specified notification address of the given user.
   * @param addressId the unique identifier of a notification address. It can be either a built-in
   * address identifier or the identifier of a custom address set by the user.
   * @param aUserId the unique identifier of a user.
   * @return a {@link Properties} instance with the notification address properties.
   * @throws NotificationException if an error occurs while building the address properties.
   */
  Properties getNotifAddressProperties(final String addressId, final String aUserId)
      throws NotificationException;

  /**
   * Gets the default notification address of the specified user. If multi-channel is supported,
   * the user can have several default notification addresses. In that case, only the first one
   * is get.
   * @param aUserId the unique identifier of the user.
   * @return the unique identifier of a notification address. It can be either a built-in
   * address identifier of a notification channel or the identifier of a custom notification
   * address set by the user.
   * @throws NotificationException if an error occurs while getting a default address.
   */
  String getDefaultAddressId(final String aUserId) throws NotificationException;

  /**
   * Gets All the notification channels that are available in Silverpeas. A channel is the medium
   * through which notification messages are transmitted. For more information about the supported
   * channels, see {@link org.silverpeas.core.notification.user.client.constant.NotifChannel}.
   * @return a list of properties containing "id" and "name" keys for each channel.
   * @throws NotificationException if an error occurs while getting the supported channels.
   */
  List<Properties> getNotifChannels() throws NotificationException;

  /**
   * Gets the notifications preferences of the specified user.
   * @param aUserId the unique identifier of the user.
   * @return a list of properties containing "name", "type", "usage" and "address" keys for each
   * notification preference. The address identifies the unique identifier of a notification
   * address that can be the identifier of either a built-in notification address or a custom
   * notification address set by the user.
   * @throws NotificationException if an error occurs while getting the preferences of the user
   * about the notifications.
   */
  List<Properties> getNotifPreferences(final String aUserId) throws NotificationException;

  /**
   * Gets the properties about the specified preference of the given user on the notifications.
   * @param aPrefId a unique identifier of the preference.
   * @param aUserId a unique identifier of the user.
   * @return the properties containing "name", "type", "usage" and "address" keys of the preference.
   * @throws NotificationException if an error occurs while getting the given preference.
   */
  Properties getNotifPreference(final String aPrefId, final String aUserId)
      throws NotificationException;

  /**
   * Sets the specified notification address as the default one for the given user. If multi-channel
   * is disabled, the specified address will replace the previous one if any.
   * @param aNotificationAddressId the unique identifier of a notification address. It can be either
   * a built-in address identifier or a custom one set by the user.
   * @param aUserId the unique identifier of the user.
   * @throws NotificationException if an error occurs while setting the specified address as a
   * default one.
   */
  void setDefaultAddress(final String aNotificationAddressId, final String aUserId)
      throws NotificationException;

  /**
   * Sets the specified notification address as a default one for the given user identifier.
   * Whatever the multi-channel support, the specified address is added among the others default
   * ones of the user.
   * @param aNotificationAddressId the unique identifier of an address.
   * @param aUserId the unique identifier of a user.
   * @throws NotificationException if an error occurs while adding the setting the address as a
   * new default one for the user.
   */
  void addDefaultAddress(final String aNotificationAddressId, final String aUserId)
      throws NotificationException;

  /**
   * Saves the preference on notification of the given user and for the specified component
   * instance and for the specified type of message.
   * @param aUserId the unique identifier of a user.
   * @param instanceLocalId the local identifier of a component instance.
   * @param aMessageType the type of message.
   * @param notifAddressId the unique identifier of a notification address.
   * @throws NotificationException if an error occurs while saving the preferences.
   */
  void savePreferences(final String aUserId, final int instanceLocalId, final int aMessageType,
      final String notifAddressId) throws NotificationException;

  /**
   * Saves the specified custom notification address for the specified user. A custom address
   * is a notification address the user defines himself in order to be notified either through
   * another channel that those related to the built-in addresses or to another address that the
   * built-in ones.
   * @param notificationAddress a custom notification address to save.
   * @throws NotificationException if an error occurs while saving the specified custom address.
   */
  void saveNotifAddress(final NotificationAddress notificationAddress) throws NotificationException;

  /**
   * Deletes the specified preference. A preference is always related to a user and it is unique to
   * that user.
   * @param aPreferenceId the unique identifier of a preference.
   * @throws NotificationException if an error occurs while deleting the notification preference.
   */
  void deletePreference(final String aPreferenceId) throws NotificationException;

  /**
   * Deletes the specified custom notification address. If this address was set as a default one,
   * then it is replaced by the first channel that values the property
   * <code>notif.defaultChannels</code> in the <code>NotificationManagerSettings.properties</code>
   * properties file.
   * @param aNotificationAddressId the unique identifier of a custom notification address.
   * @throws NotificationException if an error occurs while deleting the notification address.
   */
  void deleteNotifAddress(final String aNotificationAddressId) throws NotificationException;

  /**
   * Deletes all the notification addresses set as default for the specified user.
   * @param userId the unique identifier of a user.
   * @throws NotificationException if an error occurs while deleting the addresses.
   */
  void deleteAllDefaultAddress(final String userId) throws NotificationException;

  /**
   * Tests the specified address for specified user by sending a notification message
   * through the channel related by this address. If the address is a built-in one, then the unique
   * identifier of the user is required to know at whom the address refers. Otherwise, for a custom
   * address, the address itself identifies the concerned user.
   * @param addressId the unique identifier of a notification address. It can be either a built-in
   * or a custom one defined by the user himself.
   * @param aUserId the unique identifier of a user.
   * @throws NotificationException if an error occurs while sending a test notification message.
   */
  void testNotifAddress(final String addressId, final String aUserId) throws NotificationException;

  /**
   * Notifies the specified users by using the given notification parameters.
   * @param params the parameters that describe among others the channel to use, the message to
   * send, and so on.
   * @param userIds a collection with the unique identifiers of the users to notify.
   * @throws NotificationException if an error occurs while sending the notification.
   */
  void notifyUsers(final NotificationParameters params, final Collection<String> userIds)
      throws NotificationException;

  /**
   * Notifies the specified external users by using the given notification parameters. Whatever
   * the channels defined in the parameters, only the SMTP channel is used to notify external
   * users.
   * @param params the parameters that carries among others the message to send, the subject of
   * the notification, and so on.
   * @param externals a collection of external recipients.
   * @throws NotificationException if an error occurs while sending the notification.
   */
  void notifyExternals(final NotificationParameters params,
      final Collection<ExternalRecipient> externals) throws NotificationException;

  /**
   * Gets all the notification recipients that are members of the specified user group.
   * The user that have not an activated state in Silverpeas is not taken into account, so this
   * kind of users is not included into the returned collection.
   * @param groupId the unique identifier of a user group in Silverpeas.
   * @return a collection of recipients.
   * @throws NotificationException if an error occurs while getting the recipients of the given
   * user group.
   */
  Collection<UserRecipient> getUsersFromGroup(final String groupId) throws NotificationException;

  /**
   * Gets the full name of a component instance. Such a full name is made up of the name of the
   * space that contains the component instance followed by the name of the component instance.
   * @param compInst the unique identifier of a component instance.
   * @return the full name of the given component instance: the space name followed by the
   * component name, separated by the minus character.
   * @throws NotificationException if an error occurs computing the component instance full name.
   */
  String getComponentFullName(String compInst) throws NotificationException;

  /**
   * Is the multi-channel option enabled? If true, then several notification addresses can be set
   * as the default ones for the users.
   * @return true if the multi-channel property is enabled. False otherwise.
   */
  boolean isMultiChannelNotification();
}
