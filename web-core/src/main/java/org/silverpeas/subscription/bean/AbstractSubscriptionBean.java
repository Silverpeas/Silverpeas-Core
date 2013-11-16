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
package org.silverpeas.subscription.bean;

import com.silverpeas.subscribe.Subscription;
import com.silverpeas.subscribe.SubscriptionResource;
import com.silverpeas.subscribe.SubscriptionSubscriber;
import com.silverpeas.subscribe.constant.SubscriberType;
import com.silverpeas.subscribe.constant.SubscriptionMethod;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.core.admin.OrganisationControllerFactory;

import java.util.Date;

/**
 * User: Yohann Chastagnier
 * Date: 25/02/13
 */
public abstract class AbstractSubscriptionBean implements Subscription {

  private final Subscription subscription;
  private final String language;
  private UserDetail user = null;
  private Group group = null;
  private SpaceInstLight space = null;
  private ComponentInstLight component = null;

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
   * @return
   */
  public boolean isReadOnly() {
    return SubscriberType.GROUP.equals(getSubscriber().getType());
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
    return URLManager
        .getSimpleURL(URLManager.URL_COMPONENT, subscription.getResource().getInstanceId());
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
      group = OrganisationControllerFactory.getOrganisationController()
          .getGroup(getSubscriber().getId());
      if (group == null) {
        // Prevents from NullPointerException
        group = new Group();
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
      component = OrganisationControllerFactory.getOrganisationController()
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
      space = OrganisationControllerFactory.getOrganisationController()
          .getSpaceInstLightById(component.getDomainFatherId());
      if (space == null) {
        // Prevents from NullPointerException
        space = new SpaceInstLight();
      }
    }
    return space;
  }
}
