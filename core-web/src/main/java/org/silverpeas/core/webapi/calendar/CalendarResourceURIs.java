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
import org.silverpeas.core.web.WebResourceUri;

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
   * @param uri the request uri.
   * web resource for a given component instance.
   * @param calendar the aimed calendar.
   * @return the URI of specified calendar.
   */
  public static URI calendarUri(final WebResourceUri uri, final Calendar calendar) {
    if (calendar == null || !calendar.isPersisted()) {
      return null;
    }
    return getCalendarUriBuilder(uri, calendar).build();
  }

  /**
   * Centralizes the build of a ical private URI.
   * @param uri the request URI.
   * @param calendar the aimed calendar.
   * @return the URI of specified calendar.
   */
  public static URI iCalPrivateURI(final WebResourceUri uri, final Calendar calendar) {
    if (calendar == null || !calendar.isPersisted()) {
      return null;
    }
    return getICalUriBuilder(uri).path("private").path(calendar.getToken()).build();
  }

  /**
   * Centralizes the build of a ical public URI.
   * @param uri the request URI
   * @param calendar the aimed calendar.
   * @return the URI of specified calendar.
   */
  public static URI iCalPublicURI(final WebResourceUri uri, Calendar calendar) {
    if (calendar == null || !calendar.isPersisted()) {
      return null;
    }
    return getICalUriBuilder(uri).path("public").path(calendar.getId()).build();
  }

  /**
   * Centralizes the build of a calendar event URI.
   * @param uri the request URI
   * @param event the aimed calendar event.
   * @return the URI of specified calendar event.
   */
  public static URI eventURI(WebResourceUri uri, CalendarEvent event) {
    if (event == null) {
      return null;
    }
    return getEventUriBuilder(uri, event).build();
  }

  /**
   * Centralizes the build of a occurrence URI.
   * @param uri the request URI
   * @param occurrence the aimed occurrence.
   * @return the URI of specified occurrence.
   */
  public static URI occurrenceURI(final WebResourceUri uri, CalendarEventOccurrence occurrence) {
    if (occurrence == null) {
      return null;
    }
    return getOccurrenceUriBuilder(uri, occurrence).build();
  }

  /**
   * Centralizes the build of a occurrence view page URI.
   * @param occurrence the aimed occurrence.
   * @return the URI of specified occurrence.
   */
  public static URI occurrenceViewURI(CalendarEventOccurrence occurrence) {
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
   * @param uri the request URI
   * @param occurrence the aimed occurrence.
   *@param attendee the aimed calendar event attendee.  @return the URI of specified calendar event.
   */
  public static URI occurrenceAttendeeURI(final WebResourceUri uri,
      final CalendarEventOccurrence occurrence, Attendee attendee) {
    if (attendee == null) {
      return null;
    }
    return getOccurrenceUriBuilder(uri, occurrence)
        .path(CALENDAR_EVENT_ATTENDEE_URI_PART).path(attendee.getId())
        .build();
  }

  private static UriBuilder getICalUriBuilder(final WebResourceUri uri) {
    return uri.getAbsoluteWebResourcePathBuilder().path("ical");
  }

  private static UriBuilder getCalendarUriBuilder(final WebResourceUri uri, final Calendar calendar) {
    return uri.getWebResourcePathBuilder().path(calendar.getId());
  }

  private static UriBuilder getEventUriBuilder(final WebResourceUri uri, final CalendarEvent event) {
    return getCalendarUriBuilder(uri, event.getCalendar())
        .path(CALENDAR_EVENT_URI_PART)
        .path(event.getId());
  }

  private static UriBuilder getOccurrenceUriBuilder(final WebResourceUri uri,
      final CalendarEventOccurrence occurrence) {
    return getEventUriBuilder(uri, occurrence.getCalendarEvent())
        .path(CALENDAR_EVENT_OCCURRENCE_URI_PART)
        .path(Base64.getEncoder().encodeToString(occurrence.getId().getBytes()));
  }

  private static UriBuilder getComponentUriBuilder(final String componentInstanceId) {
    return UriBuilder.fromUri(URLUtil.getApplicationURL())
        .path(URLUtil.getComponentInstanceURL(componentInstanceId));
  }
}
