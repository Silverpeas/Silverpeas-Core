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

import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.WeekDay;
import net.fortuna.ical4j.model.WeekDayList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.RRule;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.annotation.Technical;
import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.calendar.DayOfWeekOccurrence;
import org.silverpeas.core.calendar.Recurrence;
import org.silverpeas.core.calendar.RecurrencePeriod;
import org.silverpeas.core.date.TemporalConverter;
import org.silverpeas.core.date.TimeUnit;
import org.silverpeas.core.date.TimeZoneUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.Temporal;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.silverpeas.core.calendar.DayOfWeekOccurrence.ALL_OCCURRENCES;
import static org.silverpeas.core.calendar.Recurrence.NO_RECURRENCE;
import static org.silverpeas.core.calendar.Recurrence.NO_RECURRENCE_COUNT;

/**
 * A codec to encode/decode iCal4J recurrence with Silverpeas event recurrence.
 */
@Technical
@Bean
@Singleton
public class ICal4JRecurrenceCodec {

  private final ICal4JDateCodec iCal4JDateCodec;

  @Inject
  public ICal4JRecurrenceCodec(final ICal4JDateCodec iCal4JDateCodec) {
    this.iCal4JDateCodec = iCal4JDateCodec;
  }

  /**
   * Converts the exception dates from a calendar event with a recurrence rule.
   * <p>
   * The presence of an exception date must be verified before calling this method.
   * @param event a recurrent calendar event.
   * @return the converted exception dates.
   */
  public DateList convertExceptionDates(final CalendarEvent event) {
    Recurrence recurrence = event.getRecurrence();
    if (recurrence == NO_RECURRENCE) {
      throw new IllegalArgumentException("The event isn't recurrent!");
    }
    return recurrence.getExceptionDates()
        .stream()
        .map(date -> TemporalConverter.applyByType(date, iCal4JDateCodec.localDateConversion(),
            iCal4JDateCodec.offsetDateTimeConversion()))
        .sorted()
        .collect(Collectors.toCollection(() -> {
          Value type = event.isOnAllDay() ? Value.DATE : Value.DATE_TIME;
          final DateList list = new DateList(type);
          if (iCal4JDateCodec.isEventDateToBeEncodedIntoUtc(event.isRecurrent(),
              event.asCalendarComponent())) {
            list.setUtc(true);
          } else {
            list.setUtc(false);
            list.setTimeZone(iCal4JDateCodec.getTimeZone(event.getCalendar().getZoneId()));
          }
          return list;
        }));
  }

  /**
   * Encodes the recurrence of the specified Silverpeas calendar event into an iCal4J recurrence.
   * <p>
   * The presence of the recurrence rule must be verified before calling this method.
   * @param event a recurrent calendar event.
   * @return the encoded iCal4J recurrence.
   * @throws SilverpeasRuntimeException if the encoding fails.
   */
  public Recur encode(final CalendarEvent event) {
    Recurrence eventRecurrence = event.getRecurrence();
    if (eventRecurrence == NO_RECURRENCE) {
      throw new IllegalArgumentException("Event recurrence missing!");
    }
    Recur.Builder recurBuilder =
        new Recur.Builder().frequency(asICal4JFrequency(eventRecurrence.getFrequency()));
    if (eventRecurrence.getFrequency().getInterval() > 1) {
      recurBuilder.interval(eventRecurrence.getFrequency().getInterval());
    }

    final Optional<Temporal> recurrenceEndDate = eventRecurrence.getRecurrenceEndDate();
    if (eventRecurrence.getRecurrenceCount() != NO_RECURRENCE_COUNT) {
      recurBuilder.count(eventRecurrence.getRecurrenceCount());
    } else if (recurrenceEndDate.isPresent()) {
      final Temporal endDate = recurrenceEndDate.get();
      TemporalConverter.consumeByType(endDate,
          date -> recurBuilder.until(iCal4JDateCodec.encode(date)),
          dateTime -> recurBuilder.until(iCal4JDateCodec.encode(dateTime)));
    }
    WeekDayList daysOfWeek = eventRecurrence.getDaysOfWeek()
        .stream()
        .sorted(Comparator.comparing(DayOfWeekOccurrence::dayOfWeek))
        .map(this::encode)
        .collect(Collectors.toCollection(WeekDayList::new));
    recurBuilder.dayList(daysOfWeek);
    return recurBuilder.build();
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

  /**
   * Decodes the recurrence of the specified iCal4J event into a Silverpeas event recurrence.<br>
   * The presence of an exception date must be verified before calling this method.
   * @param vEvent the iCal4J event source which contains recurrence data.
   * @param defaultZoneId the default zone id.
   * @return the decoded Silverpeas event recurrence.
   * @throws SilverpeasRuntimeException if the encoding fails.
   */
  public Recurrence decode(final VEvent vEvent, final ZoneId defaultZoneId) {
    Recur recur = ((RRule) vEvent.getProperty(Property.RRULE)).getRecur();
    if (recur == null) {
      throw new IllegalArgumentException("VEVENT recurrence missing!");
    }
    RecurrencePeriod recurrencePeriod = decodeRecurrencePeriod(recur);
    Recurrence recurrence = Recurrence.from(recurrencePeriod);
    if (recur.getCount() > 0) {
      recurrence.until(recur.getCount());
    } else if (recur.getUntil() != null) {
      Temporal temporalUntil = iCal4JDateCodec.decode(recur.getUntil(), defaultZoneId);
      recurrence.until(temporalUntil);
    }
    if (recur.getDayList() != null && !recur.getDayList().isEmpty()) {
      recurrence.on(recur.getDayList().stream().map(this::decode).collect(Collectors.toList()));
    }
    processExclusionDates(vEvent, recurrence, defaultZoneId);
    return recurrence;
  }

  private void processExclusionDates(final VEvent vEvent, final Recurrence recurrence,
      final ZoneId defaultZoneId) {
    /* TODO activating following commented forEach after fix of EXDATE UTC management (iCal4J)
       cf. https://github.com/ical4j/ical4j/issues/113 for example

       <code>exDates.forEach(exDate -> recurrence
          .excludeEventOccurrencesStartingAt(iCal4JDateCodec.decode(exDate, defaultZoneId)));</code>

      Deleting following forEach after fix of EXDATE UTC management (iCal4J)
      cf. https://github.com/ical4j/ical4j/issues/113 for example
      */
    vEvent.getProperties(Property.EXDATE).forEach(e -> ((ExDate) e).getDates().forEach(exDate -> {
      final boolean isOnAllDay = !(vEvent.getStartDate().getDate() instanceof DateTime);
      final LocalDate dateToExclude;
      if (isOnAllDay) {
        dateToExclude = iCal4JDateCodec.decode(exDate);
      } else {
        final DateTime startDateTime = (DateTime) vEvent.getStartDate().getDate();
        if (startDateTime.isUtc()) {
          dateToExclude = iCal4JDateCodec.decode((DateTime) exDate, ZoneOffset.UTC).toLocalDate();
        } else {
          final ZoneId zoneId = startDateTime.getTimeZone() != null ?
              TimeZoneUtil.toZoneId(startDateTime.getTimeZone().getID()) :
              defaultZoneId;
          dateToExclude = iCal4JDateCodec.decode((DateTime) exDate, zoneId).toLocalDate();
        }
      }
      recurrence.excludeEventOccurrencesStartingAt(dateToExclude);
    }));
  }

  private Recur.Frequency asICal4JFrequency(final RecurrencePeriod period) {
    Recur.Frequency freq;
    switch (period.getUnit()) {
      case SECOND:
        freq = Recur.Frequency.SECONDLY;
        break;
      case MINUTE:
        freq = Recur.Frequency.MINUTELY;
        break;
      case HOUR:
        freq = Recur.Frequency.HOURLY;
        break;
      case DAY:
        freq = Recur.Frequency.DAILY;
        break;
      case WEEK:
        freq = Recur.Frequency.WEEKLY;
        break;
      case MONTH:
        freq = Recur.Frequency.MONTHLY;
        break;
      case YEAR:
        freq = Recur.Frequency.YEARLY;
        break;
      default:
        throw new SilverpeasRuntimeException(
            "Recurrence frequency not supported: " + period.getUnit());
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

  private RecurrencePeriod decodeRecurrencePeriod(final Recur recur) {
    final RecurrencePeriod recurrencePeriod;
    int interval = recur.getInterval() == -1 ? 1 : recur.getInterval();
    switch (recur.getFrequency()) {
      case SECONDLY:
        recurrencePeriod = RecurrencePeriod.every(interval, TimeUnit.SECOND);
        break;
      case MINUTELY:
        recurrencePeriod = RecurrencePeriod.every(interval, TimeUnit.MINUTE);
        break;
      case HOURLY:
        recurrencePeriod = RecurrencePeriod.every(interval, TimeUnit.HOUR);
        break;
      case DAILY:
        recurrencePeriod = RecurrencePeriod.every(interval, TimeUnit.DAY);
        break;
      case WEEKLY:
        recurrencePeriod = RecurrencePeriod.every(interval, TimeUnit.WEEK);
        break;
      case MONTHLY:
        recurrencePeriod = RecurrencePeriod.every(interval, TimeUnit.MONTH);
        break;
      case YEARLY:
        recurrencePeriod = RecurrencePeriod.every(interval, TimeUnit.YEAR);
        break;
      default:
        throw new IllegalArgumentException("not handled recurrence period");
    }
    return recurrencePeriod;
  }

  private DayOfWeekOccurrence decode(final WeekDay weekDay) {
    return DayOfWeekOccurrence.nth(weekDay.getOffset(), decode(weekDay.getDay()));
  }

  private DayOfWeek decode(final WeekDay.Day weekDay) {
    DayOfWeek dayOfWeek = null;
    switch (weekDay) {
      case MO:
        dayOfWeek = DayOfWeek.MONDAY;
        break;
      case TU:
        dayOfWeek = DayOfWeek.TUESDAY;
        break;
      case WE:
        dayOfWeek = DayOfWeek.WEDNESDAY;
        break;
      case TH:
        dayOfWeek = DayOfWeek.THURSDAY;
        break;
      case FR:
        dayOfWeek = DayOfWeek.FRIDAY;
        break;
      case SA:
        dayOfWeek = DayOfWeek.SATURDAY;
        break;
      case SU:
        dayOfWeek = DayOfWeek.SUNDAY;
        break;
    }
    return dayOfWeek;
  }
}
