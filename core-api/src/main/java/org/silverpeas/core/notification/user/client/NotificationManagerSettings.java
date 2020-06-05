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

import org.apache.commons.lang3.StringUtils;
import org.silverpeas.core.notification.sse.ServerEvent;
import org.silverpeas.core.notification.user.client.constant.NotifChannel;
import org.silverpeas.core.notification.user.delayed.constant.DelayedNotificationFrequency;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Centralizing the access to the settings associated to the management of notifications.
 * @author Yohann Chastagnier
 */
public class NotificationManagerSettings {

  private static final int DEFAULT_SSE_JOB_TRIGGER = 45;
  private static final int DEFAULT_SSE_ASYNC_TIMEOUT = 180;
  private static final int DEFAULT_SSE_STORE_EVENT_LIFETIME = 40;
  private static final int MS = 1000;
  private static SettingBundle settings = ResourceLocator.getSettingBundle(
      "org.silverpeas.notificationManager.settings.notificationManagerSettings");

  private static SettingBundle silvermailIconsSettings = ResourceLocator.getSettingBundle(
      "org.silverpeas.notificationserver.channel.silvermail.settings.silvermailIcons");

  /**
   * Hidden constructor.
   */
  private NotificationManagerSettings() {
  }

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
  static List<NotifChannel> getDefaultChannels() {
    final String defaultChannelSetting = settings.getString("notif.defaultChannels", "");
    final boolean isMultiChannelSupported = isMultiChannelNotificationEnabled();
    final String[] defaultChannels = defaultChannelSetting.replaceAll("[ ]{2,}", " ").split(" ");
    final List<NotifChannel> channels;
    final Stream<NotifChannel> streamOfChannels = Stream.of(defaultChannels)
        .map(NotifChannel::decode)
        .filter(Optional::isPresent)
        .map(Optional::get);
    if (!isMultiChannelSupported) {
      channels = new ArrayList<>(1);
      channels.add(streamOfChannels.findFirst().orElse(NotifChannel.SMTP));
    } else {
      channels = streamOfChannels.distinct().collect(Collectors.toList());
    }
    if (channels.isEmpty()) {
      channels.add(NotifChannel.SMTP);
    }
    return channels;
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

  /**
   * Gets the trigger of SSE communication jobs.
   * @return the timeout as long (seconds).
   */
  public static int getSseAsyncJobTrigger() {
    return settings.getInteger("notification.sse.job.trigger", DEFAULT_SSE_JOB_TRIGGER);
  }

  /**
   * Gets the timeout of asynchronous context cleanup of SSE communication.
   * @return the timeout as long (milliseconds).
   */
  public static int getSseAsyncTimeout() {
    return settings.getInteger("notification.sse.async.timeout", DEFAULT_SSE_ASYNC_TIMEOUT) * MS;
  }

  /**
   * Gets the number of thread used to perform the send of a server event.
   * @return the maximum number of thread for send thread pool.
   */
  public static int getSseSendMaxThreadPool() {
    return settings.getInteger("notification.sse.send.thread.pool.max", 8);
  }


  /**
   * Gets The lifetime of an event stored into memory of SSE communication.
   * @return the timeout as long (milliseconds).
   */
  public static int getSseStoreEventLifeTime() {
    return settings
        .getInteger("notification.sse.store.event.lifetime", DEFAULT_SSE_STORE_EVENT_LIFETIME) * MS;
  }


  /**
   * Indicates if the server event feature is enabled.
   * @return true in order to enable, false otherwise.
   */
  public static boolean isSseEnabled() {
    return settings.getBoolean("notification.sse.enabled", true);
  }


  /**
   * Indicates if the server event has to be handled.
   * @return true in order to handle, false otherwise.
   */
  public static boolean isSseEnabledFor(final ServerEvent serverEvent) {
    return isSseEnabled() && settings
        .getBoolean("notification.sse.event." + serverEvent.getName().asString() + ".enabled",
            true);
  }

  /**
   * Gets the icon url of desktop user notification.
   * @return url as string without the application context.
   */
  public static String getUserNotificationDesktopIconUrl() {
    return silvermailIconsSettings
        .getString("silvermail.desktop.url", "/util/icons/desktop-user-notification.png");
  }

  /**
   * Is the space label should be set in the source of a notification when this property isn't set
   * explicitly.
   * @return true if the space label should be set in the notification source. False otherwise.
   */
  public static boolean isSpaceLabelInNotificationSource() {
    return settings.getBoolean("notification.source.spaceLabel");
  }

  /**
   * Is the component instance label should be set in the source of a notification when this
   * property isn't set explicitly.
   * @return true if the component instance label should be set in the notification source. False
   * otherwise.
   */
  public static boolean isComponentInstanceLabelInNotificationSource() {
    return settings.getBoolean("notification.source.componentLabel");
  }

}
