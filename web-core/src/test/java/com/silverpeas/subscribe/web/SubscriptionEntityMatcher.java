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
    boolean match = false;
    if (item instanceof SubscriptionEntity) {
      SubscriptionEntity actual = (SubscriptionEntity) item;
      match = subscription.isComponentSubscription() == actual.isComponentSubscription()
              && subscription.getTopic().getId().equals(String.valueOf(actual.getId())) && subscription.
              getSubscriber().equals(actual.getUserId()) && subscription.getTopic().getComponentName().
              equals(actual.getComponentId());
    }
    return match;
  }

  @Override
  public void describeTo(Description description) {
    description.appendValue(subscription);
  }

  private SubscriptionEntityMatcher(final Subscription subscription) {
    this.subscription = subscription;
  }
}
