/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.util;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author ehugonnet
 */
public class DateUtilTest {

  public DateUtilTest() {
  }

  /**
   * Test of formatDuration method, of class DateUtil.
   */
  @Test
  public void testFormatDuration() {
    long duration = 0l;
    String expResult = "0s";
    String result = DateUtil.formatDuration(duration);
    assertThat("Duration of 0s", result, is(expResult));


    duration = 10000l;
    expResult = "10s";
    result = DateUtil.formatDuration(duration);
    assertThat("Duration of 10 secondes ", result, is(expResult));

    duration = 60000l;
    expResult = "1m00s";
    result = DateUtil.formatDuration(duration);
    assertThat("Duration of 1 minute", result, is(expResult));

    duration = 305000l;
    expResult = "5m05s";
    result = DateUtil.formatDuration(duration);
    assertThat("Duration of 5 minutes and 5 seconds", result, is(expResult));


    duration = 3600000l;
    expResult = "01h00m00s";
    result = DateUtil.formatDuration(duration);
    assertThat("Duration of 1 hour", result, is(expResult));

    duration = 3600000l + 15 * 60000l + 30000l;
    expResult = "01h15m30s";
    result = DateUtil.formatDuration(duration);
    assertThat("Duration of 1 hour 15 minutes and 30 seconds", result, is(expResult));

    duration = 36000000l + 15 * 60000l + 15000l;
    expResult = "10h15m15s";
    result = DateUtil.formatDuration(duration);
    assertThat("Duration of 10 hours 15 minutes and 15 seconds", result, is(expResult));
  }

  @Test
  public void testAddDays() {
    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.DATE, 27);
    calend.set(Calendar.MONTH, Calendar.JUNE);
    calend.set(Calendar.YEAR, 2012);
    assertThat(calend.get(Calendar.DAY_OF_WEEK), is(Calendar.WEDNESDAY));
    DateUtil.addDaysExceptWeekEnds(calend, 2);
    assertThat(calend.get(Calendar.DAY_OF_WEEK), is(Calendar.FRIDAY));
    assertThat(calend.get(Calendar.DATE), is(29));
    assertThat(calend.get(Calendar.MONTH), is(Calendar.JUNE));
    assertThat(calend.get(Calendar.YEAR), is(2012));


    calend = Calendar.getInstance();
    calend.set(Calendar.DATE, 27);
    calend.set(Calendar.MONTH, Calendar.JUNE);
    calend.set(Calendar.YEAR, 2012);
    assertThat(calend.get(Calendar.DAY_OF_WEEK), is(Calendar.WEDNESDAY));
    DateUtil.addDaysExceptWeekEnds(calend, 4);
    assertThat(calend.get(Calendar.DAY_OF_WEEK), is(Calendar.TUESDAY));
    assertThat(calend.get(Calendar.DATE), is(3));
    assertThat(calend.get(Calendar.MONTH), is(Calendar.JULY));
    assertThat(calend.get(Calendar.YEAR), is(2012));

  }

  @Test
  public void testDatesAreEqual() {
    Date date1 = java.sql.Date.valueOf("2013-01-01");
    Date date2 = DateUtils.addHours(java.sql.Date.valueOf("2013-01-02"), 3);
    assertThat(false, is(DateUtil.datesAreEqual(date1, date2)));

    date1 = java.sql.Date.valueOf("2013-01-02");
    assertThat(false, is(date1.compareTo(date2) == 0));
    assertThat(true, is(DateUtil.datesAreEqual(date1, date2)));
  }

  @Test
  public void testCompareTo() {
    Date date1 = java.sql.Date.valueOf("2013-01-01");
    Date date2 = DateUtils.addHours(java.sql.Date.valueOf("2013-01-02"), 3);
    assertThat(false, is(DateUtil.compareTo(date1, date2) == 0));
    assertThat(false, is(DateUtil.compareTo(date1, date2) > 0));
    assertThat(true, is(DateUtil.compareTo(date1, date2) < 0));

    date1 = java.sql.Date.valueOf("2013-01-02");
    assertThat(true, is(DateUtil.compareTo(date1, date2) == 0));
    assertThat(false, is(DateUtil.compareTo(date1, date2) > 0));
    assertThat(false, is(DateUtil.compareTo(date1, date2) < 0));

    assertThat(false, is(DateUtil.compareTo(date1, date2, false) == 0));
    assertThat(false, is(DateUtil.compareTo(date1, date2, false) > 0));
    assertThat(true, is(DateUtil.compareTo(date1, date2, false) < 0));

    date1 = DateUtils.addHours(java.sql.Date.valueOf("2013-01-02"), 4);
    assertThat(false, is(DateUtil.compareTo(date1, date2, false) == 0));
    assertThat(true, is(DateUtil.compareTo(date1, date2, false) > 0));
    assertThat(false, is(DateUtil.compareTo(date1, date2, false) < 0));
  }

  @Test
  public void testGetDayNumberBetween() {
    Date date1 = java.sql.Date.valueOf("2013-01-01");
    Date date2 = java.sql.Date.valueOf("2013-01-02");
    assertThat(1, is(DateUtil.getDayNumberBetween(date1, date2)));

    date1 = java.sql.Date.valueOf("2013-01-03");
    assertThat(-1, is(DateUtil.getDayNumberBetween(date1, date2)));

    date1 = java.sql.Date.valueOf("2013-01-02");
    assertThat(0, is(DateUtil.getDayNumberBetween(date1, date2)));

    date1 = java.sql.Date.valueOf("2013-01-01");
    date2 = java.sql.Date.valueOf("2013-01-30");
    assertThat(29, is(DateUtil.getDayNumberBetween(date1, date2)));
  }

  @Test
  public void testGetFirstDateOfYear() {
    Date dateTest = DateUtil.getFirstDateOfYear(java.sql.Date.valueOf("2013-04-20"));
    Calendar cal = DateUtil.convert(dateTest);
    assertThat(cal.get(Calendar.YEAR), is(2013));
    assertThat(cal.get(Calendar.MONTH), is(Calendar.JANUARY));
    assertThat(cal.get(Calendar.DAY_OF_MONTH), is(1));
    assertThat(cal.get(Calendar.HOUR_OF_DAY), is(0));
    assertThat(cal.get(Calendar.MINUTE), is(0));
    assertThat(cal.get(Calendar.SECOND), is(0));
    assertThat(cal.get(Calendar.MILLISECOND), is(0));
  }

  @Test
  public void testGetEndDateOfYear() {
    Date dateTest = DateUtil.getEndDateOfYear(java.sql.Date.valueOf("2013-04-20"));
    Calendar cal = DateUtil.convert(dateTest);
    assertThat(cal.get(Calendar.YEAR), is(2013));
    assertThat(cal.get(Calendar.MONTH), is(Calendar.DECEMBER));
    assertThat(cal.get(Calendar.DAY_OF_MONTH), is(31));
    assertThat(cal.get(Calendar.HOUR_OF_DAY), is(23));
    assertThat(cal.get(Calendar.MINUTE), is(59));
    assertThat(cal.get(Calendar.SECOND), is(59));
    assertThat(cal.get(Calendar.MILLISECOND), is(999));
  }

  @Test
  public void testGetFirstDateOfMonth() {
    Date dateTest = DateUtil.getFirstDateOfMonth(java.sql.Date.valueOf("2013-04-20"));
    Calendar cal = DateUtil.convert(dateTest);
    assertThat(cal.get(Calendar.YEAR), is(2013));
    assertThat(cal.get(Calendar.MONTH), is(Calendar.APRIL));
    assertThat(cal.get(Calendar.DAY_OF_MONTH), is(1));
    assertThat(cal.get(Calendar.HOUR_OF_DAY), is(0));
    assertThat(cal.get(Calendar.MINUTE), is(0));
    assertThat(cal.get(Calendar.SECOND), is(0));
    assertThat(cal.get(Calendar.MILLISECOND), is(0));
  }

  @Test
  public void testGetEndDateOfMonth() {
    Date dateTest = DateUtil.getEndDateOfMonth(java.sql.Date.valueOf("2013-04-20"));
    Calendar cal = DateUtil.convert(dateTest);
    assertThat(cal.get(Calendar.YEAR), is(2013));
    assertThat(cal.get(Calendar.MONTH), is(Calendar.APRIL));
    assertThat(cal.get(Calendar.DAY_OF_MONTH), is(30));
    assertThat(cal.get(Calendar.HOUR_OF_DAY), is(23));
    assertThat(cal.get(Calendar.MINUTE), is(59));
    assertThat(cal.get(Calendar.SECOND), is(59));
    assertThat(cal.get(Calendar.MILLISECOND), is(999));
  }

  @Test
  public void testGetFirstDateOfWeekFR() {
    Date dateTest = DateUtil.getFirstDateOfWeek(java.sql.Date.valueOf("2013-04-20"), "fr");
    Calendar cal = DateUtil.convert(dateTest);
    assertThat(cal.get(Calendar.YEAR), is(2013));
    assertThat(cal.get(Calendar.MONTH), is(Calendar.APRIL));
    assertThat(cal.get(Calendar.DAY_OF_MONTH), is(15));
    assertThat(cal.get(Calendar.HOUR_OF_DAY), is(0));
    assertThat(cal.get(Calendar.MINUTE), is(0));
    assertThat(cal.get(Calendar.SECOND), is(0));
    assertThat(cal.get(Calendar.MILLISECOND), is(0));
  }

  @Test
  public void testGetEndDateOfWeekFR() {
    Date dateTest = DateUtil.getEndDateOfWeek(java.sql.Date.valueOf("2013-04-20"), "fr");
    Calendar cal = DateUtil.convert(dateTest);
    assertThat(cal.get(Calendar.YEAR), is(2013));
    assertThat(cal.get(Calendar.MONTH), is(Calendar.APRIL));
    assertThat(cal.get(Calendar.DAY_OF_MONTH), is(21));
    assertThat(cal.get(Calendar.HOUR_OF_DAY), is(23));
    assertThat(cal.get(Calendar.MINUTE), is(59));
    assertThat(cal.get(Calendar.SECOND), is(59));
    assertThat(cal.get(Calendar.MILLISECOND), is(999));
  }

  @Test
  public void testGetFirstDateOfWeekFRBetween2Months() {
    Date dateTest = DateUtil.getFirstDateOfWeek(java.sql.Date.valueOf("2013-04-29"), "fr");
    Calendar cal = DateUtil.convert(dateTest);
    assertThat(cal.get(Calendar.YEAR), is(2013));
    assertThat(cal.get(Calendar.MONTH), is(Calendar.APRIL));
    assertThat(cal.get(Calendar.DAY_OF_MONTH), is(29));
    assertThat(cal.get(Calendar.HOUR_OF_DAY), is(0));
    assertThat(cal.get(Calendar.MINUTE), is(0));
    assertThat(cal.get(Calendar.SECOND), is(0));
    assertThat(cal.get(Calendar.MILLISECOND), is(0));
  }

  @Test
  public void testGetEndDateOfWeekFRBetween2Months() {
    Date dateTest = DateUtil.getEndDateOfWeek(java.sql.Date.valueOf("2013-04-29"), "fr");
    Calendar cal = DateUtil.convert(dateTest);
    assertThat(cal.get(Calendar.YEAR), is(2013));
    assertThat(cal.get(Calendar.MONTH), is(Calendar.MAY));
    assertThat(cal.get(Calendar.DAY_OF_MONTH), is(5));
    assertThat(cal.get(Calendar.HOUR_OF_DAY), is(23));
    assertThat(cal.get(Calendar.MINUTE), is(59));
    assertThat(cal.get(Calendar.SECOND), is(59));
    assertThat(cal.get(Calendar.MILLISECOND), is(999));
  }

  @Test
  public void testGetFirstDateOfWeekFRBetween2Years() {
    Date dateTest = DateUtil.getFirstDateOfWeek(java.sql.Date.valueOf("2013-12-31"), "fr");
    Calendar cal = DateUtil.convert(dateTest);
    assertThat(cal.get(Calendar.YEAR), is(2013));
    assertThat(cal.get(Calendar.MONTH), is(Calendar.DECEMBER));
    assertThat(cal.get(Calendar.DAY_OF_MONTH), is(30));
    assertThat(cal.get(Calendar.HOUR_OF_DAY), is(0));
    assertThat(cal.get(Calendar.MINUTE), is(0));
    assertThat(cal.get(Calendar.SECOND), is(0));
    assertThat(cal.get(Calendar.MILLISECOND), is(0));
  }

  @Test
  public void testGetEndDateOfWeekFRBetween2Years() {
    Date dateTest = DateUtil.getEndDateOfWeek(java.sql.Date.valueOf("2013-12-31"), "fr");
    Calendar cal = DateUtil.convert(dateTest);
    assertThat(cal.get(Calendar.YEAR), is(2014));
    assertThat(cal.get(Calendar.MONTH), is(Calendar.JANUARY));
    assertThat(cal.get(Calendar.DAY_OF_MONTH), is(5));
    assertThat(cal.get(Calendar.HOUR_OF_DAY), is(23));
    assertThat(cal.get(Calendar.MINUTE), is(59));
    assertThat(cal.get(Calendar.SECOND), is(59));
    assertThat(cal.get(Calendar.MILLISECOND), is(999));
  }

  @Test
  public void testGetFirstDateOfWeekEN() {
    Date dateTest = DateUtil.getFirstDateOfWeek(java.sql.Date.valueOf("2013-04-20"), "en");
    Calendar cal = DateUtil.convert(dateTest);
    assertThat(cal.get(Calendar.YEAR), is(2013));
    assertThat(cal.get(Calendar.MONTH), is(Calendar.APRIL));
    assertThat(cal.get(Calendar.DAY_OF_MONTH), is(14));
    assertThat(cal.get(Calendar.HOUR_OF_DAY), is(0));
    assertThat(cal.get(Calendar.MINUTE), is(0));
    assertThat(cal.get(Calendar.SECOND), is(0));
    assertThat(cal.get(Calendar.MILLISECOND), is(0));
  }

  @Test
  public void testGetEndDateOfWeekEN() {
    Date dateTest = DateUtil.getEndDateOfWeek(java.sql.Date.valueOf("2013-04-20"), "en");
    Calendar cal = DateUtil.convert(dateTest);
    assertThat(cal.get(Calendar.YEAR), is(2013));
    assertThat(cal.get(Calendar.MONTH), is(Calendar.APRIL));
    assertThat(cal.get(Calendar.DAY_OF_MONTH), is(20));
    assertThat(cal.get(Calendar.HOUR_OF_DAY), is(23));
    assertThat(cal.get(Calendar.MINUTE), is(59));
    assertThat(cal.get(Calendar.SECOND), is(59));
    assertThat(cal.get(Calendar.MILLISECOND), is(999));
  }

  @Test
  public void testGetBeginOfDay() {
    Date dateTest = DateUtil.getBeginOfDay(java.sql.Date.valueOf("2013-04-20"));
    Calendar cal = DateUtil.convert(dateTest);
    assertThat(cal.get(Calendar.YEAR), is(2013));
    assertThat(cal.get(Calendar.MONTH), is(Calendar.APRIL));
    assertThat(cal.get(Calendar.DAY_OF_MONTH), is(20));
    assertThat(cal.get(Calendar.HOUR_OF_DAY), is(0));
    assertThat(cal.get(Calendar.MINUTE), is(0));
    assertThat(cal.get(Calendar.SECOND), is(0));
    assertThat(cal.get(Calendar.MILLISECOND), is(0));
  }

  @Test
  public void testGetEndOfDay() {
    Date dateTest = DateUtil.getEndOfDay(java.sql.Date.valueOf("2013-04-20"));
    Calendar cal = DateUtil.convert(dateTest);
    assertThat(cal.get(Calendar.YEAR), is(2013));
    assertThat(cal.get(Calendar.MONTH), is(Calendar.APRIL));
    assertThat(cal.get(Calendar.DAY_OF_MONTH), is(20));
    assertThat(cal.get(Calendar.HOUR_OF_DAY), is(23));
    assertThat(cal.get(Calendar.MINUTE), is(59));
    assertThat(cal.get(Calendar.SECOND), is(59));
    assertThat(cal.get(Calendar.MILLISECOND), is(999));
  }
}
