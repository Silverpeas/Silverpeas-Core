/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.calendar.subscription;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.subscription.service.PKSubscriptionResource;

import static org.silverpeas.core.calendar.subscription.CalendarSubscriptionConstants.CALENDAR;

/**
 * @author silveryocha
 */
public class CalendarSubscriptionResource extends PKSubscriptionResource {

  /**
   * A way to get an instance of a forum subscription resource.
   * @param calendar a calendar instance.
   * @return the corresponding {@link CalendarSubscriptionResource} instance.
   */
  public static CalendarSubscriptionResource from(final Calendar calendar) {
    return new CalendarSubscriptionResource(new ResourceReference(calendar.getId(), calendar.getComponentInstanceId()));
  }

  /**
   * Default constructor
   * @param reference a calendar reference.
   */
  public CalendarSubscriptionResource(final ResourceReference reference) {
    super(reference, CALENDAR);
  }
}
