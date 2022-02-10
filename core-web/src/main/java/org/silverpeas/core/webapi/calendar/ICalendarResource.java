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

package org.silverpeas.core.webapi.calendar;

import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.calendar.CalendarReference;
import org.silverpeas.core.importexport.ExportDescriptor;
import org.silverpeas.core.importexport.ExportException;
import org.silverpeas.core.security.token.persistent.PersistentResourceToken;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.rs.RESTWebService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.silverpeas.core.calendar.icalendar.ICalendarExporter.CALENDAR;
import static org.silverpeas.core.calendar.icalendar.ICalendarExporter.HIDE_PRIVATE_DATA;
import static org.silverpeas.core.webapi.calendar.CalendarResourceURIs.CALENDAR_BASE_URI;
import static org.silverpeas.core.webapi.calendar.CalendarWebManager.assertEntityIsDefined;

/**
 * A REST Web resource giving calendar data.
 * @author Yohann Chastagnier
 */
@WebService
@Path(ICalendarResource.PATH)
public class ICalendarResource extends RESTWebService {

  static final String PATH = CALENDAR_BASE_URI + "/ical";

  @Override
  protected String getResourceBasePath() {
    return PATH;
  }

  /**
   * Gets the JSON representation of a calendar represented by the given identifier.
   * If it doesn't exist, a 404 HTTP code is returned.
   * @param calendarId the identifier of the aimed calendar
   * @return the response to the HTTP GET request with the JSON representation of the asked
   * calendar.
   * @see WebProcess#execute()
   */
  @GET
  @Path("public/{calendarId}")
  @Produces("text/calendar")
  public Response getCalendar(@PathParam("calendarId") String calendarId) {
    final Calendar calendar = Calendar.getById(calendarId);
    assertEntityIsDefined(calendar);
    return export(calendar, true);
  }

  /**
   * Gets the JSON representation of a list of calendar.
   * If it doesn't exist, a 404 HTTP code is returned.
   * @return the response to the HTTP GET request with the JSON representation of the asked
   * calendars.
   * @see WebProcess#execute()
   */
  @GET
  @Path("private/{token}")
  @Produces("text/calendar")
  public Response privateExport(@PathParam("token") String token) {
    final PersistentResourceToken calendarToken = PersistentResourceToken.getToken(token);
    CalendarReference calendarRef = calendarToken.getResource(CalendarReference.class);
    if (calendarRef == null) {
      throw new WebApplicationException(NOT_FOUND);
    }
    final Calendar calendar = calendarRef.getEntity();
    assertEntityIsDefined(calendar);
    return export(calendar, false);
  }

  /**
   * Exports the given calendar into the body of the response.
   * @param calendar the calendar to export.
   * @param hidePrivateData indicates if private data must be hidden.
   * @return the configured response.
   */
  private Response export(final Calendar calendar, final boolean hidePrivateData) {
    return Response.ok((StreamingOutput) output -> {
      try {
        final ExportDescriptor descriptor = ExportDescriptor
            .withOutputStream(output)
            .withParameter(CALENDAR, calendar)
            .withParameter(HIDE_PRIVATE_DATA, hidePrivateData);
        getCalendarWebManager().exportCalendarAsICalendarFormat(calendar, descriptor);
      } catch (ExportException e) {
        SilverLogger.getLogger(this).error(e);
        throw new WebApplicationException(INTERNAL_SERVER_ERROR);
      }
    })
    .header("Content-Disposition",
        String.format("inline;filename=\"%s\"", calendar.getTitle() + ".ics"))
    .build();
  }

  @Override
  public String getComponentId() {
    return null;
  }

  private CalendarWebManager getCalendarWebManager() {
    return CalendarWebManager.get(null);
  }
}
