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
import org.silverpeas.core.calendar.Recurrence;
import org.silverpeas.core.calendar.RecurrencePeriod;
import org.silverpeas.core.calendar.repository.CalendarEventRepository;
import org.silverpeas.core.date.Period;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import static java.time.Month.DECEMBER;

/**
 * A collection of occurrences of events planned in a given calendar.
 * @author mmoquillon
 */
public class PlannedEventOccurrences {

  private final CalendarEventRepository repository;
  private CalendarEventOccurrenceGenerator generator;

  /**
   * Gets the occurrences of the events that occur in the specified period.
   * @param year the year during which the events occur.
   * @return a set of event occurrences sorted by their start date time.
   */
  public List<CalendarEventOccurrence> in(final Year year) {
    List<CalendarEvent> events =
        getEventsBetween(year.atDay(1), year.atMonth(DECEMBER).atEndOfMonth());
    return generator.generateOccurrencesOf(events, year);
  }

  /**
   * Gets the occurrences of the events that occur in the specified period.
   * @param yearMonth the month and year during which the events occur.
   * @return a set of event occurrences sorted by their start date time.
   */
  public List<CalendarEventOccurrence> in(final YearMonth yearMonth) {
    List<CalendarEvent> events = getEventsBetween(yearMonth.atDay(1), yearMonth.atEndOfMonth());
    return generator.generateOccurrencesOf(events, yearMonth);
  }

  /**
   * Gets the occurrences of the events that occur in the specified period.
   * @param day day during which the events occur.
   * @return a set of event occurrences sorted by their start date time.
   */
  public List<CalendarEventOccurrence> in(final LocalDate day) {
    List<CalendarEvent> events = getEventsBetween(day, day);
    return CalendarEventOccurrenceGenerator.get().generateOccurrencesOf(events, day);
  }

  /**
   * Gets the occurrences of the events that occur in the specified period.
   * @param start the start date of the period.
   * @param end the end date of the period.
   * @return a set of event occurrences sorted by their start date time.
   */
  public List<CalendarEventOccurrence> between(final LocalDate start, final LocalDate end) {
    List<CalendarEvent> events = getEventsBetween(start, end);
    return generator.generateOccurrencesOf(events, Period.between(start, end));
  }

  protected PlannedEventOccurrences(final CalendarEventRepository repository) {
    this.repository = repository;
    this.generator = CalendarEventOccurrenceGenerator.get();
  }

  private List<CalendarEvent> getEventsBetween(LocalDate startDate, LocalDate endDate) {
    return repository.getAllBetween(startDate.atStartOfDay().atOffset(ZoneOffset.UTC),
        endDate.plusDays(1).atStartOfDay().minusMinutes(1).atOffset(ZoneOffset.UTC));
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
  public void remove(final CalendarEventOccurrence occurrence) {
    CalendarEvent event = occurrence.getCalendarEvent();
    if (!event.isRecurrent()) {
      event.delete();
    } else {
      if (event.getRecurrence().getEndDate().isPresent() ||
          event.getRecurrence().getRecurrenceCount() != Recurrence.NO_RECURRENCE_COUNT) {
        OffsetDateTime recurrenceStart = event.getStartDateTime();
        OffsetDateTime recurrenceEnd = endDateTimeOf(event.getRecurrence(), recurrenceStart);
        List<CalendarEventOccurrence> occurrences =
            generator.generateOccurrencesOf(Arrays.asList(event),
                Period.between(recurrenceStart, recurrenceEnd));
        if (occurrences.size() == 1 && occurrences.get(0).equals(occurrence)) {
          event.delete();
        } else {
          excludeOccurrence(occurrence);
        }
      } else {
        excludeOccurrence(occurrence);
      }
    }
  }

  private void excludeOccurrence(final CalendarEventOccurrence occurrence) {
    CalendarEvent event = occurrence.getCalendarEvent();
    event.getRecurrence().excludeEventOccurrencesStartingAt(occurrence.getStartDateTime());
    event.update();
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
}
