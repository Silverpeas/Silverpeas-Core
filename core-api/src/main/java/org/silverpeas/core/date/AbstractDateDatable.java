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
package org.silverpeas.core.date;

import org.silverpeas.core.util.DateUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.silverpeas.core.notification.message.MessageManager;
import org.silverpeas.core.util.UnitUtil;
import org.silverpeas.core.util.time.TimeData;

/**
 * User: Yohann Chastagnier
 * Date: 06/12/13
 */
public abstract class AbstractDateDatable<T extends Datable<? super T>> extends java.util.Date
    implements Datable<T> {
  private static final long serialVersionUID = 4908009936415992351L;

  /**
   * Default constructor.
   * @param date
   */
  public AbstractDateDatable(final long date) {
    super(date);
  }

  @Override
  public abstract T clone();

  @Override
  public TimeData getTimeDataTo(final T anotherDatable) {
    return UnitUtil.getTimeData(anotherDatable.asDate().getTime() - getTime());
  }

  /**
   * Create a new instance from a given date time in milliseconds.
   * @param aDate the date time in milliseconds.
   * @return the new datable instance corresponding to the given time in milliseconds.
   */
  protected abstract T newInstanceFrom(java.util.Date aDate);

  @Override
  public T getBeginOfDay() {
    return newInstanceFrom(DateUtil.getBeginOfDay(this));
  }

  @Override
  public T getEndOfDay() {
    return newInstanceFrom(DateUtil.getEndOfDay(this));
  }

  @Override
  public T getBeginOfWeek() {
    return getBeginOfWeek(MessageManager.getLanguage());
  }

  @Override
  public T getEndOfWeek() {
    return getEndOfWeek(MessageManager.getLanguage());
  }

  @Override
  public T getBeginOfWeek(final String locale) {
    return newInstanceFrom(DateUtil.getFirstDateOfWeek(this, locale));
  }

  @Override
  public T getEndOfWeek(final String locale) {
    return newInstanceFrom(DateUtil.getEndDateOfWeek(this, locale));
  }

  @Override
  public T getBeginOfMonth() {
    return newInstanceFrom(DateUtil.getFirstDateOfMonth(this));
  }

  @Override
  public T getEndOfMonth() {
    return newInstanceFrom(DateUtil.getEndDateOfMonth(this));
  }

  @Override
  public T getBeginOfYear() {
    return newInstanceFrom(DateUtil.getFirstDateOfYear(this));
  }

  @Override
  public T getEndOfYear() {
    return newInstanceFrom(DateUtil.getEndDateOfYear(this));
  }

  @Override
  public T addYears(final int amount) {
    return newInstanceFrom(DateUtils.addYears(this, amount));
  }

  @Override
  public T addMonths(final int amount) {
    return newInstanceFrom(DateUtils.addMonths(this, amount));
  }

  @Override
  public T addWeeks(final int amount) {
    return newInstanceFrom(DateUtils.addWeeks(this, amount));
  }

  @Override
  public T addDays(final int amount) {
    return newInstanceFrom(DateUtils.addDays(this, amount));
  }

  @Override
  public T addHours(final int amount) {
    return newInstanceFrom(DateUtils.addHours(this, amount));
  }

  @Override
  public T addMinutes(final int amount) {
    return newInstanceFrom(DateUtils.addMinutes(this, amount));
  }

  @Override
  public T addSeconds(final int amount) {
    return newInstanceFrom(DateUtils.addSeconds(this, amount));
  }

  @Override
  public T addMilliseconds(final int amount) {
    return newInstanceFrom(DateUtils.addMilliseconds(this, amount));
  }

  @Override
  public boolean isDefined() {
    return compareTo(DateUtil.MINIMUM_DATE) != 0 && compareTo(DateUtil.MAXIMUM_DATE) != 0;
  }

  @Override
  public boolean isNotDefined() {
    return !isDefined();
  }
}
