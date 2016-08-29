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

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.silverpeas.core.date.TimeUnit;

import javax.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * It defines the rules of the recurrence of a {@link Plannable} in its planning in a calendar.
 * A {@link Plannable} recurrence is defined by a recurrence period, id est a frequency (hourly,
 * daily, weekly, monthly, or yearly), and optionally by some of the following properties:
 *
 * <ul>
 *   <li>some days of week on which the {@link Plannable} should regularly occur</li>
 *   <li>some exceptions in the recurrence period of the {@link Plannable}</li>
 *   <li>a termination condition.</li>
 * </ul>
 */
@Entity
@Table(name = "sb_cal_recurrence")
public class Recurrence implements Cloneable {

  /**
   * A constant that defines a specific value for an empty recurrence.
   */
  public static final Recurrence NO_RECURRENCE = null;
  /**
   * A constant that defines a specific value for no recurrence count limit.
   */
  public static final int NO_RECURRENCE_COUNT = 0;
  /**
   * A constant that defines a specific value for no recurrence end date.
   */
  public static final OffsetDateTime NO_RECURRENCE_END_DATE = null;

  /**
   * Identifier of the recurrent object planned in a calendar. This identifier is mapped to the
   * recurrent object's identifier in a one to one relationship between the recurrent object and
   * its recurrence.
   */
  @Id
  private String id;

  @Embedded
  private RecurrencePeriod frequency;
  @Column(name = "recur_count")
  private int count = NO_RECURRENCE_COUNT;
  @Column(name = "recur_endDate")
  private OffsetDateTime endDateTime = NO_RECURRENCE_END_DATE;
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "sb_cal_recurrence_dayofweek", joinColumns = {
      @JoinColumn(name = "recurrenceId")})
  private Set<DayOfWeekOccurrence> daysOfWeek = new HashSet<>();
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "sb_cal_recurrence_exception", joinColumns = {
      @JoinColumn(name = "recurrenceId")})
  @Column(name = "recur_exceptionDate")
  private Set<OffsetDateTime> exceptionDates = new HashSet<>();

  /**
   * Creates a new recurrence from the specified frequency.
   * @param frequencyUnit the unit of the frequency: DAY means DAILY, WEEK means weekly, MONTH
   * means monthly or YEAR means YEARLY.
   * @return the event recurrence instance.
   */
  public static Recurrence every(TimeUnit frequencyUnit) {
    return new Recurrence(RecurrencePeriod.every(1, frequencyUnit));
  }

  /**
   * Creates a new recurrence from the specified frequency. For example every(2, MONTH) means
   * every 2 month.
   * @param frequencyValue a positive number indicating how many times the {@link Plannable} occurs.
   * @param frequencyUnit the frequency unit: DAY, WEEK, MONTH, or YEAR.
   * @return the event recurrence instance.
   */
  public static Recurrence every(int frequencyValue, TimeUnit frequencyUnit) {
    return new Recurrence(RecurrencePeriod.every(frequencyValue, frequencyUnit));
  }

  /**
   * Creates a new recurrence by specifying the recurrence period at which a {@link Plannable}
   * should recur.
   * @param period the recurrence period of the event.
   * @return the event recurrence instance.
   */
  public static Recurrence from(final RecurrencePeriod period) {
    return new Recurrence(period);
  }

  /**
   * Excludes from this recurrence rule the occurrences of the {@link Plannable} starting at the
   * specified date and times. The time of each specified date time is set in UTC/Greenwich.
   * If the occurrences are spanning all the day, then please use
   * the <code>excludeEventOccurrencesStartingAt</code> method accepting {@link LocalDate}
   * arguments
   * instead of {@link OffsetDateTime} ones. Otherwise set the time at midnight. Nevertheless, in
   * such a case, the time hasn't to be taken into account in the computation of the actual
   * occurrences.
   * @param dateTimes a list of date times at which start the occurrences to exclude.
   * @return itself.
   */
  public Recurrence excludeEventOccurrencesStartingAt(
      final OffsetDateTime... dateTimes) {
    this.exceptionDates.addAll(Arrays.stream(dateTimes)
        .map(dateTime -> dateTime.withOffsetSameInstant(ZoneOffset.UTC))
        .collect(Collectors.toList()));
    return this;
  }

  /**
   * Excludes from this recurrence rule the occurrences of a {@link Plannable} starting at the
   * specified dates.
   * @param days a list of dates at which start the occurrences to exclude.
   * @return itself.
   */
  public Recurrence excludeEventOccurrencesStartingAt(final LocalDate... days) {
    this.exceptionDates.addAll(Arrays.stream(days)
        .map(date -> date.atStartOfDay().atOffset(ZoneOffset.UTC))
        .collect(Collectors.toList()));
    return this;
  }

  /**
   * Sets some specific days of week at which a {@link Plannable} should periodically occur.
   * For a weekly recurrence, the specified days of week are the first one in the week. For other
   * frequency, the specified days of week will be all the occurrences of those days of week in the
   * recurrence period. For example, recur every weeks on monday and on tuesday or recur every month
   * on all saturdays and on all tuesdays.
   * This method can only be applied on recurrence period higher than the day, otherwise an
   * {@link IllegalStateException} will be thrown.
   * @param days the days of week at which a {@link Plannable} should occur. Theses days replace the
   * ones already set in the recurrence.
   * @return itself.
   */
  public Recurrence on(DayOfWeek... days) {
    checkRecurrenceStateForSpecificDaySetting();
    List<DayOfWeekOccurrence> dayOccurrences = new ArrayList<>();
    int nth = getFrequency().isWeekly() ? 1 : DayOfWeekOccurrence.ALL_OCCURRENCES;
    for (DayOfWeek dayOfWeek : days) {
      dayOccurrences.add(DayOfWeekOccurrence.nth(nth, dayOfWeek));
    }
    this.daysOfWeek.clear();
    this.daysOfWeek.addAll(dayOccurrences);
    return this;
  }

  /**
   * Sets some specific occurrences of day of week at which a {@link Plannable} should periodically
   * occur within a monthly or a yearly period. For example, recur every month on the third monday
   * and on the first tuesday. The days of week for a weekly recurrence can also be indicated if,
   * and only if, the nth occurrence of the day is the first one or all occurrences (as there is
   * actually only one possible occurrence of a day in a week); any value other than 1 or
   * {@©ode ALL_OCCURRENCES} is considered as an error and an IllegalArgumentException is thrown.
   * This method can only be applied on recurrence period higher than the day, otherwise an
   * {@link IllegalStateException} will be thrown.
   * @param days the occurrences of day of week at which an event should occur. Theses days replace
   * the ones already set in the recurrence.
   * @return itself.
   */
  public Recurrence on(DayOfWeekOccurrence... days) {
    return on(Arrays.asList(days));
  }

  /**
   * Sets some specific occurrences of day of week at which a {@link Plannable} should periodically
   * occur within monthly or yearly period. For example, recur every month on the third monday and
   * on the first tuesday. The days of week for a weekly recurrence can also be indicated if, and
   * only if, the nth occurrence of the day is the first one or all occurrences (as there is
   * actually only one possible occurrence of a day in a week); any value other than 1 or
   * {@©code ALL_OCCURRENCES} is considered as an error and an IllegalArgumentException is thrown.
   * This method can only be applied on recurrence period higher than the day, otherwise an
   * {@link IllegalStateException} will be thrown.
   * @param days a list of days of week at which a {@link Plannable} should occur. Theses days
   * replace
   * the ones already set in the recurrence.
   * @return itself.
   */
  public Recurrence on(final List<DayOfWeekOccurrence> days) {
    checkRecurrenceStateForSpecificDaySetting();
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
   * Sets a termination to this recurrence by specifying the number of time a {@link Plannable}
   * should recur.
   * Settings this termination unset the recurrence end date.
   * @param recurrenceCount the number of time a {@link Plannable} should occur.
   * @return itself.
   */
  public Recurrence upTo(int recurrenceCount) {
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
   * The time of the specified date time is set in UTC/Greenwich.
   * Settings this termination unset the number of time a {@link Plannable} should occur.
   * @param endDateTime the inclusive end date and time of the recurrence.
   * @return itself.
   */
  public Recurrence upTo(final OffsetDateTime endDateTime) {
    this.count = NO_RECURRENCE_COUNT;
    this.endDateTime = endDateTime.withOffsetSameInstant(ZoneOffset.UTC);
    return this;
  }

  /**
   * Sets a termination to this recurrence by specifying an end date of the recurrence.
   * Settings this termination unset the number of time a {@link Plannable} should occur.
   * @param endDate the inclusive end date of the recurrence.
   * @return itself.
   */
  public Recurrence upTo(final LocalDate endDate) {
    this.count = NO_RECURRENCE_COUNT;
    this.endDateTime = endDate.atTime(23, 59).atOffset(ZoneOffset.UTC);
    return this;
  }

  /**
   * Is this recurrence endless?
   * @return true if this recurrence has no upper bound defined. False otherwise.
   */
  public boolean isEndless() {
    return !getEndDate().isPresent() && getRecurrenceCount() == NO_RECURRENCE_COUNT;
  }

  /**
   * Gets the frequency at which the {@link Plannable} should recur.
   * @return the frequency as a RecurrencePeriod instance.
   */
  public RecurrencePeriod getFrequency() {
    return frequency;
  }

  /**
   * Gets the number of time the {@link Plannable} should occur. If NO_RECURRENCE_COUNT is
   * returned,
   * then no termination by recurrence count is defined.
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
   * {@link Optional}. The returned date time is from UTC/Greenwich.
   */
  public Optional<OffsetDateTime> getEndDate() {
    if (this.endDateTime != NO_RECURRENCE_END_DATE) {
      return Optional.of(this.endDateTime);
    }
    return Optional.empty();
  }

  /**
   * Gets the days of week on which the {@link Plannable} should recur each time.
   * @return an unmodifiable set of days of week or an empty set if no days of week are set to this
   * recurrence.
   */
  public Set<DayOfWeekOccurrence> getDaysOfWeek() {
    return Collections.unmodifiableSet(daysOfWeek);
  }

  /**
   * Gets the date time exceptions to this recurrence rule.
   *
   * The returned date time are the start date time of the occurrences that are excluded
   * from this recurrence rule. They are the exception in the application of the recurrence rule.
   * @return an unmodifiable set of {@link OffsetDateTime} or an empty set of no exceptions are set
   * to this recurrence.
   */
  public Set<OffsetDateTime> getExceptionDates() {
    return Collections.unmodifiableSet(exceptionDates);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Recurrence)) {
      return false;
    }

    final Recurrence that = (Recurrence) o;
    if (count != that.count) {
      return false;
    }
    if (!frequency.equals(that.frequency)) {
      return false;
    }
    if (endDateTime != null ? !endDateTime.equals(that.endDateTime) : that.endDateTime != null) {
      return false;
    }
    if (!daysOfWeek.equals(that.daysOfWeek)) {
      return false;
    }
    return exceptionDates.equals(that.exceptionDates);

  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(count)
        .append(endDateTime)
        .append(daysOfWeek)
        .append(exceptionDates)
        .toHashCode();
  }

  protected Recurrence() {
  }

  @PrePersist
  protected void generateId() {
    this.id = UUID.randomUUID().toString();
  }

  /**
   * Constructs a new recurrence instance from the specified recurrence period.
   * @param frequency the frequency of the recurrence.
   */
  private Recurrence(final RecurrencePeriod frequency) {
    this.frequency = frequency;
  }

  private void checkRecurrenceStateForSpecificDaySetting() {
    if (getFrequency().isDaily()) {
      throw new IllegalStateException("Some specific days cannot be set for a daily recurrence");
    }
  }

  @Override
  public Recurrence clone() {
    Recurrence clone = null;
    try {
      clone = (Recurrence) super.clone();
      clone.id = null;
      clone.daysOfWeek = new HashSet<>(daysOfWeek);
      clone.exceptionDates = new HashSet<>(exceptionDates);
    } catch (CloneNotSupportedException ignore) {
    }
    return clone;
  }
}
