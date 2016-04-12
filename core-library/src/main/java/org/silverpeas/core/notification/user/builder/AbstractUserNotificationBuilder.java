/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.core.notification.user.builder;

import org.silverpeas.core.notification.user.client.ExternalRecipient;
import org.silverpeas.core.notification.user.client.GroupRecipient;
import org.silverpeas.core.notification.user.client.NotificationManagerSettings;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.UserRecipient;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.notification.user.client.constant.NotifMessageType;
import org.silverpeas.core.notification.user.DefaultUserNotification;
import org.silverpeas.core.notification.user.NullUserNotification;
import org.silverpeas.core.notification.user.UserNotification;
import org.silverpeas.core.notification.user.UserSubscriptionNotificationBehavior;
import org.silverpeas.core.notification.user.UserSubscriptionNotificationSendingHandler;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.apache.commons.lang3.StringUtils;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.i18n.I18NHelper;

import java.util.Collection;

/**
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
   * Default constructor
   * @param title
   * @param content
   */
  protected AbstractUserNotificationBuilder(final String title, final String content) {
    this();
    this.title = title;
    this.content = content;
  }

  /**
   * Performs common initializations
   */
  protected void initialize() {
    userNotification = createNotification();
    getNotificationMetaData().setMessageType(getMessageType().getId());
    getNotificationMetaData().setAction(getAction());
    getNotificationMetaData().setComponentId(getComponentInstanceId());
    getNotificationMetaData().setSender(getSender());
    getNotificationMetaData().setSendImmediately(isSendImmediatly());
  }

  /**
   * Create the user notification container
   * @return
   */
  protected UserNotification createNotification() {
    return new DefaultUserNotification(getTitle(), getContent());
  }

  /**
   * Gets the type of action on a resource
   * @return
   */
  protected abstract NotifAction getAction();

  /**
   * Gets the component instance id
   * @return
   */
  protected abstract String getComponentInstanceId();

  /**
   * Gets the sender (the user id usually)
   * @return
   */
  protected abstract String getSender();

  /**
   * Gets the notification meta data container
   * @return
   */
  protected final NotificationMetaData getNotificationMetaData() {
    return userNotification.getNotificationMetaData();
  }

  /**
   * Gets the type of notification message
   * @return
   */
  protected NotifMessageType getMessageType() {
    return NotifMessageType.NORMAL;
  }

  /**
   * Forces the sending immediatly if true
   * @return
   */
  protected boolean isSendImmediatly() {
    return false;
  }

  /**
   * Builds the notification data container
   */
  @Override
  public final UserNotification build() {
    try {
      if (this instanceof UserSubscriptionNotificationBehavior &&
          !UserSubscriptionNotificationSendingHandler.isEnabledForCurrentRequest() &&
          NotifAction.UPDATE == getAction()) {
        // In that case, the user requested to not send subscription notification
        stop();
      }
      initialize();
      performUsersToBeNotified();
      performBuild();
    } catch (final Stop e) {
      userNotification = new NullUserNotification();
    }
    return userNotification;
  }

  protected abstract Collection<String> getUserIdsToNotify();

  /**
   * Collection of identifiers of users that don't have to be notified ...
   * @return
   */
  protected Collection<String> getUserIdsToExcludeFromNotifying() {
    return null;
  }

  protected Collection<String> getGroupIdsToNotify() {
    return null;
  }

  protected Collection<String> getExternalAddressesToNotify() {
    return null;
  }

  protected final void performUsersToBeNotified() {
    final Collection<String> userIdsToNotify = getUserIdsToNotify();
    final Collection<String> userIdsToExcludeFromNotifying = getUserIdsToExcludeFromNotifying();
    final Collection<String> groupIdsToNotify = getGroupIdsToNotify();
    final Collection<String> emailsToNotify = getExternalAddressesToNotify();

    // Stopping the process if no user to notify
    if (stopWhenNoUserToNotify() &&
        CollectionUtil.isEmpty(userIdsToNotify) && CollectionUtil.isEmpty(groupIdsToNotify) &&
        CollectionUtil.isEmpty(emailsToNotify)) {
      SilverTrace.warn("notification", "IUserNotificationBuider.build()",
          "IUserNotificationBuider.EX_NO_USER_OR_GROUP_TO_NOTIFY");
      stop();
    }

    if (CollectionUtil.isNotEmpty(userIdsToNotify)) {
      // There is at least one user to notify
      for (final String userId : userIdsToNotify) {
        getNotificationMetaData().addUserRecipient(new UserRecipient(userId));
      }
    }

    if (CollectionUtil.isNotEmpty(userIdsToExcludeFromNotifying)) {
      // There is at least one user to notify
      for (final String userId : userIdsToExcludeFromNotifying) {
        getNotificationMetaData().addUserRecipientToExclude(new UserRecipient(userId));
      }
    }
    if (this instanceof UserSubscriptionNotificationBehavior &&
        NotificationManagerSettings.isRemoveSenderFromSubscriptionNotificationReceiversEnabled() &&
        StringUtil.isInteger(getSender())) {
      // The sender must be excluded from receivers when the notification concerns a subscription
      // and if it is enabled from the global parameter.
      getNotificationMetaData().addUserRecipientToExclude(new UserRecipient(getSender()));
    }

    if (CollectionUtil.isNotEmpty(groupIdsToNotify)) {
      // There is at least one group to notify
      for (final String groupId : groupIdsToNotify) {
        getNotificationMetaData().addGroupRecipient(new GroupRecipient(groupId));
      }
    }

    if (CollectionUtil.isNotEmpty(emailsToNotify)) {
      for (String address : emailsToNotify) {
        getNotificationMetaData().addExternalRecipient(new ExternalRecipient(address));
      }
    }
  }

  protected boolean stopWhenNoUserToNotify() {
    return true;
  }

  /**
   * Builds the notification data container
   */
  protected abstract void performBuild();

  /**
   * Gets the resource locator path
   * @return
   */
  protected String getMultilangPropertyFile() {
    return null;
  }

  /**
   * Gets the bundle
   * @return
   */
  protected final LocalizationBundle getBundle() {
    return getBundle(I18NHelper.defaultLanguage);
  }

  /**
   * Gets the bundle
   * @return
   */
  protected final LocalizationBundle getBundle(final String language) {
    LocalizationBundle bundle = null;
    if (StringUtils.isNotBlank(getMultilangPropertyFile())) {
      bundle = ResourceLocator.getLocalizationBundle(getMultilangPropertyFile(), language);
    }
    return bundle;
  }

  protected String getTitle() {
    return title;
  }

  protected String getContent() {
    return content;
  }

  /**
   * Stopping the treatment
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
}
