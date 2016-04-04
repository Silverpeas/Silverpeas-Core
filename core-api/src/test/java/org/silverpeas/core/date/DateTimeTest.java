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

import java.util.Calendar;
import java.util.TimeZone;

import org.silverpeas.core.util.DateUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.silverpeas.core.date.DatableMatcher.*;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static java.util.Calendar.*;

/**
 * Unit tests on DateTime objects.
 */
public class DateTimeTest {

  public DateTimeTest() {
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
   * Test of now method, of class DateTime.
   */
  @Test
  public void createsANowDate() {
    DateTime expected = new DateTime(getInstance().getTime());
    DateTime actual = DateTime.now();
    assertEquals(expected.getTime(), actual.getTime(), 100);
  }

  @Test
  public void createsAtASpecifiedDateTime() {
    Calendar now = getInstance();
    DateTime expected = new DateTime(now.getTime());
    DateTime actual = DateTime.dateTimeAt(now.get((Calendar.YEAR)),
        now.get(Calendar.MONTH),
        now.get(Calendar.DAY_OF_MONTH),
        now.get(Calendar.HOUR_OF_DAY),
        now.get(Calendar.MINUTE),
        now.get(Calendar.SECOND),
        now.get(Calendar.MILLISECOND));
    assertEquals(expected.getTime(), actual.getTime(), 100);
  }

  @Test
  public void createsAtASpecifiedShorterDateTime() {
    Calendar now = getInstance();
    now.set(Calendar.SECOND, 0);
    now.set(Calendar.MILLISECOND, 0);
    DateTime expected = new DateTime(now.getTime());
    DateTime actual = DateTime.dateTimeAt(now.get((Calendar.YEAR)),
        now.get(Calendar.MONTH),
        now.get(Calendar.DAY_OF_MONTH),
        now.get(Calendar.HOUR_OF_DAY),
        now.get(Calendar.MINUTE));
    assertEquals(expected.getTime(), actual.getTime(), 100);
  }

  /**
   * Test of clone method, of class DateTime.
   */
  @Test
  public void cloneADateTimeCreatesANewInstanceForTheSameDateTime() {
    DateTime expected = new DateTime(getInstance().getTime());

    DateTime actual = expected.clone();
    assertThat(actual == expected, is(false));
    assertThat(actual, isEqualTo(expected));
  }

  /**
   * Test of toISO8601 method, of class DateTime.
   */
  @Test
  public void theISO8601Representation() {
    DateTime actual = aDateTime();
    assertThat(actual.toISO8601(), equalTo(iso8601Text()));
  }

  /**
   * Test of toICal method, of class DateTime.
   */
  @Test
  public void theICalRepresentation() {
    DateTime actual = aDateTime();
    assertThat(actual.toICal(), equalTo(iCalText()));
  }

  /**
   * Test of toICalInUTC method, of class DateTime.
   */
  @Test
  public void theICalInUTCRepresentation() {
    DateTime actual = aDateTime();
    assertThat(actual.toICalInUTC(), equalTo(iCalInUTCText()));
  }

  /**
   * Test of inTimeZone method, of class DateTime.
   */
  @Test
  public void theTimeZoneImpactsOnlyTheISORepresentationOfTheDateTime() {
    final String tz = "America/Los_Angeles";
    DateTime expected = aDateTime();
    DateTime actual = aDateTime();
    actual.inTimeZone(TimeZone.getTimeZone(tz));

    assertThat(actual.getTimeZone(), equalTo(TimeZone.getTimeZone(tz)));
    assertThat(actual, isEqualTo(expected));
    assertThat(actual.toISO8601(), endsWith("-0800"));
  }

  /**
   * Test of before method, of class DateTime.
   */
  @Test
  public void nowIsBeforeAFuturDateTime() {
    Calendar aDate = getInstance();
    DateTime now = new DateTime(aDate.getTime());
    aDate.add(HOUR_OF_DAY, 1);
    DateTime afterOneHour = new DateTime(aDate.getTime());

    assertThat(now.isBefore(afterOneHour), is(true));
  }

  /**
   * Test of after method, of class DateTime.
   */
  @Test
  public void nowIsAfterAPastDateTime() {
    Calendar aDate = getInstance();
    DateTime now = new DateTime(aDate.getTime());
    aDate.add(HOUR_OF_DAY, -1);
    DateTime afterOneHour = new DateTime(aDate.getTime());

    assertThat(now.isAfter(afterOneHour), is(true));
  }

  /**
   * Test of equals method, of class DateTime.
   */
  @Test
  public void twoSameDateTimesAreEqual() {
    Calendar aDate = getInstance();
    DateTime expected = new DateTime(aDate.getTime());
    DateTime actual = new DateTime(aDate.getTime());

    assertThat(actual.isEqualTo(expected), is(true));
  }

  private DateTime aDateTime() {
    Calendar aDate = getInstance();
    aDate.set(YEAR, 2011);
    aDate.set(MONTH, 0);
    aDate.set(DAY_OF_MONTH, 20);
    aDate.set(HOUR_OF_DAY, 11);
    aDate.set(MINUTE, 0);
    aDate.set(SECOND, 20);
    aDate.set(MILLISECOND, 80);
    DateTime dateTime = new DateTime(aDate.getTime());
    dateTime.inTimeZone(TimeZone.getTimeZone("Europe/Paris"));
    return dateTime;
  }

  private String iso8601Text() {
    return "2011-01-20T11:00:20+0100";
  }

  private String iCalText() {
    return "20110120T110020";
  }

  private String iCalInUTCText() {
    return "20110120T100020Z";
  }

  @Test
  public void isDefinedNotDefined() {
    Datable date = new DateTime(DateUtil.getNow());
    assertThat(date.isDefined(), is(true));
    assertThat(date.isNotDefined(), is(false));

    date = new DateTime(DateUtils.addMilliseconds(DateUtil.MINIMUM_DATE, -1));
    assertThat(date.isDefined(), is(true));
    assertThat(date.isNotDefined(), is(false));
    date = new DateTime(DateUtil.MINIMUM_DATE);
    assertThat(date.isDefined(), is(false));
    assertThat(date.isNotDefined(), is(true));
    date = new DateTime(DateUtils.addMilliseconds(DateUtil.MINIMUM_DATE, 1));
    assertThat(date.isDefined(), is(true));
    assertThat(date.isNotDefined(), is(false));

    date = new DateTime(DateUtils.addMilliseconds(DateUtil.MAXIMUM_DATE, -1));
    assertThat(date.isDefined(), is(true));
    assertThat(date.isNotDefined(), is(false));
    date = new DateTime(DateUtil.MAXIMUM_DATE);
    assertThat(date.isDefined(), is(false));
    assertThat(date.isNotDefined(), is(true));
    date = new DateTime(DateUtils.addMilliseconds(DateUtil.MAXIMUM_DATE, 1));
    assertThat(date.isDefined(), is(true));
    assertThat(date.isNotDefined(), is(false));
  }
}
