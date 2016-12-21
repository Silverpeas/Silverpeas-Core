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
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.web.usercalendar.services;

import org.silverpeas.core.admin.component.model.PersonalComponentInstance;
import org.silverpeas.core.annotation.RequestScoped;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.calendar.event.Attendee;
import org.silverpeas.core.calendar.event.CalendarEvent;
import org.silverpeas.core.webapi.base.UserPrivilegeValidation;
import org.silverpeas.core.webapi.base.annotation.Authorized;
import org.silverpeas.core.webapi.calendar.CalendarEntity;
import org.silverpeas.core.webapi.calendar.CalendarEventAttendeeEntity;
import org.silverpeas.core.webapi.calendar.CalendarEventEntity;
import org.silverpeas.core.webapi.calendar.CalendarEventOccurrenceEntity;
import org.silverpeas.core.webapi.calendar.CalendarResource;
import org.silverpeas.core.webapi.calendar.CalendarWebServiceProvider;

import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.silverpeas.core.webapi.calendar.CalendarResourceURIs.*;
import static org.silverpeas.web.usercalendar.services.UserCalendarResource.USER_CALENDAR_BASE_URI;

/**
 * A REST Web resource giving calendar data.
 * @author Yohann Chastagnier
 */
@Service
@RequestScoped
@Path(USER_CALENDAR_BASE_URI + "/{componentInstanceId}")
@Authorized
public class UserCalendarResource extends CalendarResource {

  static final String USER_CALENDAR_BASE_URI = "usercalendar";

  @Override
  public void validateUserAuthorization(final UserPrivilegeValidation validation)
      throws WebApplicationException {
    if (!PersonalComponentInstance.from(getComponentId()).isPresent()) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
  }

  /**
   * In case of personal calendar, and if the calendar reference concerns the personal calendar of
   * the current user, then participation occurrences are added to the result.
   * @param calendar the calendar which the events belong to.
   * @param startDate the start date of the request.
   * @param endDate the end date of the request.
   * @return all the requested occurrences.
   */
  @Override
  protected List<CalendarEventOccurrenceEntity> getEventOccurrencesOf(final Calendar calendar,
      final LocalDate startDate, final LocalDate endDate) {
    List<CalendarEventOccurrenceEntity> result =
        super.getEventOccurrencesOf(calendar, startDate, endDate);
    if (calendar.getCreatedBy().equals(getUserDetail().getId()) &&
        calendar.getTitle().equals(getUserDetail().getDisplayedName())) {
      // Add occurrence participation of user
      List<CalendarEventOccurrenceEntity> participationOccurrences =
          getAllEventOccurrencesFrom(startDate, endDate, Collections.singleton(getUserDetail()))
              .get(0).getOccurrences();
      result.addAll(participationOccurrences);
    }
    return result;
  }

  @Override
  public <T extends CalendarEntity> T asWebEntity(final Calendar calendar) {
    return super.asWebEntity(calendar).withURI(buildCalendarURI(USER_CALENDAR_BASE_URI, calendar));
  }

  @Override
  public <T extends CalendarEventEntity> T asEventWebEntity(final CalendarEvent event) {
    return super.asEventWebEntity(event)
        .withURI(buildCalendarEventURI(USER_CALENDAR_BASE_URI, event))
        .withCalendarURI(buildCalendarURI(USER_CALENDAR_BASE_URI, event.getCalendar()));
  }

  @Override
  public <T extends CalendarEventAttendeeEntity> T asAttendeeWebEntity(final Attendee attendee) {
    return super.asAttendeeWebEntity(attendee)
        .withURI(buildCalendarEventAttendeeURI(USER_CALENDAR_BASE_URI, attendee));
  }
}
