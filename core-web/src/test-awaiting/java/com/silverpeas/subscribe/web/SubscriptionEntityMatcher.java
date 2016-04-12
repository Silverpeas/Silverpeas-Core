/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.subscription.web;

import org.silverpeas.core.subscription.Subscription;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * A matcher between a web subscription entity and a subscription it should represent.
 */
public class SubscriptionEntityMatcher extends BaseMatcher<SubscriptionEntity> {

  private Subscription subscription;

  /**
   * Creates a new matcher with the specified subscription.
   * @param theSubscription the subscription to match.
   * @return a subscription matcher.
   */
  public static SubscriptionEntityMatcher matches(final Subscription theSubscription) {
    return new SubscriptionEntityMatcher(theSubscription);
  }

  @Override
  public boolean matches(Object item) {
    if (item instanceof SubscriptionEntity) {
      SubscriptionEntity actual = (SubscriptionEntity) item;
      EqualsBuilder match = new EqualsBuilder();

      // Resource
      match.append(subscription.getResource().getId(), actual.getResource().getId());
      match
          .append(subscription.getResource().getInstanceId(), actual.getResource().getInstanceId());
      switch (subscription.getResource().getType()) {
        case NODE:
          match.appendSuper(!actual.getResource().isComponent());
          match.appendSuper(actual.getResource().isNode());
          break;
        case COMPONENT:
          match.appendSuper(actual.getResource().isComponent());
          match.appendSuper(!actual.getResource().isNode());
          break;
        default:
          match.appendSuper(false);
          break;
      }

      // Subscriber
      match.append(subscription.getSubscriber().getId(), actual.getSubscriber().getId());
      switch (subscription.getSubscriber().getType()) {
        case USER:
          match.appendSuper(!actual.getSubscriber().isGroup());
          match.appendSuper(actual.getSubscriber().isUser());
          break;
        case GROUP:
          match.appendSuper(actual.getSubscriber().isGroup());
          match.appendSuper(!actual.getSubscriber().isUser());
          break;
        default:
          match.appendSuper(false);
          break;
      }

      // Method
      switch (subscription.getSubscriptionMethod()) {
        case FORCED:
          match.appendSuper(actual.isForced());
          match.appendSuper(!actual.isSelfCreation());
          break;
        case SELF_CREATION:
          match.appendSuper(!actual.isForced());
          match.appendSuper(actual.isSelfCreation());
          break;
        default:
          match.appendSuper(false);
          break;
      }

      return match.isEquals();
    }
    return false;
  }

  @Override
  public void describeTo(Description description) {
    description.appendValue(subscription);
  }

  private SubscriptionEntityMatcher(final Subscription subscription) {
    this.subscription = subscription;
  }
}
