/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.date.period;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.core.date.DateTime;
import org.silverpeas.core.date.Temporal;
import org.silverpeas.core.date.TimeUnit;
import org.silverpeas.core.notification.message.MessageManager;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.time.Duration;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;
import java.util.Set;
import java.util.TimeZone;

/**
 * Class representing a period between two dates and offering tools around it.
 * <p>
 * User: Yohann Chastagnier
 * Date: 19/04/13
 * @deprecated Use {@link org.silverpeas.core.date.Period}
 */
@Deprecated
public class Period implements Comparable, Serializable, Cloneable {
  private static final long serialVersionUID = 6378275537498456869L;

  private final static Set<PeriodType> knownPeriodTypes;

  static {
    knownPeriodTypes = EnumSet.allOf(PeriodType.class);
    knownPeriodTypes.remove(PeriodType.unknown);
  }

  // Common immutable undefined period.
  public static final Period UNDEFINED = new UndefinedPeriod();

  private PeriodType periodType = PeriodType.unknown;
  private DateTime beginDatable;
  private DateTime endDatable;

  /**
   * Checks if the specified period and returns the specified one if defined,
   * or the common {@link Period#UNDEFINED} otherwise.
   * @param period the period
   * @return the specified period is defined, the common {@link Period#UNDEFINED} otherwise.
   */
  public static Period check(Period period) {
    return (period != null && period.isDefined()) ? period : UNDEFINED;
  }

  /**
   * Initialize a period from given dates (undefined dates are taken into account for null ones).
   * The treatment is trying to identify the type of the period ({@link PeriodType}).
   * @param beginDate the date of the beginning of the period.
   * @param endDate the date of the ending of the period.
   * @return the period between the two dates
   */
  public static Period getPeriodWithUndefinedIfNull(Date beginDate, Date endDate) {
    return from(beginDate != null ? new DateTime(beginDate) : DateUtil.MINIMUM_DATE,
        endDate != null ? new DateTime(endDate) : DateUtil.MAXIMUM_DATE);
  }

  /**
   * Initialize a period from given dates.
   * The treatment is trying to identify the type of the period ({@link PeriodType}).
   * @param beginDate the date of the beginning of the period.
   * @param endDate the date of the ending of the period.
   * @return the period between the two dates
   */
  public static Period from(Date beginDate, Date endDate) {
    return from(new DateTime(beginDate), new DateTime(endDate));
  }

  /**
   * Initialize a period from given dates.
   * The treatment is trying to identify the type of the period ({@link PeriodType}).
   * @param beginDate the date of the beginning of the period.
   * @param endDate the date of the ending of the period.
   * @param timeZone the time zone to set to this period.
   * @return the period between the two dates
   */
  public static Period from(Date beginDate, Date endDate, TimeZone timeZone) {
    return from(new DateTime(beginDate, timeZone), new DateTime(endDate, timeZone));
  }

  /**
   * Initialize a period from given dates.
   * The treatment is trying to identify the type of the period ({@link PeriodType}).
   * @param beginDatable the date of the beginning of the period.
   * @param endDatable the date of the ending of the period.
   * @return the period between the two dates
   */
  public static Period from(DateTime beginDatable, DateTime endDatable) {
    Period period = check(new Period(beginDatable, endDatable));
    for (PeriodType periodToIdentify : knownPeriodTypes) {
      Period guessedPeriod = from(beginDatable, periodToIdentify, MessageManager.getLanguage());
      if (guessedPeriod.equals(period)) {
        return guessedPeriod;
      }
    }
    return period;
  }

  /**
   * Initialize a period from a date and a type of period.
   * @param referenceDate the date at which starts the period
   * @param periodType the type of the period
   * @return the period
   */
  public static Period from(Date referenceDate, PeriodType periodType) {
    return from(referenceDate, periodType, MessageManager.getLanguage());
  }

  public static Period from(Date referenceDate, TimeZone timeZone, PeriodType periodType) {
    return from(referenceDate, timeZone, periodType, MessageManager.getLanguage());
  }

  public static Period from(DateTime referenceDatable, PeriodType periodType) {
    return from(referenceDatable.asDate(), periodType, MessageManager.getLanguage());
  }

  public static Period from(Date referenceDate, PeriodType periodType, String locale) {
    return from(new DateTime(referenceDate), periodType, locale);
  }

  public static Period from(Date referenceDate, TimeZone timeZone, PeriodType periodType,
      String locale) {
    return from(new DateTime(referenceDate, timeZone), periodType, locale);
  }

  public static Period from(DateTime referenceDatable, PeriodType periodType, String locale) {
    switch (periodType) {
      case year:
        return new YearPeriod(referenceDatable);
      case month:
        return new MonthPeriod(referenceDatable);
      case week:
        return new WeekPeriod(referenceDatable, locale);
      case day:
      default:
        return new DayPeriod(referenceDatable);
    }
  }

  /**
   * Constructor : Constructs a newly allocated <code>period</code>.
   * TimeZone is set from the begin date of the period.
   * @param beginDatable the date of the beginning of the period.
   * @param endDatable the date of the ending of the period.
   */
  protected Period(final DateTime beginDatable, final DateTime endDatable) {
    this(beginDatable, endDatable, beginDatable.getTimeZone());
  }

  /**
   * Constructor : Constructs a newly allocated <code>period</code>.
   * @param beginDatable the date of the beginning of the period.
   * @param endDatable the date of the ending of the period.
   * @param timeZone the time zone to set to this period.
   */
  protected Period(final DateTime beginDatable, final DateTime endDatable,
      final TimeZone timeZone) {
    this.beginDatable = beginDatable;
    this.endDatable = endDatable;
    inTimeZone(timeZone == null ? TimeZone.getDefault() : timeZone);
  }

  /**
   * Indicates if one of begin date or end date is defined.
   * @return true if period is defined, false otherwise.
   */
  public boolean isDefined() {
    return getBeginDatable().isDefined() || getEndDatable().isDefined();
  }

  /**
   * Indicates the opssite of {@link #isDefined()}.
   * @return true if period is not defined, false otherwise.
   */
  public boolean isNotDefined() {
    return !isDefined();
  }

  /**
   * The time zone for a period has no meaning.
   * @param timeZone the time zone to set to this period.
   * @return itself
   */
  public Period inTimeZone(final TimeZone timeZone) {
    beginDatable.inTimeZone(timeZone);
    endDatable.inTimeZone(timeZone);
    return this;
  }

  /**
   * The time zone has no meaning for a date.
   * @return the time zone in which this period is set.
   */
  public TimeZone getTimeZone() {
    return beginDatable.getTimeZone();
  }

  /**
   * Gets the type of the period.
   * @return the period type
   */
  public PeriodType getPeriodType() {
    return periodType;
  }

  /**
   * Sets the type of the period.
   * @param periodType the period type
   */
  protected void setPeriodType(final PeriodType periodType) {
    this.periodType = periodType;
  }

  /**
   * Gets the begin date of the period.
   * It represents the begin date of the event if no recurrence exists,
   * or the begin date of the first occurence of the event if a recurrence exists.
   * @return the beginning date
   */
  public Date getBeginDate() {
    return beginDatable.asDate();
  }

  /**
   * Gets the end date of the period.
   * @return the ending date
   */
  public Date getEndDate() {
    return endDatable.asDate();
  }

  /**
   * Gets the begin date of the period represented as a {@link Temporal}.
   * (see {@link #getBeginDate()} for more details).
   * @return the beginning date
   */
  public DateTime getBeginDatable() {
    return beginDatable;
  }

  /**
   * This method is a shortcut of {@link #isDefined()} call on {@link #getBeginDatable()}.
   * @return true if {@link #getBeginDatable()} returns defined date, false otherwise.
   */
  public boolean isBeginDefined() {
    return beginDatable.isDefined();
  }

  /**
   * This method is a shortcut of {@link #isNotDefined()} call on {@link #getBeginDatable()}.
   * @return true if {@link #getBeginDatable()} returns a not defined date, false otherwise.
   */
  public boolean isBeginNotDefined() {
    return beginDatable.isNotDefined();
  }

  /**
   * Gets the end date of the period represented as a {@link Temporal}.
   * (see {@link #getEndDate()} for more details).
   * @return the ending date
   */
  public DateTime getEndDatable() {
    return endDatable;
  }

  /**
   * This method is a shortcut of {@link #isDefined()} call on {@link #getEndDatable()}.
   * @return true if {@link #getEndDatable()} returns defined date, false otherwise.
   */
  public boolean isEndDefined() {
    return endDatable.isDefined();
  }

  /**
   * This method is a shortcut of {@link #isNotDefined()} call on {@link #getEndDatable()}.
   * @return true if {@link #getEndDatable()} returns a not defined date, false otherwise.
   */
  public boolean isEndNotDefined() {
    return endDatable.isNotDefined();
  }

  public void setDate(final Date dateReference, PeriodType periodType) {
    setDate(new DateTime(dateReference), periodType);
  }

  public void setDate(final Date dateReference, TimeZone timeZone, PeriodType periodType) {
    setDate(new DateTime(dateReference, timeZone), periodType);
  }

  public void setDate(final DateTime referenceDatable, PeriodType periodType) {
    Period myReferencePeriod = from(referenceDatable, periodType, MessageManager.getLanguage());
    this.beginDatable = myReferencePeriod.getBeginDatable();
    this.endDatable = myReferencePeriod.getEndDatable();
    this.periodType = myReferencePeriod.getPeriodType();
  }

  public void setDates(final Date beginDate, Date endDate) {
    setDates(new DateTime(beginDate), new DateTime(endDate));
  }

  public void setDates(final Date beginDate, Date endDate, TimeZone timeZone) {
    setDates(new DateTime(beginDate, timeZone), new DateTime(endDate, timeZone));
  }

  public void setDates(final DateTime beginDatable, DateTime endDatable) {
    this.beginDatable = beginDatable;
    this.endDatable = endDatable;
    this.periodType = from(beginDatable, endDatable).getPeriodType();
  }

  /**
   * Computes the elapsed time between the begin and the end dates.
   * @return the elapsed time computed represented by {@link Duration}.
   * To retrieve informations from this returned object :
   * <pre>
   *   // Gets the elpased time in milliseconds<br>
   *   period.getElapsedTimeData().getTime();<br>
   * <br>
   *   // Gets the elpased time in seconds<br>
   *   period.getElapsedTimeData().getTimeConverted(TimeUnit.SEC);<br>
   * <br>
   *   // Gets the elpased time in years<br>
   *   period.getElapsedTimeData().getTimeConverted(TimeUnit.YEAR);<br>
   * <br>
   *   ...<br>
   *   // Gets the elpased time in the best unit value<br>
   *   period.getElapsedTimeData().getBestValue();<br>
   * </pre>
   */
  public Duration getElapsedTimeData() {
    return beginDatable.getTimeDataTo(endDatable);
  }

  /**
   * Computes the number of whole or partial days covered by the period.
   * @return the number of whole or partial days covered by the period. represented by {@link
   * Duration}.
   * To retrieve informations from this returned object :
   * <pre>
   *   // Gets the number of days (in milliseconds)<br>
   *   period.getCoveredDaysTimeData().getTime();<br>
   * <br>
   *   // Gets the number of days (in days)<br>
   *   period.getCoveredDaysTimeData().getTimeConverted(TimeUnit.DAY);<br>
   * <br>
   *   ...<br>
   *   // Gets the number of days (in best unit value)<br>
   *   period.getCoveredDaysTimeData().getBestValue();<br>
   * </pre>
   */
  public Duration getCoveredDaysTimeData() {
    return beginDatable.getBeginOfDay()
        .getTimeDataTo(endDatable.addMilliseconds(-1).getEndOfDay().addMilliseconds(1));
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(Object o) {
    return compareTo((Period) o);
  }

  /**
   * The comparison is at first time executed on the begin dates.
   * If they are equals, then it is executed on the end dates.
   * @param period the period
   * @return the comparison result
   */
  public int compareTo(final Period period) {
    return getBeginDate().compareTo(period.getBeginDate()) != 0 ?
        getBeginDate().compareTo(period.getBeginDate()) :
        getEndDate().compareTo(period.getEndDate());
  }

  public boolean isLongerThan(final Period period) {
    long elapsedTime = getEndDate().getTime() - getBeginDate().getTime();
    long givenElapsedTime = period.getEndDate().getTime() - period.getBeginDate().getTime();
    return elapsedTime > givenElapsedTime;
  }

  public boolean equals(Object o) {
    return o instanceof Period && getBeginDate().equals(((Period) o).getBeginDate()) &&
        getEndDate().equals(((Period) o).getEndDate());
  }

  public int hashCode() {
    HashCodeBuilder hash = new HashCodeBuilder();
    hash.append(getBeginDate());
    hash.append(getEndDate());
    return hash.toHashCode();
  }

  public boolean isValid() {
    return isDefined() && (getBeginDate() != null && getEndDate() != null &&
        getBeginDate().compareTo(getEndDate()) <= 0);
  }

  public String toString() {
    return formatPeriodForTests();
  }

  protected String formatPeriodForTests() {
    StringBuilder sb = new StringBuilder("Period(");
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    if (getBeginDate() != null) {
      sb.append(sdf.format(getBeginDate()));
    } else {
      sb.append("NA");
    }
    sb.append(", ");
    if (getEndDate() != null) {
      sb.append(sdf.format(getEndDate()));
    } else {
      sb.append("NA");
    }
    sb.append(") -> elapsed time ");
    sb.append(getElapsedTimeData().getFormattedValueOnly(TimeUnit.DAY));
    sb.append(" day(s), covered time ");
    sb.append(getCoveredDaysTimeData().getFormattedValueOnly(TimeUnit.DAY));
    sb.append(" day(s), ");
    sb.append(getPeriodType().name());
    sb.append(" type, ");
    sb.append(isValid() ? "is valid" : "is not valid");
    return sb.toString();
  }

  /**
   * Checks if the given date is included in the period.
   * @param date a date
   * @return <code>true</code> if the given date is included in the period.
   */
  public boolean contains(final Date date) {
    return DateUtil.compareTo(getBeginDate(), date, false) <= 0 &&
        DateUtil.compareTo(getEndDate(), date, false) >= 0;
  }

  /**
   * Checks if the given period is included in the period.
   * @param period a period
   * @return <code>true</code> if the given period is included in the period.
   */
  public boolean contains(final Period period) {
    return DateUtil.compareTo(getBeginDate(), period.getBeginDate()) <= 0 &&
        DateUtil.compareTo(getEndDate(), period.getEndDate()) >= 0;
  }

  /**
   * Checks if the given period is partially included in the period.
   * @param period Une date
   * @return <code>true</code> if the given period is partially included in the period.
   */
  public boolean containsPartOf(final Period period) {
    return DateUtil.compareTo(getBeginDate(), period.getEndDate()) <= 0 &&
        DateUtil.compareTo(getEndDate(), period.getBeginDate()) >= 0;
  }

  @SuppressWarnings("CloneDoesntDeclareCloneNotSupportedException")
  @Override
  public Period clone() {
    try {
      return (Period) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }
}