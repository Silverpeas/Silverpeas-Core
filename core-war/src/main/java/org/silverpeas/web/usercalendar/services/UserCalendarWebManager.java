/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

package org.silverpeas.web.usercalendar.services;

import org.apache.commons.lang3.tuple.Pair;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.calendar.CalendarEventOccurrence;
import org.silverpeas.core.webapi.calendar.CalendarWebManager;

import javax.inject.Named;
import java.time.LocalDate;
import java.util.List;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;

/**
 * @author Yohann Chastagnier
 */
@Service
@Named("userCalendar" + CalendarWebManager.NAME_SUFFIX)
public class UserCalendarWebManager extends CalendarWebManager {

  protected UserCalendarWebManager() {
  }

  @Override
  public List<CalendarEventOccurrence> getEventOccurrencesOf(final LocalDate startDate,
      final LocalDate endDate, final List<Calendar> calendars, final User currentRequester) {
    List<CalendarEventOccurrence> result =
        super.getEventOccurrencesOf(startDate, endDate, calendars, currentRequester);
    calendars.stream().filter(c -> c.isMainPersonalOf(currentRequester)).forEach(p -> {
      // Add occurrence participation of user
      final List<CalendarEventOccurrence> participationOccurrences =
          getAllEventOccurrencesByUserIds(
              Pair.of(singletonList(p.getComponentInstanceId()), currentRequester),
              startDate, endDate, singleton(currentRequester))
              .get(currentRequester.getId());
      result.addAll(participationOccurrences);
    });
    return result;
  }
}
