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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.webapi.calendar;


import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.calendar.Attendee;
import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.calendar.CalendarEventOccurrence;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.web.mvc.route.ComponentInstanceRoutingMapProvider;
import org.silverpeas.core.web.mvc.route.ComponentInstanceRoutingMapProviderByInstance;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Base64;

/**
 * Base URIs from which the REST-based resources representing calendar and event entities are
 * defined.
 * @author Yohann Chastagnier
 */
@Bean
@Singleton
public final class CalendarResourceURIs {

  static final String CALENDAR_EVENT_URI_PART = "events";
  static final String CALENDAR_EVENT_ATTENDEE_URI_PART = "attendees";
  static final String CALENDAR_EVENT_OCCURRENCE_URI_PART = "occurrences";
  static final String CALENDAR_BASE_URI = "calendar";

  @Inject
  private ComponentInstanceRoutingMapProviderByInstance routingMapProvider;

  public static CalendarResourceURIs get() {
    return ServiceProvider.getSingleton(CalendarResourceURIs.class);
  }

  /**
   * Centralizes the build of a calendar URI.
   * @param calendar the aimed calendar.
   * @return the URI of specified calendar.
   */
  public URI ofCalendar(final Calendar calendar) {
    if (calendar == null || !calendar.isPersisted()) {
      return null;
    }
    return getCalendarUriBuilder(calendar).build();
  }

  /**
   * Centralizes the build of a ical private URI.
   * @param calendar the aimed calendar.
   * @return the URI of specified calendar.
   */
  URI ofICalPrivate(final Calendar calendar) {
    if (calendar == null || !calendar.isPersisted()) {
      return null;
    }
    return getICalUriBuilder(calendar).path("private").path(calendar.getToken()).build();
  }

  /**
   * Centralizes the build of a ical public URI.
   * @param calendar the aimed calendar.
   * @return the URI of specified calendar.
   */
  URI ofICalPublic(Calendar calendar) {
    if (calendar == null || !calendar.isPersisted()) {
      return null;
    }
    return getICalUriBuilder(calendar).path("public").path(calendar.getId()).build();
  }

  /**
   * Centralizes the build of a calendar event URI.
   * @param event the aimed calendar event.
   * @return the URI of specified calendar event.
   */
  URI ofEvent(CalendarEvent event) {
    if (event == null) {
      return null;
    }
    return getEventUriBuilder(event).build();
  }

  /**
   * Centralizes the build of a occurrence URI.
   * @param occurrence the aimed occurrence.
   * @return the URI of specified occurrence.
   */
  public URI ofOccurrence(CalendarEventOccurrence occurrence) {
    if (occurrence == null) {
      return null;
    }
    return getOccurrenceUriBuilder(occurrence).build();
  }

  /**
   * Centralizes the build of an event permalink URI.
   * @param occurrence the aimed occurrence.
   * @return the permalink URI of specified occurrence.
   */
  URI ofEventPermalink(CalendarEventOccurrence occurrence) {
    if (occurrence == null) {
      return null;
    }
    return URI.create(occurrence.getCalendarEvent().getPermalink());
  }

  /**
   * Centralizes the build of an occurrence permalink URI.
   * @param occurrence te aimed occurrence.
   * @return the permalink URI of specified occurrence.
   */
  public URI ofOccurrencePermalink(final CalendarEventOccurrence occurrence) {
    if (occurrence == null) {
      return null;
    }
    return URI.create(occurrence.getPermalink());
  }

  /**
   * Centralizes the build of a occurrence view page URI.
   * @param occurrence the aimed occurrence.
   * @return the URI of specified occurrence.
   */
  public URI ofOccurrenceView(CalendarEventOccurrence occurrence) {
    if (occurrence == null) {
      return null;
    }
    final String instanceId = occurrence.getCalendarEvent().getCalendar().getComponentInstanceId();
    return getRoutingMap(instanceId).relativeToSilverpeas().getViewPage(occurrence.getIdentifier());
  }

  /**
   * Centralizes the build of a occurrence edit page URI.
   * @param occurrence the aimed occurrence.
   * @return the URI of specified occurrence.
   */
  URI ofOccurrenceEdition(CalendarEventOccurrence occurrence) {
    if (occurrence == null) {
      return null;
    }
    final String instanceId = occurrence.getCalendarEvent().getCalendar().getComponentInstanceId();
    return getRoutingMap(instanceId).relativeToSilverpeas().getEditionPage(occurrence.getIdentifier());
  }

  /**
   * Centralizes the build of a calendar event attendee URI.
   * @param occurrence the aimed occurrence.
   * @param attendee the aimed calendar event attendee.  @return the URI of specified calendar
   * event.
   */
  URI ofOccurrenceAttendee(final CalendarEventOccurrence occurrence, Attendee attendee) {
    if (attendee == null) {
      return null;
    }
    return getOccurrenceUriBuilder(occurrence)
        .path(CALENDAR_EVENT_ATTENDEE_URI_PART).path(attendee.getId())
        .build();
  }

  private UriBuilder getICalUriBuilder(final Calendar calendar) {
    final String calendarComponentInstanceId = calendar.getComponentInstanceId();
    return getRoutingMap(calendarComponentInstanceId).absolute().getWebResourceUriBuilder()
        .path("ical");
  }

  private UriBuilder getCalendarUriBuilder(final Calendar calendar) {
    final String calendarComponentInstanceId = calendar.getComponentInstanceId();
    return getRoutingMap(calendarComponentInstanceId).relativeToSilverpeas().getWebResourceUriBuilder()
        .path(calendarComponentInstanceId).path(calendar.getId());
  }

  private UriBuilder getEventUriBuilder(final CalendarEvent event) {
    return getCalendarUriBuilder(event.getCalendar())
        .path(CALENDAR_EVENT_URI_PART)
        .path(event.getId());
  }

  private UriBuilder getOccurrenceUriBuilder(final CalendarEventOccurrence occurrence) {
    return getEventUriBuilder(occurrence.getCalendarEvent())
        .path(CALENDAR_EVENT_OCCURRENCE_URI_PART)
        .path(Base64.getEncoder().encodeToString(occurrence.getId().getBytes()));
  }

  private ComponentInstanceRoutingMapProvider getRoutingMap(final String componentInstanceId) {
    return routingMapProvider.getByInstanceId(componentInstanceId);
  }
}
