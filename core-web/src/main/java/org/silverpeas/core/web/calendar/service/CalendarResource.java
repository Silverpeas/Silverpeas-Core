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
package org.silverpeas.core.web.calendar.service;

import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.annotation.RequestScoped;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.webapi.base.annotation.Authorized;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import static org.silverpeas.core.web.calendar.service.CalendarResourceURIs.*;

/**
 * A REST Web resource giving calendar data.
 * @author Yohann Chastagnier
 */
@Service
@RequestScoped
@Path(CALENDAR_BASE_URI + "/{componentInstanceId}")
@Authorized
public class CalendarResource extends AbstractCalendarResource {

  @Inject
  private CalendarWebServiceProvider calendarWebServiceProvider;

  /**
   * Gets the JSON representation of a list of calendar.
   * If it doesn't exist, a 404 HTTP code is returned.
   * @return the response to the HTTP GET request with the JSON representation of the asked
   * calendars.
   * @see WebProcess#execute()
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<CalendarEntity> getCalendars() {
    return process(() -> calendarWebServiceProvider.getCalendarsOf(getComponentId()))
        .lowestAccessRole(SilverpeasRole.admin).execute();
  }

  /**
   * Gets the JSON representation of a list of calendar event occurrence.
   * If it doesn't exist, a 404 HTTP code is returned.
   * @return the response to the HTTP GET request with the JSON representation of the asked
   * calendars.
   * @see WebProcess#execute()
   */
  @GET
  @Path("{calendarId}/" + CALENDAR_EVENT_URI_PART + "/" + CALENDAR_EVENT_OCCURRENCE_URI_PART)
  @Produces(MediaType.APPLICATION_JSON)
  public List<CalendarEventOccurrenceEntity> getEventOccurrencesOf(
      @PathParam("calendarId") String calendarId,
      @QueryParam("startDateTime") Date startDateOfWindowTime,
      @QueryParam("endDateOfWindowTime") Date endDateTime) {
    Calendar calendar = Calendar.getById(calendarId);
    return process(() -> calendarWebServiceProvider
        .getEventOccurrencesOf(calendar, LocalDate.from(startDateOfWindowTime.toInstant()),
            LocalDate.from(endDateTime.toInstant()))).lowestAccessRole(SilverpeasRole.admin)
        .execute();
  }
}
