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

package org.silverpeas.core.webapi.calendar;


import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.calendar.event.Attendee;
import org.silverpeas.core.calendar.event.CalendarEvent;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.webapi.base.RESTWebService;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * Base URIs from which the REST-based ressources representing calendar and event entities are
 * defined.
 * @author Yohann Chastagnier
 */
public final class CalendarResourceURIs {

  static final String CALENDAR_EVENT_URI_PART = "events";
  static final String CALENDAR_EVENT_ATTENDEE_URI_PART = "attendees";
  static final String CALENDAR_EVENT_OCCURRENCE_URI_PART = "occurrences";
  public static final String CALENDAR_BASE_URI = "calendar";

  /**
   * Centralizes the build of a calendar URI.
   * @param baseUri the base URI of the service.
   * @param calendar the aimed calendar.
   * @return the URI of specified calendar.
   */
  public static URI buildCalendarURI(String baseUri, Calendar calendar) {
    if (calendar == null || !calendar.isPersisted()) {
      return null;
    }
    return UriBuilder.fromUri(URLUtil.getApplicationURL())
        .path(RESTWebService.REST_WEB_SERVICES_URI_BASE).path(baseUri)
        .path(calendar.getComponentInstanceId())
        .path(calendar.getId()).build();
  }

  /**
   * Centralizes the build of a calendar event URI.
   * @param baseUri the base URI of the service.
   * @param event the aimed calendar event.
   * @return the URI of specified calendar event.
   */
  public static URI buildCalendarEventURI(String baseUri, CalendarEvent event) {
    if (event == null) {
      return null;
    }
    return UriBuilder.fromUri(URLUtil.getApplicationURL())
        .path(RESTWebService.REST_WEB_SERVICES_URI_BASE).path(baseUri)
        .path(event.getCalendar().getComponentInstanceId())
        .path(event.getCalendar().getId())
        .path(CALENDAR_EVENT_URI_PART).path(event.getId()).build();
  }

  /**
   * Centralizes the build of a calendar event attendee URI.
   * @param baseUri the base URI of the service.
   * @param attendee the aimed calendar event attendee.
   * @return the URI of specified calendar event.
   */
  public static URI buildCalendarEventAttendeeURI(String baseUri, Attendee attendee) {
    if (attendee == null) {
      return null;
    }
    return UriBuilder.fromUri(URLUtil.getApplicationURL())
        .path(RESTWebService.REST_WEB_SERVICES_URI_BASE).path(baseUri)
        .path(attendee.getEvent().getCalendar().getComponentInstanceId())
        .path(attendee.getEvent().getCalendar().getId())
        .path(CALENDAR_EVENT_URI_PART).path(attendee.getEvent().getId())
        .path(CALENDAR_EVENT_ATTENDEE_URI_PART).path(attendee.getId()).build();
  }
}
