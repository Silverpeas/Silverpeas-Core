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
package org.silverpeas.core.calendar.event;

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.calendar.CalendarTimeWindow;
import org.silverpeas.core.calendar.Recurrence;
import org.silverpeas.core.calendar.RecurrencePeriod;
import org.silverpeas.core.calendar.repository.CalendarEventRepository;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.util.ServiceProvider;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * A collection of occurrences of events planned in a given calendar.
 * @author mmoquillon
 */
@Singleton
public class PlannedEventOccurrences {

  static PlannedEventOccurrences get() {
    return ServiceProvider.getService(PlannedEventOccurrences.class);
  }

  @Inject
  private CalendarEventRepository repository;

  @Inject
  private CalendarEventOccurrenceGenerator generator;

  private PlannedEventOccurrences() {
  }

  /**
   * Lists the occurrences of the events that occur in the specified time window.
   * @param timeWindow the time window.
   * @return a list of event occurrences sorted by their start date time.
   */
  @SuppressWarnings("unused")
  List<CalendarEventOccurrence> in(final CalendarTimeWindow timeWindow) {
    List<CalendarEvent> events = repository.getAllBetween(timeWindow.getCalendar(),
        timeWindow.getStartDate().atStartOfDay().atOffset(ZoneOffset.UTC),
        timeWindow.getEndDate().plusDays(1).atStartOfDay().minusMinutes(1)
            .atOffset(ZoneOffset.UTC));
    return getGenerator().generateOccurrencesOf(events, timeWindow);
  }

  /**
   * Removes the specified occurrence from this collection of planned event occurrences.
   * This will follow the following rules:
   * <ul>
   * <li>If the occurrence is the single one of an event, then the event is deleted.</li>
   * <li>If the occurrence is one of among any of an event, then the date time at which this
   * occurrence starts is added as an exception in the recurrence rule of the event.</li>
   * <li>If the occurrence is the last one of the event, then the event is deleted.</li>
   * </ul>
   * @param occurrence the occurrence to remove.
   */
  void remove(final CalendarEventOccurrence occurrence) {
    doEitherOr(occurrence, CalendarEvent::delete, this::excludeOccurrence);
  }

  /**
   * Updates the specified occurrence.
   * This will follow the following rules:
   * <ul>
   * <li>If the occurrence is the single one of an event, then the event is updated.</li>
   * <li>If the occurrence is one of among any of an event, then the date time at which this
   * occurrence starts is added as an exception in the recurrence rule of the event and a new event
   * is created from the change.</li>
   * <li>If the occurrence is the last one of the event, then the event is updated.</li>
   * </ul>
   * @param occurrence the occurrence to remove.
   */
  void update(final CalendarEventOccurrence occurrence) {
    doEitherOr(occurrence, e -> {
      e.setPeriod(Period.between(occurrence.getStartDateTime(), occurrence.getEndDateTime()));
      e.update();
    }, o -> {
      excludeOccurrence(o);
      createNewEventFrom(o);
    });
  }

  private void excludeOccurrence(final CalendarEventOccurrence occurrence) {
    CalendarEvent event = occurrence.getCalendarEvent();
    event.getRecurrence().excludeEventOccurrencesStartingAt(occurrence.getLastStartDateTime());
    event.update();
  }

  private void createNewEventFrom(final CalendarEventOccurrence occurrence) {
    CalendarEvent newEvent = occurrence.getCalendarEvent().clone()
        .createdBy(occurrence.getCalendarEvent().getLastUpdatedBy());
    newEvent.unsetRecurrence();
    newEvent.setPeriod(Period.between(occurrence.getStartDateTime(), occurrence.getEndDateTime()));
    newEvent.planOn(occurrence.getCalendarEvent().getCalendar());
    occurrence.setCalendarEvent(newEvent);
  }

  private OffsetDateTime endDateTimeOf(final Recurrence recurrence,
      final OffsetDateTime recurrenceStart) {
    return recurrence.getEndDate().orElseGet(() -> {
      RecurrencePeriod frequency = recurrence.getFrequency();
      long timeCount = frequency.getInterval() * recurrence.getRecurrenceCount();
      switch (frequency.getUnit()) {
        case DAY:
          return recurrenceStart.plusDays(timeCount);
        case WEEK:
          return recurrenceStart.plusWeeks(timeCount);
        case MONTH:
          return recurrenceStart.plusMonths(timeCount);
        case YEAR:
          return recurrenceStart.plusYears(timeCount);
        default:
          throw new SilverpeasRuntimeException("Unsupported unit: " + frequency.getUnit());
      }
    });
  }

  private void doEitherOr(final CalendarEventOccurrence withOccurrence,
      Consumer<CalendarEvent> ifSingleOccurrence,
      Consumer<CalendarEventOccurrence> ifManyOccurrences) {
    Transaction.getTransaction().perform(() -> {
      CalendarEvent event = withOccurrence.getCalendarEvent();
      if (event.isRecurrent() && event.getRecurrence().isEndless()) {
        ifManyOccurrences.accept(withOccurrence);
      } else if (event.isRecurrent()) {
        OffsetDateTime recurrenceStart = event.getStartDateTime();
        OffsetDateTime recurrenceEnd = endDateTimeOf(event.getRecurrence(), recurrenceStart);
        List<CalendarEventOccurrence> occurrences =
            getGenerator().generateOccurrencesOf(Collections.singletonList(event),
                Period.between(recurrenceStart, recurrenceEnd));
        if (occurrences.size() == 1 && occurrences.get(0).equals(withOccurrence)) {
          ifSingleOccurrence.accept(event);
        } else {
          ifManyOccurrences.accept(withOccurrence);
        }
      } else {
        ifSingleOccurrence.accept(event);
      }
      return null;
    });
  }

  private CalendarEventOccurrenceGenerator getGenerator() {
    return this.generator;
  }
}
