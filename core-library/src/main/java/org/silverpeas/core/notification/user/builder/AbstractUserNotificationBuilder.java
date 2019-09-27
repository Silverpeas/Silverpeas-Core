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
package org.silverpeas.core.notification.user.builder;

import org.apache.commons.lang3.StringUtils;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.notification.user.NullUserNotification;
import org.silverpeas.core.notification.user.RemoveSenderRecipientBehavior;
import org.silverpeas.core.notification.user.UserNotification;
import org.silverpeas.core.notification.user.UserSubscriptionNotificationBehavior;
import org.silverpeas.core.notification.user.client.ExternalRecipient;
import org.silverpeas.core.notification.user.client.GroupRecipient;
import org.silverpeas.core.notification.user.client.NotificationManagerSettings;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.UserRecipient;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.notification.user.client.constant.NotifMessageType;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.Collection;
import java.util.Collections;

import static org.silverpeas.core.notification.user.UserSubscriptionNotificationSendingHandler.getSubscriptionNotificationUserNoteFromCurrentRequest;
import static org.silverpeas.core.notification.user.UserSubscriptionNotificationSendingHandler.isSubscriptionNotificationEnabledForCurrentRequest;
import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * Abstract implementation of the {@link UserNotificationBuilder} in which common code to build
 * a user notification is already defined.
 * It is recommended to extend this class instead of the {@link UserNotificationBuilder}
 * interface.
 * @author Yohann Chastagnier
 */
public abstract class AbstractUserNotificationBuilder implements UserNotificationBuilder {

  private String title = null;
  private String content = null;
  private UserNotification userNotification = null;

  /**
   * Default constructor
   */
  protected AbstractUserNotificationBuilder() {
    // Nothing to do
  }

  /**
   * Constructs the notification builder and prepares the notification to build with the specified
   * title and content.
   * @param title the title of the notification.
   * @param content the content of the notification.
   */
  protected AbstractUserNotificationBuilder(final String title, final String content) {
    this();
    this.title = title;
    this.content = content;
  }

  /**
   * Performs common initializations. The {@link UserNotification} object is constructed here by
   * invoking the {@link #createNotification()} method.
   */
  protected void initialize() {
    userNotification = createNotification();
    getNotificationMetaData().setMessageType(getMessageType().getId());
    getNotificationMetaData().setAction(getAction());
    getNotificationMetaData().setComponentId(getComponentInstanceId());
    getNotificationMetaData().setSender(getSender());
    getNotificationMetaData().setSendImmediately(isSendImmediately());
  }

  /**
   * Creates the user notification. This method is used to construct and initialize a
   * {@link UserNotification} object in the {@link #build()} method.
   * @return a {@link UserNotification} object.
   */
  protected abstract UserNotification createNotification();

  /**
   * Gets the type of action on the resource concerned by the notification if any.
   * @return a value of the {@link NotifAction} enumeration.
   */
  protected abstract NotifAction getAction();

  /**
   * Gets the component instance identifier.
   * @return the unique identifier of the component instance concerned by the notification to build.
   */
  protected abstract String getComponentInstanceId();

  /**
   * Gets the sender (the user identifier usually)
   * @return the unique identifier of the sender.
   */
  protected abstract String getSender();

  /**
   * Is the specified user can be notified? The reason depends on the nature of the notification
   * and as such it is delegated to the implementor. For example, for notifications about a
   * resource, only users that can access the resource can be notified, not the others even if they
   * are part of the recipients.
   * @return true of the specified user satisfies all the requirements to be notified.
   */
  protected abstract boolean isUserCanBeNotified(final String userId);

  /**
   * Is the specified group of users can be notified? The reason depends on the nature of the
   * notification and as such it is delegated to the implementor. For example, for notifications
   * about a resource, only groups of users that can access the resource can be notified, not the
   * others even if they are part of the recipients.
   * @return true of the specified group satisfies all the requirements to be notified.
   */
  protected abstract boolean isGroupCanBeNotified(final String groupId);

  /**
   * Gets the notification metadata.
   * @return the metadata about the notification to build.
   */
  protected final NotificationMetaData getNotificationMetaData() {
    return userNotification.getNotificationMetaData();
  }

  /**
   * Gets the type of notification message.
   * @return a value in the {@link NotifMessageType} enumeration.
   */
  protected NotifMessageType getMessageType() {
    return NotifMessageType.NORMAL;
  }

  /**
   * Is the notification to build has to be sent immediately? If no, then its sending will be
   * delayed according to the preferences of the users.
   * @return true if the notification to build has to be sent immediately.
   */
  protected boolean isSendImmediately() {
    return false;
  }

  /**
   * Builds the notification.
   * @return a {@link UserNotification} object.
   */
  @Override
  public final UserNotification build() {
    final Mutable<String> userNote = Mutable.empty();
    try {
      if (isUserSubscriptionNotification() &&
          (NotifAction.UPDATE == getAction() || NotifAction.CLASSIFIED == getAction())) {
        if (!isSubscriptionNotificationEnabledForCurrentRequest()) {
          // In that case, the user requested to not send subscription notification
          stop();
        } else {
          // Maybe a note has been written?
          userNote.set(getSubscriptionNotificationUserNoteFromCurrentRequest());
        }
      }
      initialize();
      performUsersToBeNotified();
      performBuild();
    } catch (final Stop e) {
      SilverLogger.getLogger(this).silent(e);
      userNotification = new NullUserNotification();
    }

    // Handling the user note
    if (isDefined(userNote.orElse(null))) {
      // pushing it into user notification
      DisplayI18NHelper.getLanguages().forEach(
          l -> userNotification.getNotificationMetaData().addExtraMessage(userNote.get(), l));
    }
    return userNotification;
  }

  /**
   * A collection of user identifiers. All the users in this collection will be notified. This
   * method requires to be implemented.
   * @return
   */
  protected abstract Collection<String> getUserIdsToNotify();

  /**
   * Collection of identifiers of users that don't have to be notified. By default, an empty
   * collection is returned.
   * @return a collection of identifiers of the users to exclude from the notification.
   */
  protected Collection<String> getUserIdsToExcludeFromNotifying() {
    return Collections.emptyList();
  }

  /**
   * Gets a collection of user group's identifiers. All the users in this collection will be
   * notified. By default, an empty collection is returned.
   * @return a collection of user group's identifiers.
   */
  protected Collection<String> getGroupIdsToNotify() {
    return Collections.emptyList();
  }

  /**
   * Gets a collection of email addresses, each of them corresponding to a person external of
   * Silverpeas. By default, an empty collection is returned.
   * @return a collection of email addresses.
   */
  protected Collection<String> getExternalAddressesToNotify() {
    return Collections.emptyList();
  }

  private void performUsersToBeNotified() {
    final Collection<String> userIdsToNotify = getUserIdsToNotify();
    final Collection<String> userIdsToExcludeFromNotifying = getUserIdsToExcludeFromNotifying();
    final Collection<String> groupIdsToNotify = getGroupIdsToNotify();
    final Collection<String> emailsToNotify = getExternalAddressesToNotify();

    // Stopping the process if no user to notify
    if (stopWhenNoUserToNotify() && CollectionUtil.isEmpty(userIdsToNotify) &&
        CollectionUtil.isEmpty(groupIdsToNotify) && CollectionUtil.isEmpty(emailsToNotify)) {
      SilverLogger.getLogger(this).info("No user or groups to notify!");
      stop();
    }

    addUserRecipients(userIdsToNotify, userIdsToExcludeFromNotifying);
    addGroupRecipients(groupIdsToNotify);
    addExternalRecipients(emailsToNotify);
  }

  private void addExternalRecipients(final Collection<String> emailsToNotify) {
    if (CollectionUtil.isNotEmpty(emailsToNotify)) {
      for (String address : emailsToNotify) {
        getNotificationMetaData().addExternalRecipient(new ExternalRecipient(address));
      }
    }
  }

  private void addGroupRecipients(final Collection<String> groupIdsToNotify) {
    if (CollectionUtil.isNotEmpty(groupIdsToNotify)) {
      // There is at least one group to notify
      for (final String groupId : groupIdsToNotify) {
        if (isGroupCanBeNotified(groupId)) {
          getNotificationMetaData().addGroupRecipient(new GroupRecipient(groupId));
        }
      }
    }
  }

  private void addUserRecipients(final Collection<String> userIdsToNotify,
      final Collection<String> userIdsToExcludeFromNotifying) {
    if (CollectionUtil.isNotEmpty(userIdsToNotify)) {
      // There is at least one user to notify
      for (final String userId : userIdsToNotify) {
        if (isUserCanBeNotified(userId)) {
          getNotificationMetaData().addUserRecipient(new UserRecipient(userId));
        }
      }
    }

    if (CollectionUtil.isNotEmpty(userIdsToExcludeFromNotifying)) {
      // There is at least one user to notify
      for (final String userId : userIdsToExcludeFromNotifying) {
        getNotificationMetaData().addUserRecipientToExclude(new UserRecipient(userId));
      }
    }
    if (this instanceof RemoveSenderRecipientBehavior && StringUtil.isInteger(getSender())) {
      final boolean excludeSender = !(this instanceof UserSubscriptionNotificationBehavior) ||
          NotificationManagerSettings.isRemoveSenderFromSubscriptionNotificationReceiversEnabled();
      if (excludeSender) {
        // The sender must be excluded from receivers when the notification concerns a subscription
        // and if it is enabled from the global parameter.
        getNotificationMetaData().addUserRecipientToExclude(new UserRecipient(getSender()));
      }
    }
  }

  /**
   * Should the notification treatment be stopped in there is no users to notify? By default true.
   * This method can be overridden to specify a different or a contextualized answer. In that case,
   * the recipients setting should be then performed out of the builder.
   * @return true if no notification has to be done when no recipients are defined.
   */
  protected boolean stopWhenNoUserToNotify() {
    return true;
  }

  /**
   * Builds the notification data container
   */
  protected abstract void performBuild();

  /**
   * Gets the path of the localization bundle to load. By default, returns the general translations.
   * For more specific localized text to use in the notification, override this method.
   * @return the path of the localization bundle.
   */
  protected String getLocalizationBundlePath() {
    return LocalizationBundle.GENERAL_BUNDLE_NAME;
  }

  /**
   * Gets the localization bundle from which the localized text to use in the notification can
   * be get.
   * @return the localization bundle whose the path is provided by the
   * {@link #getLocalizationBundlePath()} method.
   */
  protected final LocalizationBundle getBundle() {
    return getBundle(I18NHelper.defaultLanguage);
  }

  /**
   * Gets the localization bundle for the specified locale to use in the building the notification.
   * @param language the ISO-631 code of a language.
   * @return the localization bundle whose path is provided by the
   * {@link #getLocalizationBundlePath()} method.
   */
  protected final LocalizationBundle getBundle(final String language) {
    LocalizationBundle bundle = null;
    if (StringUtils.isNotBlank(getLocalizationBundlePath())) {
      bundle = ResourceLocator.getLocalizationBundle(getLocalizationBundlePath(), language);
    }
    return bundle;
  }

  /**
   * Gets the title of the notification. By overriding this method, the title can be customized. By
   * default, the title is the one set explicitly in the constructor of this builder.
   * @return the title of the notification.
   */
  protected String getTitle() {
    return title;
  }

  /**
   * Gets the content of the notification. By overriding this method, the content can be customized.
   * By default, the content is the one set explicitly in the constructor of this builder.
   * @return the content of the notification (aka the message itself).
   */
  protected String getContent() {
    return content;
  }

  /**
   * Stops the treatment
   */
  protected void stop() {
    throw new Stop();
  }

  /**
   * Used to stop the treatment at any time
   * @author Yohann Chastagnier
   */
  private class Stop extends RuntimeException {
    private static final long serialVersionUID = 1L;
    // Nothing to do
  }

  private boolean isUserSubscriptionNotification() {
    return this instanceof UserSubscriptionNotificationBehavior;
  }
}
