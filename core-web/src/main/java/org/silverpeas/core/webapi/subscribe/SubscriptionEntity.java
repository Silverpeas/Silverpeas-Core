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
package org.silverpeas.core.webapi.subscribe;

import org.silverpeas.core.subscription.Subscription;
import org.silverpeas.core.subscription.constant.SubscriptionMethod;
import org.silverpeas.core.subscription.service.ComponentSubscription;
import org.silverpeas.core.subscription.service.NodeSubscription;
import org.silverpeas.core.web.subscription.bean.AbstractSubscriptionBean;
import org.silverpeas.core.web.subscription.bean.ComponentSubscriptionBean;
import org.silverpeas.core.web.subscription.bean.NodeSubscriptionBean;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

/**
 * @author ehugonnet
 */
@XmlRootElement
public class SubscriptionEntity {

  @XmlElement(required = true)
  private SubscriptionResourceEntity resource;
  @XmlElement(required = true)
  private SubscriberEntity subscriber;
  @XmlElement(defaultValue = "true")
  private boolean forced = true;
  @XmlElement(defaultValue = "true")
  private boolean selfCreation = true;
  @XmlElement(defaultValue = "true")
  private Date creationDate;
  @XmlElement
  private boolean enabled = true;

  static SubscriptionEntity from(Subscription subscription) {
    AbstractSubscriptionBean bean = decorate(subscription);
    SubscriptionEntity entity = new SubscriptionEntity();
    entity.resource = SubscriptionResourceEntity.from(subscription.getResource());
    entity.subscriber = SubscriberEntity.from(subscription.getSubscriber());
    entity.forced = SubscriptionMethod.FORCED.equals(subscription.getSubscriptionMethod());
    entity.selfCreation =
        SubscriptionMethod.SELF_CREATION.equals(subscription.getSubscriptionMethod());
    entity.creationDate = subscription.getCreationDate();
    entity.enabled = bean.isValid();
    return entity;
  }

  public SubscriptionResourceEntity getResource() {
    return resource;
  }

  public SubscriberEntity getSubscriber() {
    return subscriber;
  }

  public boolean isForced() {
    return forced;
  }

  public boolean isSelfCreation() {
    return selfCreation;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public boolean isEnabled() {
    return this.enabled;
  }

  private static AbstractSubscriptionBean decorate(final Subscription subscription) {
    final AbstractSubscriptionBean bean;
    if (subscription instanceof AbstractSubscriptionBean) {
      bean = (AbstractSubscriptionBean) subscription;
    } else if (subscription instanceof ComponentSubscription) {
      bean = new ComponentSubscriptionBean(subscription, null, null);
    } else if (subscription instanceof NodeSubscription) {
      bean = new NodeSubscriptionBean(subscription, null, null, null);
    } else {
      throw new IllegalArgumentException(
          "Type of subscription not supported: " + subscription.getResource().getType());
    }
    return bean;
  }
}
