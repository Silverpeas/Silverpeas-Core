/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.silverpeas.core.date.TemporalConverter;
import org.silverpeas.core.date.TemporalConverter.Conversion;
import org.silverpeas.core.date.TimeUnit;

import javax.persistence.*;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.silverpeas.core.date.TemporalConverter.asOffsetDateTime;

/**
 * It defines the rules of the recurrence of a {@link PlannableOnCalendar} in its planning in a calendar.
 * A {@link PlannableOnCalendar} recurrence is defined by a recurrence period, id est a frequency (hourly,
 * daily, weekly, monthly, or yearly), and optionally by some of the following properties:
 *
 * <ul>
 *   <li>some days of week on which the {@link PlannableOnCalendar} should regularly occur</li>
 *   <li>some exceptions in the recurrence period of the {@link PlannableOnCalendar}</li>
 *   <li>a termination condition.</li>
 * </ul>
 */
@Entity
@Table(name = "sb_cal_recurrence")
public class Recurrence implements Serializable {

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
  @SuppressWarnings("WeakerAccess")
  public static final OffsetDateTime NO_RECURRENCE_END_DATE = null;

  /**
   * Identifier of the recurrent object planned in a calendar. This identifier is mapped to the
   * recurrent object's identifier in a one to one relationship between the recurrent object and
   * its recurrence.
   */
  @SuppressWarnings("unused")
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
  @Transient
  private transient Temporal startDate;

  /**
   * Constructs an empty recurrence for the persistence engine.
   */
  protected Recurrence() {
    // empty for JPA
  }

  /**
   * Constructs a new recurrence instance from the specified recurrence period.
   * @param frequency the frequency of the recurrence.
   */
  private Recurrence(final RecurrencePeriod frequency) {
    withFrequency(frequency);
  }

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
   * @param frequencyValue a positive number indicating how many times the {@link PlannableOnCalendar} occurs.
   * @param frequencyUnit the frequency unit: DAY, WEEK, MONTH, or YEAR.
   * @return the event recurrence instance.
   */
  public static Recurrence every(int frequencyValue, TimeUnit frequencyUnit) {
    return new Recurrence(RecurrencePeriod.every(frequencyValue, frequencyUnit));
  }

  /**
   * Creates a new recurrence by specifying the recurrence period at which a {@link PlannableOnCalendar}
   * should recur.
   * @param period the recurrence period of the event.
   * @return the event recurrence instance.
   */
  public static Recurrence from(final RecurrencePeriod period) {
    return new Recurrence(period);
  }

  /**
   * Excludes from this recurrence rule the occurrences originally starting at the specified date
   * or datetime. In the case the argument is one or more {@link OffsetDateTime}, their time is set
   * with the time of the start datetime of the calendar component concerned by this recurrence.
   *
   * If the calendar component from which the occurrences come is on all day, the specified
   * temporal instance is then converted into a {@link LocalDate} instance. Otherwise, if the
   * the temporal instance is a {@link LocalDate} it is then converted into a {@link OffsetDateTime}
   * with the time the one of the calendar component's start datetime; you have to ensure then the
   * {@link LocalDate} you pass comes from a value in UTC. If the temporal instance is already
   * an {@link OffsetDateTime}, then it is converted in UTC and its time set with the one
   * of the calendar component's start datetime.
   * @param temporal a list of either {@link LocalDate} or {@link OffsetDateTime} at which
   * originally start the occurrences to exclude.
   * @return itself.
   */
  public Recurrence excludeEventOccurrencesStartingAt(final Temporal... temporal) {
    this.exceptionDates.addAll(
        Arrays.stream(temporal).map(this::normalize).sorted().collect(Collectors.toList()));
    return this;
  }

  /**
   * Sets some specific days of week at which a {@link PlannableOnCalendar} should periodically occur.
   * For a weekly recurrence, the specified days of week are the first one in the week. For other
   * frequency, the specified days of week will be all the occurrences of those days of week in the
   * recurrence period. For example, recur every weeks on monday and on tuesday or recur every month
   * on all saturdays and on all tuesdays.
   * This method can only be applied on recurrence period higher than the day, otherwise an
   * {@link IllegalStateException} will be thrown.
   * @param days the days of week at which a {@link PlannableOnCalendar} should occur. Theses days replace the
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
   * Sets some specific occurrences of day of week at which a {@link PlannableOnCalendar} should periodically
   * occur within a monthly or a yearly period. For example, recur every month on the third monday
   * and on the first tuesday. The days of week for a weekly recurrence can also be indicated if,
   * and only if, the nth occurrence of the day is the first one or all occurrences (as there is
   * actually only one possible occurrence of a day in a week); any value other than 1 or
   * {@code ALL_OCCURRENCES} is considered as an error and an IllegalArgumentException is thrown.
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
   * Sets some specific occurrences of day of week at which a {@link PlannableOnCalendar} should periodically
   * occur within monthly or yearly period. For example, recur every month on the third monday and
   * on the first tuesday. The days of week for a weekly recurrence can also be indicated if, and
   * only if, the nth occurrence of the day is the first one or all occurrences (as there is
   * actually only one possible occurrence of a day in a week); any value other than 1 or
   * {@code ALL_OCCURRENCES} is considered as an error and an IllegalArgumentException is thrown.
   * This method can only be applied on recurrence period higher than the day, otherwise an
   * {@link IllegalStateException} will be thrown.
   * @param days a list of days of week at which a {@link PlannableOnCalendar} should occur. Theses days
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
   * Sets that the recurrence is not linked to a specific day. So the occurrence generation will
   * take into account only the start datetime of the event.
   * @return itself.
   */
  public Recurrence onNoSpecificDay() {
    this.daysOfWeek.clear();
    return this;
  }

  /**
   * Sets a termination to this recurrence by specifying the count of time a {@link PlannableOnCalendar}
   * should occur.
   * Settings this termination unset the recurrence end date/datetime.
   * @param recurrenceCount the number of time a {@link PlannableOnCalendar} should occur.
   * @return itself.
   */
  public Recurrence until(int recurrenceCount) {
    if (recurrenceCount <= 0) {
      throw new IllegalArgumentException("The number of time the event has to recur should be a"
          + " positive value");
    }
    this.endDateTime = NO_RECURRENCE_END_DATE;
    this.count = recurrenceCount;
    clearsUnnecessaryExceptionDates();
    return this;
  }

  /**
   * Sets a termination to this recurrence by specifying an inclusive date or datetime.
   *
   * If a datetime is passed, it is set in UTC/Greenwich and then the time is overridden by the one
   * of the start date time of the calendar component concerned by this recurrence. In the case
   * the calendar component is on all day(s), then the specified datetime is converted into a date.
   *
   * Settings this termination unset the number of time a {@link PlannableOnCalendar} should occur.
   * @param endDate the inclusive date or datetime at which the recurrence ends.
   * @return itself.
   */
  public Recurrence until(final Temporal endDate) {
    this.endDateTime = normalize(endDate);
    this.count = NO_RECURRENCE_COUNT;
    clearsUnnecessaryExceptionDates();
    return this;
  }

  /**
   * Sets that the recurrence never ends.
   * @return itself.
   */
  public Recurrence endless() {
    this.count = NO_RECURRENCE_COUNT;
    this.endDateTime = NO_RECURRENCE_END_DATE;
    return this;
  }

  /**
   * Sets a frequency to this recurrence by specifying a recurrence period.<br>
   * When the new frequency is a daily or a yearly one, days of weeks are reset.
   * @param frequency the frequency to set.
   * @return itself.
   */
  public Recurrence withFrequency(final RecurrencePeriod frequency) {
    this.frequency = frequency;
    if(getFrequency().isDaily() || getFrequency().isYearly()) {
      this.daysOfWeek.clear();
    }
    return this;
  }

  /**
   * Is this recurrence endless?
   * @return true if this recurrence has no upper bound defined. False otherwise.
   */
  @SuppressWarnings("WeakerAccess")
  public boolean isEndless() {
    return !getRecurrenceEndDate().isPresent() && getRecurrenceCount() == NO_RECURRENCE_COUNT;
  }

  /**
   * Gets the frequency at which the {@link PlannableOnCalendar} should recur.
   * @return the frequency as a RecurrencePeriod instance.
   */
  public RecurrencePeriod getFrequency() {
    return frequency;
  }

  /**
   * Gets the number of time the {@link PlannableOnCalendar} should occur. If NO_RECURRENCE_COUNT is
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
   * recurrence is unspecified, otherwise the recurrence termination date or datetime can be get
   * from the {@link Optional}. The returned datetime is from UTC/Greenwich.
   */
  public Optional<Temporal> getRecurrenceEndDate() {
    if (this.endDateTime != NO_RECURRENCE_END_DATE) {
      return Optional.of(
          getStartDate() instanceof LocalDate ? this.endDateTime.toLocalDate() : this.endDateTime);
    }
    return Optional.empty();
  }

  /**
   * Gets the end date of the period over which this recurrence is played by taking into account
   * either the number of time he recurrent {@link PlannableOnCalendar} occurs or the end date of its
   * recurrence. The computed date can match the date of the last occurrence of the recurrent
   * {@link PlannableOnCalendar} for a finite recurrence without an end date explicitly set. It can be also
   * a date after the last occurrence. The exception dates in the recurrence rule aren't taken
   * into account.
   *
   * If this recurrence isn't yet applied to any recurrence calendar component, then an
   * {@link IllegalStateException} exception is thrown.
   * @return an optional recurrence actual end date. The optional is empty if the recurrence is
   * endless.
   */
  public Optional<Temporal> getEndDate() {
    if (!isEndless()) {
      return Optional.of(getRecurrenceEndDate().orElse(computeEndDate()));
    }
    return Optional.empty();
  }

  /**
   * Gets the start date of the period over which this recurrence is played. It is the date of the
   * first occurrence of the recurrent {@link PlannableOnCalendar} on which this recurrence is applied.
   * <p>
   * If this recurrence isn't yet applied to any recurrence calendar component, then an
   * {@link IllegalStateException} exception is thrown.
   * @return the start date of this recurrence.
   */
  public Temporal getStartDate() {
    if (this.startDate == null) {
      throw new IllegalStateException(
          "The recurrence isn't applied to any recurrent calendar component!");
    }
    return this.startDate;
  }

  /**
   * Gets the days of week on which the {@link PlannableOnCalendar} should recur each time.
   * @return an unmodifiable set of days of week or an empty set if no days of week are set to this
   * recurrence.
   */
  public Set<DayOfWeekOccurrence> getDaysOfWeek() {
    return Collections.unmodifiableSet(daysOfWeek);
  }

  /**
   * Gets the datetime exceptions to this recurrence rule.
   *
   * The returned datetime are the start datetime of the occurrences that are excluded
   * from this recurrence rule. They are the exception in the application of the recurrence rule.
   * @return a set of either {@link LocalDate} or {@link OffsetDateTime} instances, or an empty set
   * if there is no exception dates to this recurrence.
   */
  public Set<Temporal> getExceptionDates() {
    return exceptionDates.stream().map(this::decode).collect(Collectors.toSet());
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
    if (this.count != that.count || !frequency.equals(that.frequency)) {
      return false;
    }
    if (this.endDateTime != null) {
      if (!this.endDateTime.equals(that.endDateTime)) {
        return false;
      }
    } else if (that.endDateTime != null) {
      return false;
    }
    return daysOfWeek.equals(that.daysOfWeek) && exceptionDates.equals(that.exceptionDates);
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(count)
        .append(endDateTime)
        .append(daysOfWeek)
        .append(exceptionDates)
        .toHashCode();
  }

  /**
   * Copies the specified recurrence into another object. The identifier of the recurrence is set to
   * null as it is not yet persisted.
   * @return a copy of this recurrence.
   */
  public Recurrence copy() {
    Recurrence copy = new Recurrence();
    copy.id = null;
    copy.startDate = this.startDate;
    copy.endDateTime = this.endDateTime;
    copy.count = this.count;
    copy.frequency = this.frequency;
    copy.daysOfWeek = new HashSet<>(this.daysOfWeek);
    copy.exceptionDates = new HashSet<>(this.exceptionDates);
    return copy;
  }

  /**
   * Is this recurrence identical in value than the specified one.
   * @param recurrence the recurrence with which this recurrence is compared to.
   * @return true if this recurrence is same as the given one, false otherwise.
   */
  boolean sameAs(final Recurrence recurrence) {
    if (this.equals(recurrence)) {
      return true;
    }
    if (recurrence == null) {
      return false;
    }
    if (recurrence.count != this.count || !recurrence.daysOfWeek.equals(this.daysOfWeek) ||
        !recurrence.startDate.equals(this.startDate) || !sameFrequencyAs(recurrence)) {
      return false;
    }
    return sameEndTimeAs(recurrence);
  }

  private boolean sameFrequencyAs(final Recurrence recurrence) {
    return recurrence.frequency.getUnit() == this.frequency.getUnit() &&
           recurrence.frequency.getInterval() == this.frequency.getInterval();
  }

  private boolean sameEndTimeAs(final Recurrence recurrence) {
    return (recurrence.endDateTime == NO_RECURRENCE_END_DATE && this.endDateTime == NO_RECURRENCE_END_DATE) ||
           (recurrence.endDateTime != NO_RECURRENCE_END_DATE && recurrence.endDateTime.equals(this.endDateTime));
  }

  /**
   * Sets the date or datetime at which this recurrence starts. The date or datetime should be the
   * one at which the concerned recurrent component calendar is planned.
   * <p>
   * This method is dedicated to the recurrent component calendar to set its own start date or
   * datetime. The start date or datetime is used to compute the correct value of both the
   * recurrence end date or datetime (in case it is set) and the exception date or datetime (when
   * they are set).
   * @param date a temporal instance of either {@link LocalDate} for a date or
   * {@link OffsetDateTime} for a datetime.
   * @return itself.
   */
  Recurrence startingAt(final Temporal date) {
    this.startDate = date;
    if (this.endDateTime != null) {
      this.until(this.endDateTime.toLocalDate());
    }
    if (!this.exceptionDates.isEmpty()) {
      Temporal[] exceptions = this.exceptionDates.toArray(new Temporal[0]);
      this.exceptionDates.clear();
      this.excludeEventOccurrencesStartingAt(exceptions);
    }
    return this;
  }

  /**
   * Clears all the registered exception dates.
   */
  void clearsAllExceptionDates() {
    exceptionDates.clear();
  }

  @PrePersist
  protected void generateId() {
    this.id = UUID.randomUUID().toString();
  }

  private void checkRecurrenceStateForSpecificDaySetting() {
    if (getFrequency().isDaily()) {
      throw new IllegalStateException("Some specific days cannot be set for a daily recurrence");
    }
  }

  /**
   * Clears all the registered exception dates which are after the end datetime of the recurrence.
   */
  private void clearsUnnecessaryExceptionDates() {
    if (!this.exceptionDates.isEmpty()) {
      getEndDate().ifPresent(e -> exceptionDates.removeIf(
          exceptionDate -> asOffsetDateTime(e).isBefore(exceptionDate)));
    }
  }

  private OffsetDateTime normalize(final Temporal temporal) {
    OffsetDateTime dateTime = asOffsetDateTime(temporal);
    if (this.startDate != null) {
      return TemporalConverter.applyByType(this.startDate,
          Conversion.of(LocalDate.class, t ->
              dateTime.with(LocalTime.MIDNIGHT.atOffset(ZoneOffset.UTC))),
          Conversion.of(OffsetDateTime.class, t -> dateTime.with(t.toOffsetTime())));
    }
    return dateTime;
  }

  private Temporal decode(final OffsetDateTime dateTime) {
    return getStartDate() instanceof LocalDate ? dateTime.toLocalDate() : dateTime;
  }

  private Temporal computeDateForMonthlyFrequencyFrom(final Temporal source,
      DayOfWeekOccurrence dayOfWeek) {
    Temporal current = source;
    if (dayOfWeek.nth() > 1) {
      current = current.with(ChronoField.ALIGNED_WEEK_OF_MONTH, dayOfWeek.nth());
    } else if (dayOfWeek.nth() < 0) {
      current = current.with(ChronoField.DAY_OF_MONTH, 1)
          .plus(1, ChronoUnit.MONTHS)
          .minus(1, ChronoUnit.DAYS)
          .plus(dayOfWeek.nth(), ChronoUnit.WEEKS)
          .with(dayOfWeek.dayOfWeek());
    }
    return current;
  }

  private Temporal computeDateForYearlyFrequencyFrom(final Temporal source,
      DayOfWeekOccurrence dayOfWeek) {
    final int lastDayOfYear = 31;
    Temporal current = source;
    if (dayOfWeek.nth() > 1) {
      current = current.with(ChronoField.ALIGNED_WEEK_OF_YEAR, dayOfWeek.nth());
    } else if (dayOfWeek.nth() < 0) {
      current = current.with(MonthDay.of(Month.DECEMBER, lastDayOfYear))
          .plus(dayOfWeek.nth(), ChronoUnit.WEEKS)
          .with(dayOfWeek.dayOfWeek());
    }
    return current;
  }

  private Temporal computeEndDate() {
    Temporal date = this.getStartDate();
    if (getRecurrenceCount() == 1) {
      return date;
    }
    final long interval = (long)getRecurrenceCount() *
        (getFrequency().getInterval() >= 1 ? getFrequency().getInterval() : 1);
    date = date.plus(interval, getFrequency().getUnit().toChronoUnit());
    boolean firstDayOfWeekSet = false;
    for (DayOfWeekOccurrence dayOfWeek : daysOfWeek) {
      Temporal current = date.with(dayOfWeek.dayOfWeek());
      if (getFrequency().isMonthly()) {
        current = computeDateForMonthlyFrequencyFrom(current, dayOfWeek);
      } else if (getFrequency().isYearly()) {
        current = computeDateForYearlyFrequencyFrom(current, dayOfWeek);
      }

      if (!firstDayOfWeekSet || LocalDate.from(current).isAfter(LocalDate.from(date))) {
        date = current;
        firstDayOfWeekSet = true;
      }
    }
    return date;
  }
}
