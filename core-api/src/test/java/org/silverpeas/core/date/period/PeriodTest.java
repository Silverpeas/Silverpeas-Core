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
package org.silverpeas.core.date.period;

import org.silverpeas.core.date.DateTime;
import org.silverpeas.core.util.DateUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.silverpeas.core.notification.message.MessageManager;
import org.silverpeas.core.util.time.TimeUnit;

import java.sql.Timestamp;
import java.util.Date;
import java.util.TimeZone;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * User: Yohann Chastagnier
 * Date: 27/11/13
 */
public class PeriodTest {

  Date periodReferenceBeginDate = Timestamp.valueOf("2013-11-28 12:00:00.000");
  Date periodReferenceEndDate = Timestamp.valueOf("2013-12-25 18:30:40.006");
  Period periodReferenceTest = Period.from(periodReferenceBeginDate, periodReferenceEndDate);

  @Before
  public void setup() {
    MessageManager.initialize();
    MessageManager.setLanguage("fr");
    periodReferenceTest.inTimeZone(TimeZone.getTimeZone("Europe/Paris"));
  }

  @After
  public void tearDown() {
    MessageManager.clear(MessageManager.getRegistredKey());
    MessageManager.destroy();
  }

  @Test
  public void check() {
    assertThat(Period.check(null), sameInstance(Period.UNDEFINED));

    assertThat(Period.check(periodReferenceTest), sameInstance(periodReferenceTest));
    assertThat(Period.check(periodReferenceTest), not(sameInstance(Period.UNDEFINED)));

    Period period = Period.from(DateUtil.MINIMUM_DATE, DateUtil.getNow());
    assertThat(Period.check(period), sameInstance(period));
    assertThat(Period.check(period), not(sameInstance(Period.UNDEFINED)));

    period = Period.from(DateUtil.getNow(), DateUtil.MAXIMUM_DATE);
    assertThat(Period.check(period), sameInstance(period));
    assertThat(Period.check(period), not(sameInstance(Period.UNDEFINED)));

    period = Period.from(DateUtil.MINIMUM_DATE, DateUtil.MAXIMUM_DATE);
    assertThat(Period.check(period), sameInstance(period));
    assertThat(Period.check(period), sameInstance(Period.UNDEFINED));
  }

  @Test
  public void undefinedPeriod() {
    Period period = Period.UNDEFINED;
    assertThat(period.isDefined(), is(false));
    assertThat(period.isNotDefined(), is(true));
    assertThat(period.isBeginDefined(), is(false));
    assertThat(period.isBeginNotDefined(), is(true));
    assertThat(period.isEndDefined(), is(false));
    assertThat(period.isEndNotDefined(), is(true));
    assertThat(period.getPeriodType(), is(PeriodType.unknown));

    period.setDate(DateUtil.getNow(), PeriodType.day);

    assertThat(period.isDefined(), is(false));
    assertThat(period.isNotDefined(), is(true));
    assertThat(period.isBeginDefined(), is(false));
    assertThat(period.isBeginNotDefined(), is(true));
    assertThat(period.isEndDefined(), is(false));
    assertThat(period.isEndNotDefined(), is(true));
    assertThat(period.getPeriodType(), is(PeriodType.unknown));

    period.setDate(DateUtil.getNow(), TimeZone.getDefault(), PeriodType.day);

    assertThat(period.isDefined(), is(false));
    assertThat(period.isNotDefined(), is(true));
    assertThat(period.isBeginDefined(), is(false));
    assertThat(period.isBeginNotDefined(), is(true));
    assertThat(period.isEndDefined(), is(false));
    assertThat(period.isEndNotDefined(), is(true));
    assertThat(period.getPeriodType(), is(PeriodType.unknown));

    period.setDate(new DateTime(DateUtil.getNow()), PeriodType.day);

    assertThat(period.isDefined(), is(false));
    assertThat(period.isNotDefined(), is(true));
    assertThat(period.isBeginDefined(), is(false));
    assertThat(period.isBeginNotDefined(), is(true));
    assertThat(period.isEndDefined(), is(false));
    assertThat(period.isEndNotDefined(), is(true));
    assertThat(period.getPeriodType(), is(PeriodType.unknown));

    period.setDates(DateUtil.getNow(), DateUtil.getNow());

    assertThat(period.isDefined(), is(false));
    assertThat(period.isNotDefined(), is(true));
    assertThat(period.isBeginDefined(), is(false));
    assertThat(period.isBeginNotDefined(), is(true));
    assertThat(period.isEndDefined(), is(false));
    assertThat(period.isEndNotDefined(), is(true));
    assertThat(period.getPeriodType(), is(PeriodType.unknown));

    period.setDates(DateUtil.getNow(), DateUtil.getNow(), TimeZone.getDefault());

    assertThat(period.isDefined(), is(false));
    assertThat(period.isNotDefined(), is(true));
    assertThat(period.isBeginDefined(), is(false));
    assertThat(period.isBeginNotDefined(), is(true));
    assertThat(period.isEndDefined(), is(false));
    assertThat(period.isEndNotDefined(), is(true));
    assertThat(period.getPeriodType(), is(PeriodType.unknown));

    period.setDates(new DateTime(DateUtil.getNow()), new DateTime(DateUtil.getNow()));

    assertThat(period.isDefined(), is(false));
    assertThat(period.isNotDefined(), is(true));
    assertThat(period.isBeginDefined(), is(false));
    assertThat(period.isBeginNotDefined(), is(true));
    assertThat(period.isEndDefined(), is(false));
    assertThat(period.isEndNotDefined(), is(true));
    assertThat(period.getPeriodType(), is(PeriodType.unknown));

    period.setPeriodType(PeriodType.day);

    assertThat(period.isDefined(), is(false));
    assertThat(period.isNotDefined(), is(true));
    assertThat(period.isBeginDefined(), is(false));
    assertThat(period.isBeginNotDefined(), is(true));
    assertThat(period.isEndDefined(), is(false));
    assertThat(period.isEndNotDefined(), is(true));
    assertThat(period.getPeriodType(), is(PeriodType.unknown));

    assertThat(period.clone(), sameInstance(period));
  }

  /**
   * Testing the defined reference period for the tests.
   */
  @Test
  public void periodReference() {
    assertReferencePeriod();
    assertThat(periodReferenceTest.getBeginDatable().toICal(), is("20131128T120000"));
    assertThat(periodReferenceTest.getBeginDatable().toICalInUTC(), is("20131128T110000Z"));
    assertThat(periodReferenceTest.getBeginDatable().toISO8601(), is("2013-11-28T12:00:00+0100"));
    assertThat(periodReferenceTest.getBeginDatable().toShortISO8601(), is("2013-11-28T12:00+0100"));
    assertThat(periodReferenceTest.getEndDatable().toICal(), is("20131225T183040"));
    assertThat(periodReferenceTest.getEndDatable().toICalInUTC(), is("20131225T173040Z"));
    assertThat(periodReferenceTest.getEndDatable().toISO8601(), is("2013-12-25T18:30:40+0100"));
    assertThat(periodReferenceTest.getEndDatable().toShortISO8601(), is("2013-12-25T18:30+0100"));

    // Other time zone
    periodReferenceTest.inTimeZone(TimeZone.getTimeZone("America/Phoenix"));

    assertReferencePeriod();
    assertThat(periodReferenceTest.getBeginDatable().toICal(), is("20131128T120000"));
    assertThat(periodReferenceTest.getBeginDatable().toICalInUTC(), is("20131128T110000Z"));
    assertThat(periodReferenceTest.getBeginDatable().toISO8601(), is("2013-11-28T04:00:00-0700"));
    assertThat(periodReferenceTest.getBeginDatable().toShortISO8601(), is("2013-11-28T04:00-0700"));
    assertThat(periodReferenceTest.getEndDatable().toICal(), is("20131225T183040"));
    assertThat(periodReferenceTest.getEndDatable().toICalInUTC(), is("20131225T173040Z"));
    assertThat(periodReferenceTest.getEndDatable().toISO8601(), is("2013-12-25T10:30:40-0700"));
    assertThat(periodReferenceTest.getEndDatable().toShortISO8601(), is("2013-12-25T10:30-0700"));
  }

  private void assertReferencePeriod() {
    assertThat(periodReferenceTest.getBeginDate(), is(periodReferenceBeginDate));
    assertThat(periodReferenceTest.getEndDate(), is(periodReferenceEndDate));
    assertThat(periodReferenceTest.getElapsedTimeData().getFormattedValueOnly(TimeUnit.WEEK),
        is("3,89"));
    assertThat(periodReferenceTest.getPeriodType(), is(PeriodType.unknown));
    assertThat(periodReferenceTest.isValid(), is(true));
    assertThat(periodReferenceTest.formatPeriodForTests(),
        is("Period(2013-11-28 12:00:00.000, 2013-12-25 18:30:40.006) -> elapsed time 27," +
            "27 day(s), covered time 28 day(s), unknown type, is valid"));
    assertThat(periodReferenceTest.equals(Period
        .from((Date) periodReferenceBeginDate.clone(), (Date) periodReferenceEndDate.clone())),
        is(true));
  }

  @Test
  public void periodDefinedNotDefined() {
    Period period = Period.from(Timestamp.valueOf("2013-11-28 00:00:00.000"),
        Timestamp.valueOf("2013-11-29 00:00:00.000"));
    assertThat(period.isDefined(), is(true));
    assertThat(period.isNotDefined(), is(false));
    assertThat(period.isBeginDefined(), is(true));
    assertThat(period.isBeginNotDefined(), is(false));
    assertThat(period.isEndDefined(), is(true));
    assertThat(period.isEndNotDefined(), is(false));
    period = Period.from(DateUtil.MINIMUM_DATE, Timestamp.valueOf("2013-11-29 00:00:00.000"));
    assertThat(period.isDefined(), is(true));
    assertThat(period.isNotDefined(), is(false));
    assertThat(period.isBeginDefined(), is(false));
    assertThat(period.isBeginNotDefined(), is(true));
    assertThat(period.isEndDefined(), is(true));
    assertThat(period.isEndNotDefined(), is(false));
    period = Period.from(Timestamp.valueOf("2013-11-28 00:00:00.000"), DateUtil.MAXIMUM_DATE);
    assertThat(period.isDefined(), is(true));
    assertThat(period.isNotDefined(), is(false));
    assertThat(period.isBeginDefined(), is(true));
    assertThat(period.isBeginNotDefined(), is(false));
    assertThat(period.isEndDefined(), is(false));
    assertThat(period.isEndNotDefined(), is(true));
    period = Period.from(DateUtil.MINIMUM_DATE, DateUtil.MAXIMUM_DATE);
    assertThat(period.isDefined(), is(false));
    assertThat(period.isNotDefined(), is(true));
    assertThat(period.isBeginDefined(), is(false));
    assertThat(period.isBeginNotDefined(), is(true));
    assertThat(period.isEndDefined(), is(false));
    assertThat(period.isEndNotDefined(), is(true));

    assertThat(Period.UNDEFINED.isDefined(), is(false));
    assertThat(Period.UNDEFINED.isNotDefined(), is(true));
  }

  @Test
  public void periodNotValid() {
    Period periodTest = Period.from(Timestamp.valueOf("2013-11-28 12:00:00.010"),
        Timestamp.valueOf("2013-11-28 12:00:00.009"));
    assertThat(periodTest.formatPeriodForTests(),
        is("Period(2013-11-28 12:00:00.010, 2013-11-28 12:00:00.009) -> elapsed time 0 day(s), " +
            "covered time 1 day(s), unknown type, is not valid"));

    assertThat(Period.UNDEFINED.formatPeriodForTests(),
        is("Period(1900-01-01 00:00:00.000, 2999-12-31 00:00:00.000) -> elapsed time 401 766 day" +
            "(s), covered time 401 766 day(s), unknown type, is not valid"));
  }

  @Test
  public void initilizeDayPeriod() {
    String expectedDay =
        "Period(2013-11-28 00:00:00.000, 2013-11-29 00:00:00.000) -> elapsed time 1 day(s), " +
            "covered time 1 day(s), day type, is valid";

    // A valid day
    Period dayPeriod = Period.from(Timestamp.valueOf("2013-11-28 00:00:00.000"),
        Timestamp.valueOf("2013-11-29 00:00:00.000"));
    assertThat(dayPeriod.formatPeriodForTests(), is(expectedDay));
    dayPeriod = Period.from(Timestamp.valueOf("2013-11-28 00:00:00.000"), PeriodType.day);
    assertThat(dayPeriod.formatPeriodForTests(), is(expectedDay));

    // Adding one millisecond to the end date -> elapsed time not a day
    dayPeriod.setDates(Timestamp.valueOf("2013-11-28 00:00:00.000"),
        Timestamp.valueOf("2013-11-29 00:00:00.001"));
    assertThat(dayPeriod.formatPeriodForTests(),
        is("Period(2013-11-28 00:00:00.000, 2013-11-29 00:00:00.001) -> elapsed time 1 day(s), " +
            "covered time 2 day(s), unknown type, is valid"));

    // Setting a reference date and a day period type -> elapsed time a day
    dayPeriod.setDate(dayPeriod.getBeginDate(), PeriodType.day);
    assertThat(dayPeriod.formatPeriodForTests(), is(expectedDay));

    Period notADayPeriod = Period.from(Timestamp.valueOf("2013-11-28 00:00:00.000"),
        Timestamp.valueOf("2013-11-28 23:59:59.999"));
    assertThat(notADayPeriod.formatPeriodForTests(),
        is("Period(2013-11-28 00:00:00.000, 2013-11-28 23:59:59.999) -> elapsed time 0,99 day(s)," +
            " covered time 1 day(s), unknown type, is valid"));
  }

  @Test
  public void initilizeWeekPeriod() {
    String expectedWeek =
        "Period(2013-11-25 00:00:00.000, 2013-12-02 00:00:00.000) -> elapsed time 7 day(s), " +
            "covered time 7 day(s), week type, is valid";

    Period weekPeriod = Period.from(Timestamp.valueOf("2013-11-25 00:00:00.000"),
        Timestamp.valueOf("2013-12-02 00:00:00.000"));
    assertThat(weekPeriod.formatPeriodForTests(), is(expectedWeek));
    weekPeriod = Period.from(Timestamp.valueOf("2013-11-25 00:00:00.000"), PeriodType.week);
    assertThat(weekPeriod.formatPeriodForTests(), is(expectedWeek));

    // Substracting one millisecond to the end date -> elapsed time not a week
    weekPeriod.setDates(Timestamp.valueOf("2013-11-25 00:00:00.000"),
        Timestamp.valueOf("2013-12-01 23:59:59.999"));
    assertThat(weekPeriod.formatPeriodForTests(),
        is("Period(2013-11-25 00:00:00.000, 2013-12-01 23:59:59.999) -> elapsed time 6,99 day(s)," +
            " covered time 7 day(s), unknown type, is valid"));

    // Setting a reference date and a week period type -> elapsed time a week
    weekPeriod.setDate(weekPeriod.getBeginDate(), PeriodType.week);
    assertThat(weekPeriod.formatPeriodForTests(), is(expectedWeek));

    Period notAWeekPeriod = Period.from(Timestamp.valueOf("2013-11-25 00:00:00.000"),
        Timestamp.valueOf("2013-12-02 00:00:00.001"));
    assertThat(notAWeekPeriod.formatPeriodForTests(),
        is("Period(2013-11-25 00:00:00.000, 2013-12-02 00:00:00.001) -> elapsed time 7 day(s), " +
            "covered time 8 day(s), unknown type, is valid"));
  }

  @Test
  public void initilizeMonthPeriod() {
    String expectedMonth =
        "Period(2013-02-01 00:00:00.000, 2013-03-01 00:00:00.000) -> elapsed time 28 day(s), " +
            "covered time 28 day(s), month type, is valid";

    Period monthPeriod = Period.from(Timestamp.valueOf("2013-02-01 00:00:00.000"),
        Timestamp.valueOf("2013-03-01 00:00:00.000"));
    assertThat(monthPeriod.formatPeriodForTests(), is(expectedMonth));
    monthPeriod = Period.from(Timestamp.valueOf("2013-02-01 00:00:00.000"), PeriodType.month);
    assertThat(monthPeriod.formatPeriodForTests(), is(expectedMonth));

    // Substracting one millisecond to the end date -> elapsed time not a month
    monthPeriod.setDates(Timestamp.valueOf("2013-02-01 00:00:00.000"),
        Timestamp.valueOf("2013-02-28 23:59:59.999"));
    assertThat(monthPeriod.formatPeriodForTests(),
        is("Period(2013-02-01 00:00:00.000, 2013-02-28 23:59:59.999) -> elapsed time 27,99 day(s)" +
            ", covered time 28 day(s), unknown type, is valid"));

    // Setting a reference date and a month period type -> elapsed time a month
    monthPeriod.setDate(monthPeriod.getBeginDate(), PeriodType.month);
    assertThat(monthPeriod.formatPeriodForTests(), is(expectedMonth));

    Period notAMonthPeriod = Period.from(Timestamp.valueOf("2013-02-01 00:00:00.000"),
        Timestamp.valueOf("2013-03-01 00:00:00.001"));
    assertThat(notAMonthPeriod.formatPeriodForTests(),
        is("Period(2013-02-01 00:00:00.000, 2013-03-01 00:00:00.001) -> elapsed time 28 day(s), " +
            "covered time 29 day(s), unknown type, is valid"));
  }

  @Test
  public void initilizeYearPeriod() {
    String expectedYear =
        "Period(2013-01-01 00:00:00.000, 2014-01-01 00:00:00.000) -> elapsed time 365 day(s), " +
            "covered time 365 day(s), year type, is valid";

    Period yearPeriod = Period.from(Timestamp.valueOf("2013-01-01 00:00:00.000"),
        Timestamp.valueOf("2014-01-01 00:00:00.000"));
    assertThat(yearPeriod.formatPeriodForTests(), is(expectedYear));
    yearPeriod = Period.from(Timestamp.valueOf("2013-01-01 00:00:00.000"), PeriodType.year);
    assertThat(yearPeriod.formatPeriodForTests(), is(expectedYear));

    // Adding one millisecond to the end date -> elapsed time not a year
    yearPeriod.setDates(Timestamp.valueOf("2013-01-01 00:00:00.000"),
        Timestamp.valueOf("2014-01-01 00:00:00.001"));
    assertThat(yearPeriod.formatPeriodForTests(),
        is("Period(2013-01-01 00:00:00.000, 2014-01-01 00:00:00.001) -> elapsed time 365 day(s), " +
            "covered time 366 day(s), unknown type, is valid"));

    // Setting a reference date and a year period type -> elapsed time a year
    yearPeriod.setDate(yearPeriod.getBeginDate(), PeriodType.year);
    assertThat(yearPeriod.formatPeriodForTests(), is(expectedYear));

    Period notAYearPeriod = Period.from(Timestamp.valueOf("2013-01-01 00:00:00.000"),
        Timestamp.valueOf("2013-12-31 23:59:59.999"));
    assertThat(notAYearPeriod.formatPeriodForTests(),
        is("Period(2013-01-01 00:00:00.000, 2013-12-31 23:59:59.999) -> elapsed time 364," +
            "99 day(s), covered time 365 day(s), unknown type, is valid"));
  }

  @Test
  public void compareDayAndMonthPeriods() {
    Period dayPeriod = Period.from(Timestamp.valueOf("2013-02-18 00:00:00.000"), PeriodType.day);
    Period monthPeriod =
        Period.from(Timestamp.valueOf("2013-02-18 00:00:00.000"), PeriodType.month);
    // The day starting later than the month ...
    assertThat(dayPeriod.compareTo(monthPeriod), is(1));
    assertThat(monthPeriod.compareTo(dayPeriod), is(-1));
    // The month is longer than the day
    assertThat(dayPeriod.isLongerThan(monthPeriod), is(false));
    assertThat(monthPeriod.isLongerThan(dayPeriod), is(true));
  }

  @Test
  public void compareMonthAndYearPeriods() {
    Period monthPeriod =
        Period.from(Timestamp.valueOf("2013-02-18 00:00:00.000"), PeriodType.month);
    Period yearPeriod = Period.from(Timestamp.valueOf("2013-02-18 00:00:00.000"), PeriodType.year);
    // The month starting later than the year ...
    assertThat(yearPeriod.compareTo(monthPeriod), is(-1));
    assertThat(monthPeriod.compareTo(yearPeriod), is(1));
    // The year is longer than the month
    assertThat(yearPeriod.isLongerThan(monthPeriod), is(true));
    assertThat(monthPeriod.isLongerThan(yearPeriod), is(false));
  }
}
