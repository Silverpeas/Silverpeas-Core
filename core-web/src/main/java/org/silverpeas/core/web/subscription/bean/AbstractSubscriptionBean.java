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
package org.silverpeas.core.web.subscription.bean;

import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.GroupDetail;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.core.security.authorization.NodeAccessControl;
import org.silverpeas.core.subscription.Subscription;
import org.silverpeas.core.subscription.SubscriptionResource;
import org.silverpeas.core.subscription.SubscriptionSubscriber;
import org.silverpeas.core.subscription.constant.SubscriberType;
import org.silverpeas.core.subscription.constant.SubscriptionMethod;
import org.silverpeas.core.subscription.constant.SubscriptionResourceType;
import org.silverpeas.core.util.URLUtil;

import java.util.Date;

/**
 * Abstract class that defines all the common properties of the different concrete type of
 * a subscription.
 * @author Yohann Chastagnier
 * Date: 25/02/13
 */
public abstract class AbstractSubscriptionBean implements Subscription {

  private final Subscription subscription;
  private final String language;
  private UserDetail user = null;
  private Group group = null;
  private SpaceInstLight space = null;
  private ComponentInstLight component;

  protected AbstractSubscriptionBean(final Subscription subscription,
      final ComponentInstLight component, final String language) {
    this.subscription = subscription;
    this.component = component;
    this.language = language;
  }

  public String getLanguage() {
    return language;
  }

  @Override
  public SubscriptionResource getResource() {
    return subscription.getResource();
  }

  @Override
  public SubscriptionSubscriber getSubscriber() {
    return subscription.getSubscriber();
  }

  @Override
  public SubscriptionMethod getSubscriptionMethod() {
    return subscription.getSubscriptionMethod();
  }

  @Override
  public String getCreatorId() {
    return subscription.getCreatorId();
  }

  @Override
  public Date getCreationDate() {
    return subscription.getCreationDate();
  }

  /**
   * Indicates if the subscription is read only.
   * @return true if the subscriber is a group of users. In such a case, a user of the group has no
   * modification rights on the subscription.
   */
  public boolean isReadOnly() {
    return SubscriberType.GROUP.equals(getSubscriber().getType());
  }


  /**
   * Is this subscription valid? A subscription is valid if and only if the user or the group
   * of users can access the subscribed resource.
   * @return true if the subscriber can access the resource. False otherwise.
   */
  public boolean isValid() {
    SubscriptionSubscriber subscriber = getSubscriber();
    SubscriptionResource resource = getResource();
    if (subscriber.getType() == SubscriberType.USER) {
      return isUserCanAccess(subscriber.getId(), resource);
    } else if (subscriber.getType() == SubscriberType.GROUP) {
      return isGroupCanAccess(subscriber.getId(), resource);
    }

    return true;
  }

  private boolean isUserCanAccess(final String userId, SubscriptionResource resource) {
    final boolean accessOK;
    if (resource.getType() == SubscriptionResourceType.NODE) {
      NodePK nodePK = new NodePK(resource.getId(), resource.getInstanceId());
      accessOK = NodeAccessControl.get().isUserAuthorized(userId, nodePK);
    } else {
      accessOK = ComponentAccessControl.get().isUserAuthorized(userId, resource.getInstanceId());
    }
    return accessOK;
  }

  private boolean isGroupCanAccess(final String groupId, SubscriptionResource resource) {
    final boolean accessOK;
    if (resource.getType() == SubscriptionResourceType.NODE) {
      NodePK nodePK = new NodePK(resource.getId(), resource.getInstanceId());
      accessOK = NodeAccessControl.get().isGroupAuthorized(groupId, nodePK);
    } else {
      String instanceId = resource.getInstanceId();
      accessOK = ComponentAccessControl.get().isGroupAuthorized(groupId, instanceId);
    }
    return accessOK;
  }

  /**
   * Gets the path.
   * @return
   */
  public String getPath() {
    return getSpace().getName() + " > " + getComponent().getLabel();
  }

  /**
   * Gets the link to access the resource.
   * @return
   */
  public String getLink() {
    return URLUtil
        .getSimpleURL(URLUtil.URL_COMPONENT, subscription.getResource().getInstanceId());
  }

  /**
   * Gets the full name of the subscriber.
   * @return
   */
  public String getSubscriberName() {
    switch (getSubscriber().getType()) {
      case USER:
        return getUser().getDisplayedName();
      case GROUP:
        return getGroup().getName();
      default:
        return "";
    }
  }

  /**
   * Gets the user data.
   * @return
   */
  protected UserDetail getUser() {
    if (user == null) {
      user = UserDetail.getById(getSubscriber().getId());
      if (user == null) {
        // Prevents from NullPointerException
        user = new UserDetail();
      }
    }
    return user;
  }

  /**
   * Gets the group data.
   * @return
   */
  protected Group getGroup() {
    if (group == null) {
      group = OrganizationControllerProvider.getOrganisationController()
          .getGroup(getSubscriber().getId());
      if (group == null) {
        // Prevents from NullPointerException
        group = new GroupDetail();
      }
    }
    return group;
  }

  /**
   * Gets the component data.
   * @return
   */
  public ComponentInstLight getComponent() {
    if (component == null) {
      component = OrganizationControllerProvider.getOrganisationController()
          .getComponentInstLight(subscription.getResource().getInstanceId());
      if (component == null) {
        // Prevents from NullPointerException
        component = new ComponentInstLight();
      }
    }
    return component;
  }

  /**
   * Gets the space data.
   * @return
   */
  public SpaceInstLight getSpace() {
    if (space == null) {
      space = OrganizationControllerProvider.getOrganisationController()
          .getSpaceInstLightById(component.getDomainFatherId());
      if (space == null) {
        // Prevents from NullPointerException
        space = new SpaceInstLight();
      }
    }
    return space;
  }
}
