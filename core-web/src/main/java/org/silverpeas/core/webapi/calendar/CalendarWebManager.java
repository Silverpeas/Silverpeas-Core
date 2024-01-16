/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
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

import org.apache.commons.lang3.tuple.Pair;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.component.model.PersonalComponent;
import org.silverpeas.core.admin.component.model.PersonalComponentInstance;
import org.silverpeas.core.admin.component.model.SilverpeasPersonalComponentInstance;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Base;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.calendar.Attendee;
import org.silverpeas.core.calendar.Attendee.ParticipationStatus;
import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.calendar.CalendarEvent.EventOperationResult;
import org.silverpeas.core.calendar.CalendarEventOccurrence;
import org.silverpeas.core.calendar.CalendarEventOccurrenceGenerator;
import org.silverpeas.core.calendar.ICalendarEventImportProcessor;
import org.silverpeas.core.calendar.ICalendarImportResult;
import org.silverpeas.core.calendar.Plannable;
import org.silverpeas.core.calendar.icalendar.ICalendarExporter;
import org.silverpeas.core.calendar.view.CalendarEventInternalParticipationView;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.importexport.ExportDescriptor;
import org.silverpeas.core.importexport.ExportException;
import org.silverpeas.core.importexport.ImportException;
import org.silverpeas.core.persistence.datasource.model.IdentifiableEntity;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.mvc.webcomponent.WebMessager;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.*;
import static org.silverpeas.core.calendar.CalendarEventOccurrence.COMPARATOR_BY_DATE_ASC;
import static org.silverpeas.core.calendar.CalendarEventUtil.getDateWithOffset;
import static org.silverpeas.core.contribution.attachment.AttachmentServiceProvider.getAttachmentService;
import static org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController.wysiwygPlaceHaveChanged;
import static org.silverpeas.core.util.StringUtil.isNotDefined;
import static org.silverpeas.core.webapi.calendar.OccurrenceEventActionMethodType.ALL;
import static org.silverpeas.core.webapi.calendar.OccurrenceEventActionMethodType.UNIQUE;


/**
 * @author Yohann Chastagnier
 */
@Service
@Base
@Named("default" + CalendarWebManager.NAME_SUFFIX)
public class CalendarWebManager {

  /**
   * The predefined suffix that must compound the name of each implementation of this interface.
   * An implementation of this interface by a Silverpeas application named Kmelia must be named
   * <code>kmelia[NAME_SUFFIX]</code> where NAME_SUFFIX is the predefined suffix as defined below.
   */
  public static final String NAME_SUFFIX = "CalendarWebManager";

  private static final SettingBundle settings =
      ResourceLocator.getSettingBundle("org.silverpeas.calendar.settings.calendar");
  private final SilverLogger silverLogger = SilverLogger.getLogger(Plannable.class);

  private static final int END_YEAR_OFFSET = 3;
  private static final int DEFAULT_NB_MAX_NEXT_OCC = 10;

  @Inject
  private ICalendarExporter iCalendarExporter;

  @Inject
  private CalendarEventOccurrenceGenerator generator;

  @Inject
  private ICalendarEventImportProcessor iCalendarEventImportProcessor;

  protected CalendarWebManager() {
  }

  /**
   * Gets the singleton instance of the provider.
   * @param componentInstanceIdOrComponentName a component instance identifier of a component name.
   * @see ServiceProvider#getServiceByComponentInstanceAndNameSuffix(String, String)
   */
  public static CalendarWebManager get(final String componentInstanceIdOrComponentName) {
    if (isNotDefined(componentInstanceIdOrComponentName)) {
      return ServiceProvider.getSingleton(CalendarWebManager.class, new AnnotationLiteral<Base>() {});
    }
    return ServiceProvider
        .getServiceByComponentInstanceAndNameSuffix(componentInstanceIdOrComponentName,
            NAME_SUFFIX);
  }

  /**
   * Asserts the consistency of given data, otherwise an HTTP error is sent back.<br>
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
   * Asserts the consistency of given data, otherwise an HTTP error is sent back.<br>
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
   * Asserts the consistency of given data, otherwise an HTTP error is sent back.<br>
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
   * Creates an event from the given calendar and event data.<br>
   * This method handles also a common behavior the UI must have between each way an event is
   * saved (from a controller, a WEB service...)
   * @param calendar the calendar on which the event is added.
   * @param event the event to create.
   * @param volatileEventId the volatile identifier used to attach the images on WYSIWYG editor.
   * @return the calendar event.
   */
  public CalendarEvent createEvent(Calendar calendar, CalendarEvent event, String volatileEventId) {
    if (!calendar.canBeAccessedBy(User.getCurrentRequester())) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    event.planOn(calendar);

    // Attaching all documents linked to volatile identifier to the persisted one
    final String finalEventId = event.getId();
    final String instanceId = calendar.getComponentInstanceId();
    final ResourceReference
        volatileAttachmentSourcePK = new ResourceReference(volatileEventId, instanceId);
    final ResourceReference finalAttachmentSourcePK = new ResourceReference(finalEventId, instanceId);
    final List<SimpleDocumentPK> movedDocumentPks = getAttachmentService()
        .moveAllDocuments(volatileAttachmentSourcePK, finalAttachmentSourcePK);
    if (!movedDocumentPks.isEmpty()) {
      // Change images path in wysiwyg
      wysiwygPlaceHaveChanged(instanceId, volatileEventId, instanceId, finalEventId);
    }

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
   * Gets all calendars handled by a component instance.
   * <p>This centralization is useful for components which handles other agendas than those linked
   * to the instance.</p>
   * @param componentInstanceId the identifier of the component instance.
   * @return the list of calendars.
   */
  public List<Calendar> getCalendarsHandledBy(final String componentInstanceId) {
    return getCalendarsHandledBy(singleton(componentInstanceId));
  }

  /**
   * Gets all calendars handled by component instances.
   * <p>This centralization is useful for components which handles other agendas than those linked
   * to the instance.</p>
   * <p>This is a signature design for performances.</p>
   * @param componentInstanceIds identifier of the component instances.
   * @return the list of calendars.
   */
  public List<Calendar> getCalendarsHandledBy(final Collection<String> componentInstanceIds) {
    return Calendar.getByComponentInstanceIds(componentInstanceIds);
  }

  /**
   * Saves the given calendar.<br>
   * This method handles also a common behavior the UI must have between each way a calendar is
   * saved (from a controller, a WEB service...)
   * @param calendar the calendar to save.
   * @return the calendar.
   */
  protected Calendar saveCalendar(Calendar calendar) {
    User owner = User.getCurrentRequester();
    String successfulMessageKey = calendar.isPersisted() ? "calendar.message.calendar.updated" :
        "calendar.message.calendar.created";
    if (calendar.isPersisted() && !calendar.canBeModifiedBy(User.getCurrentRequester())) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    calendar.save();
    String userLanguage = owner.getUserPreferences().getLanguage();
    getMessager().addSuccess(
        getLocalizationBundle(userLanguage).getStringWithParams(successfulMessageKey,
            calendar.getTitle()));
    return calendar;
  }

  /**
   * Deletes the given calendar.<br>
   * This method handles also a common behavior the UI must have between each way a calendar is
   * deleted (from a controller, a WEB service...)
   * @param calendar the calendar to delete.
   */
  protected void deleteCalendar(Calendar calendar) {
    User owner = User.getCurrentRequester();
    if (!calendar.canBeDeletedBy(User.getCurrentRequester())) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
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
  protected void exportCalendarAsICalendarFormat(final Calendar calendar, final ExportDescriptor descriptor)
      throws ExportException {
    final Mutable<User> currentUser = Mutable.ofNullable(User.getCurrentRequester());
    if (!currentUser.isPresent()) {
      SilverpeasPersonalComponentInstance.getById(calendar.getComponentInstanceId())
          .ifPresent(i -> currentUser.set(i.getUser()));
    }
    if (currentUser.isPresent() && calendar.isMainPersonalOf(currentUser.get())) {
      iCalendarExporter.exports(descriptor, () -> Stream.concat(
              Calendar.getEvents().filter(f -> f.onCalendar(calendar)).stream(),
              Calendar.getEvents().filter(f -> f.onParticipants(currentUser.get())).stream())
          .distinct());
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
  protected void synchronizeCalendar(final Calendar calendar) throws ImportException {
    if (calendar.getExternalCalendarUrl() == null) {
      throw new WebApplicationException("aimed calendar is not a synchronized one",
          Response.Status.FORBIDDEN);
    }
    final String calendarTitle = calendar.getTitle();
    final String calendarId = calendar.getId();
    silverLogger
        .info("start event synchronization of calendar {0} (id={1})", calendarTitle, calendarId);
    ICalendarImportResult result = calendar.synchronize();

    silverLogger.info(
        "end event synchronization of calendar {0} (id={1}), with {2} created events, {3} updated" +
            " events and {4} deleted events", calendarTitle, calendarId, result.added(),
        result.updated(), result.deleted());
    successMessage("calendar.message.calendar.synchronized", calendar.getTitle(), result.added(),
        result.updated(), result.deleted());
  }

  /**
   * Imports the calendar events into the specified calendar from the specified input stream.
   * @param inputStream an input stream from which the serialized calendar events can be imported.
   */
  protected void importEventsAsICalendarFormat(final Calendar calendar, final InputStream inputStream)
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
   * Saves an event occurrence.<br>
   * This method handles also a common behavior the UI must have between each way an event is
   * saved (from a controller, a WEB service...)
   * @param occurrence the occurrence to save.
   * @param updateMethodType indicates the method of the occurrence update.
   * @param zoneId the zoneId into which dates are displayed (optional).  @return the calendar
   * event.
   */
  protected List<CalendarEvent> saveOccurrence(final CalendarEventOccurrence occurrence,
      OccurrenceEventActionMethodType updateMethodType, final ZoneId zoneId) {
    if (!occurrence.getCalendarEvent().canBeModifiedBy(User.getCurrentRequester())) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
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
   * Deletes occurrences of an event from the given occurrence.<br>
   * This method handles also a common behavior the UI must have between each way an event is
   * deleted (from a controller, a WEB service...)
   * @param occurrence the occurrence to delete.
   * @param deleteMethodType indicates the method of the occurrence deletion.
   * @param zoneId the zoneId into which dates are displayed (optional).
   */
  protected CalendarEvent deleteOccurrence(CalendarEventOccurrence occurrence,
      OccurrenceEventActionMethodType deleteMethodType, final ZoneId zoneId) {
    if (!occurrence.getCalendarEvent().canBeDeletedBy(User.getCurrentRequester())) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
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
        endDate = updatedEvent.get()
            .getRecurrence()
            .getRecurrenceEndDate()
            .orElseThrow(() -> new SilverpeasRuntimeException("No Recurrence end date!"));
      }
      successMessage(bundleKey, occurrence.getTitle(), getMessager()
          .formatDate(getDateWithOffset(occurrence.asCalendarComponent(), endDate, zoneId)));
    }

    return updatedEvent.orElse(null);
  }

  /**
   * Updates the participation of an attendee of an event or on an occurrence of an event from
   * the given data.<br>
   * This method handles also a common behavior the UI must have between each way an event is
   * deleted (from a controller, a WEB service...)
   * @param occurrence the occurrence.
   * @param attendeeId the identifier of the attendee which answered.
   * @param participationStatus the participation answer of the attendee.
   * @param answerMethodType indicates the method of the occurrence deletion.
   * @param zoneId the zoneId into which dates are displayed (optional).
   */
  protected CalendarEvent updateOccurrenceAttendeeParticipation(CalendarEventOccurrence occurrence,
      String attendeeId, ParticipationStatus participationStatus,
      OccurrenceEventActionMethodType answerMethodType, final ZoneId zoneId) {
    OccurrenceEventActionMethodType methodType = answerMethodType == null ? ALL : answerMethodType;
    CalendarEvent modifiedEvent = null;
    if (methodType == UNIQUE) {
      Optional<EventOperationResult> optionalResult =
          updateSingleOccurrenceAttendeeParticipation(occurrence, attendeeId, participationStatus);
      if (optionalResult.isPresent()) {
        modifiedEvent = optionalResult.get()
            .instance()
            .orElseThrow(() -> new SilverpeasRuntimeException(
                "No event occurrence in the operation result!"))
            .getCalendarEvent();
        successMessage("calendar.message.event.occurrence.attendee.participation.updated.unique",
            occurrence.getTitle(), getMessager().formatDate(
                getDateWithOffset(occurrence.asCalendarComponent(),
                    occurrence.getOriginalStartDate(), zoneId)));
      }
    } else if (methodType == ALL) {
      Optional<EventOperationResult> optionalResult =
          updateEventAttendeeParticipation(occurrence, attendeeId, participationStatus);
      if (optionalResult.isPresent()) {
        if (optionalResult.get().updated().isPresent()) {
          modifiedEvent = optionalResult.get().updated().get();
        } else {
          modifiedEvent = optionalResult.get()
              .instance()
              .orElseThrow(() -> new SilverpeasRuntimeException(
                  "No event occurrence in the operation result!"))
              .getCalendarEvent();
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
   * Gets the first occurrence of an event from the identifier of an event.
   * @param eventId an event identifier.
   * @return the first {@link CalendarEventOccurrence} instance of an event.
   */
  public CalendarEventOccurrence getFirstCalendarEventOccurrenceFromEventId(final String eventId) {
    CalendarEvent event = CalendarEvent.getById(eventId);
    final Temporal startTemporal = event.getStartDate();
    final Temporal endTemporal;
    if (!event.isRecurrent()) {
      endTemporal = event.getEndDate();
    } else {
      endTemporal = event.getEndDate().plus(END_YEAR_OFFSET, ChronoUnit.YEARS);
    }
    return generator
        .generateOccurrencesOf(singletonList(event), Period.between(startTemporal, endTemporal))
        .get(0);
  }

  /**
   * Gets the event occurrences associated to a calendar and contained a the time window specified
   * by the start and end datetimes.<br>
   * The occurrences are sorted from the lowest to the highest date.
   * @param startDate the start date of time window.
   * @param endDate the end date of time window.
   * @param calendars the calendars the event occurrences belong to.
   * @return a list of entities of calendar event occurrences.
   */
  public List<CalendarEventOccurrence> getEventOccurrencesOf(LocalDate startDate, LocalDate endDate,
      List<Calendar> calendars) {
    return getEventOccurrencesOf(startDate, endDate, calendars, User.getCurrentRequester());
  }

  /**
   * Gets the event occurrences associated to a calendar and contained a the time window specified
   * by the start and end datetimes.<br>
   * The occurrences are sorted from the lowest to the highest date.
   * @param startDate the start date of time window.
   * @param endDate the end date of time window.
   * @param calendars the calendars the event occurrences belong to.
   * @return a list of entities of calendar event occurrences.
   */
  public List<CalendarEventOccurrence> getEventOccurrencesOf(LocalDate startDate, LocalDate endDate,
      List<Calendar> calendars, User currentRequester) {
    if (currentRequester == null) {
      throw new IllegalArgumentException("Current requester MUST be defined");
    }
    return calendars.isEmpty() ? emptyList() : Calendar
        .getTimeWindowBetween(startDate, endDate)
        .filter(f -> f.onCalendar(calendars))
        .getEventOccurrences();
  }

  /**
   * Gets all event occurrences associated to users and contained a the time window specified
   * by the start and end date times.<br>
   * Attendees which have answered negatively about their presence are not taken into account.
   * The occurrences are sorted from the lowest to the highest date and mapped by user identifiers.
   * @param currentUserAndComponentInstanceId the current user and the current component instance
   * ids from which the service is requested.
   * @param startDate the start date of time window.
   * @param endDate the end date of time window.
   * @param users the users to filter on.
   * @return a list of entities of calendar event occurrences mapped by user identifiers.
   */
  protected Map<String, List<CalendarEventOccurrence>> getAllEventOccurrencesByUserIds(
      final Pair<List<String>, User> currentUserAndComponentInstanceId, LocalDate startDate,
      LocalDate endDate, Collection<User> users) {
    // Retrieving the occurrences from personal calendars
    final List<Calendar> personalCalendars = new ArrayList<>();
    users.forEach(u -> personalCalendars.addAll(getCalendarsHandledBy(
        PersonalComponentInstance.from(u, PersonalComponent.getByName("userCalendar").orElse(null))
            .getId())));
    final List<CalendarEventOccurrence> entities = personalCalendars.isEmpty() ? emptyList() :
        Calendar.getTimeWindowBetween(startDate, endDate)
            .filter(f -> f.onCalendar(personalCalendars))
            .getEventOccurrences();
    entities.addAll(
        Calendar.getTimeWindowBetween(startDate, endDate)
            .filter(f -> f.onParticipants(users))
            .getEventOccurrences());
    // Getting the occurrences by users
    Map<String, List<CalendarEventOccurrence>> result =
        new CalendarEventInternalParticipationView(users)
            .apply(entities.stream().distinct().collect(Collectors.toList()));
    final String currentUserId = currentUserAndComponentInstanceId.getRight().getId();
    if (result.containsKey(currentUserId)) {
      List<CalendarEventOccurrence> currentUserOccurrences = result.get(currentUserId);
      // Remove occurrence associated to given user when he is the creator
      currentUserOccurrences.removeIf(calendarEventOccurrence -> {
        CalendarEvent event = calendarEventOccurrence.getCalendarEvent();
        return currentUserAndComponentInstanceId.getLeft()
            .contains(event.getCalendar().getComponentInstanceId()) &&
            event.getCreator().getId().equals(currentUserId);
      });
    } else {
      result.put(currentUserId, emptyList());
    }
    return result;
  }

  /**
   * Gets the next event occurrences from now.
   * @param componentIds identifiers of aimed component instance.
   * @param calendarIdsToExclude identifier of calendars which linked occurrences must be excluded
   * from the result.
   * @param usersToInclude identifiers of users which linked occurrences must be included into the
   * result
   * @param calendarIdsToInclude identifier of calendars which linked occurrences must be included
   * into the result.
   * @param zoneId the identifier of the zone.
   * @param limit the maximum occurrences the result must have (must be lower than 500)
   * @return a list of {@link CalendarEventOccurrence}.
   */
  public Stream<CalendarEventOccurrence> getNextEventOccurrences(final List<String> componentIds,
      final Set<String> calendarIdsToExclude, final Set<User> usersToInclude,
      final Set<String> calendarIdsToInclude, final ZoneId zoneId, final Integer limit) {
    final User currentRequester = User.getCurrentRequester();
    // load calendars
    final List<Calendar> calendars = getCalendarsHandledBy(componentIds);
    // includes/excludes
    calendarIdsToInclude.removeAll(calendarIdsToExclude);
    calendars.removeIf(c -> calendarIdsToExclude.contains(c.getId()));
    if (!calendarIdsToInclude.isEmpty()) {
      calendars.forEach(c -> calendarIdsToInclude.remove(c.getId()));
      calendarIdsToInclude.forEach(i -> {
        Calendar calendarToInclude = Calendar.getById(i);
        if (calendarToInclude.canBeAccessedBy(currentRequester)) {
          calendars.add(calendarToInclude);
        }
      });
    }
    // loading occurrences
    final int nbOccLimit =
        (limit != null && limit > 0 && limit <= 500) ? limit : DEFAULT_NB_MAX_NEXT_OCC;
    final LocalDate startDate =
        zoneId != null ? LocalDateTime.now(zoneId).toLocalDate() : LocalDate.now();
    final Set<CalendarEventOccurrence> occurrences = new HashSet<>();
    for (int nbMonthsToAdd : getNextEventTimeWindows()) {
      occurrences.clear();
      LocalDate endDate = startDate.plusMonths(nbMonthsToAdd);
      occurrences.addAll(getEventOccurrencesOf(startDate, endDate, calendars));
      if (!usersToInclude.isEmpty()) {
        getAllEventOccurrencesByUserIds(Pair.of(componentIds, currentRequester), startDate,
            endDate, usersToInclude).forEach((u, o) -> occurrences.addAll(o));
      }
      if (occurrences.size() >= nbOccLimit) {
        break;
      }
    }
    return occurrences.stream().sorted(COMPARATOR_BY_DATE_ASC).limit(nbOccLimit);
  }

  /**
   * Gets next event time windows from settings.
   * @return list of integer which represents months.
   */
  protected Integer[] getNextEventTimeWindows() {
    final String[] timeWindows = settings.getString("calendar.nextEvents.time.windows").split(",");
    return Arrays.stream(timeWindows).map(w -> Integer.parseInt(w.trim())).toArray(Integer[]::new);
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
