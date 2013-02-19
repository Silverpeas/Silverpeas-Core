/**
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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.silverpeas.subscribe.web;

import com.silverpeas.subscribe.Subscription;
import com.silverpeas.subscribe.constant.SubscriberType;
import com.silverpeas.subscribe.constant.SubscriptionResourceType;
import com.silverpeas.web.Exposable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.net.URI;

/**
 * @author ehugonnet
 */
@XmlRootElement
public class SubscriptionEntity implements Exposable {

  @XmlElement(defaultValue = "")
  private URI uri;
  @XmlElement(required = true)
  private String id = "0";
  @XmlElement(required = true)
  private String componentId;
  @XmlElement(defaultValue = "true")
  private boolean userSubscriber = true;
  @XmlElement(defaultValue = "true")
  private boolean componentSubscription = true;
  @XmlElement()
  private String name;
  @XmlElement()
  private String subscriberId;
  @XmlElement()
  private String subscriberName;
  @XmlTransient
  private static final long serialVersionUID = -621014423925580476L;

  static SubscriptionEntity fromSubscription(Subscription subscription) {
    SubscriptionEntity entity = new SubscriptionEntity();
    entity.id = subscription.getResource().getId();
    entity.componentId = subscription.getResource().getPK().getComponentName();
    entity.userSubscriber = SubscriberType.USER.equals(subscription.getSubscriber().getType());
    entity.componentSubscription =
        SubscriptionResourceType.COMPONENT.equals(subscription.getResource().getType());
    entity.subscriberId = subscription.getSubscriber().getId();
    return entity;
  }

  SubscriptionEntity withName(String name) {
    this.name = name;
    return this;
  }

  SubscriptionEntity withURI(URI uri) {
    this.uri = uri;
    return this;
  }

  @Override
  public URI getURI() {
    return uri;
  }

  public String getComponentId() {
    return componentId;
  }

  public boolean isUserSubscriber() {
    return userSubscriber;
  }

  public boolean isComponentSubscription() {
    return componentSubscription;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getSubscriberId() {
    return subscriberId;
  }

  public String getSubscriberName() {
    return subscriberName;
  }
}
