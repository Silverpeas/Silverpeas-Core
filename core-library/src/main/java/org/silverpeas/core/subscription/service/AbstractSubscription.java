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

package org.silverpeas.core.subscription.service;

import org.silverpeas.core.subscription.Subscription;
import org.silverpeas.core.subscription.SubscriptionResource;
import org.silverpeas.core.subscription.SubscriptionSubscriber;
import org.silverpeas.core.subscription.constant.SubscriberType;
import org.silverpeas.core.subscription.constant.SubscriptionMethod;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Date;

/**
 * User: Yohann Chastagnier
 * Date: 19/02/13
 */
public abstract class AbstractSubscription implements Subscription {

  private final SubscriptionResource resource;
  private final SubscriptionSubscriber subscriber;
  private final SubscriptionMethod subscriptionMethod;
  private final String creatorId;
  private final Date creationDate;

  /**
   * Constructor which use by default SubscriptionMethod.SELF_CREATION and null creation date.
   * @param subscriber id and type of the subscriber
   * @param resource id, type and pk of the resource aimed by the subscription
   * @param creatorId the user id that has handled the subscription
   */
  protected AbstractSubscription(SubscriptionSubscriber subscriber, SubscriptionResource resource,
      String creatorId) {
    this(subscriber, resource, SubscriptionMethod.UNKNOWN, creatorId, null);
  }

  /**
   * The more complete constructor.
   * @param subscriber id and type of the subscriber
   * @param resource id, type and pk of the resource aimed by the subscription
   * @param subscriptionMethod the way the subscriber has subscribed (for now, himself or forced)
   * @param creatorId the user id that has handled the subscription
   * @param creationDate date of the subscription creation (date when saved)
   */
  protected AbstractSubscription(SubscriptionSubscriber subscriber, SubscriptionResource resource,
      SubscriptionMethod subscriptionMethod, String creatorId, Date creationDate) {
    this.subscriber = subscriber;
    this.resource = resource;
    if (!subscriptionMethod.isValid() || subscriptionMethod == null) {
      // Guessing the method if it is unknown
      if (SubscriberType.GROUP.equals(subscriber.getType()) ||
          !subscriber.getId().equals(creatorId)) {
        this.subscriptionMethod = SubscriptionMethod.FORCED;
      } else if (SubscriberType.USER.equals(subscriber.getType())) {
        this.subscriptionMethod = SubscriptionMethod.SELF_CREATION;
      } else {
        this.subscriptionMethod = SubscriptionMethod.UNKNOWN;
      }
    } else {
      this.subscriptionMethod = subscriptionMethod;
    }
    this.creatorId = creatorId;
    this.creationDate = creationDate;
  }

  @Override
  public SubscriptionSubscriber getSubscriber() {
    return subscriber;
  }

  @Override
  public SubscriptionResource getResource() {
    return resource;
  }

  @Override
  public SubscriptionMethod getSubscriptionMethod() {
    return subscriptionMethod;
  }

  @Override
  public String getCreatorId() {
    return creatorId;
  }

  @Override
  public Date getCreationDate() {
    return creationDate;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    AbstractSubscription that = (AbstractSubscription) o;

    final EqualsBuilder builder = new EqualsBuilder();
    builder.append(subscriber, that.subscriber);
    builder.append(resource, that.resource);
    builder.append(subscriptionMethod, that.subscriptionMethod);
    return builder.isEquals();
  }

  @Override
  public int hashCode() {
    final HashCodeBuilder builder = new HashCodeBuilder();
    builder.append(subscriber);
    builder.append(resource);
    builder.append(subscriptionMethod);
    return builder.toHashCode();
  }
}