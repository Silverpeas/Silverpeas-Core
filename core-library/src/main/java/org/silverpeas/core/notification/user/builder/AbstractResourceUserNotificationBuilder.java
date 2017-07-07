/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
import org.silverpeas.core.contribution.model.SilverpeasContent;
import org.silverpeas.core.contribution.model.SilverpeasToolContent;
import org.silverpeas.core.notification.user.DefaultUserNotification;
import org.silverpeas.core.notification.user.UserNotification;
import org.silverpeas.core.notification.user.UserSubscriptionNotificationSendingHandler;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.util.URLUtil;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractResourceUserNotificationBuilder<T>
    extends AbstractUserNotificationBuilder {

  private T resource;

  public AbstractResourceUserNotificationBuilder(final T resource, final String title,
      final String content) {
    super(title, content);
    setResource(resource);
  }

  public AbstractResourceUserNotificationBuilder(final T resource) {
    this(resource, null, null);
  }

  /**
   * Performs common initializations from a given resource
   */
  @Override
  protected void initialize() {
    super.initialize();
    getNotificationMetaData().setLink(getResourceURL(resource));
  }

  /**
   * Builds the notification data container
   */
  @Override
  protected final void performBuild() {
    performBuild(resource);
    performNotificationResource(resource);
  }

  @Override
  protected UserNotification createNotification() {
    return new DefaultUserNotification(getTitle(), getContent());
  }

  @Override
  protected boolean isUserSubscriptionNotificationEnabled() {
    return UserSubscriptionNotificationSendingHandler.isEnabledForCurrentRequest();
  }

  protected abstract void performBuild(T resource);

  protected void performNotificationResource(final T resource) {
    final NotificationResourceData notificationResourceData = initializeNotificationResourceData();
    performNotificationResource(resource, notificationResourceData);
    getNotificationMetaData().setNotificationResourceData(notificationResourceData);
  }

  protected NotificationResourceData initializeNotificationResourceData() {
    final NotificationResourceData notificationResourceData = new NotificationResourceData();
    notificationResourceData.setComponentInstanceId(getNotificationMetaData().getComponentId());
    notificationResourceData.setResourceUrl(getNotificationMetaData().getLink());
    if (resource instanceof SilverpeasContent) {
      fill(notificationResourceData, (SilverpeasContent) resource);
    }
    return notificationResourceData;
  }

  protected abstract void performNotificationResource(T resource,
      NotificationResourceData notificationResourceData);

  protected String getResourceURL(final T resource) {
    String resourceUrl = null;
    if (resource instanceof SilverpeasContent) {
      resourceUrl = URLUtil.getSearchResultURL((SilverpeasContent) resource);
    }
    if (StringUtils.isBlank(resourceUrl)) {
      resourceUrl = "";
      SilverTrace.warn("NotificationBuider",
          "AbstractResourceNotificationBuilder.getResourceURL(T resource)",
          "notificationBuider.RESOURCE_URL_IS_EMPTY");
    }
    return resourceUrl;
  }

  @Override
  protected boolean isSendImmediatly() {
    return getResource() instanceof SilverpeasToolContent;
  }

  /*
   * Tools
   */

  protected final T getResource() {
    return resource;
  }

  protected final void setResource(final T resource) {
    this.resource = resource;
  }

  private void fill(final NotificationResourceData notificationResourceData,
      final SilverpeasContent silverpeasContent) {
    notificationResourceData.setResourceId(silverpeasContent.getId());
    notificationResourceData.setResourceType(silverpeasContent.getContributionType());
  }
}
