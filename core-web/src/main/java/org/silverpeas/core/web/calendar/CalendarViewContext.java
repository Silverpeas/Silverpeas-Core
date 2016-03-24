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
package org.silverpeas.core.web.calendar;

import org.apache.commons.lang3.time.DateUtils;
import org.silverpeas.core.date.period.Period;
import org.silverpeas.core.date.period.PeriodType;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;

import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;

/**
 * User: Yohann Chastagnier
 * Date: 17/04/13
 */
public class CalendarViewContext {
  private final static EnumSet<PeriodType> DISPLAY_WEEK_PERIODS =
      EnumSet.of(PeriodType.week, PeriodType.day);

  private String locale = null;
  private final String componentInstanceId;
  private CalendarViewType viewType = CalendarViewType.MONTHLY;
  private CalendarDay referenceDay;
  private CalendarPeriod referencePeriod;
  private boolean withWeekend = true;

  /**
   * Default constructor.
   * @param componentInstanceId the component instance identifier
   * @param locale the locale
   */
  public CalendarViewContext(final String componentInstanceId, final String locale) {
    this.componentInstanceId = componentInstanceId;
    this.locale = locale;
    setReferenceDay(DateUtil.getDate());
  }

  /**
   * Reset to null all filters.
   */
  public CalendarViewContext resetFilters() {
    setViewType(CalendarViewType.MONTHLY);
    today();
    return this;
  }

  public String getComponentInstanceId() {
    return componentInstanceId;
  }

  public CalendarViewType getViewType() {
    return viewType;
  }

  public void setViewType(final CalendarViewType viewType) {
    this.viewType = viewType;
    setReferenceDay(referenceDay.getDate());
  }

  public CalendarPeriod getReferencePeriod() {
    return referencePeriod;
  }

  public String getReferencePeriodLabel() {
    return getPeriodLabel(getReferencePeriod(), locale);
  }

  public CalendarDay getReferenceDay() {
    return referenceDay;
  }

  public String getFormattedReferenceDay() {
    return DateUtil.getOutputDate(getReferenceDay().getDate(), locale);
  }

  public void setReferenceDay(final Date date) {
    setReferenceDay(date, 0);
  }

  private void setReferenceDay(final Date date, int offset) {

    // Reference date
    Calendar cal = DateUtil.convert(date, locale);
    final Date referenceDate;
    if (!withWeekend &&
        (viewType.equals(CalendarViewType.WEEKLY) || viewType.equals(CalendarViewType.DAILY))) {
      switch (cal.get(Calendar.DAY_OF_WEEK)) {
        case Calendar.SATURDAY:
          if (Calendar.SATURDAY == DateUtil.getFirstDayOfWeek(locale)) {
            referenceDate = DateUtils.addDays(date, (offset >= 0 ? 2 : -1));
          } else {
            referenceDate = DateUtils.addDays(date, (offset > 0 ? 2 : -1));
          }
          break;
        case Calendar.SUNDAY:
          if (Calendar.SUNDAY == DateUtil.getFirstDayOfWeek(locale)) {
            referenceDate = DateUtils.addDays(date, (offset >= 0 ? 1 : -2));
          } else {
            referenceDate = DateUtils.addDays(date, (offset > 0 ? 1 : -2));
          }
          break;
        default:
          referenceDate = date;
      }
    } else {
      referenceDate = date;
    }
    this.referenceDay = new CalendarDay(referenceDate, locale);

    // Period
    referencePeriod =
        CalendarPeriod.from(Period.from(referenceDate, viewType.getPeriodeType(), locale), locale);
  }

  /**
   * Change reference date to the previous period according to the current view.
   */
  public void previous() {
    moveReferenceDate(-1);
  }

  /**
   * Change reference date to the next period according to the current view.
   */
  public void next() {
    moveReferenceDate(1);
  }

  /**
   * Centralization.
   * @param offset
   */
  private void moveReferenceDate(int offset) {
    switch (viewType) {
      case YEARLY:
        setReferenceDay(DateUtils.addYears(referenceDay.getDate(), offset), offset);
        break;
      case MONTHLY:
        setReferenceDay(DateUtils.addMonths(referenceDay.getDate(), offset), offset);
        break;
      case WEEKLY:
        setReferenceDay(DateUtils.addWeeks(referenceDay.getDate(), offset), offset);
        break;
      case DAILY:
        setReferenceDay(DateUtils.addDays(referenceDay.getDate(), offset), offset);
        break;
    }
  }

  /**
   * Set the reference date to the date of today.
   */
  public void today() {
    setReferenceDay(DateUtil.getDate());
  }

  /**
   * Indicates if weekends have to be displayed.
   * @return
   */
  public boolean isWithWeekend() {
    return withWeekend;
  }

  /**
   * Set if weekends have to be displayed.
   * @param withWeekend
   */
  public void setWithWeekend(final boolean withWeekend) {
    this.withWeekend = withWeekend;
  }

  /**
   * Gets the first day of weeks of the calendar with 1 meaning for sunday, 2 meaning for monday,
   * and so on. The first day of weeks depends on the locale; the first day of weeks is monday for
   * french whereas it is for sunday for US.
   * @return the first day of week.
   */
  public int getFirstDayOfWeek() {
    return DateUtil.getFirstDayOfWeek(locale);
  }

  /**
   * Gets the bundle
   * @return
   */
  protected static LocalizationBundle getBundle(final String language) {
    return ResourceLocator.getGeneralLocalizationBundle(language);
  }

  /**
   * Compute a period label.
   * @param period
   * @param language
   * @return
   */
  public static String getPeriodLabel(CalendarPeriod period, String language) {
    LocalizationBundle bundle = getBundle(language);
    StringBuilder periodLabel = new StringBuilder();
    boolean displayWeek = DISPLAY_WEEK_PERIODS.contains(period.getPeriodType());
    if (period.getPeriodType().isWeek()) {
      // Month of the begin date of the period
      periodLabel.append(bundle.getString("GML.mois" + period.getBeginDate().getMonth()));
      // Verifies if months are different between the beginning and the end of the period
      if (period.getBeginDate().getMonth() != period.getEndDate().getMonth()) {
        displayWeek = true;
        // Verifies if years are different between the beginning and the end of the period
        if (period.getBeginDate().getYear() != period.getEndDate().getYear()) {
          // Appends the year of the beginning date after the month of this same date
          periodLabel.append(" ").append(period.getBeginDate().getYear());
        }
        // Appends the month of the end date of the period
        periodLabel.append(" / ").append(bundle.getString("GML.mois" + period.getEndDate().getMonth()));
      }
      // Appends the year of the end date after the month of this same date
      periodLabel.append(" ").append(period.getEndDate().getYear());
      // Appends the week number if it is necessary
      if (displayWeek) {
        periodLabel.append(" - ")
            .append(bundle.getString("GML.week"))
            .append(' ')
            .append(period.getBeginDate().getWeekOfYear());
      }
    } else if (period.getPeriodType().isMonth()) {
      periodLabel.append(bundle.getString("GML.mois" + period.getBeginDate().getMonth()))
          .append(" ").append(period.getBeginDate().getYear());
    } else if (period.getPeriodType().isYear()) {
      periodLabel.append(period.getBeginDate().getYear());
    }
    // Result
    return periodLabel.toString();
  }
}
