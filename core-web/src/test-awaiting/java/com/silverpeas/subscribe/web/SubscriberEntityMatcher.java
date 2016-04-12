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

package org.silverpeas.core.subscription.web;

import org.silverpeas.core.subscription.SubscriptionSubscriber;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * A matcher between a web subscriber entity and a subscriber it should represent.
 */
public class SubscriberEntityMatcher extends BaseMatcher<SubscriberEntity> {

  private SubscriptionSubscriber subscriber;

  /**
   * Creates a new matcher with the specified subscriber.
   * @param theSubscriber the subscriber to match.
   * @return a subscriber matcher.
   */
  public static SubscriberEntityMatcher matches(final SubscriptionSubscriber theSubscriber) {
    return new SubscriberEntityMatcher(theSubscriber);
  }

  @Override
  public boolean matches(Object item) {
    if (item instanceof SubscriberEntity) {
      SubscriberEntity actual = (SubscriberEntity) item;
      EqualsBuilder match = new EqualsBuilder();
      match.append(subscriber.getId(), actual.getId());
      switch (subscriber.getType()) {
        case GROUP:
          match.appendSuper(actual.isGroup());
          match.appendSuper(!actual.isUser());
          break;
        case USER:
          match.appendSuper(!actual.isGroup());
          match.appendSuper(actual.isUser());
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
    description.appendValue(subscriber);
  }

  private SubscriberEntityMatcher(final SubscriptionSubscriber Subscriber) {
    this.subscriber = Subscriber;
  }
}
