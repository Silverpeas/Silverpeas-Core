/*
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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.notification.builder;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.silverpeas.util.CollectionUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.UserRecipient;
import com.stratelia.silverpeas.notificationManager.constant.NotifAction;
import com.stratelia.silverpeas.notificationManager.constant.NotifMessageType;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractUserNotificationBuilder implements IUserNotificationBuider {

  private String title = null;
  private String content = null;
  private IUserNotification userNotification = null;
  private final Map<String, ResourceLocator> bundles = new HashMap<String, ResourceLocator>();

  /**
   * Default constructor
   * @param notification
   */
  protected AbstractUserNotificationBuilder() {
    // Nothing to do
  }

  /**
   * Default constructor
   * @param notification
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
    getNotification().setMessageType(getMessageType().getId());
    getNotification().setAction(getAction());
    getNotification().setComponentId(getComponentInstanceId());
    getNotification().setSender(getSender());
    getNotification().setSendImmediately(isSendImmediatly());
  }

  /**
   * Create the user notification container
   * @return
   */
  protected IUserNotification createNotification() {
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
  protected final NotificationMetaData getNotification() {
    return userNotification.getNotification();
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
  public final IUserNotification build() {
    try {
      initialize();
      performUsersToBeNotified();
      performBuild();
    } catch (final Stop e) {
      userNotification = new NullUserNotification();
    }
    return userNotification;
  }

  protected abstract Collection<String> getUserIdsToNotify();

  protected final void performUsersToBeNotified() {
    final Collection<String> userIdsToNotify = getUserIdsToNotify();

    // Stopping the process if no user to notify
    if (stopWhenNoUserToNotify() && CollectionUtil.isEmpty(userIdsToNotify)) {
      SilverTrace.warn("notification", "IUserNotificationBuider.build()",
          "IUserNotificationBuider.EX_NO_USER_TO_NOTIFY");
      stop();
    }

    if (CollectionUtil.isNotEmpty(userIdsToNotify)) {
      // There is at least one user to notify
      for (final String userId : userIdsToNotify) {
        getNotification().addUserRecipient(new UserRecipient(userId));
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
  protected final ResourceLocator getBundle() {
    return getBundle(I18NHelper.defaultLanguage);
  }

  /**
   * Gets the bundle
   * @return
   */
  protected final ResourceLocator getBundle(final String language) {
    ResourceLocator bundle = bundles.get(language);
    if (bundle == null && StringUtils.isNotBlank(getMultilangPropertyFile())) {
      bundle = new ResourceLocator(getMultilangPropertyFile(), language);
      bundles.put(language, bundle);
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
