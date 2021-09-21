/*
 * Copyright (C) 2000 - 2021 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.calendar.subscription;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.subscription.SubscriberDirective;
import org.silverpeas.core.subscription.SubscriptionFactory;
import org.silverpeas.core.subscription.SubscriptionResourceType;
import org.silverpeas.core.subscription.SubscriptionSubscriber;
import org.silverpeas.core.subscription.service.AbstractResourceSubscriptionService;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberList;

import java.util.Collection;
import java.util.HashSet;

import static org.silverpeas.core.calendar.subscription.CalendarSubscriptionConstants.CALENDAR;
import static org.silverpeas.core.subscription.SubscriptionServiceProvider.getSubscribeService;
import static org.silverpeas.core.subscription.constant.CommonSubscriptionResourceConstants.COMPONENT;

/**
 * As the class is implementing {@link org.silverpeas.core.initialization.Initialization}, no
 * annotation appears in order to be taken into account by CDI.<br>
 * The service will be taken in charge by initialization treatments.
 * @author silveryocha
 */
public abstract class AbstractCalendarSubscriptionService extends AbstractResourceSubscriptionService {

  @Override
  public void init() throws Exception {
    super.init();
    SubscriptionFactory.get().register(CALENDAR,
        (r, s, i) -> new CalendarSubscriptionResource(new ResourceReference(r, i)),
        (s, r, c) -> new CalendarSubscription(s, (CalendarSubscriptionResource) r, c));
  }

  @Override
  public SubscriptionSubscriberList getSubscribersOfComponentAndTypedResource(
      final String componentInstanceId, final SubscriptionResourceType resourceType,
      final String resourceId, final SubscriberDirective... directives) {
    final Collection<SubscriptionSubscriber> subscribers = new HashSet<>();
    SubscriptionResourceType nextTypeToHandle = resourceType;
    if (nextTypeToHandle == CALENDAR) {
      // In that case, subscribers of forum and their parents and of component must be verified.
      verifyCalendar(componentInstanceId, resourceId, subscribers);
      nextTypeToHandle = COMPONENT;
    }
    if (nextTypeToHandle == COMPONENT) {
      // In that case, subscribers of component must be verified.
      subscribers.addAll(super.getSubscribersOfComponentAndTypedResource(componentInstanceId,
          COMPONENT, resourceId));
    }
    return new SubscriptionSubscriberList(subscribers);
  }

  private void verifyCalendar(final String componentInstanceId, final String resourceId,
      final Collection<SubscriptionSubscriber> subscribers) {
    final Calendar calendar = Calendar.getById(resourceId);
    if (calendar != null && calendar.getComponentInstanceId().equals(componentInstanceId)) {
      subscribers.addAll(getSubscribeService().getSubscribers(CalendarSubscriptionResource.from(calendar)));
    }
  }
}
