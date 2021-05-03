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
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.WeekDay;
import net.fortuna.ical4j.model.WeekDayList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Uid;
import org.silverpeas.core.NotSupportedException;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.annotation.Technical;
import org.silverpeas.core.calendar.CalendarComponent;
import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.calendar.CalendarEventOccurrence;
import org.silverpeas.core.calendar.CalendarEventOccurrenceBuilder;
import org.silverpeas.core.calendar.CalendarEventOccurrenceGenerator;
import org.silverpeas.core.calendar.Recurrence;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.date.TemporalConverter;
import org.silverpeas.core.date.TimeUnit;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * An implementation of the {@link CalendarEventOccurrenceGenerator} by using the iCal4J library.
 * @author mmoquillon
 */
@Technical
@Bean
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
  public List<CalendarEventOccurrence> generateOccurrencesOf(final List<CalendarEvent> events,
      final Period inPeriod) {
    List<CalendarEventOccurrence> occurrences = new ArrayList<>();
    events.forEach(event -> {
      final VEvent vEvent = fromCalendarEvent(event);
      PeriodList periodList = getPeriodList(vEvent, inPeriod);
      periodList.forEach(occurPeriod -> {
        CalendarEventOccurrence occurrence = buildCalendarEventOccurrence(event, occurPeriod);
        occurrences.add(occurrence);
      });
    });
    occurrences.sort(
        Comparator.comparing(o -> TemporalConverter.asOffsetDateTime(o.getStartDate())));
    return occurrences;
  }

  @Override
  public long countOccurrencesOf(final CalendarEvent event, final Period inPeriod) {
    if (!event.isPlanned()) {
      return -1;
    } else if (!event.isRecurrent()) {
      return 1;
    } else if (event.getRecurrence().isEndless()) {
      return Long.MAX_VALUE;
    }
    RRule recurrenceRule = generateRecurrenceRule(event);
    Date firstOccurrenceStartDate =
        TemporalConverter.applyByType(event.getStartDate(), iCal4JDateCodec.localDateConversion(),
            iCal4JDateCodec.offsetDateTimeConversion());
    Date periodStartDate = TemporalConverter.applyByType(
        inPeriod == null ? event.getStartDate() : inPeriod.getStartDate(),
        iCal4JDateCodec.localDateConversion(), iCal4JDateCodec.offsetDateTimeConversion());
    Temporal endDate = event.getRecurrence()
        .getEndDate()
        .orElseThrow(
            () -> new NotSupportedException("Endless period of recurrent event not supported!"));
    Date periodEndDate = TemporalConverter.applyByType(
        inPeriod == null ? endDate.plus(1, ChronoUnit.DAYS) : inPeriod.getEndDate(),
        iCal4JDateCodec.localDateConversion(), iCal4JDateCodec.offsetDateTimeConversion());

    return recurrenceRule.getRecur()
        .getDates(firstOccurrenceStartDate, periodStartDate, periodEndDate,
            firstOccurrenceStartDate instanceof DateTime ? Value.DATE_TIME : Value.DATE)
        .size();
  }

  @Override
  public CalendarEventOccurrence generateNextOccurrenceOf(final CalendarEvent event,
      final ZonedDateTime since) {
    if (event.isRecurrent()) {
      return generateNextOccurrenceOfRecurrentEvent(event, since);
    }
    return generateNextOccurrenceOfSingleEvent(event, since);
  }

  private CalendarEventOccurrence generateNextOccurrenceOfSingleEvent(final CalendarEvent event,
      final ZonedDateTime since) {
    final ZoneId actualZoneId = event.getCalendar().getZoneId();
    boolean isEventAfter;
    if (event.isOnAllDay()) {
      isEventAfter = since.toLocalDate().isBefore((LocalDate) event.getStartDate());
    } else {
      isEventAfter = since.withZoneSameInstant(actualZoneId)
          .isBefore(((OffsetDateTime) event.getStartDate()).atZoneSameInstant(actualZoneId));
    }

    if (isEventAfter) {
      return CalendarEventOccurrenceBuilder.forEvent(event)
          .startingAt(event.getStartDate())
          .endingAt(event.getEndDate())
          .build();
    }
    return null;
  }

  private CalendarEventOccurrence generateNextOccurrenceOfRecurrentEvent(final CalendarEvent event,
      final ZonedDateTime since) {
    final ZoneId actualZoneId = event.getCalendar().getZoneId();
    final ZonedDateTime eventStartDate = event.isOnAllDay() ?
        ((LocalDate) event.getStartDate()).atStartOfDay(actualZoneId) :
        ((OffsetDateTime) event.getStartDate()).atZoneSameInstant(actualZoneId);
    final Optional<Temporal> optionalRecurEndDate = event.getRecurrence().getEndDate();
    LocalDate recurEndDate = null;
    if (optionalRecurEndDate.isPresent()) {
      recurEndDate = optionalRecurEndDate.get() instanceof LocalDate ?
          (LocalDate) optionalRecurEndDate.get() :
          ((OffsetDateTime) optionalRecurEndDate.get()).toLocalDate();
    }
    final VEvent vEvent = fromCalendarEvent(event);
    final ChronoUnit recurUnit = event.getRecurrence().getFrequency().getUnit().toChronoUnit();

    final ZonedDateTime sinceDateTime =
        since.withZoneSameInstant(actualZoneId).isBefore(eventStartDate) ?
            eventStartDate.minusMinutes(1) :
            since.withZoneSameInstant(actualZoneId);
    final Date iCalSinceDate = iCal4JDateCodec.encode(sinceDateTime);
    LocalDate searchPeriodStart = sinceDateTime.toLocalDate();
    int nbNextStartDateComputations = 0;
    do {
      // Taking care about date exceptions
      if (recurEndDate != null && recurEndDate.isBefore(searchPeriodStart)) {
        return null;
      }
      final PeriodList occurDateList = getPeriodList(vEvent,
          Period.between(searchPeriodStart, searchPeriodStart.plus(2, recurUnit)));
      for (final net.fortuna.ical4j.model.Period nextOccurDate : occurDateList) {
        if (nextOccurDate.getStart().after(iCalSinceDate)) {
          return buildCalendarEventOccurrence(event, nextOccurDate);
        }
      }
      searchPeriodStart = searchPeriodStart.plus(2, recurUnit);
      nbNextStartDateComputations++;
    } while (nbNextStartDateComputations < 100);
    throw new IllegalStateException("the next date seems to be hard to guess...");
  }

  private CalendarEventOccurrence buildCalendarEventOccurrence(final CalendarEvent event,
      final net.fortuna.ical4j.model.Period occurPeriod) {
    final Temporal occurStart;
    final Temporal occurEnd;
    if (event.isOnAllDay()) {
      occurStart = asOffsetDateTime(occurPeriod.getStart()).toLocalDate();
      occurEnd = asOffsetDateTime(occurPeriod.getEnd()).toLocalDate();
    } else {
      occurStart = asOffsetDateTime(occurPeriod.getStart());
      occurEnd = asOffsetDateTime(occurPeriod.getEnd());
    }
    return CalendarEventOccurrenceBuilder.forEvent(event)
        .startingAt(occurStart)
        .endingAt(occurEnd)
        .build();
  }

  private RRule generateRecurrenceRule(final CalendarEvent event) {
    Recurrence recurrence = event.getRecurrence();
    Recur.Frequency recurrenceType = getRecurrentType(recurrence.getFrequency().getUnit());
    Recur.Builder builder = new Recur.Builder().frequency(recurrenceType);
    final Optional<Temporal> endDate = recurrence.getRecurrenceEndDate();
    if (endDate.isPresent()) {
      builder.until(
          TemporalConverter.applyByType(endDate.get(), iCal4JDateCodec.localDateConversion(),
              iCal4JDateCodec.offsetDateTimeConversion()));
    } else if (recurrence.getRecurrenceCount() != Recurrence.NO_RECURRENCE_COUNT) {
      builder.count(recurrence.getRecurrenceCount());
    }
    builder.interval(recurrence.getFrequency().getInterval());

    WeekDayList dayList = recurrence.getDaysOfWeek().stream().
        map(dayOfWeekOccurrence -> {
          WeekDay weekDay = iCal4JRecurrenceCodec.encode(dayOfWeekOccurrence.dayOfWeek());
          if (recurrence.getFrequency().isWeekly() || recurrence.getFrequency().isDaily() ||
              dayOfWeekOccurrence.nth() == 0) {
            return weekDay;
          } else {
            return new WeekDay(weekDay, dayOfWeekOccurrence.nth());
          }
        }).
        collect(Collectors.toCollection(WeekDayList::new));
    builder.dayList(dayList);
    return new RRule(builder.build());
  }

  private Recur.Frequency getRecurrentType(final TimeUnit recurrenceUnit) {
    final Recur.Frequency recurrenceType;
    switch (recurrenceUnit) {
      case DAY:
        recurrenceType = Recur.Frequency.DAILY;
        break;
      case WEEK:
        recurrenceType = Recur.Frequency.WEEKLY;
        break;
      case MONTH:
        recurrenceType = Recur.Frequency.MONTHLY;
        break;
      case YEAR:
        recurrenceType = Recur.Frequency.YEARLY;
        break;
      default:
        throw new NotSupportedException("Recurrence unit not yet supported: " + recurrenceUnit);
    }
    return recurrenceType;
  }

  private VEvent fromCalendarEvent(CalendarEvent event) {
    final CalendarComponent component = event.asCalendarComponent();
    Date dtStart = iCal4JDateCodec.encode(event.isRecurrent(), component, event.getStartDate());
    Date dtEnd = iCal4JDateCodec.encode(event.isRecurrent(), component, event.getEndDate());
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

  private PeriodList getPeriodList(final VEvent vEvent, final Period inPeriod) {
    final net.fortuna.ical4j.model.Period icalPeriod = fromPeriod(inPeriod);
    PeriodList periodList = vEvent.calculateRecurrenceSet(icalPeriod);
    periodList.removeIf(period -> period.getEnd().equals(icalPeriod.getStart()));
    return periodList;
  }

  private net.fortuna.ical4j.model.Period fromPeriod(final Period period) {
    final OffsetDateTime start = TemporalConverter.asOffsetDateTime(period.getStartDate());
    final OffsetDateTime end = TemporalConverter.asOffsetDateTime(period.getEndDate());
    return new net.fortuna.ical4j.model.Period(
        iCal4JDateCodec.encode(start),
        iCal4JDateCodec.encode(end));
  }

  private OffsetDateTime asOffsetDateTime(DateTime dateTime) {
    return dateTime.toInstant().atOffset(ZoneOffset.UTC);
  }
}
