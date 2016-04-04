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

package org.silverpeas.core.date;

import org.silverpeas.core.util.DateUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Calendar;

import static org.silverpeas.core.date.DatableMatcher.*;
import static java.util.Calendar.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests on the Date objects.
 */
public class DateTest {

  public DateTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of today method, of class Date.
   */
  @Test
  public void createsATodayDate() {
    Date expected = new Date(getInstance().getTime());
    Date actual = Date.today();
    assertThat(actual, isEqualTo(expected));
  }

  /**
   * Test of tomorrow method, of class Date.
   */
  @Test
  public void createsATomorrowDate() {
    Date today = new Date(getInstance().getTime());
    Calendar tomorrow = getInstance();
    tomorrow.add(DAY_OF_MONTH, 1);
    Date expected = new Date(tomorrow.getTime());

    Date actual = Date.tomorrow();
    assertThat(actual, isEqualTo(expected));
    assertThat(actual, isAfter(today));
  }

  /**
   * Test of yesterday method, of class Date.
   */
  @Test
  public void createsAYesterdayDate() {
    Date today = new Date(getInstance().getTime());
    Calendar yesterday = getInstance();
    yesterday.add(DAY_OF_MONTH, -1);
    Date expected = new Date(yesterday.getTime());

    Date actual = Date.yesterday();
    assertThat(actual, isEqualTo(expected));
    assertThat(actual, isBefore(today));
  }

  /**
   * Test of dateOn method, of class Date.
   */
  @Test
  public void createsADateFromYearMonthAndDayIndication() {
    Calendar date = getInstance();
    date.set(YEAR, 2011);
    date.set(MONTH, 0);
    date.set(DAY_OF_MONTH, 20);
    Date expected = new Date(date.getTime());

    Date actual = Date.dateOn(2011, 1, 20);
    assertThat(actual, isEqualTo(expected));
  }

  /**
   * Test of clone method, of class Date.
   */
  @Test
  public void cloneADateCreatesANewInstanceForTheSameDate() {
    Date expected = new Date(getInstance().getTime());

    Date actual = expected.clone();
    assertThat(actual == expected, is(false));
    assertThat(actual, isEqualTo(expected));
  }

  /**
   * Test of toISO8601 method, of class Date.
   */
  @Test
  public void theISO8601Representation() {
    Date aDate = Date.dateOn(2011, 1, 20);
    assertThat(aDate.toISO8601(), equalTo("2011-01-20"));
  }

  /**
   * Test of toICal method, of class Date.
   */
  @Test
  public void theICalRepresentation() {
    Date aDate = Date.dateOn(2011, 1, 20);
    assertThat(aDate.toICal(), equalTo("20110120"));
  }

  /**
   * Test of toICalInUTC method, of class Date.
   */
  @Test
  public void theICalInUTCRepresentation() {
    Date aDate = Date.dateOn(2011, 1, 20);
    assertThat(aDate.toICalInUTC(), equalTo("20110120"));
  }

  /**
   * Test of next method, of class Date.
   */
  @Test
  public void nextTodayIsTomorrow() {
    Calendar aDate = getInstance();
    aDate.add(DAY_OF_MONTH, 1);
    Date expected = new Date(aDate.getTime());

    Date today = new Date(getInstance().getTime());
    Date actual = today.next();
    assertThat(actual, isEqualTo(expected));
    assertThat(actual, isAfter(today));
  }

  /**
   * Test of previous method, of class Date.
   */
  @Test
  public void previousTodayIsYesterday() {
    Calendar aDate = getInstance();
    aDate.add(DAY_OF_MONTH, -1);
    Date expected = new Date(aDate.getTime());

    Date today = new Date(getInstance().getTime());
    Date actual = today.previous();
    assertThat(actual, isEqualTo(expected));
    assertThat(actual, isBefore(today));
  }

  /**
   * Test of before method, of class Date.
   */
  @Test
  public void todayIsBeforeTomorrow() {
    Calendar aDate = getInstance();
    Date today = new Date(aDate.getTime());
    aDate.add(DAY_OF_MONTH, 1);
    Date tomorrow = new Date(aDate.getTime());

    assertThat(today.isBefore(tomorrow), is(true));
  }

  /**
   * Test of after method, of class Date.
   */
  @Test
  public void todayIsAfterYesterday() {
    Calendar aDate = getInstance();
    Date today = new Date(aDate.getTime());
    aDate.add(DAY_OF_MONTH, -1);
    Date yesterday = new Date(aDate.getTime());

    assertThat(today.isAfter(yesterday), is(true));
  }

  /**
   * Test of equals method, of class Date.
   */
  @Test
  public void twoSameDatesAreEqual() {
    Calendar aDate = getInstance();
    Date expected = new Date(aDate.getTime());

    Date actual = new Date(aDate.getTime());
    assertThat(actual.isEqualTo(expected), is(true));
  }

  @Test
  public void isDefinedNotDefined() {
    Datable date = new Date(DateUtil.getNow());
    assertThat(date.isDefined(), is(true));
    assertThat(date.isNotDefined(), is(false));

    date = new Date(DateUtils.addMilliseconds(DateUtil.MINIMUM_DATE, -1));
    assertThat(date.isDefined(), is(true));
    assertThat(date.isNotDefined(), is(false));
    date = new Date(DateUtil.MINIMUM_DATE);
    assertThat(date.isDefined(), is(false));
    assertThat(date.isNotDefined(), is(true));
    date = new Date(DateUtils.addMilliseconds(DateUtil.MINIMUM_DATE, 1));
    assertThat(date.isDefined(), is(true));
    assertThat(date.isNotDefined(), is(false));

    date = new Date(DateUtils.addMilliseconds(DateUtil.MAXIMUM_DATE, -1));
    assertThat(date.isDefined(), is(true));
    assertThat(date.isNotDefined(), is(false));
    date = new Date(DateUtil.MAXIMUM_DATE);
    assertThat(date.isDefined(), is(false));
    assertThat(date.isNotDefined(), is(true));
    date = new Date(DateUtils.addMilliseconds(DateUtil.MAXIMUM_DATE, 1));
    assertThat(date.isDefined(), is(true));
    assertThat(date.isNotDefined(), is(false));
  }
}