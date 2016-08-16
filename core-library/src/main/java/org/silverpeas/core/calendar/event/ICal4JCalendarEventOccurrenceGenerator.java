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

import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.WeekDay;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Uid;
import org.silverpeas.core.NotSupportedException;
import org.silverpeas.core.calendar.Recurrence;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.date.TimeUnit;

import javax.inject.Singleton;
import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An implementation of the {@link CalendarEventOccurrenceGenerator} by using the iCal4J library.
 * @author mmoquillon
 */
@Singleton
public class ICal4JCalendarEventOccurrenceGenerator implements CalendarEventOccurrenceGenerator {

  @Override
  public List<CalendarEventOccurrence> generateOccurrencesOf(
      final Collection<CalendarEvent> events, final Period inPeriod) {
    List<CalendarEventOccurrence> occurrences = new ArrayList<>();
    events.forEach(event -> {
      VEvent vEvent = fromCalendarEvent(event);
      PeriodList periodList = vEvent.calculateRecurrenceSet(fromPeriod(inPeriod));
      periodList.forEach(period -> {
        net.fortuna.ical4j.model.Period occurPeriod = (net.fortuna.ical4j.model.Period) period;
        OffsetDateTime occurStart = asOffsetDateTime(occurPeriod.getStart());
        OffsetDateTime occurEnd = asOffsetDateTime(occurPeriod.getEnd());
        occurrences.add(new CalendarEventOccurrence(event, occurStart, occurEnd));
      });
    });
    occurrences.sort((occurLeft, occurRight) -> occurLeft.getStartDateTime()
        .compareTo(occurRight.getStartDateTime()));
    return occurrences;
  }

  private ExDate generateExceptionDates(final Recurrence recurrence) {
    DateList exDateList = new DateList();
    exDateList.setUtc(true);
    recurrence.getExceptionDates()
        .stream()
        .map(offsetDateTime -> asDateTime(offsetDateTime))
        .forEach(dateTime -> exDateList.add(dateTime));
    return new ExDate(exDateList);
  }

  private RRule generateRecurrenceRule(final Recurrence recurrence) {
    String recurrenceType = getRecurrentType(recurrence.getFrequency().getUnit());
    Recur recur;
    if (recurrence.getEndDate().isPresent()) {
      recur = new Recur(recurrenceType, asDateTime(recurrence.getEndDate().get()));
    } else if (recurrence.getRecurrenceCount() != Recurrence.NO_RECURRENCE_COUNT) {
      recur = new Recur(recurrenceType, recurrence.getRecurrenceCount());
    } else {
      recur = new Recur(recurrenceType, null);
    }
    recur.setInterval(recurrence.getFrequency().getInterval());

    recurrence.getDaysOfWeek().stream().
        forEach(dayOfWeekOccurrence -> {
          WeekDay weekDay = fromDayOfWeek(dayOfWeekOccurrence.dayOfWeek());
          if (recurrence.getFrequency().isWeekly() || recurrence.getFrequency().isDaily() ||
              dayOfWeekOccurrence.nth() == 0) {
            recur.getDayList().add(weekDay);
          } else {
            recur.getDayList().add(new WeekDay(weekDay, dayOfWeekOccurrence.nth()));
          }
        });
    return new RRule(recur);
  }

  private String getRecurrentType(final TimeUnit recurrenceUnit) {
    String recurrenceType;
    switch (recurrenceUnit) {
      case DAY:
        recurrenceType = Recur.DAILY;
        break;
      case WEEK:
        recurrenceType = Recur.WEEKLY;
        break;
      case MONTH:
        recurrenceType = Recur.MONTHLY;
        break;
      case YEAR:
        recurrenceType = Recur.YEARLY;
        break;
      default:
        throw new NotSupportedException("Recurrence unit not yet supported: " + recurrenceUnit);
    }
    return recurrenceType;
  }

  private VEvent fromCalendarEvent(CalendarEvent event) {
    DateTime dtStart = asDateTime(event.getStartDateTime());
    DateTime dtEnd = asDateTime(event.getEndDateTime());
    dtStart.setUtc(true);
    dtEnd.setUtc(true);
    VEvent vEvent = new VEvent(dtStart, dtEnd, event.getTitle());
    vEvent.getProperties().add(new Uid(event.getId()));
    if (event.isRecurrent()) {
      vEvent.getProperties().add(generateRecurrenceRule(event.getRecurrence()));
      if (!event.getRecurrence().getExceptionDates().isEmpty()) {
        vEvent.getProperties().add(generateExceptionDates(event.getRecurrence()));
      }
    }
    return vEvent;
  }

  private WeekDay fromDayOfWeek(final DayOfWeek dayOfWeek) {
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

  private net.fortuna.ical4j.model.Period fromPeriod(final Period period) {
    return new net.fortuna.ical4j.model.Period(
        new DateTime(period.getStartDateTime().toInstant().toEpochMilli()),
        new DateTime(period.getEndDateTime().toInstant().toEpochMilli()));
  }

  private DateTime asDateTime(OffsetDateTime offsetDateTime) {
    return new DateTime(offsetDateTime.toInstant().toEpochMilli());
  }

  private OffsetDateTime asOffsetDateTime(DateTime dateTime) {
    return dateTime.toInstant().atOffset(ZoneOffset.UTC);
  }
}
