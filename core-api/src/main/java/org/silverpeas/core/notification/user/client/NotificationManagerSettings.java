/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

import org.silverpeas.core.notification.user.delayed.constant.DelayedNotificationFrequency;
import org.apache.commons.lang3.StringUtils;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 * Centralizing the access to the settings associated to the management of notifications.
 * @author Yohann Chastagnier
 */
public class NotificationManagerSettings {

  private static SettingBundle settings = ResourceLocator.getSettingBundle(
      "org.silverpeas.notificationManager.settings.notificationManagerSettings");

  /**
   * Gets the default delayed notification frequency of the server.
   * @return the default delayed notification frequency of the server.
   */
  public static DelayedNotificationFrequency getDefaultDelayedNotificationFrequency() {
    DelayedNotificationFrequency defaultFrequency = DelayedNotificationFrequency
        .decode(settings.getString("DEFAULT_DELAYED_NOTIFICATION_FREQUENCY", null));
    if (defaultFrequency == null) {
      defaultFrequency = DelayedNotificationFrequency.NONE;
    }
    return defaultFrequency;
  }

  /**
   * The ordered frequency choice set associated to the delayed notification mechanism.
   * @return the ordered set of available frequencies.
   */
  public static Set<DelayedNotificationFrequency> getDelayedNotificationFrequencyChoiceList() {

    // Initialization
    final Set<DelayedNotificationFrequency> possibleFrequencies =
        new HashSet<>();

    // The parameter value
    final String frequencyChoiceList =
        settings.getString("DELAYED_NOTIFICATION_FREQUENCY_CHOICE_LIST", "")
            .replaceAll("[ ]+", ",");

    // The possible frequencies
    if (StringUtils.isNotBlank(frequencyChoiceList)) {
      for (final String frequencyCode : frequencyChoiceList.split("[,;|]")) {
        if ("*".equals(frequencyCode)) {
          possibleFrequencies.clear();
          possibleFrequencies.addAll(Arrays.asList(DelayedNotificationFrequency.values()));
          break;
        } else {
          possibleFrequencies.add(DelayedNotificationFrequency.decode(frequencyCode));
        }
      }
    }

    // Eliminating wrong frequencies
    possibleFrequencies.remove(null);
    return new TreeSet<>(possibleFrequencies);
  }

  /**
   * Indicates if the limitation of maximum number of recipient is enabled.
   * @return true if enabled, false otherwise.
   */
  public static boolean isUserManualNotificationRecipientLimitEnabled() {
    return getUserManualNotificationRecipientLimit() > 0;
  }

  /**
   * Gets the maximum number of recipient for manual user notification.
   * @return the maximum number of recipient for manual user notification.
   */
  public static int getUserManualNotificationRecipientLimit() {
    return settings.getInteger("notif.manual.receiver.limit", 0);
  }

  /**
   * Gets the receiver threshold after that the user list is replaced by the group name into which
   * the user is associated.
   * @return an integer value, 0 = no threshold.
   */
  public static int getReceiverThresholdAfterThatReplaceUserNameListByGroupName() {
    return settings.getInteger("notif.receiver.displayUser.threshold", 0);
  }

  /**
   * Indicates if group name must be displayed instead of user names of group.
   * @return true if enabled, false otherwise.
   */
  public static boolean isDisplayingUserNameListInsteadOfGroupEnabled() {
    return settings.getBoolean("notif.receiver.displayGroup", false);
  }

  /**
   * Indicates if the feature of displaying receivers in the notification message is enabled.
   * @return true if enabled, false otherwise.
   */
  public static boolean isDisplayingReceiversInNotificationMessageEnabled() {
    return settings.getBoolean("addReceiversInBody", false);
  }

  /**
   * Gets the cron configuration of the delayed notification sending.
   * @return the cron configuration of the delayed notification sending.
   */
  public static String getCronOfDelayedNotificationSending() {
    return settings.getString("cronDelayedNotificationSending", "");
  }

  /**
   * Indicates if the multi channel for user notification is enabled.
   * @return true if enabled, false otherwise.
   */
  public static boolean isMultiChannelNotificationEnabled() {
    return settings.getBoolean("multiChannelNotification", false);
  }

  /**
   * Gets the addresses as default notification channels. If the multi channel isn't supported,
   * then
   * returns only one among the channels set up as default. In the case no default channels are set
   * up, then the previous behaviour is used; the SMTP is used as default channel.
   * @return a set of default notification channels.
   */
  public static List<Integer> getDefaultChannels() {
    String defaultChannels = settings.getString("notif.defaultChannels", "");
    boolean isMultiChannelSupported = isMultiChannelNotificationEnabled();
    StringTokenizer channelTokenizer =
        new StringTokenizer(defaultChannels.replaceAll("[ ]{2,}", " "), " ");
    List<Integer> mediaIds = new ArrayList<>(channelTokenizer.countTokens() + 1);
    while (channelTokenizer.hasMoreTokens()) {
      String channel = channelTokenizer.nextToken();
      if ("BASIC_POPUP".equalsIgnoreCase(channel) &&
          !mediaIds.contains(NotificationParameters.ADDRESS_BASIC_POPUP)) {
        mediaIds.add(NotificationParameters.ADDRESS_BASIC_POPUP);
      } else if ("BASIC_REMOVE".equalsIgnoreCase(channel) &&
          !mediaIds.contains(NotificationParameters.ADDRESS_BASIC_REMOVE)) {
        mediaIds.add(NotificationParameters.ADDRESS_BASIC_REMOVE);
      } else if ("BASIC_SILVERMAIL".equalsIgnoreCase(channel) &&
          !mediaIds.contains(NotificationParameters.ADDRESS_BASIC_SILVERMAIL)) {
        mediaIds.add(NotificationParameters.ADDRESS_BASIC_SILVERMAIL);
      } else if ("BASIC_SMTP_MAIL".equalsIgnoreCase(channel) &&
          !mediaIds.contains(NotificationParameters.ADDRESS_BASIC_SMTP_MAIL)) {
        mediaIds.add(NotificationParameters.ADDRESS_BASIC_SMTP_MAIL);
      } else if ("BASIC_SERVER".equalsIgnoreCase(channel) &&
          !mediaIds.contains(NotificationParameters.ADDRESS_BASIC_SERVER)) {
        mediaIds.add(NotificationParameters.ADDRESS_BASIC_SERVER);
      } else if ("BASIC_COMMUNICATION_USER".equalsIgnoreCase(channel) &&
          !mediaIds.contains(NotificationParameters.ADDRESS_BASIC_COMMUNICATION_USER)) {
        mediaIds.add(NotificationParameters.ADDRESS_BASIC_COMMUNICATION_USER);
      }
      if (!(isMultiChannelSupported || mediaIds.isEmpty())) {
        break;
      }
    }
    if (mediaIds.isEmpty()) {
      mediaIds.add(NotificationParameters.ADDRESS_BASIC_SMTP_MAIL);
    }
    return mediaIds;
  }

  /**
   * Indicates if the sender must be removed from the list of receivers of a subscription
   * notification.
   * @return true if enabled, false otherwise.
   */
  public static boolean isRemoveSenderFromSubscriptionNotificationReceiversEnabled() {
    return settings.getBoolean("notification.subscription.removeSenderFromReceivers.enabled", true);
  }

  /**
   * Indicates if the the confirmation of subscription notification is enabled.
   * @return true if enabled (default value), false otherwise.
   */
  public static boolean isSubscriptionNotificationConfirmationEnabled() {
    return settings.getBoolean("notification.subscription.confirmation.enabled", true);

  }
}
