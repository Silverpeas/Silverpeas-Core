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

package org.silverpeas.core.webapi.subscribe;

import org.silverpeas.core.subscription.SubscriptionSubscriber;
import org.silverpeas.core.subscription.constant.SubscriberType;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * User: Yohann Chastagnier
 */
@XmlRootElement
public class SubscriberEntity {

  @XmlElement(required = true)
  private String id = "0";
  @XmlElement(required = true)
  private boolean group = false;
  @XmlElement(required = true)
  private boolean user = true;

  static SubscriberEntity from(SubscriptionSubscriber subscriber) {
    SubscriberEntity entity = new SubscriberEntity();
    entity.group = SubscriberType.GROUP.equals(subscriber.getType());
    entity.user = SubscriberType.USER.equals(subscriber.getType());
    entity.id = subscriber.getId();
    return entity;
  }

  public String getId() {
    return id;
  }

  public boolean isGroup() {
    return group;
  }

  public boolean isUser() {
    return user;
  }
}
