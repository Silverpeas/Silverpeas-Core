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

package org.silverpeas.core.importexport.ical.ical4j;

import org.silverpeas.core.calendar.CalendarEventRecurrence;
import org.silverpeas.core.calendar.DayOfWeekOccurrence;
import org.silverpeas.core.calendar.RecurrencePeriod;
import org.silverpeas.core.importexport.EncodingException;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.WeekDay;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.text.ParseException;

import static org.silverpeas.core.calendar.CalendarEventRecurrence.*;
import static org.silverpeas.core.calendar.DayOfWeekOccurrence.ALL_OCCURRENCES;

/**
 * A codec to encode/decode iCal4J recurrence with Silverpeas event recurrence.
 */
@Singleton
public class ICal4JRecurrenceCodec {

  @Inject
  private ICal4JDateCodec iCal4JDateCodec;

  /**
   * Encodes the specified Silverpeas event recurrence into an iCal4J recurrence.
   * @param eventRecurrence the specified event recurrence to encode.
   * @return the encoded iCal4J recurrence.
   * @throws EncodingException if the encoding fails.
   */
  public Recur encode(final CalendarEventRecurrence eventRecurrence) throws EncodingException {
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
      } else if (eventRecurrence.getEndDate() != NO_RECURRENCE_END_DATE) {
        Date endDate = iCal4JDateCodec.encodeInUTC(eventRecurrence.getEndDate());
        recur.setUntil(endDate);
      }
      for (DayOfWeekOccurrence dayOfWeekOccurrence : eventRecurrence.getDaysOfWeek()) {
        recur.getDayList().add(asICal4JWeekOfDay(dayOfWeekOccurrence));
      }
      return recur;
    } catch (ParseException ex) {
      throw new EncodingException(ex.getMessage(), ex);
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

  private WeekDay asICal4JWeekOfDay(final DayOfWeekOccurrence dayOfWeekOccurrence) {
    WeekDay weekday = null;
    switch (dayOfWeekOccurrence.dayOfWeek()) {
      case MONDAY:
        weekday = WeekDay.MO;
        break;
      case TUESDAY:
        weekday = WeekDay.TU;
        break;
      case WEDNESDAY:
        weekday = WeekDay.WE;
        break;
      case THURSDAY:
        weekday = WeekDay.TH;
        break;
      case FRIDAY:
        weekday = WeekDay.FR;
        break;
      case SATURDAY:
        weekday = WeekDay.SA;
        break;
      case SUNDAY:
        weekday = WeekDay.SU;
        break;
    }
    if (dayOfWeekOccurrence.nth() != ALL_OCCURRENCES) {
      weekday = new WeekDay(weekday, dayOfWeekOccurrence.nth());
    }
    return weekday;
  }
}
