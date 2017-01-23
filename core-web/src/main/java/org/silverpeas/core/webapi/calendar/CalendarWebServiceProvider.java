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
import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.calendar.event.Attendee;
import org.silverpeas.core.calendar.event.Attendee.ParticipationStatus;
import org.silverpeas.core.calendar.event.CalendarEvent;
import org.silverpeas.core.calendar.event.CalendarEvent.CalendarEventModificationResult;
import org.silverpeas.core.calendar.event.CalendarEventOccurrence;
import org.silverpeas.core.calendar.event.CalendarEventOccurrenceReferenceData;
import org.silverpeas.core.calendar.event.CalendarEventUtil;
import org.silverpeas.core.calendar.event.view.CalendarEventInternalParticipationView;
import org.silverpeas.core.calendar.icalendar.ICalendarException;
import org.silverpeas.core.calendar.icalendar.ICalendarImport;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.model.Entity;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.comparator.AbstractComplexComparator;
import org.silverpeas.core.web.mvc.webcomponent.WebMessager;

import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.silverpeas.core.calendar.event.CalendarEventUtil.getDateWithOffset;
import static org.silverpeas.core.util.StringUtil.isNotDefined;
import static org.silverpeas.core.webapi.calendar.OccurrenceEventActionMethodType.ALL;
import static org.silverpeas.core.webapi.calendar.OccurrenceEventActionMethodType.UNIQUE;

/**
 * @author Yohann Chastagnier
 */
@Singleton
public class CalendarWebServiceProvider {

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
  public static void assertDataConsistency(final String componentInstanceId,
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
  public static void assertDataConsistency(final String componentInstanceId,
      final Calendar originalCalendar, final CalendarEvent event) {
    assertDataConsistency(componentInstanceId, originalCalendar);
    assertEntityIsDefined(event);
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
   * @param newOne the new event data to check against the others.
   */
  public static void assertDataConsistency(final String componentInstanceId,
      final Calendar originalCalendar, final CalendarEvent previousOne,
      final CalendarEvent newOne) {
    assertDataConsistency(componentInstanceId, originalCalendar, previousOne);
    assertEntityIsDefined(newOne);
    // Checking the component instance id with the new event data.
    if (!newOne.getCalendar().getComponentInstanceId().equals(componentInstanceId)) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    // Checking previous and old event data
    if (!previousOne.getId().equals(newOne.getId())) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
  }

  /**
   * Asserts the specified entity is well defined, otherwise an HTTP 404 error is sent back.
   * @param entity the entity to check.
   */
  public static void assertEntityIsDefined(final Entity entity) {
    if (entity == null || isNotDefined(entity.getId())) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
  }

  /**
   * Centralization of checking if the specified user is the creator of the specified calendar.
   * @param user the user to verify.
   * @param calendar the calendar to check.
   */
  public static void checkUserIsCreator(User user, Entity calendar) {
    assertEntityIsDefined(calendar);
    if (!user.equals(calendar.getCreator())) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
  }

  /**
   * Gets all calendars associated to a component instance.
   * @param componentInstanceId the identifier of the component instance.
   * @return the list of calendars.
   */
  public List<Calendar> getCalendarsOf(final String componentInstanceId) {
    // Retrieving the calendars
    List<Calendar> entities = Calendar.getByComponentInstanceId(componentInstanceId);
    // Sorting them by ascending creation date
    Collections.sort(entities, new AbstractComplexComparator<Calendar>() {
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
  public Calendar saveCalendar(Calendar calendar) {
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
        getMultilang(userLanguage).getStringWithParams(successfulMessageKey, calendar.getTitle()));
    return calendar;
  }

  /**
   * Deletes the given calendar.<br/>
   * This method handles also a common behavior the UI must have between each way a calendar is
   * deleted (from a controller, a WEB service...)
   * @param calendar the calendar to delete.
   */
  public void deleteCalendar(Calendar calendar) {
    User owner = User.getCurrentRequester();
    checkUserIsCreator(owner, calendar);
    if (owner.getDisplayedName().equals(calendar.getTitle())) {
      throw new WebApplicationException("", Response.Status.FORBIDDEN);
    }
    calendar.delete();
    String userLanguage = owner.getUserPreferences().getLanguage();
    getMessager().addSuccess(getMultilang(userLanguage)
        .getStringWithParams("calendar.message.calendar.deleted", calendar.getTitle()));
  }

  /**
   * Imports events as ICalendar format.
   * @param eventImport the import to perform.
   */
  public void importEventsAsICalendarFormat(final ICalendarImport eventImport)
      throws ICalendarException {
    final Stream<CalendarEvent> events = eventImport.streamEvents();
    Transaction.performInOne(() -> {
      events.forEach(event -> {
        // Adjustments
        if (StringUtil.isNotDefined(event.getTitle())) {
          event.withTitle("N/A");
        }
        // Persist operation
        Optional<CalendarEvent> optionalPersistedEvent =
            eventImport.getCalendar().externalEvent(event.getExternalId());
        if (!optionalPersistedEvent.isPresent()) {
          optionalPersistedEvent = eventImport.getCalendar().event(event.getExternalId());
        }
        if (optionalPersistedEvent.isPresent()) {
          event.getAttendees().addAll(optionalPersistedEvent.get().getAttendees());
          optionalPersistedEvent.get().merge(event);
        } else {
          event.planOn(eventImport.getCalendar());
        }
      });
      return null;
    });
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
   * Saves an event from the given calendar event occurrence.<br/>
   * This method handles also a common behavior the UI must have between each way an event is
   * saved (from a controller, a WEB service...)
   * @param event the event reference from which the update is performed.
   * @param data the occurrence necessary data to perform the operation.
   * @param updateMethodType indicates the method of the occurrence update.
   * @return the calendar event.
   */
  public List<CalendarEvent> saveEventFromAnOccurrence(CalendarEvent event,
      CalendarEventOccurrenceReferenceData data, OccurrenceEventActionMethodType updateMethodType) {
    User owner = User.getCurrentRequester();
    checkUserIsCreator(owner, event);
    OccurrenceEventActionMethodType methodType = updateMethodType == null ? ALL : updateMethodType;

    final CalendarEventModificationResult result;
    switch (methodType) {
      case FROM:
        result = event.updateFrom(data);
        break;
      case UNIQUE:
        result = event.update(data);
        break;
      default:
        result = event.update();
        break;
    }

    if (!result.isCreatedEvent() || !result.getUpdatedEvent().isRecurrent()) {
      successMessage("calendar.message.event.updated", result.getUpdatedEvent().getTitle());
    } else {
      final String bundleKey;
      final Temporal endDate;
      if (methodType == UNIQUE) {
        bundleKey = "calendar.message.event.occurrence.updated.unique";
        endDate = data.getOriginalStartDate();
      } else {
        bundleKey = "calendar.message.event.occurrence.updated.from";
        endDate = result.getUpdatedEvent().getRecurrence().getEndDate().get();
      }
      successMessage(bundleKey, result.getUpdatedEvent().getTitle(),
          getMessager().formatDate(getDateWithOffset(event, endDate)));
    }

    final List<CalendarEvent> events = new ArrayList<>();
    events.add(result.getUpdatedEvent());
    if (result.isCreatedEvent()) {
      final CalendarEvent createdEvent = result.getCreatedEvent();
      events.add(createdEvent);
      successMessage("calendar.message.event.created", createdEvent.getTitle());
    }
    return events;
  }

  /**
   * Deletes an event or occurrences of an event from the given occurrence.<br/>
   * This method handles also a common behavior the UI must have between each way an event is
   * deleted (from a controller, a WEB service...)
   * @param event the event reference from which the deletion is performed.
   * @param data the occurrence necessary data to perform the operation.
   * @param deleteMethodType indicates the method of the occurrence deletion.
   */
  public CalendarEvent deleteEventFromAnOccurrence(CalendarEvent event,
      CalendarEventOccurrenceReferenceData data, OccurrenceEventActionMethodType deleteMethodType) {
    User owner = User.getCurrentRequester();
    checkUserIsCreator(owner, event);
    OccurrenceEventActionMethodType methodType = deleteMethodType == null ? ALL : deleteMethodType;

    final CalendarEventModificationResult result;
    switch (methodType) {
      case FROM:
        result = event.deleteFrom(data);
        break;
      case UNIQUE:
        result = event.delete(data);
        break;
      default:
        result = event.delete();
        break;
    }

    if (!result.isUpdatedEvent() || !result.getUpdatedEvent().isRecurrent()) {
      successMessage("calendar.message.event.deleted", event.getTitle());
    } else {
      final String bundleKey;
      final Temporal endDate;
      if (methodType == UNIQUE) {
        bundleKey = "calendar.message.event.occurrence.deleted.unique";
        endDate = data.getOriginalStartDate();
      } else {
        bundleKey = "calendar.message.event.occurrence.deleted.from";
        endDate = result.getUpdatedEvent().getRecurrence().getEndDate().get();
      }
      successMessage(bundleKey, event.getTitle(),
          getMessager().formatDate(getDateWithOffset(event, endDate)));
    }

    return result.getUpdatedEvent();
  }

  /**
   * Updates the participation of an attendee of an event or on an occurrence of an event from
   * the given data.<br/>
   * This method handles also a common behavior the UI must have between each way an event is
   * deleted (from a controller, a WEB service...)
   * @param event the event reference.
   * @param data the occurrence necessary data to perform the operation.
   * @param attendeeId the identifier of the attendee which answered.
   * @param participationStatus the participation answer of the attendee.
   * @param answerMethodType indicates the method of the occurrence deletion.
   */
  public CalendarEvent updateEventAttendeeParticipationFromAnOccurrence(CalendarEvent event,
      CalendarEventOccurrenceReferenceData data, String attendeeId,
      ParticipationStatus participationStatus, OccurrenceEventActionMethodType answerMethodType) {
    OccurrenceEventActionMethodType methodType = answerMethodType == null ? ALL : answerMethodType;
    Temporal participationOnDate = null;
    if (methodType == UNIQUE) {
      participationOnDate = data.getOriginalStartDate();
    } else if (methodType != ALL) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }

    for (Attendee attendee : event.getAttendees()) {
      if (attendee.getId().equals(attendeeId)) {
        if (participationOnDate == null) {
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
        } else {
          switch (participationStatus) {
            case ACCEPTED:
              attendee.acceptOn(participationOnDate);
              break;
            case DECLINED:
              attendee.declineOn(participationOnDate);
              break;
            case TENTATIVE:
              attendee.tentativelyAcceptOn(participationOnDate);
              break;
            default:
              throw new WebApplicationException(Response.Status.FORBIDDEN);
          }
        }

        final CalendarEventModificationResult result = event.update();
        switch (methodType) {
          case ALL:
            successMessage("calendar.message.event.attendee.participation.updated",
                event.getTitle());
            break;
          case UNIQUE:
            successMessage(
                "calendar.message.event.occurrence.attendee.participation.updated.unique",
                event.getTitle(),
                getMessager().formatDate(getDateWithOffset(event, data.getOriginalStartDate())));
            break;
        }

        return result.getUpdatedEvent();
      }
    }

    throw new WebApplicationException(Response.Status.FORBIDDEN);
  }

  /**
   * Gets the event occurrences associated to a calendar and contained a the time window specified
   * by the start and end date times.<br/>
   * The occurrences are sorted from the lowest to the highest date.
   * @param calendar the calendar the event occurrences belong to.
   * @param startDate the start date of time window.
   * @param endDate the end date of time window.
   * @return a list of entities of calendar event occurrences.
   */
  public List<CalendarEventOccurrence> getEventOccurrencesOf(Calendar calendar,
      LocalDate startDate, LocalDate endDate) {
    return calendar.between(startDate, endDate).getEventOccurrences();
  }

  /**
   * Gets all event occurrences associated to users and contained a the time window specified
   * by the start and end date times.<br/>
   * Attendees which have answered negatively about their presence are not taken into account.
   * The occurrences are sorted from the lowest to the highest date and mapped by user identifiers.
   * @param currentUserAndComponentInstanceId the current user and current the component instance
   * id from which the service is requested.
   * @param startDate the start date of time window.
   * @param endDate the end date of time window.
   * @param users the users to filter on.
   * @return a list of entities of calendar event occurrences mapped by user identifiers.
   */
  public Map<String, List<CalendarEventOccurrence>> getAllEventOccurrencesByUserIds(
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
            event.getCreatedBy().equals(currentUserId);
      });
    }
    return result;
  }

  /**
   * Push a success message to the current user.
   * @param messageKey the key of the message.
   * @param params the message parameters.
   */
  private void successMessage(String messageKey, String... params) {
    User owner = User.getCurrentRequester();
    String userLanguage = owner.getUserPreferences().getLanguage();
    getMessager().addSuccess(getMultilang(userLanguage).getStringWithParams(messageKey, params));
  }

  private WebMessager getMessager() {
    return WebMessager.getInstance();
  }

  /**
   * Gets the common calendar bundle according to the given locale.
   * @param locale the locale into which the requested bundle must be set.
   * @return a localized bundle.
   */
  public LocalizationBundle getMultilang(String locale) {
    return ResourceLocator
        .getLocalizationBundle("org.silverpeas.calendar.multilang.calendarBundle", locale);
  }
}
