/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.date;

import com.stratelia.webactiv.util.DateUtil;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class representing a period between two dates and offering tools around it.
 * User: Yohann Chastagnier
 * Date: 19/04/13
 */
public class Period implements Comparable, Serializable {
  private static final long serialVersionUID = 6378275537498456869L;

  private PeriodType periodType = PeriodType.unknown;
  private Date beginDate;
  private Date endDate;

  /**
   * Initialize a period from given dates.
   * @param beginDate the date of the beginning of the period.
   * @param endDate the date of the ending of the period.
   * @return
   */
  public static Period from(Date beginDate, Date endDate) {
    return new Period(beginDate, endDate);
  }

  /**
   * Initialize a period from a date and a type of period.
   * @param referenceDate
   * @param periodType
   * @param locale
   * @return
   */
  public static Period from(Date referenceDate, PeriodType periodType, String locale) {
    switch (periodType) {
      case year:
        return new YearPeriod(referenceDate);
      case month:
        return new MonthPeriod(referenceDate);
      case week:
        return new WeekPeriod(referenceDate, locale);
      case day:
      default:
        return new DayPeriod(referenceDate);
    }
  }

  /**
   * Constructor : Constructs a newly allocated <code>period</code>.
   * @param beginDate the date of the beginning of the period.
   * @param endDate the date of the ending of the period.
   */
  protected Period(final Date beginDate, final Date endDate) {
    this.beginDate = beginDate;
    this.endDate = endDate;
  }

  /**
   * Gets the type of the period.
   * @return
   */
  public PeriodType getPeriodType() {
    return periodType;
  }

  /**
   * Sets the type of the period.
   * @param periodType
   */
  protected void setPeriodType(final PeriodType periodType) {
    this.periodType = periodType;
  }

  /**
   * Gets the begin date of the period.
   * @return
   */
  public Date getBeginDate() {
    return beginDate;
  }

  /**
   * Sets the begin of the period.
   * @param beginDate
   */
  public void setBeginDate(final Date beginDate) {
    this.beginDate = beginDate;
  }

  /**
   * Gets the end date of the period.
   * @return
   */
  public Date getEndDate() {
    return endDate;
  }

  /**
   * Sets the end date of the period.
   * @param endDate
   */
  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  /**
   * @return the number of days included in the period.
   */
  public int getNumberOfDays() {
    if (beginDate == null || endDate == null) {
      return -1;
    }
    return DateUtil.getDayNumberBetween(beginDate, endDate) + 1;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(Object o) {
    return compareTo((Period) o);
  }

  public int compareTo(final Period period) {
    return getBeginDate().compareTo(period.getBeginDate()) != 0 ?
        getBeginDate().compareTo(period.getBeginDate()) :
        getEndDate().compareTo(period.getEndDate());
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

  /**
   * Indicates if the period is valid.
   * @return
   */
  public boolean isValid() {
    return (getBeginDate() != null && getEndDate() != null &&
        getBeginDate().compareTo(getEndDate()) <= 0);
  }

  /**
   * For debugging.
   * @return
   */
  public String toString() {
    StringBuilder sb = new StringBuilder("Period(");
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
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
    sb.append(")");
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
}