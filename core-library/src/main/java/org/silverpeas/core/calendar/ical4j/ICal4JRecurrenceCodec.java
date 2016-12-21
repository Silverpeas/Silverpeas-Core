/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.calendar.ical4j;

import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.WeekDay;
import net.fortuna.ical4j.model.parameter.Value;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.calendar.DayOfWeekOccurrence;
import org.silverpeas.core.calendar.Recurrence;
import org.silverpeas.core.calendar.RecurrencePeriod;
import org.silverpeas.core.calendar.event.CalendarEvent;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.util.Comparator;
import java.util.stream.Collectors;

import static org.silverpeas.core.calendar.DayOfWeekOccurrence.ALL_OCCURRENCES;
import static org.silverpeas.core.calendar.Recurrence.NO_RECURRENCE;
import static org.silverpeas.core.calendar.Recurrence.NO_RECURRENCE_COUNT;

/**
 * A codec to encode/decode iCal4J recurrence with Silverpeas event recurrence.
 */
@Singleton
public class ICal4JRecurrenceCodec {

  private final ICal4JDateCodec iCal4JDateCodec;

  @Inject
  public ICal4JRecurrenceCodec(final ICal4JDateCodec iCal4JDateCodec) {
    this.iCal4JDateCodec = iCal4JDateCodec;
  }

  /**
   * Converts the exception dates from a calendar event containing recurrence data.<br/>
   * The presence of an exception date must be verified before calling this method.
   * @param event the event source which contains recurrence data.
   * @return the converted exception dates.
   */
  public DateList convertExceptionDates(final CalendarEvent event) {
    Recurrence recurrence = event.getRecurrence();
    if (recurrence == NO_RECURRENCE) {
      throw new IllegalArgumentException("Event recurrence missing!");
    }
    return recurrence.getExceptionDates().stream()
      .map(offsetDateTime -> {
        if (event.isOnAllDay()) {
          return iCal4JDateCodec.encode(offsetDateTime.toLocalDate());
        } else {
          return iCal4JDateCodec.encode(offsetDateTime);
        }
      })
      .sorted()
      .collect(Collectors.toCollection(() -> {
        Value type = event.isOnAllDay() ? Value.DATE : Value.DATE_TIME;
        final DateList list = new DateList(type);
        list.setUtc(!event.isOnAllDay());
        return list;
      }));
  }

  /**
   * Encodes the recurrence of the specified Silverpeas event into an iCal4J recurrence.<br/>
   * The presence of recurrence data must be verified before calling this method.
   * @param event the event source which contains recurrence data.
   * @return the encoded iCal4J recurrence.
   * @throws SilverpeasRuntimeException if the encoding fails.
   */
  public Recur encode(final CalendarEvent event) throws SilverpeasRuntimeException {
    Recurrence eventRecurrence = event.getRecurrence();
    if (eventRecurrence == NO_RECURRENCE) {
      throw new IllegalArgumentException("Event recurrence missing!");
    }
    try {
      Recur recur = new Recur(asICal4JFrequency(eventRecurrence.getFrequency()));
      if (eventRecurrence.getFrequency().getInterval() > 1) {
        recur.setInterval(eventRecurrence.getFrequency().getInterval());
      }
      if (eventRecurrence.getRecurrenceCount() != NO_RECURRENCE_COUNT) {
        recur.setCount(eventRecurrence.getRecurrenceCount());
      } else if (eventRecurrence.getEndDate().isPresent()) {
        final OffsetDateTime endDate = eventRecurrence.getEndDate().get();
        if (event.isOnAllDay()) {
          recur.setUntil(iCal4JDateCodec.encode(endDate.toLocalDate()));
        } else {
          recur.setUntil(iCal4JDateCodec.encode(endDate.plusDays(1)));
        }
      }
      eventRecurrence.getDaysOfWeek().stream()
          .sorted(Comparator.comparing(DayOfWeekOccurrence::dayOfWeek))
          .forEach(dayOfWeekOccurrence ->
              recur.getDayList().add(encode(dayOfWeekOccurrence)));
      return recur;
    } catch (ParseException ex) {
      throw new SilverpeasRuntimeException(ex.getMessage(), ex);
    }
  }

  private String asICal4JFrequency(final RecurrencePeriod period) {
    String freq = "FREQ=";
    switch (period.getUnit()) {
      case SECOND:
        freq += Recur.SECONDLY;
        break;
      case MINUTE:
        freq += Recur.MINUTELY;
        break;
      case HOUR:
        freq += Recur.HOURLY;
        break;
      case DAY:
        freq += Recur.DAILY;
        break;
      case WEEK:
        freq += Recur.WEEKLY;
        break;
      case MONTH:
        freq += Recur.MONTHLY;
        break;
      case YEAR:
        freq += Recur.YEARLY;
        break;
    }
    return freq;
  }

  private WeekDay encode(final DayOfWeekOccurrence dayOfWeekOccurrence) {
    WeekDay weekday = encode(dayOfWeekOccurrence.dayOfWeek());
    if (dayOfWeekOccurrence.nth() != ALL_OCCURRENCES) {
      weekday = new WeekDay(weekday, dayOfWeekOccurrence.nth());
    }
    return weekday;
  }

  public WeekDay encode(final DayOfWeek dayOfWeek) {
    WeekDay weekDay = null;
    switch (dayOfWeek) {
      case MONDAY:
        weekDay = WeekDay.MO;
        break;
      case TUESDAY:
        weekDay = WeekDay.TU;
        break;
      case WEDNESDAY:
        weekDay = WeekDay.WE;
        break;
      case THURSDAY:
        weekDay = WeekDay.TH;
        break;
      case FRIDAY:
        weekDay = WeekDay.FR;
        break;
      case SATURDAY:
        weekDay = WeekDay.SA;
        break;
      case SUNDAY:
        weekDay = WeekDay.SU;
        break;
    }
    return weekDay;
  }
}
