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

import com.silverpeas.web.Exposable;
import com.silverpeas.subscribe.Subscription;

import java.net.URI;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author ehugonnet
 */
@XmlRootElement
public class SubscriptionEntity implements Exposable {

  @XmlElement(defaultValue = "")
  private URI uri;
  @XmlElement(required = true)
  private int id = 0;
  @XmlElement(required = true)
  private String componentId;
  @XmlElement(defaultValue = "true")
  private boolean componentSubscription = true;
  @XmlElement()
  private String name;
  @XmlElement()
  private String userId;
  @XmlElement()
  private String userName;
  @XmlTransient
  private static final long serialVersionUID = -621014423925580476L;

  static SubscriptionEntity fromSubscription(Subscription subscription) {
    SubscriptionEntity entity = new SubscriptionEntity();
    entity.id = Integer.parseInt(subscription.getTopic().getId());
    entity.componentId = subscription.getTopic().getComponentName();
    entity.componentSubscription = subscription.isComponentSubscription();
    entity.userId = subscription.getSubscriber();
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

  public boolean isComponentSubscription() {
    return componentSubscription;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getUserId() {
    return userId;
  }

  public String getUserName() {
    return userName;
  }
}
