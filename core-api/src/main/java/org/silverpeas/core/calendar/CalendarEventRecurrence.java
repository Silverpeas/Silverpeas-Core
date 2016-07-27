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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * It defines the recurrence rules of a calendar event. An event recurrence is defined by a
 * frequency (secondly, minutely, hourly, daily, weekly, monthly, or yearly) and optionally by some
 * days of week on which the event should frequently occur, and by a termination condition.
 */
public class CalendarEventRecurrence {

  /**
   * A constant that defines a specific value for an empty recurrence.
   */
  public static final CalendarEventRecurrence NO_RECURRENCE = null;
  /**
   * A constant that defines a specific value for no recurrence count limit.
   */
  public static final int NO_RECURRENCE_COUNT = 0;
  /**
   * A constant that defines a specific value for no recurrence end date.
   */
  public static final OffsetDateTime NO_RECURRENCE_END_DATE = null;
  private RecurrencePeriod frequency;
  private int count = NO_RECURRENCE_COUNT;
  private OffsetDateTime endDateTime = NO_RECURRENCE_END_DATE;
  private List<DayOfWeekOccurrence> daysOfWeek = new ArrayList<>();
  private List<OffsetDateTime> exceptionDates = new ArrayList<>();

  /**
   * Creates a new event recurrence from the specified frequency.
   * @param frequencyUnit the frequency in which the event should recur: SECOND means SECONDLY,
   * MINUTE means minutely, HOUR means hourly, WEEK means weekly, DAY means daily, WEEK means
   * weekly,
   * MONTH means monthly or YEAR means YEARLY.
   * @return the event recurrence instance.
   */
  public static CalendarEventRecurrence every(TimeUnit frequencyUnit) {
    return new CalendarEventRecurrence(RecurrencePeriod.every(1, frequencyUnit));
  }

  /**
   * Creates a new event recurrence from the specified frequency. For example every(2, MONTH) means
   * every 2 month.
   * @param frequencyValue the value of the event frequency. every two weeks.
   * @param frequencyUnit the frequency unit.
   * @return the event recurrence instance.
   */
  public static CalendarEventRecurrence every(int frequencyValue, TimeUnit frequencyUnit) {
    return new CalendarEventRecurrence(RecurrencePeriod.every(frequencyValue, frequencyUnit));
  }

  /**
   * Creates a new event recurrence by specifying the recurrence period at which the event should
   * recur.
   * @param period the recurrence period of the event.
   * @return the event recurrence instance.
   */
  public static CalendarEventRecurrence from(final RecurrencePeriod period) {
    return new CalendarEventRecurrence(period);
  }

  /**
   * Excludes from this recurrence rule the occurrences of the event starting at the specified
   * date and times. If the occurrences of the event are spanning all the day, then the time should
   * to be set at midnight. Nevertheless, in such a case, the time hasn't to be taken into account
   * in the computation of the actual occurrences of the event.
   * @param dateTimes a list of date times at which start the occurrences to exclude.
   * @return itself.
   */
  public CalendarEventRecurrence excludeEventOccurrencesStartingAt(
      final OffsetDateTime... dateTimes) {
    this.exceptionDates.addAll(Arrays.asList(dateTimes));
    return this;
  }

  /**
   * Sets some specific days of week at which the event should periodically occur. For example,
   * recur every weeks on monday and on tuesday. For a monthly or an yearly recurrence, the days of
   * week are the first days of the month or of the year; for example, the first monday and tuesday
   * of each month.
   * @param days the days of week at which an event should occur. Theses days replace the ones
   * already set in the recurrence.
   * @return itself.
   */
  public CalendarEventRecurrence on(DayOfWeek... days) {
    List<DayOfWeekOccurrence> dayOccurrences = new ArrayList<DayOfWeekOccurrence>();
    for (DayOfWeek dayOfWeek : days) {
      dayOccurrences.add(
          DayOfWeekOccurrence.nthOccurrence(DayOfWeekOccurrence.ALL_OCCURRENCES, dayOfWeek));
    }
    this.daysOfWeek.clear();
    this.daysOfWeek.addAll(dayOccurrences);
    return this;
  }

  /**
   * Sets some specific occurrences of day of week at which the event should periodically occur
   * within a monthly or a yearly period. For example, recur every month on the third monday and on
   * the first tuesday. The days of week for a weekly recurrence can also be indicated if, and only
   * if, the nth occurrence of the day is the first one or all occurrences (as there is actually
   * only
   * one possible occurrence of a day in a week); any value other than 1 or ALL_OCCURRENCES is
   * considered as an error and an IllegalArgumentException is thrown.
   * @param days the occurrences of day of week at which an event should occur. Theses days replace
   * the ones already set in the recurrence.
   * @return itself.
   */
  public CalendarEventRecurrence on(DayOfWeekOccurrence... days) {
    return on(Arrays.asList(days));
  }

  /**
   * Sets some specific occurrences of day of week at which the event should periodically occur
   * within monthly or yearly period. For example, recur every month on the third monday and on the
   * first tuesday. The days of week for a weekly recurrence can also be indicated if, and only if,
   * the nth occurrence of the day is the first one or all occurrences (as there is actually only
   * one possible occurrence of a day in a week); any value other than 1 or ALL_OCCURRENCES is
   * considered as an error and an IllegalArgumentException is thrown.
   * @param days a list of days of week at which an event should occur. Theses days replace the ones
   * already set in the recurrence.
   * @return itself.
   */
  public CalendarEventRecurrence on(final List<DayOfWeekOccurrence> days) {
    if (frequency.getUnit() == TimeUnit.WEEK) {
      for (DayOfWeekOccurrence dayOfWeekOccurrence : days) {
        if (dayOfWeekOccurrence.nth() != 1 && dayOfWeekOccurrence.nth() != DayOfWeekOccurrence.ALL_OCCURRENCES) {
          throw new IllegalArgumentException("The occurrence of the day of week " +
              dayOfWeekOccurrence.dayOfWeek().name() + " cannot be possible with a weekly "
              + "recurrence");
        }
      }
    }
    this.daysOfWeek.clear();
    this.daysOfWeek.addAll(days);
    return this;
  }

  /**
   * Sets a termination to this recurrence by specifying the number of time the event should recur.
   * Settings this termination overrides the recurrence end date.
   * @param recurrenceCount the number of time the event should occur.
   * @return itself.
   */
  public CalendarEventRecurrence upTo(int recurrenceCount) {
    if (recurrenceCount <= 0) {
      throw new IllegalArgumentException("The number of time the event has to recur should be a"
          + " positive value");
    }
    this.endDateTime = NO_RECURRENCE_END_DATE;
    this.count = recurrenceCount;
    return this;
  }

  /**
   * Sets a termination to this recurrence by specifying an end date and time of the recurrence.
   * Settings this termination overrides the number of time the event should occur.
   * @param endDateTime the end date and time of the recurrence.
   * @return itself.
   */
  public CalendarEventRecurrence upTo(final OffsetDateTime endDateTime) {
    this.count = NO_RECURRENCE_COUNT;
    this.endDateTime = endDateTime;
    return this;
  }

  /**
   * Sets a termination to this recurrence by specifying an end date of the recurrence.
   * Settings this termination overrides the number of time the event should occur.
   * @param endDate the end date and time of the recurrence.
   * @return itself.
   */
  public CalendarEventRecurrence upTo(final LocalDate endDate) {
    this.count = NO_RECURRENCE_COUNT;
    this.endDateTime = endDate.atStartOfDay().atOffset(ZoneOffset.UTC);
    return this;
  }

  /**
   * Gets the frequency at which the event should recur.
   * @return the frequency as a RecurrencePeriod instance.
   */
  public RecurrencePeriod getFrequency() {
    return frequency;
  }

  /**
   * Gets the number of time the event should occur. If NO_RECURRENCE_COUNT is returned, then no
   * termination by recurrence count is defined.
   * @return the recurrence count or NO_RECURRENCE_COUNT if no such termination is defined.
   */
  public int getRecurrenceCount() {
    return count;
  }

  /**
   * Gets the end date of the recurrence. The end date of the recurrence can be unspecified, in that
   * case the returned end date is empty.
   * @return an optional recurrence end date. The optional is empty if the end date of the
   * recurrence is unspecified, otherwise the recurrence termination date time can be get from the
   * {@link Optional}.
   */
  public Optional<OffsetDateTime> getEndDate() {
    if (this.endDateTime != NO_RECURRENCE_END_DATE) {
      return Optional.of(this.endDateTime);
    }
    return Optional.empty();
  }

  /**
   * Gets the days of week on which the event should recur each time.
   * @return an unmodifiable list of days of week or an empty list if no days of week is set to this
   * recurrence.
   */
  public List<DayOfWeekOccurrence> getDaysOfWeek() {
    return Collections.unmodifiableList(daysOfWeek);
  }

  /**
   * Gets the date time exceptions to this recurrence rule.
   *
   * The returned date time are the start date time of the occurrences that are excluded
   * from this recurrence rule. They are the exception in the application of the recurrence rule.
   * @return an unmodifiable list of {@link OffsetDateTime}.
   */
  public List<OffsetDateTime> getExceptionDates() {
    return Collections.unmodifiableList(exceptionDates);
  }

  /**
   * Constructs a new event recurrence instance with the specified new
   * @param frequency the frequency of the recurrence.
   */
  private CalendarEventRecurrence(final RecurrencePeriod frequency) {
    this.frequency = frequency;
  }
}
