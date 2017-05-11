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

import org.silverpeas.core.calendar.CalendarEvent.EventOperationResult;
import org.silverpeas.core.calendar.icalendar.ICalendarImporter;
import org.silverpeas.core.importexport.ImportDescriptor;
import org.silverpeas.core.importexport.ImportException;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.repository.OperationContext;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.util.StringUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.InputStream;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static org.silverpeas.core.persistence.datasource.repository.OperationContext.State.IMPORT;

/**
 * A processor of importation of events from an iCalendar source into the Silverpeas Calendar
 * Engine.
 * <p>
 * The processor consumes an input stream or a reader on an iCalendar source to produce
 * the {@link CalendarEvent} and the {@link CalendarEventOccurrence} instances that will be then
 * saved into the Calendar data source of Silverpeas.
 * </p>
 * @author mmoquillon
 */
@Singleton
public class ICalendarEventImportProcessor {

  @Inject
  private ICalendarImporter iCalendarImporter;

  /**
   * Imports into the specified calendar in Silverpeas the events encoded in the iCalendar format
   * coming from the specified input stream.
   * @param calendar a calendar in Silverpeas.
   * @param inputStream the input stream in which are encoded the events to import.
   * @throws ImportException exception thrown if the import fails.
   */
  public ICalendarImportResult importInto(final Calendar calendar, final InputStream inputStream)
      throws ImportException {
    return importInto(calendar, ImportDescriptor.withInputStream(inputStream));
  }

  /**
   * Imports into the specified calendar in Silverpeas the events encoded in the iCalendar format
   * coming from the specified io reader.
   * @param calendar a calendar in Silverpeas.
   * @param reader the reader in which are encoded the events to import.
   * @throws ImportException exception thrown if the import fails.
   */
  public ICalendarImportResult importInto(final Calendar calendar, final Reader reader)
      throws ImportException {
    return importInto(calendar, ImportDescriptor.withReader(reader));
  }

  private ICalendarImportResult importInto(final Calendar calendar,
      final ImportDescriptor descriptor) throws ImportException {
    final ICalendarImportResult importResult = new ICalendarImportResult();
    iCalendarImporter.imports(descriptor, events -> Transaction.performInOne(() -> {
      events.forEach(e -> {
        CalendarEvent event = e.getLeft();
        List<CalendarEventOccurrence> occurrences = e.getRight();
        adjustSomeProperties(event);
        EventOperationResult result = doImport(calendar, event, occurrences);
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
  private EventOperationResult doImport(final Calendar calendar, final CalendarEvent event,
      final List<CalendarEventOccurrence> occurrences) {
    OperationContext.fromCurrentRequester();
    OperationContext.addStates(IMPORT);
    try {
      return Transaction.performInOne(() -> {
        final EventOperationResult result = importEvent(calendar, event);
        if (!occurrences.isEmpty()) {
          EventOperationResult occurrenceImportResult =
              importOccurrences(EventImportResult.eventFrom(result), occurrences);
          occurrenceImportResult.updated().ifPresent(e -> {
            if (!result.created().isPresent()) {
              result.withUpdated(e);
            }
          });
        }
        return result;
      });
    } finally {
      OperationContext.removeStates(IMPORT);
    }
  }

  private void adjustSomeProperties(final CalendarEvent event) {
    if (StringUtil.isNotDefined(event.getTitle())) {
      event.withTitle("N/A");
    }
  }

  private EventOperationResult importEvent(final Calendar calendar, final CalendarEvent event) {
    EventOperationResult result;

    Optional<CalendarEvent> persistedEvent = getExistingCalendarEvent(calendar, event);
    if (persistedEvent.isPresent()) {
      final CalendarEvent existingEvent = persistedEvent.get();
      result = new EventImportResult().withExisting(existingEvent);
      if (wasUpdated(event, existingEvent)) {
        result = existingEvent.updateFrom(event);
      }
    } else {
      result = new EventOperationResult().withCreated(event.planOn(calendar));
    }
    return result;
  }

  private EventOperationResult importOccurrences(final CalendarEvent event,
      final List<CalendarEventOccurrence> occurrencesToImport) throws ImportException {
    final Mutable<EventOperationResult> result = Mutable.of(new EventOperationResult());

    List<CalendarEventOccurrence> persistedOccurrences = event.getPersistedOccurrences();

    final Iterator<CalendarEventOccurrence> existingOccurrences =
        persistedOccurrences.stream().sorted(CalendarEventOccurrence.COMPARATOR_BY_DATE).iterator();
    final Mutable<CalendarEventOccurrence> currentExistingOccurrence =
        Mutable.of(existingOccurrences.hasNext() ? existingOccurrences.next() : null);

    occurrencesToImport.stream().sorted(CalendarEventOccurrence.COMPARATOR_BY_DATE).forEach(o -> {
      o.setCalendarEvent(event);
      Optional<CalendarEventOccurrence> existingOccurrence = currentExistingOccurrence.isPresent() ?
          findMatching(o, existingOccurrences, currentExistingOccurrence.get()):Optional.empty();
      if (existingOccurrence.isPresent()) {
        currentExistingOccurrence.set(existingOccurrence.get());
        if (wasUpdated(o, currentExistingOccurrence.get())) {
          result.set(currentExistingOccurrence.get().updateFrom(o));
        }
      } else {
        // we save it directly into the persistence engine as the event could be not yet saved
        // (hence the update method is meaningless; the event could be into the transactional cache)
        o.saveIntoPersistence();
        result.get().withUpdated(o.getCalendarEvent());
      }
    });

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
    return imported.getLastUpdateDate().after(existing.getLastUpdateDate());
  }

  private boolean wasUpdated(final CalendarEventOccurrence imported,
      final CalendarEventOccurrence existing) {
    return imported.getLastUpdateDate().after(existing.getLastUpdateDate());
  }

  private Optional<CalendarEventOccurrence> findMatching(final CalendarEventOccurrence imported,
      Iterator<CalendarEventOccurrence> existingOccurrences,
      final CalendarEventOccurrence fromOccurrence) {
    CalendarEventOccurrence existingOccurrence = fromOccurrence;
    while (existingOccurrences.hasNext() && existingOccurrence.isOriginallyBefore(imported)) {
      existingOccurrence = existingOccurrences.next();
    }
    return matches(imported, existingOccurrence) ? Optional.of(existingOccurrence) :
        Optional.empty();
  }

  private boolean matches(final CalendarEventOccurrence imported,
      final CalendarEventOccurrence existing) {
    return existing != null &&
        imported.getOriginalStartDate().equals(existing.getOriginalStartDate());
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
