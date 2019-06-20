/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.web.calendar;

import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.calendar.CalendarEventOccurrence;
import org.silverpeas.core.calendar.ComponentInstanceCalendars;
import org.silverpeas.core.web.mvc.webcomponent.WebComponentRequestContext;
import org.silverpeas.core.webapi.calendar.CalendarResourceURIs;

import javax.ws.rs.WebApplicationException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.Temporal;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.silverpeas.core.SilverpeasExceptionMessages.unknown;
import static org.silverpeas.core.util.StringUtil.isDefined;
import static org.silverpeas.core.webapi.calendar.CalendarEventOccurrenceEntity.decodeId;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractCalendarWebRequestContext<T extends AbstractCalendarWebController>
    extends WebComponentRequestContext<T> {

  private ComponentInstanceCalendars componentInstanceCalendars;
  private CalendarResourceURIs uri;

  @Override
  public void beforeRequestProcessing() {
    setComponentInstanceCalendars(Calendar.getByComponentInstanceId(getComponentInstanceId()));
  }

  /**
   * Gets the calendars.
   * @return list of calendars.
   */
  protected ComponentInstanceCalendars getComponentInstanceCalendars() {
    return componentInstanceCalendars;
  }

  protected void setComponentInstanceCalendars(
      final ComponentInstanceCalendars componentInstanceCalendars) {
    this.componentInstanceCalendars = componentInstanceCalendars;
  }

  /**
   * Gets the calendar URI producer.
   * @return the calendar URI producer.
   */
  public CalendarResourceURIs uri() {
    if (uri == null) {
      uri = CalendarResourceURIs.get();
    }
    return uri;
  }

  /**
   * Gets the main calendar.
   * @return the main calendar linked to the instance.
   * @throws WebApplicationException with {@link javax.ws.rs.core.Response.Status#NOT_FOUND} code if
   * no calendars exist for component instance.
   */
  public Calendar getMainCalendar() {
    return getComponentInstanceCalendars().getMainCalendar()
        .orElseThrow(() -> new WebApplicationException(NOT_FOUND));
  }

  /**
   * Gets the calendar corresponding to the identifier contained into request as {@code
   * calendarId} parameter name.
   * @return a calendar.
   * @throw WebApplicationException when calendarId is set but is not linked to the current user.
   */
  public Temporal getOccurrenceStartDate() {
    String startDate = getRequest().getParameter("occurrenceStartDate");
    if (isDefined(startDate)) {
      if (startDate.contains("T")) {
        return OffsetDateTime.parse(startDate);
      } else {
        return LocalDate.parse(startDate);
      }
    }
    return null;
  }

  /**
   * Gets the event occurrence corresponding to the identifier contained into request as
   * {@code occurrenceId} parameter name.
   * @return an event.
   * @throw WebApplicationException when calendarId is set but is not linked to the current user.
   */
  public CalendarEventOccurrence getCalendarEventOccurrenceById() {
    String occurrenceId = getPathVariables().get("occurrenceId");
    return getCalendarEventOccurrence(occurrenceId);
  }

  static CalendarEventOccurrence getCalendarEventOccurrence(final String occurrenceId) {
    CalendarEventOccurrence event = null;
    if (isDefined(occurrenceId)) {
      final String decodedId = decodeId(occurrenceId);
      event = CalendarEventOccurrence.getById(decodedId).orElse(null);
      if (event == null) {
        throw new WebApplicationException(unknown("calendar", decodedId), NOT_FOUND);
      }
    }
    return event;
  }
}
