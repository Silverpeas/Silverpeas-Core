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
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.model.SilverpeasContent;
import org.silverpeas.core.contribution.model.SilverpeasToolContent;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.notification.user.DefaultUserNotification;
import org.silverpeas.core.notification.user.UserNotification;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.core.security.authorization.NodeAccessControl;
import org.silverpeas.core.util.Link;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.mvc.route.ComponentInstanceRoutingMapProvider;
import org.silverpeas.core.web.mvc.route.ComponentInstanceRoutingMapProviderByInstance;

import java.util.Collection;

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
    final String link = getResourceURL(resource);
    getNotificationMetaData().setLink(link);
  }

  /**
   * The access control of the specified user to the resource behind this notification builder
   * is verified. If the resource is a contribution, then the access controllers are used to verify
   * such an access. Otherwise, by default the user can be notified (no access control required).
   * @param userId the unique identifier of the user.
   * @return true if the user can access the resource. False otherwise.
   */
  @Override
  protected boolean isUserCanBeNotified(final String userId) {
    final boolean isAccessible;
    if (resource instanceof Contribution) {
      isAccessible = isUserAuthorized(userId, (Contribution) resource);
    } else if (resource instanceof NodeDetail) {
      final NodeDetail node = (NodeDetail) resource;
      isAccessible = NodeAccessControl.get().isUserAuthorized(userId, node.getNodePK());
    } else {
      isAccessible = true;
    }
    return isAccessible;
  }

  /**
   * The access control of the specified group to the resource behind this notification builder
   * is verified. If the resource is a contribution, then the access controllers are used to verify
   * such an access. Otherwise, by default the users in the group can be notified (no access
   * control required).
   * @param groupId the unique identifier of the group of users.
   * @return true if the group can access the resource. False otherwise.
   */
  @Override
  protected boolean isGroupCanBeNotified(final String groupId) {
    final boolean isAccessible;
    if (resource instanceof Contribution) {
      isAccessible = isGroupAuthorized(groupId, (Contribution) resource);
    } else if (resource instanceof NodeDetail) {
      final NodeDetail node = (NodeDetail) resource;
      isAccessible = NodeAccessControl.get().isGroupAuthorized(groupId, node.getNodePK());
    } else {
      isAccessible = true;
    }
    return isAccessible;
  }

  private boolean isUserAuthorized(final String userId, final Contribution contribution) {
    final String id = contribution.getContributionId().getLocalId();
    final String instanceId = contribution.getContributionId().getComponentInstanceId();
    Collection<NodePK> fatherPKs = PublicationService.get()
        .getAllFatherPKInSamePublicationComponentInstance(new PublicationPK(id, instanceId));
    boolean isAccessible;
    if (fatherPKs != null && !fatherPKs.isEmpty()) {
      NodeAccessControl nodeAccessControl = NodeAccessControl.get();
      isAccessible = false;
      for (NodePK fatherPK : fatherPKs) {
        if ((nodeAccessControl.isUserAuthorized(userId, fatherPK))) {
          isAccessible = true;
          break;
        }
      }
    } else {
      ComponentAccessControl componentAccessControl = ComponentAccessControl.get();
      isAccessible = componentAccessControl.isUserAuthorized(userId, instanceId);
    }
    return isAccessible;
  }

  private boolean isGroupAuthorized(final String groupId, final Contribution contribution) {
    final String id = contribution.getContributionId().getLocalId();
    final String instanceId = contribution.getContributionId().getComponentInstanceId();
    Collection<NodePK> fatherPKs = PublicationService.get()
        .getAllFatherPKInSamePublicationComponentInstance(new PublicationPK(id, instanceId));
    boolean isAccessible;
    if (fatherPKs != null && !fatherPKs.isEmpty()) {
      NodeAccessControl nodeAccessControl = NodeAccessControl.get();
      isAccessible = false;
      for (NodePK fatherPK: fatherPKs) {
        if ((nodeAccessControl.isGroupAuthorized(groupId, fatherPK))) {
          isAccessible = true;
          break;
        }
      }
    } else {
      isAccessible = ComponentAccessControl.get().isGroupAuthorized(groupId, instanceId);
    }
    return isAccessible;
  }

  /**
   * Builds the notification data container
   */
  @Override
  protected final void performBuild() {
    performBuild(resource);
    performNotificationResource(resource);
    if (getAction() == NotifAction.DELETE) {
      getNotificationMetaData().setLink(Link.EMPTY_LINK);
    }
  }

  @Override
  protected UserNotification createNotification() {
    return new DefaultUserNotification(getTitle(), getContent());
  }

  protected abstract void performBuild(T resource);

  protected void performNotificationResource(final T resource) {
    final NotificationResourceData notificationResourceData = initializeNotificationResourceData();
    performNotificationResource(resource, notificationResourceData);
    getNotificationMetaData().setNotificationResourceData(notificationResourceData);
  }

  protected NotificationResourceData initializeNotificationResourceData() {
    final NotificationMetaData metaData = getNotificationMetaData();
    final NotificationResourceData notificationResourceData = new NotificationResourceData();
    notificationResourceData.setComponentInstanceId(metaData.getComponentId());
    notificationResourceData.setResourceUrl(metaData.getLink().getLinkUrl());
    if (resource instanceof SilverpeasContent) {
      fill(notificationResourceData, (SilverpeasContent) resource);
    } else if (resource instanceof Contribution) {
      fill(notificationResourceData, (Contribution) resource);
    }
    return notificationResourceData;
  }

  protected abstract void performNotificationResource(T resource,
      NotificationResourceData notificationResourceData);

  protected String getResourceURL(final T resource) {
    String resourceUrl = null;
    if (resource instanceof SilverpeasContent) {
      resourceUrl = URLUtil.getSearchResultURL((SilverpeasContent) resource);
    } else if (resource instanceof Contribution) {
      Contribution contribution = (Contribution) resource;
      final ComponentInstanceRoutingMapProvider routingMapProvider =
          ComponentInstanceRoutingMapProviderByInstance.get()
              .getByInstanceId(contribution.getContributionId().getComponentInstanceId());
      resourceUrl =
          routingMapProvider.absolute().getPermalink(contribution.getContributionId()).toString();
    }
    if (StringUtils.isBlank(resourceUrl)) {
      resourceUrl = "";
    }
    return resourceUrl;
  }

  @Override
  protected boolean isSendImmediately() {
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

  private void fill(final NotificationResourceData notificationResourceData,
      final Contribution contribution) {
    final ContributionIdentifier contributionId = contribution.getContributionId();
    notificationResourceData.setComponentInstanceId(contributionId.getComponentInstanceId());
    notificationResourceData.setResourceId(contributionId.getLocalId());
    notificationResourceData.setResourceType(contributionId.getType());
    notificationResourceData.setResourceName(contribution.getTitle());
    notificationResourceData.setResourceDescription(contribution.getDescription());
  }
}
