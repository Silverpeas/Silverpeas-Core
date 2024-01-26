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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.web.calendar;

import org.silverpeas.core.calendar.CalendarEventOccurrence;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.web.mvc.route.AbstractComponentInstanceRoutingMap;
import org.silverpeas.core.webapi.calendar.CalendarWebManager;

import java.net.URI;

import static org.silverpeas.kernel.util.StringUtil.asBase64;

/**
 * Routing map centralization about the calendar event resources.
 * @author silveryocha
 */
public abstract class AbstractCalendarInstanceRoutingMap
    extends AbstractComponentInstanceRoutingMap {

  @Override
  public URI getViewPage(final ContributionIdentifier contributionIdentifier) {
    if (CalendarEventOccurrence.TYPE.equals(contributionIdentifier.getType())) {
      return getOccurrenceViewPage(contributionIdentifier);
    }
    return getEventViewPage(contributionIdentifier);
  }

  private URI getEventViewPage(final ContributionIdentifier contributionIdentifier) {
    final CalendarEventOccurrence occurrence = CalendarWebManager
        .get(contributionIdentifier.getComponentInstanceId())
        .getFirstCalendarEventOccurrenceFromEventId(contributionIdentifier.getLocalId());
    return newUriBuilder(getBaseForPages(), "calendars/occurrences",
        asBase64(occurrence.getId().getBytes())).build();
  }

  private URI getOccurrenceViewPage(final ContributionIdentifier contributionIdentifier) {
    return newUriBuilder(getBaseForPages(), "calendars/occurrences",
        asBase64(contributionIdentifier.getLocalId().getBytes())).build();
  }
}
