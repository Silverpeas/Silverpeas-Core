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
 * "http://www.silverpeas.org/legal/licensing"

 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.util;

import java.util.Calendar;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 *
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
}