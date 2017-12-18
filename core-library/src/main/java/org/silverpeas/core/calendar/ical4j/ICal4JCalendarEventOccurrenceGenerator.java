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
package org.silverpeas.core.calendar.ical4j;

import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.WeekDay;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Uid;
import org.silverpeas.core.NotSupportedException;
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
import javax.inject.Singleton;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.silverpeas.core.calendar.ical4j.ICal4JDateCodec.UTC_ZONE_ID;

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
    occurrences.sort(Comparator.comparing(o -> Period.asOffsetDateTime(o.getStartDate())));
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
        TemporalConverter.applyByType(event.getStartDate(), iCal4JDateCodec::encode,
            iCal4JDateCodec::encode);
    Date periodStartDate = TemporalConverter.applyByType(
        inPeriod == null ? event.getStartDate() : inPeriod.getStartDate(), iCal4JDateCodec::encode,
        iCal4JDateCodec::encode);
    Date periodEndDate = TemporalConverter.applyByType(
        inPeriod == null ? event.getRecurrence().getEndDate().get().plus(1, ChronoUnit.DAYS) :
            inPeriod.getEndDate(),
        iCal4JDateCodec::encode, iCal4JDateCodec::encode);

    return recurrenceRule.getRecur()
        .getDates(firstOccurrenceStartDate, periodStartDate, periodEndDate,
            firstOccurrenceStartDate instanceof DateTime ? Value.DATE_TIME : Value.DATE).size();
  }

  @Override
  public CalendarEventOccurrence generateNextOccurrenceOf(final CalendarEvent event,
      final ZonedDateTime from) {
    if (event.isRecurrent()) {
      return generateNextOccurrenceOfRecurrentEvent(event, from);
    }
    return generateNextOccurrenceOfSingleEvent(event, from);
  }

  private CalendarEventOccurrence generateNextOccurrenceOfSingleEvent(final CalendarEvent event,
      final ZonedDateTime from) {
    final Temporal temporalStartDate = event.getStartDate();
    final boolean isOnDay = temporalStartDate instanceof LocalDate;
    final Date eventStartDate = isOnDay
        ? iCal4JDateCodec.encode(temporalStartDate)
        : iCal4JDateCodec.encode(((OffsetDateTime)temporalStartDate).atZoneSameInstant(from.getZone()));
    final Date fromDate = iCal4JDateCodec.encode(isOnDay ? from.toLocalDate() : from);
    if (fromDate.compareTo(eventStartDate) < 0) {
      return CalendarEventOccurrenceBuilder.forEvent(event)
          .startingAt(temporalStartDate)
          .endingAt(event.getEndDate())
          .build();
    }
    return null;
  }

  private CalendarEventOccurrence generateNextOccurrenceOfRecurrentEvent(final CalendarEvent event,
      final ZonedDateTime from) {
    final Temporal temporalStartDate = event.getStartDate();
    final boolean isOnDay = temporalStartDate instanceof LocalDate;
    final Date eventStartDate = isOnDay
        ? iCal4JDateCodec.encode(temporalStartDate)
        : iCal4JDateCodec.encode(((OffsetDateTime)temporalStartDate).atZoneSameInstant(from.getZone()));
    final VEvent vEvent = fromCalendarEvent(event);
    final Recur vEventRecur = ((RRule) vEvent.getProperty(RRule.RRULE)).getRecur();
    Temporal nextFrom = isOnDay ? from.toLocalDate() : from;
    int nbNextStartDateComputations = 0;
    do {
      final Date currentFromDate = iCal4JDateCodec.encode(nextFrom);
      final Date nextStartDate = vEventRecur.getNextDate(eventStartDate, currentFromDate);
      if (nextStartDate == null) {
        // No next start date = no more occurrence
        return null;
      }
      // Taking care about date exceptions
      final Temporal nextStartReference = isOnDay
          ? iCal4JDateCodec.decode(nextStartDate)
          : ((OffsetDateTime)iCal4JDateCodec.decode(nextStartDate, UTC_ZONE_ID)).toLocalDate();
      final ChronoUnit chronoUnit = event.getRecurrence().getFrequency().getUnit().toChronoUnit();
      final List<CalendarEventOccurrence> potentialOccurrences = generateOccurrencesOf(
          singletonList(event), Period.between(
                                       nextStartReference.minus(2,chronoUnit),
                                       nextStartReference.plus(2, chronoUnit)));
      final Optional<CalendarEventOccurrence> occurrence = potentialOccurrences.stream()
          .filter(o -> from.withZoneSameInstant(UTC_ZONE_ID).toOffsetDateTime().toString()
                           .compareTo(o.getStartDate().toString()) < 0)
          .findFirst();
      if (occurrence.isPresent()) {
        return occurrence.get();
      }
      nextFrom = isOnDay
          ? nextStartReference
          : ((OffsetDateTime)iCal4JDateCodec.decode(nextStartDate, UTC_ZONE_ID)).atZoneSameInstant(from.getZone());
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
    String recurrenceType = getRecurrentType(recurrence.getFrequency().getUnit());
    Recur recur;
    final Optional<Temporal> endDate = recurrence.getRecurrenceEndDate();
    if (endDate.isPresent()) {
      recur = new Recur(recurrenceType,
          TemporalConverter.applyByType(endDate.get(), iCal4JDateCodec::encode,
              iCal4JDateCodec::encode));
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

  private PeriodList getPeriodList(final VEvent vEvent,
      final Period inPeriod) {
    final net.fortuna.ical4j.model.Period icalPeriod = fromPeriod(inPeriod);
    PeriodList periodList = vEvent.calculateRecurrenceSet(icalPeriod);
    periodList.removeIf(period -> period.getEnd().equals(icalPeriod.getStart()));
    return periodList;
  }

  private net.fortuna.ical4j.model.Period fromPeriod(final Period period) {
    final OffsetDateTime start = Period.asOffsetDateTime(period.getStartDate());
    final OffsetDateTime end = Period.asOffsetDateTime(period.getEndDate());
    return new net.fortuna.ical4j.model.Period(
        iCal4JDateCodec.encode(start),
        iCal4JDateCodec.encode(end));
  }

  private OffsetDateTime asOffsetDateTime(DateTime dateTime) {
    return dateTime.toInstant().atOffset(ZoneOffset.UTC);
  }
}
