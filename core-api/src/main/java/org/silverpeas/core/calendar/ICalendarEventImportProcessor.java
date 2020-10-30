/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.core.calendar;

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.calendar.CalendarEvent.EventOperationResult;
import org.silverpeas.core.calendar.icalendar.ICalendarImporter;
import org.silverpeas.core.calendar.repository.CalendarEventRepository;
import org.silverpeas.core.importexport.ImportDescriptor;
import org.silverpeas.core.importexport.ImportException;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.OperationContext;
import org.silverpeas.core.persistence.datasource.model.jpa.JpaEntityReflection;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.util.StringUtil;

import javax.inject.Inject;
import java.io.InputStream;
import java.io.Reader;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.silverpeas.core.calendar.CalendarComponent.DESCRIPTION_MAX_LENGTH;
import static org.silverpeas.core.calendar.CalendarComponent.TITLE_MAX_LENGTH;
import static org.silverpeas.core.persistence.datasource.OperationContext.State.IMPORT;

/**
 * A processor of importation of events from an iCalendar source into the Silverpeas Calendar
 * Engine.
 * <p>
 * The processor consumes an input stream or a reader of an iCalendar source to produce
 * the {@link CalendarEvent} and the {@link CalendarEventOccurrence} instances that will be then
 * saved into the Silverpeas calendar passed as argument. Any events or occurrences yet existing in
 * the calendar will be updated. The occurrences in the calendar that aren't anymore referenced in
 * the iCalendar source will be deleted. The import doesn't delete any events but only adds or
 * updates the events from the iCalendar source.
 * </p>
 * <p>
 * In the case of an import into a synchronized calendar, the import processor will consider the
 * operation as being part of a synchronization with an external calendar. In that case, the events
 * coming from the iCalendar source will be timestamped with the synchronization date of the
 * synchronized calendar.
 * </p>
 * @author mmoquillon
 */
@Service
public class ICalendarEventImportProcessor {

  @Inject
  private ICalendarImporter iCalendarImporter;

  @Inject
  private CalendarEventRepository eventRepository;

  /**
   * Imports into the specified calendar in Silverpeas the events encoded in the iCalendar format
   * coming from the specified input stream.
   * <p>
   * All new events will be added in the given calendar. All already existing events in the calendar
   * will be updated with their more recent counterpart in the iCalendar source. This policy is
   * applied also on the occurrences of the events. unlike with the occurrences, any events in the
   * calendar not present in the iCalendar source won't be touched. For a synchronized calendar,
   * the imported events will be timestamped with calendar's synchronization date.
   * </p>
   * <p>
   * The calendar must exist in Silverpeas otherwise an {@link IllegalArgumentException} is thrown.
   * </p>
   * @param calendar a calendar in Silverpeas.
   * @param inputStream the input stream in which are encoded the events to import.
   * @return the result of the import process.
   * @throws ImportException exception thrown if the import fails.
   */
  public ICalendarImportResult importInto(final Calendar calendar, final InputStream inputStream)
      throws ImportException {
    return importInto(calendar, ImportDescriptor.withInputStream(inputStream));
  }

  /**
   * Imports into the specified calendar in Silverpeas the events encoded in the iCalendar format
   * coming from the specified io reader.
   *  <p>
   * All new events will be added in the given calendar. All already existing events in the calendar
   * will be updated with their more recent counterpart in the iCalendar source. This policy is
   * applied also on the occurrences of the events. unlike with the occurrences, any events in the
   * calendar not present in the iCalendar source won't be touched. For a synchronized calendar,
   * the imported events will be timestamped with calendar's synchronization date.
   * </p>
   * <p>
   * The calendar must exist in Silverpeas otherwise an {@link IllegalArgumentException} is thrown.
   * </p>
   * @param calendar a calendar in Silverpeas.
   * @param reader the reader in which are encoded the events to import.
   * @return the result of the import process.
   * @throws ImportException exception thrown if the import fails.
   */
  public ICalendarImportResult importInto(final Calendar calendar, final Reader reader)
      throws ImportException {
    return importInto(calendar, ImportDescriptor.withReader(reader));
  }

  /**
   * Imports into the specified calendar in Silverpeas the events encoded in the iCalendar format
   * with the specified descriptor of import.
   * <p>
   * It is the effective import process dedicated to be used by the Silverpeas Calendar Engine to
   * fetch and store calendar events coming from an external calendar. This process can be simply
   * an importation of an iCalendar source content or a synchronization with an external calendar.
   * The ony difference between an simple importation and a synchronization is that in the
   * synchronization the calendar's synchronization date will be set for each event fetched from the
   * iCalendar source; the behavior of the method doesn't change.
   * </p>
   * <p>
   * The calendar must exist in Silverpeas otherwise an {@link IllegalArgumentException} is thrown.
   * </p>
   * @param calendar a calendar in Silverpeas.
   * @param descriptor a descriptor of import with the iCalendar source from which the event will
   * be parsed.
   * @return the result of the import process.
   * @throws ImportException exception thrown if the import fails.
   */
  protected ICalendarImportResult importInto(final Calendar calendar,
      final ImportDescriptor descriptor) throws ImportException {
    if (!calendar.isPersisted()) {
      throw new IllegalArgumentException(
          "The calendar " + calendar.getTitle() + " (id = " + calendar.getId() +
              ") doesn't exist in Silverpeas");
    }
    OperationContext.fromCurrentRequester();
    final ICalendarImportResult importResult = new ICalendarImportResult();
    iCalendarImporter.imports(descriptor, events -> Transaction.performInOne(() -> {
      events.forEach(e -> {
        CalendarEvent event = e.getLeft();
        List<CalendarEventOccurrence> occurrences = e.getRight();
        adjustSomeProperties(event.asCalendarComponent());
        EventOperationResult result = importEvent(calendar, event, occurrences);
        result.created().ifPresent(ce -> importResult.incAdded());
        result.updated().ifPresent(ue -> importResult.incUpdated());
      });
      return null;
    }));
    return importResult;
  }

  /**
   * Imports into the specified calendar in Silverpeas the specified event with its modified
   * occurrences.
   * <p>
   * If the event already exists in the calendar, then it is updated with the properties carried
   * by the specified event. Otherwise this latter is planned into the calendar. All of the
   * specified occurrences of the given event follows the same rule.
   * </p>
   * @param calendar the calendar into which the specified event and occurrences should be
   * imported.
   * @param event the event to plan or to update in the calendar.
   * @param occurrences the occurrences of the events that are modified from the event or its
   * recurrence rule and that have to be added into the calendar.
   * @return the result of the importation. It has the event in the calendar if either the event
   * was updated or its modified occurrences saved.
   */
  private EventOperationResult importEvent(final Calendar calendar, final CalendarEvent event,
      final List<CalendarEventOccurrence> occurrences) {
    OperationContext.addStates(IMPORT);
    try {
      return Transaction.performInOne(() -> {
        if (calendar.isSynchronized() && calendar.getLastSynchronizationDate().isPresent()) {
          event.setLastSynchronizationDate(calendar.getLastSynchronizationDate().get());
        }
        final EventOperationResult result = importEventOnly(calendar, event);
        if (!occurrences.isEmpty()) {
          EventOperationResult occurrenceImportResult =
              importOccurrencesOnly(EventImportResult.eventFrom(result), occurrences);
          adjustOccurrenceImportResult(occurrenceImportResult, result);
        }
        return result;
      });
    } finally {
      OperationContext.removeStates(IMPORT);
    }
  }

  private void adjustOccurrenceImportResult(final EventOperationResult occurrenceImportResult,
      final EventOperationResult result) {
    occurrenceImportResult.updated().ifPresent(e -> {
      if (!result.created().isPresent()) {
        result.withUpdated(e);
      }
    });
    occurrenceImportResult.instance().ifPresent(i -> {
      if (!result.created().isPresent() && !result.updated().isPresent()) {
        result.withUpdated(i.getCalendarEvent());
      }
    });
  }

  private void adjustSomeProperties(final CalendarComponent component) {
    if (StringUtil.isNotDefined(component.getTitle())) {
      component.setTitle("N/A");
    } else if (component.getTitle().length() > TITLE_MAX_LENGTH) {
      component.setTitle(StringUtil.truncate(component.getTitle().trim(), TITLE_MAX_LENGTH));
    }
    if (component.getDescription().length() > DESCRIPTION_MAX_LENGTH) {
      component.setDescription(
          StringUtil.truncate(component.getDescription().trim(), DESCRIPTION_MAX_LENGTH));
    }
    if (component.getLocation().length() > TITLE_MAX_LENGTH) {
      component.setLocation(StringUtil.truncate(component.getLocation().trim(), TITLE_MAX_LENGTH));
    }
  }

  private EventOperationResult importEventOnly(final Calendar calendar, final CalendarEvent event) {
    EventOperationResult result;
    Optional<CalendarEvent> persistedEvent = getExistingCalendarEvent(calendar, event);
    if (persistedEvent.isPresent()) {
      final CalendarEvent existingEvent = persistedEvent.get();
      result = new EventImportResult().withExisting(existingEvent);
      if (wasUpdated(event, existingEvent)) {
        final Date lastUpdateDateBeforeUpdate = existingEvent.getLastUpdateDate();
        final EventOperationResult updateResult = existingEvent.updateFrom(event);
        if (updateResult.updated().isPresent() && !updateResult.updated().get().getLastUpdateDate()
            .equals(lastUpdateDateBeforeUpdate)) {
          result = updateResult;
        }
      } else if (event.isSynchronized()) {
        existingEvent.setLastSynchronizationDate(event.getLastSynchronizationDate());
        eventRepository.save(existingEvent);
      }
    } else {
      result = new EventOperationResult().withCreated(event.planOn(calendar));
    }
    return result;
  }

  private EventOperationResult importOccurrencesOnly(final CalendarEvent event,
      final List<CalendarEventOccurrence> occurrencesToImport) {
    final Mutable<EventOperationResult> result = Mutable.of(new EventOperationResult());
    final Map<String, CalendarEventOccurrence> existingOccurrences = new HashMap<>();

    event.getPersistedOccurrences()
        .forEach(o -> existingOccurrences.put(o.getOriginalStartDate().toString(), o));

    occurrencesToImport.stream().sorted(CalendarEventOccurrence.COMPARATOR_BY_ORIGINAL_DATE_ASC)
        .forEach(o -> {
          o.setCalendarEvent(event);
          adjustSomeProperties(o.asCalendarComponent());
          Optional<CalendarEventOccurrence> existingOccurrence =
              Optional.ofNullable(existingOccurrences.remove(o.getOriginalStartDate().toString()));
          if (existingOccurrence.isPresent()) {
            if (wasUpdated(o, existingOccurrence.get())) {
              result.set(existingOccurrence.get().updateFrom(o));
            }
          } else {
            // we save it directly into the persistence engine as the event could be not yet saved
            // (hence the update method is meaningless; the event could be into the transactional
            // cache)
            o.saveIntoPersistence();
            result.get().withUpdated(o.getCalendarEvent());
          }
        });

    // Deleting the existing occurrences which do not exist anymore
    if (!existingOccurrences.isEmpty()) {
      result.get().withUpdated(event);
    }
    existingOccurrences.forEach((originalStartDate, occurrence) -> event.deleteOnly(occurrence));

    return result.get();
  }

  private Optional<CalendarEvent> getExistingCalendarEvent(final Calendar calendar,
      final CalendarEvent event) {
    Optional<CalendarEvent> optionalPersistedEvent = calendar.externalEvent(event.getExternalId());
    if (!optionalPersistedEvent.isPresent()) {
      // If none, searching the existence of the event on its id
      optionalPersistedEvent = calendar.event(event.getExternalId());
    }
    return optionalPersistedEvent;
  }

  private boolean wasUpdated(final CalendarEvent imported, final CalendarEvent existing) {
    if (imported.getLastUpdateDate() == null) {
      // Setting the last modification date to the one of event and indicate it as updated
      JpaEntityReflection.setUpdateData(imported.asCalendarComponent(), existing.getLastUpdater(),
          existing.getLastUpdateDate());
      return true;
    }
    return imported.getLastUpdateDate().after(existing.getLastUpdateDate()) ||
        (imported.getRecurrence() != null &&
            !imported.getRecurrence().equals(existing.getRecurrence()));
  }

  private boolean wasUpdated(final CalendarEventOccurrence imported,
      final CalendarEventOccurrence existing) {
    if (imported.getLastUpdateDate() == null) {
      // Setting the last modification date to the one of occurrence and indicate it as updated
      JpaEntityReflection.setUpdateData(imported.asCalendarComponent(), existing.getLastUpdater(),
          existing.getLastUpdateDate());
      return true;
    }
    return imported.getLastUpdateDate().after(existing.getLastUpdateDate());
  }

  private static class EventImportResult extends EventOperationResult {

    private CalendarEvent existing;

    public static CalendarEvent eventFrom(final EventOperationResult result) {
      if (result.created().isPresent()) {
        return result.created().get();
      }
      if (result.updated().isPresent()) {
        return result.updated().get();
      }
      if (result instanceof EventImportResult) {
        EventImportResult importResult = (EventImportResult) result;
        if (importResult.existing().isPresent()) {
          return importResult.existing;
        }
      }
      return null;
    }

    public EventImportResult withExisting(final CalendarEvent event) {
      this.existing = event;
      return this;
    }

    public Optional<CalendarEvent> existing() {
      return Optional.ofNullable(this.existing);
    }
  }
}
