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

import org.apache.commons.lang3.tuple.Pair;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.RequestScoped;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.cache.model.SimpleCache;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.calendar.event.Attendee;
import org.silverpeas.core.calendar.event.CalendarEvent;
import org.silverpeas.core.calendar.event.CalendarEventOccurrence;
import org.silverpeas.core.calendar.icalendar.ICalendarException;
import org.silverpeas.core.calendar.icalendar.ICalendarExport;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.http.RequestParameterDecoder;
import org.silverpeas.core.webapi.base.annotation.Authorized;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.silverpeas.core.util.StringUtil.isDefined;
import static org.silverpeas.core.webapi.calendar.CalendarResourceURIs.*;
import static org.silverpeas.core.webapi.calendar.CalendarWebServiceProvider.assertDataConsistency;
import static org.silverpeas.core.webapi.calendar.CalendarWebServiceProvider.assertEntityIsDefined;

/**
 * A REST Web resource giving calendar data.
 * @author Yohann Chastagnier
 */
@Service
@RequestScoped
@Path(CALENDAR_BASE_URI + "/{componentInstanceId}")
@Authorized
public class CalendarResource extends AbstractCalendarResource {

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
    List<Calendar> calendars =
        process(() -> getCalendarWebServiceProvider().getCalendarsOf(getComponentId())).execute();
    return asWebEntities(calendars);
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
  @Path("{calendarId}")
  @Produces(MediaType.APPLICATION_JSON)
  public CalendarEntity getCalendar(@PathParam("calendarId") String calendarId) {
    final Calendar calendar = process(() -> Calendar.getById(calendarId)).execute();
    assertDataConsistency(getComponentId(), calendar);
    return asWebEntity(calendar);
  }

  /**
   * Creates the calendar from its JSON representation and returns it once created.<br/>
   * If the user isn't authenticated, a 401 HTTP code is returned. If the user isn't authorized to
   * save the calendar, a 403 is returned. If a problem occurs when processing the request, a 503
   * HTTP code is returned.
   * @param calendarEntity the calendar data
   * @return the response to the HTTP POST request with the JSON representation of the created
   * calendar.
   */
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  public CalendarEntity createCalendar(CalendarEntity calendarEntity) {
    final Calendar calendar = new Calendar(getComponentId());
    calendarEntity.merge(calendar);
    Calendar createdCalendar =
        process(() -> getCalendarWebServiceProvider().saveCalendar(calendar)).execute();
    return asWebEntity(createdCalendar);
  }

  /**
   * Updates the calendar from its JSON representation and returns it once updated. If the calendar
   * to update doesn't match with the requested one, a 400 HTTP code is returned. If the calendar
   * doesn't exist, a 404 HTTP code is returned. If the user isn't authenticated, a 401 HTTP code is
   * returned. If the user isn't authorized to save the calendar, a 403 is returned. If a problem
   * occurs when processing the request, a 503 HTTP code is returned.
   * @param calendarId the identifier of the updated calendar
   * @param calendarEntity the data of the calendar
   * @return the response to the HTTP PUT request with the JSON representation of the updated
   * calendar.
   */
  @PUT
  @Path("{calendarId}")
  @Produces(MediaType.APPLICATION_JSON)
  public CalendarEntity updateCalendar(@PathParam("calendarId") String calendarId,
      CalendarEntity calendarEntity) {
    final Calendar calendar = Calendar.getById(calendarId);
    assertDataConsistency(getComponentId(), calendar);
    calendarEntity.merge(calendar);
    Calendar updatedCalendar =
        process(() -> getCalendarWebServiceProvider().saveCalendar(calendar)).execute();
    return asWebEntity(updatedCalendar);
  }

  /**
   * Deletes the calendar from its JSON identifier.
   * If the calendar doesn't exist, a 404 HTTP code is returned. If the user isn't authenticated, a
   * 401 HTTP code is returned. If the user isn't authorized to save the calendar, a 403 is
   * returned. If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @param calendarId the identifier of the deleted calendar
   */
  @DELETE
  @Path("{calendarId}")
  @Produces(MediaType.APPLICATION_JSON)
  public void deleteCalendar(@PathParam("calendarId") String calendarId) {
    final Calendar calendar = Calendar.getById(calendarId);
    assertDataConsistency(getComponentId(), calendar);
    process(() -> {
      getCalendarWebServiceProvider().deleteCalendar(calendar);
      return null;
    }).execute();
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
  @Path("{calendarId}/export/ical")
  @Produces("text/calendar")
  public Response exportCalendarAsICalendarFormat(@PathParam("calendarId") String calendarId) {
    final Calendar calendar = process(() -> Calendar.getById(calendarId)).execute();
    assertDataConsistency(getComponentId(), calendar);
    return Response.ok((StreamingOutput) output -> {
      try {
        if (calendar.isMainPersonalOf(getUserDetail())) {
          ICalendarExport
              .from(calendar, () -> Calendar.getEvents()
                                    .filter(f -> f.onParticipants(getUserDetail())).stream())
              .to(() -> output);
        } else {
          ICalendarExport
              .from(calendar, () -> Calendar.getEvents()
                                    .filter(f -> f.onCalendar(calendar)).stream())
              .to(() -> output);
        }
      } catch (ICalendarException e) {
        throw new WebApplicationException(INTERNAL_SERVER_ERROR);
      }
    })
    .header("Content-Disposition",
        String.format("inline;filename=\"%s\"", calendar.getTitle() + ".ics"))
    .build();
  }

  /**
   * Gets the JSON representation of a list of calendar event occurrence.
   * If it doesn't exist, a 404 HTTP code is returned.
   * @return the response to the HTTP GET request with the JSON representation of the asked
   * occurrences.
   * @see WebProcess#execute()
   */
  @GET
  @Path(CalendarResourceURIs.CALENDAR_EVENT_URI_PART + "/" +
      CalendarResourceURIs.CALENDAR_EVENT_OCCURRENCE_URI_PART)
  @Produces(MediaType.APPLICATION_JSON)
  public List<ParticipantCalendarEventOccurrencesEntity> getAllEventOccurrencesFrom() {
    CalendarEventOccurrenceRequestParameters params = RequestParameterDecoder
        .decode(getHttpRequest(), CalendarEventOccurrenceRequestParameters.class);
    final LocalDate startDate = params.getStartDateOfWindowTime().toLocalDate();
    final LocalDate endDate = params.getEndDateOfWindowTime().toLocalDate();
    final Set<User> users = params.getUsers();
    return getAllEventOccurrencesFrom(startDate, endDate, users);
  }

  /**
   * Can be extended.
   * @see #getAllEventOccurrencesFrom()
   */
  protected List<ParticipantCalendarEventOccurrencesEntity> getAllEventOccurrencesFrom(
      final LocalDate startDate, final LocalDate endDate, final Set<User> users) {
    Map<String, List<CalendarEventOccurrence>> occurrences = process(
        () -> getCalendarWebServiceProvider()
            .getAllEventOccurrencesByUserIds(Pair.of(getComponentId(), getUserDetail()), startDate,
                endDate, users)).execute();
    List<ParticipantCalendarEventOccurrencesEntity> webEntities = new ArrayList<>();
    users.forEach(user -> {
      webEntities.add(ParticipantCalendarEventOccurrencesEntity.from(user).withOccurrences(
          asOccurrenceWebEntities(
              Optional.ofNullable(occurrences.get(user.getId())).orElse(Collections.emptyList()))));
    });
    return webEntities;
  }

  /**
   * Gets the JSON representation of a list of calendar event occurrence.
   * If it doesn't exist, a 404 HTTP code is returned.
   * @param calendarId the identifier of calendar the occurrences must belong with
   * @return the response to the HTTP GET request with the JSON representation of the asked
   * occurrences.
   * @see WebProcess#execute()
   */
  @GET
  @Path("{calendarId}/" + CalendarResourceURIs.CALENDAR_EVENT_URI_PART + "/" +
      CalendarResourceURIs.CALENDAR_EVENT_OCCURRENCE_URI_PART)
  @Produces(MediaType.APPLICATION_JSON)
  public List<CalendarEventOccurrenceEntity> getEventOccurrencesOf(
      @PathParam("calendarId") String calendarId) {
    Calendar calendar = Calendar.getById(calendarId);
    assertDataConsistency(getComponentId(), calendar);
    CalendarEventOccurrenceRequestParameters params = RequestParameterDecoder
        .decode(getHttpRequest(), CalendarEventOccurrenceRequestParameters.class);
    final LocalDate startDate = params.getStartDateOfWindowTime().toLocalDate();
    final LocalDate endDate = params.getEndDateOfWindowTime().toLocalDate();
    return getEventOccurrencesOf(calendar, startDate, endDate);
  }

  /**
   * Can be extended.
   * @see #getEventOccurrencesOf(String) ()
   */
  protected List<CalendarEventOccurrenceEntity> getEventOccurrencesOf(final Calendar calendar,
      final LocalDate startDate, final LocalDate endDate) {
    List<CalendarEventOccurrence> occurrences = process(
        () -> getCalendarWebServiceProvider().getEventOccurrencesOf(calendar, startDate, endDate))
        .execute();
    return asOccurrenceWebEntities(occurrences);
  }

  /**
   * Gets the JSON representation of a list of calendar event occurrence of an aimed event.
   * If it doesn't exist, a 404 HTTP code is returned.
   * @param calendarId the identifier of calendar the event must belong with
   * @param eventId the identifier of event the returned occurrences must be linked with
   * @param startDate optional. If it exists, it represents  an occurrence, and so, the
   * service will return only the requested occurrence.
   * @return the response to the HTTP GET request with the JSON representation of the asked
   * occurrences.
   * @see WebProcess#execute()
   */
  @GET
  @Path("{calendarId}/" + CalendarResourceURIs.CALENDAR_EVENT_URI_PART + "/{eventId}/" +
      CalendarResourceURIs.CALENDAR_EVENT_OCCURRENCE_URI_PART)
  @Produces(MediaType.APPLICATION_JSON)
  public List<CalendarEventOccurrenceEntity> getEventOccurrencesOf(
      @PathParam("calendarId") String calendarId, @PathParam("eventId") String eventId,
      @QueryParam("startDate") String startDate) {
    final Calendar calendar = Calendar.getById(calendarId);
    final CalendarEvent event = CalendarEvent.getById(eventId);
    assertDataConsistency(calendar.getComponentInstanceId(), calendar, event);
    CalendarEventOccurrenceRequestParameters params = RequestParameterDecoder
        .decode(getHttpRequest(), CalendarEventOccurrenceRequestParameters.class);
    final Temporal temporalStart;
    final LocalDate occStartDate;
    final LocalDate occEndDate;
    if (isDefined(startDate)) {
      if (event.isOnAllDay()) {
        LocalDate date = LocalDate.parse(startDate);
        temporalStart = date;
        occStartDate = date.minusDays(1);
        occEndDate = date.plusDays(1);
      } else {
        OffsetDateTime dateTime = OffsetDateTime.parse(startDate);
        temporalStart = dateTime;
        occStartDate = dateTime.minusDays(1).toLocalDate();
        occEndDate = dateTime.plusDays(1).toLocalDate();
      }
    } else {
      temporalStart = null;
      occStartDate = params.getStartDateOfWindowTime().toLocalDate();
      occEndDate = params.getEndDateOfWindowTime().toLocalDate();
    }
    List<CalendarEventOccurrenceEntity> occurrences =
        getEventOccurrencesOf(calendar, occStartDate, occEndDate);
    occurrences.removeIf(occurrence -> !occurrence.getEvent().getId().equals(eventId) ||
        (temporalStart != null && !occurrence.getStartDate().equals(temporalStart.toString())));
    if (temporalStart != null && occurrences.isEmpty()) {
      throw new WebApplicationException(NOT_FOUND);
    }
    return occurrences;
  }

  /**
   * Creates a calendar event from the JSON representation of an occurrence and returns the
   * created event.<br/> If the user isn't authenticated, a 401 HTTP code is returned. If the user
   * isn't authorized to save the calendar, a 403 is returned. If a problem occurs when
   * processing the request, a 503 HTTP code is returned.
   * @param calendarId the identifier of calendar the event must belong with
   * @param eventEntity the calendar event data
   * @return the response to the HTTP POST request with the JSON representation of the created
   * calendar event.
   */
  @POST
  @Path("{calendarId}/" + CalendarResourceURIs.CALENDAR_EVENT_URI_PART)
  @Produces(MediaType.APPLICATION_JSON)
  public CalendarEventEntity createEvent(@PathParam("calendarId") String calendarId,
      CalendarEventEntity eventEntity) {
    final Calendar calendar = Calendar.getById(calendarId);
    assertDataConsistency(getComponentId(), calendar);
    CalendarEvent createdEvent = process(() -> getCalendarWebServiceProvider()
        .createEvent(calendar, eventEntity.getMergedPersistentModel(null)))
        .execute();
    return asEventWebEntity(createdEvent);
  }

  /**
   * Updates an event from the JSON representation of an occurrence and returns the list of
   * updated and created events.<br/> If the user isn't authenticated, a 401 HTTP code is
   * returned. If the user isn't authorized to save the calendar, a 403 is returned. If a problem
   * occurs when processing the request, a 503 HTTP code is returned.
   * @param calendarId the identifier of calendar the event must belong with
   * @param eventId the identifier of updated event
   * @param occurrenceEntity the calendar event data given threw an occurrence structure
   * @return the response to the HTTP POST request with the JSON representation of the
   * updated/created events.
   */
  @PUT
  @Path("{calendarId}/" + CalendarResourceURIs.CALENDAR_EVENT_URI_PART + "/{eventId}/" +
      CalendarResourceURIs.CALENDAR_EVENT_OCCURRENCE_URI_PART)
  @Produces(MediaType.APPLICATION_JSON)
  public List<CalendarEventEntity> updateEventOccurrence(@PathParam("calendarId") String calendarId,
      @PathParam("eventId") String eventId, CalendarEventOccurrenceUpdateEntity occurrenceEntity) {
    final Calendar originalCalendar = Calendar.getById(calendarId);
    final CalendarEvent previousEventData = CalendarEvent.getById(eventId);
    final CalendarEvent eventDataToUpdate = occurrenceEntity.getMergedPersistentEventModel();
    assertDataConsistency(getComponentId(), originalCalendar, previousEventData, eventDataToUpdate);
    List<CalendarEvent> updatedEvents = process(() -> getCalendarWebServiceProvider()
        .saveEventFromAnOccurrence(eventDataToUpdate, occurrenceEntity.getReferenceData(),
            occurrenceEntity.getUpdateMethodType()))
        .execute();
    return asEventWebEntities(updatedEvents);
  }

  /**
   * Deletes an event from the JSON representation of an occurrence and returns an updated event if
   * any.<br/> If the user isn't authenticated, a 401 HTTP code is returned. If the user isn't
   * authorized to save the calendar, a 403 is returned. If a problem occurs when processing the
   * request, a 503 HTTP code is returned.
   * @param calendarId the identifier of calendar the event must belong with
   * @param eventId the identifier of deleted event
   * @param occurrenceEntity the calendar event data given threw an occurrence structure
   * @return the response to the HTTP POST request with the JSON representation of an updated
   * event if any.
   */
  @DELETE
  @Path("{calendarId}/" + CalendarResourceURIs.CALENDAR_EVENT_URI_PART + "/{eventId}/" +
      CalendarResourceURIs.CALENDAR_EVENT_OCCURRENCE_URI_PART)
  @Produces(MediaType.APPLICATION_JSON)
  public CalendarEventEntity deleteEventOccurrence(@PathParam("calendarId") String calendarId,
      @PathParam("eventId") String eventId, CalendarEventOccurrenceDeleteEntity occurrenceEntity) {
    final Calendar originalCalendar = Calendar.getById(calendarId);
    final CalendarEvent previousEventData = CalendarEvent.getById(eventId);
    final CalendarEvent eventDataToDelete = occurrenceEntity.getMergedPersistentEventModel();
    assertDataConsistency(getComponentId(), originalCalendar, previousEventData, eventDataToDelete);
    CalendarEvent updatedEvent = process(() -> getCalendarWebServiceProvider()
        .deleteEventFromAnOccurrence(eventDataToDelete, occurrenceEntity.getReferenceData(),
            occurrenceEntity.getDeleteMethodType()))
        .execute();
    return updatedEvent != null ? asEventWebEntity(updatedEvent) : null;
  }

  /**
   * Updates the participation status of an attendee about an event.<br/> If the user isn't
   * authenticated, a 401 HTTP code is returned. If the user isn't authorized to save the calendar,
   * a 403 is returned. If a problem occurs when processing the request, a 503 HTTP code is
   * returned.
   * @param calendarId the identifier of calendar the event must belong with
   * @param eventId the identifier of event
   * @param attendeeId the identifier of the attendee belonging the event
   * @param answerEntity the new participation status with all needed data to save it.
   * @return the response to the HTTP POST request with the JSON representation of the
   * updated/created events.
   */
  @PUT
  @Path("{calendarId}/" + CalendarResourceURIs.CALENDAR_EVENT_URI_PART + "/{eventId}/" +
      CalendarResourceURIs.CALENDAR_EVENT_OCCURRENCE_URI_PART + "/" +
      CalendarResourceURIs.CALENDAR_EVENT_ATTENDEE_URI_PART + "/{attendeeId}")
  @Produces(MediaType.APPLICATION_JSON)
  public CalendarEventEntity updateEventAttendeeParticipation(
      @PathParam("calendarId") String calendarId, @PathParam("eventId") String eventId,
      @PathParam("attendeeId") String attendeeId, CalendarEventAttendeeAnswerEntity answerEntity) {
    if(StringUtil.isLong(attendeeId) && !getUserDetail().getId().equals(attendeeId)) {
      throw new WebApplicationException(FORBIDDEN);
    }
    final Calendar originalCalendar = Calendar.getById(calendarId);
    final CalendarEvent event = CalendarEvent.getById(eventId);
    assertDataConsistency(originalCalendar.getComponentInstanceId(), originalCalendar, event);
    answerEntity.setId(attendeeId);
    CalendarEvent updatedEvent = process(
        () -> getCalendarWebServiceProvider().updateEventAttendeeParticipationFromAnOccurrence(event,
            answerEntity.getOccurrence().getReferenceData(), answerEntity.getId(),
            answerEntity.getParticipationStatus(), answerEntity.getAnswerMethodType()))
        .lowestAccessRole(null)
        .execute();
    return updatedEvent != null ? asEventWebEntity(updatedEvent) : null;
  }

  /**
   * Converts the list of calendar into list of calendar web entities.
   * @param calendars the calendars to convert.
   * @return the calendar web entities.
   */
  @SuppressWarnings("unchecked")
  public <T extends CalendarEntity> List<T> asWebEntities(Collection<Calendar> calendars) {
    return calendars.stream().map(calendar -> (T) asWebEntity(calendar))
        .collect(Collectors.toList());
  }

  /**
   * Converts the calendar into its corresponding web entity. If the specified calendar isn't
   * defined, then an HTTP 404 error is sent back instead of the entity representation of the
   * calendar.
   * @param calendar the calendar to convert.
   * @return the corresponding calendar entity.
   */
  @SuppressWarnings("unchecked")
  public <T extends CalendarEntity> T asWebEntity(Calendar calendar) {
    assertEntityIsDefined(calendar);
    return (T) CalendarEntity.fromCalendar(calendar)
        .withURI(buildCalendarURI(CALENDAR_BASE_URI, calendar));
  }

  /**
   * Converts the list of calendar event into list of calendar event web entities.
   * @param events the calendar events to convert.
   * @return the calendar event web entities.
   */
  @SuppressWarnings("unchecked")
  public <T extends CalendarEventEntity> List<T> asEventWebEntities(
      Collection<CalendarEvent> events) {
    return events.stream().map(event -> (T) asEventWebEntity(event)).collect(Collectors.toList());
  }

  /**
   * Converts the calendar event into its corresponding web entity. If the specified
   * calendar event isn't defined, then an HTTP 404 error is sent back instead of the
   * entity representation of the calendar event.
   * @param event the calendar event  to convert.
   * @return the corresponding calendar event  entity.
   */
  @SuppressWarnings("unchecked")
  public <T extends CalendarEventEntity> T asEventWebEntity(CalendarEvent event) {
    assertEntityIsDefined(event);
    SimpleCache cache = CacheServiceProvider.getRequestCacheService().getCache();
    CalendarEventEntity entity = cache.get(event.getId(), CalendarEventEntity.class);
    if (entity == null) {
      entity = CalendarEventEntity.fromEvent(event, getComponentId())
          .withURI(buildCalendarEventURI(CALENDAR_BASE_URI, event))
          .withCalendarURI(buildCalendarURI(CALENDAR_BASE_URI, event.getCalendar()))
          .withAttendees(asAttendeeWebEntities(event.getAttendees()));
      cache.put(event.getId(), entity);
    }
    return (T) entity;
  }

  /**
   * Converts the list of calendar event occurrence into list of calendar event occurrence web
   * entities.
   * @param occurrences the calendar event occurrences to convert.
   * @return the calendar event occurrence web entities.
   */
  @SuppressWarnings("unchecked")
  public <T extends CalendarEventOccurrenceEntity> List<T> asOccurrenceWebEntities(
      Collection<CalendarEventOccurrence> occurrences) {
    return occurrences.stream().map(occurrence -> (T) asOccurrenceWebEntity(occurrence))
        .collect(Collectors.toList());
  }

  /**
   * Converts the calendar event occurrence into its corresponding web entity. If the specified
   * calendar event occurrence isn't defined, then an HTTP 404 error is sent back instead of the
   * entity representation of the calendar event occurrence.
   * @param occurrence the calendar event occurrence to convert.
   * @return the corresponding calendar event occurrence entity.
   */
  @SuppressWarnings("unchecked")
  public <T extends CalendarEventOccurrenceEntity> T asOccurrenceWebEntity(
      CalendarEventOccurrence occurrence) {
    assertEntityIsDefined(occurrence.getCalendarEvent());
    List<CalendarEventAttendeeEntity> attendeeEntities =
        occurrence.getCalendarEvent().getAttendees().stream().map(attendee -> {
          CalendarEventAttendeeEntity entity = asAttendeeWebEntity(attendee);
          final Optional<Attendee.ParticipationStatus> participationStatusOptional =
              attendee.getParticipationOn().get(occurrence.getStartDate());
          participationStatusOptional.ifPresent(entity::setParticipationStatus);
          return entity;
        }).collect(Collectors.toList());
    return (T) CalendarEventOccurrenceEntity.fromOccurrence(occurrence)
        .withEventEntity(asEventWebEntity(occurrence.getCalendarEvent()))
        .withAttendees(attendeeEntities);
  }

  /**
   * Converts the list of calendar event attendee into list of calendar event attendee web
   * entities.
   * @param attendees the calendar event attendees to convert.
   * @return the calendar event attendees web entities.
   */
  @SuppressWarnings("unchecked")
  public <T extends CalendarEventAttendeeEntity> List<T> asAttendeeWebEntities(
      Collection<Attendee> attendees) {
    return attendees.stream()
        .map(attendee -> (T) asAttendeeWebEntity(attendee))
        .collect(Collectors.toList());
  }

  /**
   * Converts the calendar event attendee into its corresponding web entity. If the specified
   * calendar event attendee isn't defined, then an HTTP 404 error is sent back instead of the
   * entity representation of the calendar event occurrence.
   * @param attendee the calendar event attendee to convert.
   * @return the corresponding calendar event attendee entity.
   */
  @SuppressWarnings("unchecked")
  public <T extends CalendarEventAttendeeEntity> T asAttendeeWebEntity(Attendee attendee) {
    assertEntityIsDefined(attendee.getEvent());
    return (T) CalendarEventAttendeeEntity.from(attendee)
        .withURI(buildCalendarEventAttendeeURI(CALENDAR_BASE_URI, attendee));
  }

  private CalendarWebServiceProvider getCalendarWebServiceProvider() {
    return CalendarWebServiceProvider.get();
  }
}
