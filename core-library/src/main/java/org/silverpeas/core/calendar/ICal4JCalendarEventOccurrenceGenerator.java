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
 * FLOSS exception.  You should have received a copy of the text describing
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
package org.silverpeas.core.calendar;

import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.WeekDay;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Uid;
import org.silverpeas.core.NotSupportedException;
import org.silverpeas.core.calendar.ical4j.ICal4JDateCodec;
import org.silverpeas.core.calendar.ical4j.ICal4JRecurrenceCodec;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.date.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * An implementation of the {@link CalendarEventOccurrenceGenerator} by using the iCal4J library.
 * @author mmoquillon
 */
@Singleton
public class ICal4JCalendarEventOccurrenceGenerator implements CalendarEventOccurrenceGenerator {

  private final ICal4JDateCodec iCal4JDateCodec;
  private final ICal4JRecurrenceCodec iCal4JRecurrenceCodec;

  @Inject
  public ICal4JCalendarEventOccurrenceGenerator(final ICal4JDateCodec iCal4JDateCodec,
      final ICal4JRecurrenceCodec iCal4JRecurrenceCodec) {
    this.iCal4JDateCodec = iCal4JDateCodec;
    this.iCal4JRecurrenceCodec = iCal4JRecurrenceCodec;
  }

  @Override
  public List<CalendarEventOccurrence> computeOccurrencesOf(final Collection<CalendarEvent> events,
      final Period inPeriod) {
    List<CalendarEventOccurrence> occurrences = new ArrayList<>();
    events.forEach(event -> {
      VEvent vEvent = fromCalendarEvent(event);
      PeriodList periodList = vEvent.calculateRecurrenceSet(fromPeriod(inPeriod));
      periodList.forEach(occurPeriod -> {
        final Temporal occurStart;
        final Temporal occurEnd;
        if (event.isOnAllDay()) {
          occurStart = asOffsetDateTime(occurPeriod.getStart()).toLocalDate();
          occurEnd = asOffsetDateTime(occurPeriod.getEnd()).toLocalDate();
        } else {
          occurStart = asOffsetDateTime(occurPeriod.getStart());
          occurEnd = asOffsetDateTime(occurPeriod.getEnd());
        }
        occurrences.add(new CalendarEventOccurrence(event, occurStart, occurEnd));
      });
    });
    occurrences.sort(Comparator.comparing(o -> Period.asOffsetDateTime(o.getStartDate())));
    return occurrences;
  }

  private RRule generateRecurrenceRule(final CalendarEvent event) {
    Recurrence recurrence = event.getRecurrence();
    String recurrenceType = getRecurrentType(recurrence.getFrequency().getUnit());
    Recur recur;
    final Optional<OffsetDateTime> endDate = recurrence.getEndDate();
    if (endDate.isPresent()) {
      if (event.isOnAllDay()) {
        recur = new Recur(recurrenceType, iCal4JDateCodec.encode(endDate.get().toLocalDate()));
      } else {
        recur = new Recur(recurrenceType, iCal4JDateCodec.encode(endDate.get().plusDays(1)));
      }
    } else if (recurrence.getRecurrenceCount() != Recurrence.NO_RECURRENCE_COUNT) {
      recur = new Recur(recurrenceType, recurrence.getRecurrenceCount());
    } else {
      recur = new Recur(recurrenceType, null);
    }
    recur.setInterval(recurrence.getFrequency().getInterval());

    recurrence.getDaysOfWeek().
        forEach(dayOfWeekOccurrence -> {
          WeekDay weekDay = iCal4JRecurrenceCodec.encode(dayOfWeekOccurrence.dayOfWeek());
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
    Date dtStart = iCal4JDateCodec.encode(event, event.getStartDate());
    Date dtEnd = iCal4JDateCodec.encode(event, event.getEndDate());
    VEvent vEvent = new VEvent(dtStart, dtEnd, event.getTitle());
    vEvent.getProperties().add(new Uid(event.getId()));
    if (event.isRecurrent()) {
      vEvent.getProperties().add(generateRecurrenceRule(event));
      if (!event.getRecurrence().getExceptionDates().isEmpty()) {
        vEvent.getProperties().add(new ExDate(iCal4JRecurrenceCodec.convertExceptionDates(event)));
      }
    }
    return vEvent;
  }

  private net.fortuna.ical4j.model.Period fromPeriod(final Period period) {
    final OffsetDateTime start = Period.asOffsetDateTime(period.getStartDate());
    final OffsetDateTime end = Period.asOffsetDateTime(period.getEndDate());
    return new net.fortuna.ical4j.model.Period(
        new DateTime(start.toInstant().toEpochMilli()),
        new DateTime(end.toInstant().toEpochMilli()));
  }

  private OffsetDateTime asOffsetDateTime(DateTime dateTime) {
    return dateTime.toInstant().atOffset(ZoneOffset.UTC);
  }

  private CalendarEventOccurrence occurrenceFor(final CalendarEvent event, final Temporal startDate,
      final Temporal endDate) {
    CalendarEventOccurrence occurrence = new CalendarEventOccurrence(event, startDate, endDate);
    return occurrence;
  }
}
