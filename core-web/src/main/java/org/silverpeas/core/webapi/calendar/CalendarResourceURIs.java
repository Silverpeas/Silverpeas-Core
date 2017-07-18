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


import org.silverpeas.core.calendar.Attendee;
import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.calendar.CalendarEventOccurrence;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.webapi.base.RESTWebService;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Base64;

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
   * Hidden constructor.
   */
  private CalendarResourceURIs() {
  }

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
    return getCalendarUriBuilder(baseUri, calendar).build();
  }

  /**
   * Centralizes the build of a ical private URI.
   * @param request the current request.
   * @param baseUri the base URI of the service.
   * @param calendar the aimed calendar.
   * @return the URI of specified calendar.
   */
  public static URI buildIcalPrivateURI(final HttpServletRequest request, String baseUri,
      Calendar calendar) {
    if (calendar == null || !calendar.isPersisted()) {
      return null;
    }
    return getICalUriBuilder(request, baseUri).path("private").path(calendar.getToken()).build();
  }

  /**
   * Centralizes the build of a ical public URI.
   * @param request the current request.
   * @param baseUri the base URI of the service.
   * @param calendar the aimed calendar.
   * @return the URI of specified calendar.
   */
  public static URI buildIcalPublicURI(final HttpServletRequest request, String baseUri,
      Calendar calendar) {
    if (calendar == null || !calendar.isPersisted()) {
      return null;
    }
    return getICalUriBuilder(request, baseUri).path("public").path(calendar.getId()).build();
  }

  /**
   * Centralizes the build of a calendar event URI.
   * @param baseUri the base URI of the service.
   * @param event the aimed calendar event.
   * @return the URI of specified calendar event.
   */
  public static URI buildEventURI(String baseUri, CalendarEvent event) {
    if (event == null) {
      return null;
    }
    return getEventUriBuilder(baseUri, event).build();
  }

  /**
   * Centralizes the build of a occurrence URI.
   * @param baseUri the base URI of the service.
   * @param occurrence the aimed occurrence.
   * @return the URI of specified occurrence.
   */
  public static URI buildOccurrenceURI(String baseUri, CalendarEventOccurrence occurrence) {
    if (occurrence == null) {
      return null;
    }
    return getOccurrenceUriBuilder(baseUri, occurrence).build();
  }

  /**
   * Centralizes the build of a occurrence view page URI.
   * @param occurrence the aimed occurrence.
   * @return the URI of specified occurrence.
   */
  public static URI buildOccurrenceViewURI(CalendarEventOccurrence occurrence) {
    if (occurrence == null) {
      return null;
    }
    return getComponentUriBuilder(
        occurrence.getCalendarEvent().getCalendar().getComponentInstanceId())
        .path("calendars/occurrences")
        .path(Base64.getEncoder().encodeToString(occurrence.getId().getBytes())).build();
  }

  /**
   * Centralizes the build of a calendar event attendee URI.
   * @param baseUri the base URI of the service.
   * @param occurrence the aimed occurrence.
   *@param attendee the aimed calendar event attendee.  @return the URI of specified calendar event.
   */
  public static URI buildOccurrenceAttendeeURI(String baseUri,
      final CalendarEventOccurrence occurrence, Attendee attendee) {
    if (attendee == null) {
      return null;
    }
    return getOccurrenceUriBuilder(baseUri, occurrence)
        .path(CALENDAR_EVENT_ATTENDEE_URI_PART).path(attendee.getId())
        .build();
  }

  private static UriBuilder getBaseUri(final HttpServletRequest request, final String baseUri) {
    return UriBuilder.fromUri(URLUtil.getFullApplicationURL(request))
        .path(RESTWebService.REST_WEB_SERVICES_URI_BASE).path(baseUri);
  }

  private static UriBuilder getBaseUri(final String baseUri) {
    return UriBuilder.fromUri(URLUtil.getApplicationURL())
        .path(RESTWebService.REST_WEB_SERVICES_URI_BASE).path(baseUri);
  }

  private static UriBuilder getICalUriBuilder(final HttpServletRequest request, final String baseUri) {
    return getBaseUri(request, baseUri).path("ical");
  }

  private static UriBuilder getCalendarUriBuilder(final String baseUri, final Calendar calendar) {
    return getBaseUri(baseUri)
        .path(calendar.getComponentInstanceId())
        .path(calendar.getId());
  }

  private static UriBuilder getEventUriBuilder(final String baseUri, final CalendarEvent event) {
    return getCalendarUriBuilder(baseUri, event.getCalendar())
        .path(CALENDAR_EVENT_URI_PART)
        .path(event.getId());
  }

  private static UriBuilder getOccurrenceUriBuilder(final String baseUri,
      final CalendarEventOccurrence occurrence) {
    return getEventUriBuilder(baseUri, occurrence.getCalendarEvent())
        .path(CALENDAR_EVENT_OCCURRENCE_URI_PART)
        .path(Base64.getEncoder().encodeToString(occurrence.getId().getBytes()));
  }

  private static UriBuilder getComponentUriBuilder(final String componentInstanceId) {
    return UriBuilder.fromUri(URLUtil.getApplicationURL())
        .path(URLUtil.getComponentInstanceURL(componentInstanceId));
  }
}
