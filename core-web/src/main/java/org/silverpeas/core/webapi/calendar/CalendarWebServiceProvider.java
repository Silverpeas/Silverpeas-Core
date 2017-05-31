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
import org.silverpeas.core.calendar.Attendee;
import org.silverpeas.core.calendar.Attendee.ParticipationStatus;
import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.calendar.CalendarEvent.EventOperationResult;
import org.silverpeas.core.calendar.CalendarEventOccurrence;
import org.silverpeas.core.calendar.ICalendarEventImportProcessor;
import org.silverpeas.core.calendar.ICalendarImportResult;
import org.silverpeas.core.calendar.Plannable;
import org.silverpeas.core.calendar.icalendar.ICalendarExporter;
import org.silverpeas.core.calendar.view.CalendarEventInternalParticipationView;
import org.silverpeas.core.importexport.ExportDescriptor;
import org.silverpeas.core.importexport.ExportException;
import org.silverpeas.core.importexport.ImportException;
import org.silverpeas.core.persistence.datasource.model.Entity;
import org.silverpeas.core.persistence.datasource.model.IdentifiableEntity;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.comparator.AbstractComplexComparator;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.mvc.webcomponent.WebMessager;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.silverpeas.core.calendar.CalendarEventUtil.getDateWithOffset;
import static org.silverpeas.core.util.StringUtil.isNotDefined;
import static org.silverpeas.core.webapi.calendar.OccurrenceEventActionMethodType.ALL;
import static org.silverpeas.core.webapi.calendar.OccurrenceEventActionMethodType.UNIQUE;

/**
 * @author Yohann Chastagnier
 */
@Singleton
public class CalendarWebServiceProvider {

  private final SilverLogger silverLogger = SilverLogger.getLogger(Plannable.class);

  @Inject
  private ICalendarExporter iCalendarExporter;

  @Inject
  private ICalendarEventImportProcessor iCalendarEventImportProcessor;

  private CalendarWebServiceProvider() {
  }

  /**
   * Gets the singleton instance of the provider.
   */
  public static CalendarWebServiceProvider get() {
    return ServiceProvider.getService(CalendarWebServiceProvider.class);
  }

  /**
   * Asserts the consistency of given data, otherwise an HTTP error is sent back.<br/>
   * Calendar must exists and be linked to the component instance represented bu given identifier.
   * @param componentInstanceId the identifier of current handled component instance.
   * @param originalCalendar the calendar to check against the other data.
   */
  static void assertDataConsistency(final String componentInstanceId,
      final Calendar originalCalendar) {
    assertEntityIsDefined(originalCalendar);
    if (!originalCalendar.getComponentInstanceId().equals(componentInstanceId)) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
  }

  /**
   * Asserts the consistency of given data, otherwise an HTTP error is sent back.<br/>
   * Checks of {@link #assertDataConsistency(String, Calendar)} is performed.
   * And also the followings ones:
   * <ul>
   * <li>all entities must be linked to the given component instance identifier</li>
   * <li>the calendar of previous event data must be equal</li>
   * <li>the identifier must be equal between old and new data</li>
   * </ul>
   * @param componentInstanceId the identifier of current handled component instance.
   * @param originalCalendar the calendar to check against the other data.
   * @param event the event to check against the others.
   */
  static void assertDataConsistency(final String componentInstanceId,
      final Calendar originalCalendar, final CalendarEvent event) {
    assertDataConsistency(componentInstanceId, originalCalendar);
    assertEntityIsDefined(event.asCalendarComponent());
    // Checking the component instance id.
    if (!originalCalendar.getComponentInstanceId().equals(componentInstanceId) ||
        !event.getCalendar().getComponentInstanceId().equals(componentInstanceId)) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    // Checking event data which must be linked to the original calendar
    if (!event.getCalendar().getId().equals(originalCalendar.getId())) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
  }

  /**
   * Asserts the consistency of given data, otherwise an HTTP error is sent back.<br/>
   * Checks of {@link #assertDataConsistency(String, Calendar, CalendarEvent)} is performed.
   * And also the followings ones:
   * <ul>
   * <li>all entities must be linked to the given component instance identifier</li>
   * <li>the calendar of previous event data must be equal</li>
   * <li>the identifier must be equal between old and new data</li>
   * </ul>
   * @param componentInstanceId the identifier of current handled component instance.
   * @param originalCalendar the calendar to check against the other data.
   * @param previousOne the previous event data to check against the others.
   * @param occurrence the occurrence data to check against the others.
   */
  static void assertDataConsistency(final String componentInstanceId,
      final Calendar originalCalendar, final CalendarEvent previousOne,
      final CalendarEventOccurrence occurrence) {
    assertDataConsistency(componentInstanceId, originalCalendar, previousOne);
    assertEntityIsDefined(occurrence);
    // Checking the component instance id with the new event data.
    if (!occurrence.getCalendarEvent().getCalendar().getComponentInstanceId()
        .equals(componentInstanceId)) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    // Checking previous and old event data
    if (!previousOne.getId().equals(occurrence.getCalendarEvent().getId())) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
  }

  /**
   * Asserts the specified entity is well defined, otherwise an HTTP 404 error is sent back.
   * @param entity the entity to check.
   */
  static void assertEntityIsDefined(final IdentifiableEntity entity) {
    if (entity == null || isNotDefined(entity.getId())) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
  }

  /**
   * Creates an event from the given calendar and event data.<br/>
   * This method handles also a common behavior the UI must have between each way an event is
   * saved (from a controller, a WEB service...)
   * @param calendar the calendar on which the event is added.
   * @param event the event to create.
   * @return the calendar event.
   */
  public CalendarEvent createEvent(Calendar calendar, CalendarEvent event) {
    User owner = User.getCurrentRequester();
    checkUserIsCreator(owner, calendar);
    event.planOn(calendar);
    successMessage("calendar.message.event.created", event.getTitle());
    return event;
  }

  /**
   * Gets the common calendar bundle according to the given locale.
   * @param locale the locale into which the requested bundle must be set.
   * @return a localized bundle.
   */
  public LocalizationBundle getLocalizationBundle(String locale) {
    return ResourceLocator.getLocalizationBundle("org.silverpeas.calendar.multilang.calendarBundle",
        locale);
  }

  /**
   * Gets all calendars associated to a component instance.
   * @param componentInstanceId the identifier of the component instance.
   * @return the list of calendars.
   */
  List<Calendar> getCalendarsOf(final String componentInstanceId) {
    // Retrieving the calendars
    List<Calendar> entities = Calendar.getByComponentInstanceId(componentInstanceId);
    // Sorting them by ascending creation date
    entities.sort(new AbstractComplexComparator<Calendar>() {
      @Override
      protected ValueBuffer getValuesToCompare(final Calendar object) {
        return new ValueBuffer().append(object.getCreateDate());
      }
    });
    // Returning the result
    return entities;
  }

  /**
   * Saves the given calendar.<br/>
   * This method handles also a common behavior the UI must have between each way a calendar is
   * saved (from a controller, a WEB service...)
   * @param calendar the calendar to save.
   * @return the calendar.
   */
  Calendar saveCalendar(Calendar calendar) {
    User owner = User.getCurrentRequester();
    String successfulMessageKey = calendar.isPersisted() ? "calendar.message.calendar.updated" :
        "calendar.message.calendar.created";
    if (calendar.isPersisted()) {
      checkUserIsCreator(owner, calendar);
      Calendar oldCalendar = Calendar.getById(calendar.getId());
      if (owner.getDisplayedName().equals(oldCalendar.getTitle())) {
        throw new WebApplicationException("", Response.Status.FORBIDDEN);
      }
    }
    calendar.save();
    String userLanguage = owner.getUserPreferences().getLanguage();
    getMessager().addSuccess(
        getLocalizationBundle(userLanguage).getStringWithParams(successfulMessageKey,
            calendar.getTitle()));
    return calendar;
  }

  /**
   * Deletes the given calendar.<br/>
   * This method handles also a common behavior the UI must have between each way a calendar is
   * deleted (from a controller, a WEB service...)
   * @param calendar the calendar to delete.
   */
  void deleteCalendar(Calendar calendar) {
    User owner = User.getCurrentRequester();
    checkUserIsCreator(owner, calendar);
    if (owner.getDisplayedName().equals(calendar.getTitle())) {
      throw new WebApplicationException("", Response.Status.FORBIDDEN);
    }
    calendar.delete();
    String userLanguage = owner.getUserPreferences().getLanguage();
    getMessager().addSuccess(getLocalizationBundle(userLanguage)
        .getStringWithParams("calendar.message.calendar.deleted", calendar.getTitle()));
  }

  /**
   * Exports the given calendar into ICalendar format.
   * @param calendar the calendar to export.
   * @param descriptor the export descriptor.
   * @throws ExportException on export exception.
   */
  void exportCalendarAsICalendarFormat(final Calendar calendar, final ExportDescriptor descriptor)
      throws ExportException {
    final User currentRequester = User.getCurrentRequester();
    if (calendar.isMainPersonalOf(currentRequester)) {
      iCalendarExporter.exports(descriptor,
          () -> Calendar.getEvents().filter(f -> f.onParticipants(currentRequester)).stream());
    } else {
      iCalendarExporter.exports(descriptor,
          () -> Calendar.getEvents().filter(f -> f.onCalendar(calendar)).stream());
    }
  }

  /**
   * Synchronizes the given calendar.
   * <p>Throws a forbidden WEB application exception if the calendar is not a synchronized one</p>
   * @param calendar the calendar to synchronize.
   */
  void synchronizeCalendar(final Calendar calendar) {
    if (calendar.getExternalCalendarUrl() == null) {
      throw new WebApplicationException("aimed calendar is not a synchronized one",
          Response.Status.FORBIDDEN);
    }
    final String calendarTitle = calendar.getTitle();
    final String calendarId = calendar.getId();
    silverLogger
        .info("start event synchronization of calendar {0} (id={1})", calendarTitle, calendarId);
    ICalendarImportResult result = new ICalendarImportResult();

    // TODO CALENDAR wiring or writing here the synchronization process

    silverLogger.info(
        "end event synchronization of calendar {0} (id={1}), with {2} created events and {3} " +
            "updated events", calendarTitle, calendarId, result.added(), result.updated());
    successMessage("calendar.message.calendar.synchronized", calendar.getTitle(), result.added(),
        result.updated());
  }

  /**
   * Imports the calendar events into the specified calendar from the specified input stream.
   * @param inputStream an input stream from which the serialized calendar events can be imported.
   */
  void importEventsAsICalendarFormat(final Calendar calendar, final InputStream inputStream)
      throws ImportException {
    final String calendarTitle = calendar.getTitle();
    final String calendarId = calendar.getId();
    silverLogger.info("start event import into calendar {0} (id={1})", calendarTitle, calendarId);

    ICalendarImportResult result = iCalendarEventImportProcessor.importInto(calendar, inputStream);

    silverLogger.info(
        "end event import into calendar {0} (id={1}), with {2} created events and {3} updated " +
            "events", calendarTitle, calendarId, result.added(), result.updated());
    successMessage("calendar.message.event.imported", calendarTitle, result.added(),
        result.updated());
  }

  /**
   * Saves an event occurrence.<br/>
   * This method handles also a common behavior the UI must have between each way an event is
   * saved (from a controller, a WEB service...)
   * @param occurrence the occurrence to save.
   * @param updateMethodType indicates the method of the occurrence update.
   * @param zoneId the zoneId into which dates are displayed (optional).  @return the calendar
   * event.
   */
  List<CalendarEvent> saveOccurrence(final CalendarEventOccurrence occurrence,
      OccurrenceEventActionMethodType updateMethodType, final ZoneId zoneId) {
    User owner = User.getCurrentRequester();
    checkUserIsCreator(owner, occurrence.getCalendarEvent().asCalendarComponent());
    OccurrenceEventActionMethodType methodType = updateMethodType == null ? ALL : updateMethodType;

    final String originalTitle = occurrence.getCalendarEvent().getTitle();
    final Temporal originalStartDate = occurrence.getOriginalStartDate();

    final EventOperationResult result;
    switch (methodType) {
      case FROM:
        result = occurrence.updateSinceMe();
        break;
      case UNIQUE:
        result = occurrence.update();
        break;
      default:
        final CalendarEvent event = occurrence.getCalendarEvent();
        occurrence.asCalendarComponent().copyTo(event.asCalendarComponent());
        result = event.update();
        break;
    }

    final List<CalendarEvent> events = new ArrayList<>();
    Optional<CalendarEvent> createdEvent = result.created();
    Optional<CalendarEvent> updatedEvent = result.updated();
    Optional<CalendarEventOccurrence> updatedOccurrence = result.instance();

    updatedOccurrence.ifPresent(o -> {
      final CalendarEvent event = o.getCalendarEvent();
      successMessage("calendar.message.event.occurrence.updated.unique", originalTitle,
          getMessager().formatDate(
              getDateWithOffset(event.asCalendarComponent(), originalStartDate, zoneId)));
      events.add(event);
    });

    updatedEvent.ifPresent(e -> {
      if (!createdEvent.isPresent()) {
        successMessage("calendar.message.event.updated", e.getTitle());
      } else {
        //noinspection OptionalGetWithoutIsPresent
        final Temporal endDate = e.getRecurrence().getRecurrenceEndDate().get();
        successMessage("calendar.message.event.occurrence.updated.from", e.getTitle(),
            getMessager().formatDate(getDateWithOffset(e.asCalendarComponent(), endDate, zoneId)));
      }
      events.add(e);
    });

    createdEvent.ifPresent(e -> {
      events.add(e);
      successMessage("calendar.message.event.created", e.getTitle());
    });

    return events;
  }

  /**
   * Deletes occurrences of an event from the given occurrence.<br/>
   * This method handles also a common behavior the UI must have between each way an event is
   * deleted (from a controller, a WEB service...)
   * @param occurrence the occurrence to delete.
   * @param deleteMethodType indicates the method of the occurrence deletion.
   * @param zoneId the zoneId into which dates are displayed (optional).
   */
  CalendarEvent deleteOccurrence(CalendarEventOccurrence occurrence,
      OccurrenceEventActionMethodType deleteMethodType, final ZoneId zoneId) {
    User owner = User.getCurrentRequester();
    checkUserIsCreator(owner, occurrence.getCalendarEvent().asCalendarComponent());
    OccurrenceEventActionMethodType methodType = deleteMethodType == null ? ALL : deleteMethodType;

    final EventOperationResult result;
    switch (methodType) {
      case FROM:
        result = occurrence.deleteSinceMe();
        break;
      case UNIQUE:
        result = occurrence.delete();
        break;
      default:
        result = occurrence.getCalendarEvent().delete();
        break;
    }

    Optional<CalendarEvent> updatedEvent = result.updated();
    if (!updatedEvent.isPresent() || !updatedEvent.get().isRecurrent()) {
      successMessage("calendar.message.event.deleted", occurrence.getTitle());
    } else {
      final String bundleKey;
      final Temporal endDate;
      if (methodType == UNIQUE) {
        bundleKey = "calendar.message.event.occurrence.deleted.unique";
        endDate = occurrence.getOriginalStartDate();
      } else {
        bundleKey = "calendar.message.event.occurrence.deleted.from";
        //noinspection OptionalGetWithoutIsPresent
        endDate = updatedEvent.get().getRecurrence().getRecurrenceEndDate().get();
      }
      successMessage(bundleKey, occurrence.getTitle(), getMessager()
          .formatDate(getDateWithOffset(occurrence.asCalendarComponent(), endDate, zoneId)));
    }

    return updatedEvent.orElse(null);
  }

  /**
   * Updates the participation of an attendee of an event or on an occurrence of an event from
   * the given data.<br/>
   * This method handles also a common behavior the UI must have between each way an event is
   * deleted (from a controller, a WEB service...)
   * @param occurrence the occurrence.
   * @param attendeeId the identifier of the attendee which answered.
   * @param participationStatus the participation answer of the attendee.
   * @param answerMethodType indicates the method of the occurrence deletion.
   * @param zoneId the zoneId into which dates are displayed (optional).
   */
  CalendarEvent updateOccurrenceAttendeeParticipation(CalendarEventOccurrence occurrence,
      String attendeeId, ParticipationStatus participationStatus,
      OccurrenceEventActionMethodType answerMethodType, final ZoneId zoneId) {
    OccurrenceEventActionMethodType methodType = answerMethodType == null ? ALL : answerMethodType;
    CalendarEvent modifiedEvent = null;
    if (methodType == UNIQUE) {
      Optional<EventOperationResult> optionalResult =
          updateSingleOccurrenceAttendeeParticipation(occurrence, attendeeId, participationStatus);
      if (optionalResult.isPresent()) {
        modifiedEvent = optionalResult.get().instance().get().getCalendarEvent();
        successMessage("calendar.message.event.occurrence.attendee.participation.updated.unique",
            occurrence.getTitle(), getMessager().formatDate(
                getDateWithOffset(occurrence.asCalendarComponent(), occurrence.getOriginalStartDate(),
                    zoneId)));
      }
    } else if (methodType == ALL) {
      Optional<EventOperationResult> optionalResult =
          updateEventAttendeeParticipation(occurrence, attendeeId, participationStatus);
      if (optionalResult.isPresent()) {
        if (optionalResult.get().updated().isPresent()) {
          modifiedEvent = optionalResult.get().updated().get();
        } else {
          modifiedEvent = optionalResult.get().instance().get().getCalendarEvent();
        }
        successMessage("calendar.message.event.attendee.participation.updated",
            occurrence.getCalendarEvent().getTitle());
      }
    }
    if (modifiedEvent == null) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    return modifiedEvent;
  }

  private Optional<EventOperationResult> updateEventAttendeeParticipation(
      final CalendarEventOccurrence occurrence, final String attendeeId,
      final ParticipationStatus participationStatus) {
    EventOperationResult result = null;
    final Optional<Attendee> attendee = occurrence.getCalendarEvent().getAttendees().stream()
        .filter(a -> a.getId().equals(attendeeId)).findFirst();
    if (attendee.isPresent()) {
      setAttendeeStatus(participationStatus, attendee.get());
      result = occurrence.getCalendarEvent().update();
    } else {
      // It is the particular case where the attendee is set on occurrences but not on the
      // original event.
      List<CalendarEventOccurrence> allOccurrences =
          occurrence.getCalendarEvent().getPersistedOccurrences();
      for (CalendarEventOccurrence eventOccurrence : allOccurrences) {
        Optional<EventOperationResult> optionalResult =
            updateSingleOccurrenceAttendeeParticipation(eventOccurrence, attendeeId,
                participationStatus);
        if (result == null && optionalResult.isPresent()) {
          result = optionalResult.get();
        }
      }
    }
    return Optional.ofNullable(result);
  }

  private Optional<EventOperationResult> updateSingleOccurrenceAttendeeParticipation(
      final CalendarEventOccurrence occurrence, final String attendeeId,
      final ParticipationStatus participationStatus) {
    final Optional<Attendee> attendee =
        occurrence.getAttendees().stream().filter(a -> a.getId().equals(attendeeId)).findFirst();
    if (attendee.isPresent()) {
      setAttendeeStatus(participationStatus, attendee.get());
      return Optional.of(occurrence.update());
    }
    return Optional.empty();
  }

  private void setAttendeeStatus(final ParticipationStatus participationStatus,
      final Attendee attendee) {
    switch (participationStatus) {
      case ACCEPTED:
        attendee.accept();
        break;
      case DECLINED:
        attendee.decline();
        break;
      case TENTATIVE:
        attendee.tentativelyAccept();
        break;
      default:
        throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
  }

  /**
   * Gets the event occurrences associated to a calendar and contained a the time window specified
   * by the start and end datetimes.<br/>
   * The occurrences are sorted from the lowest to the highest date.
   * @param calendar the calendar the event occurrences belong to.
   * @param startDate the start date of time window.
   * @param endDate the end date of time window.
   * @return a list of entities of calendar event occurrences.
   */
  List<CalendarEventOccurrence> getEventOccurrencesOf(Calendar calendar,
      LocalDate startDate, LocalDate endDate) {
    return calendar.between(startDate, endDate).getEventOccurrences();
  }

  /**
   * Gets all event occurrences associated to users and contained a the time window specified
   * by the start and end datetimes.<br/>
   * Attendees which have answered negatively about their presence are not taken into account.
   * The occurrences are sorted from the lowest to the highest date and mapped by user identifiers.
   * @param currentUserAndComponentInstanceId the current user and current the component instance
   * id from which the service is requested.
   * @param startDate the start date of time window.
   * @param endDate the end date of time window.
   * @param users the users to filter on.
   * @return a list of entities of calendar event occurrences mapped by user identifiers.
   */
  Map<String, List<CalendarEventOccurrence>> getAllEventOccurrencesByUserIds(
      final Pair<String, User> currentUserAndComponentInstanceId, LocalDate startDate,
      LocalDate endDate, Collection<User> users) {
    // Retrieving the occurrences
    List<CalendarEventOccurrence> entities =
        Calendar.getTimeWindowBetween(startDate, endDate)
            .filter(f -> f.onParticipants(users))
            .getEventOccurrences();
    // Getting the occurrences by users
    Map<String, List<CalendarEventOccurrence>> result =
        new CalendarEventInternalParticipationView(users).apply(entities);
    final String currentUserId = currentUserAndComponentInstanceId.getRight().getId();
    if (result.containsKey(currentUserId)) {
      List<CalendarEventOccurrence> currentUserOccurrences = result.get(currentUserId);
      // Remove occurrence associated to given user when he is the creator
      currentUserOccurrences.removeIf(calendarEventOccurrence -> {
        CalendarEvent event = calendarEventOccurrence.getCalendarEvent();
        return event.getCalendar().getComponentInstanceId()
            .equals(currentUserAndComponentInstanceId.getLeft()) &&
            event.getCreator().getId().equals(currentUserId);
      });
    }
    return result;
  }

  /**
   * Centralization of checking if the specified user is the creator of the specified entity.
   * @param user the user to verify.
   * @param entity the calendar to check.
   */
  private void checkUserIsCreator(User user, Entity entity) {
    assertEntityIsDefined(entity);
    if (!user.equals(entity.getCreator())) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
  }

  /**
   * Push a success message to the current user.
   * @param messageKey the key of the message.
   * @param params the message parameters.
   */
  private void successMessage(String messageKey, Object... params) {
    User owner = User.getCurrentRequester();
    String userLanguage = owner.getUserPreferences().getLanguage();
    getMessager().addSuccess(
        getLocalizationBundle(userLanguage).getStringWithParams(messageKey, params));
  }

  private WebMessager getMessager() {
    return WebMessager.getInstance();
  }

}
