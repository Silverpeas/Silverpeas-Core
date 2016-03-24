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
package org.silverpeas.core.util;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.silverpeas.core.util.time.TimeData;
import org.silverpeas.core.util.time.TimeUnit;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * User: Yohann Chastagnier
 * Date: 14/11/13
 */
public class TestTimeData extends AbstractUnitTest {

  @Test
  public void getTime() {
    TimeData timeData = createDefaultTimeData();
    assertThat(timeData.getTime(), is(getDefaultTime()));
  }

  @Test
  public void getTimeAsLong() {
    TimeData timeData = createDefaultTimeData();
    assertThat(timeData.getTimeAsLong(), is(getDefaultTime().longValue()));
  }

  @Test
  public void getFormattedDurationHMSM() {
    TimeData timeData = createDefaultTimeData();
    assertThat(timeData.getFormattedDurationAsHMSM(), is("589874017:56:04.987"));
    timeData = new TimeData(0);
    assertThat(timeData.getFormattedDurationAsHMSM(), is("00:00:00.000"));
    timeData = new TimeData(1);
    assertThat(timeData.getFormattedDurationAsHMSM(), is("00:00:00.001"));
    timeData = new TimeData(1001);
    assertThat(timeData.getFormattedDurationAsHMSM(), is("00:00:01.001"));
    timeData = new TimeData(4589001);
    assertThat(timeData.getFormattedDurationAsHMSM(), is("01:16:29.001"));
  }

  @Test
  public void getFormattedDurationHMS() {
    TimeData timeData = createDefaultTimeData();
    assertThat(timeData.getFormattedDurationAsHMS(), is("589874017:56:05"));
    timeData = new TimeData(0);
    assertThat(timeData.getFormattedDurationAsHMS(), is("00:00:00"));
    timeData = new TimeData(1);
    assertThat(timeData.getFormattedDurationAsHMS(), is("00:00:00"));
    timeData = new TimeData(1001);
    assertThat(timeData.getFormattedDurationAsHMS(), is("00:00:01"));
    timeData = new TimeData(4589001);
    assertThat(timeData.getFormattedDurationAsHMS(), is("01:16:29"));
  }

  @Test
  public void getFormattedDuration() {
    String format = "H:mm:ss.S|";
    TimeData timeData = createDefaultTimeData();
    assertThat(timeData.getFormattedDuration(format), is("589874017:56:04.987|"));
    timeData = new TimeData(0);
    assertThat(timeData.getFormattedDuration(format), is("0:00:00.000|"));
    timeData = new TimeData(1);
    assertThat(timeData.getFormattedDuration(format), is("0:00:00.001|"));
    timeData = new TimeData(1001);
    assertThat(timeData.getFormattedDuration(format), is("0:00:01.001|"));
    timeData = new TimeData(4589001);
    assertThat(timeData.getFormattedDuration(format), is("1:16:29.001|"));
  }

  @Test
  public void getRoundedTimeConverted() {
    TimeData timeData = createDefaultTimeData();
    Map<TimeUnit, String> expected = new LinkedHashMap<TimeUnit, String>();
    expected.put(TimeUnit.MILLI, getDefaultTime().toString() + ".000");
    expected.put(TimeUnit.SEC, "2123546464564.987");
    expected.put(TimeUnit.MIN, "35392441076.08");
    expected.put(TimeUnit.HOUR, "589874017.93");
    expected.put(TimeUnit.DAY, "24578084.08");
    expected.put(TimeUnit.WEEK, "3511154.86");
    expected.put(TimeUnit.MONTH, "808046.59");
    expected.put(TimeUnit.YEAR, "67337.21");
    for (Map.Entry<TimeUnit, String> entry : expected.entrySet()) {
      assertThat(entry.getKey().name(), timeData.getRoundedTimeConverted(entry.getKey()),
          is(new BigDecimal(entry.getValue())));
    }
  }

  @Test
  public void getTimeConverted() {
    TimeData timeData = createDefaultTimeData();
    Map<TimeUnit, String> expected = new LinkedHashMap<TimeUnit, String>();
    expected.put(TimeUnit.MILLI, getDefaultTime().toString());
    expected.put(TimeUnit.SEC, "2123546464564.9870000000");
    expected.put(TimeUnit.MIN, "35392441076.083116666666");
    expected.put(TimeUnit.HOUR, "589874017.93471861111111");
    expected.put(TimeUnit.DAY, "24578084.080613275462962");
    expected.put(TimeUnit.WEEK, "3511154.868659039351851");
    expected.put(TimeUnit.MONTH, "808046.599910573439878");
    expected.put(TimeUnit.YEAR, "67337.216659214453323");
    for (Map.Entry<TimeUnit, String> entry : expected.entrySet()) {
      assertThat(entry.getKey().name(),
          StringUtils.substring(timeData.getTimeConverted(entry.getKey()).toString(), 0, 24),
          is(entry.getValue()));
    }
  }

  @Test
  public void getBestUnit() {
    Map<Long, TimeUnit> expected = new LinkedHashMap<Long, TimeUnit>();
    expected.put(1L, TimeUnit.MILLI);
    expected.put(999L, TimeUnit.MILLI);
    expected.put(1000L, TimeUnit.SEC);
    expected.put(59999L, TimeUnit.SEC);
    expected.put(60000L, TimeUnit.MIN);
    expected.put(3599999L, TimeUnit.MIN);
    expected.put(3600000L, TimeUnit.HOUR);
    expected.put(86399999L, TimeUnit.HOUR);
    expected.put(86400000L, TimeUnit.DAY);
    expected.put(604799999L, TimeUnit.DAY);
    expected.put(604800000L, TimeUnit.WEEK);
    expected.put(2627999999L, TimeUnit.WEEK);
    expected.put(2628000000L, TimeUnit.MONTH);
    expected.put(31535999999L, TimeUnit.MONTH);
    expected.put(31536000000L, TimeUnit.YEAR);
    expected.put(31536000000000L, TimeUnit.YEAR);
    for (Map.Entry<Long, TimeUnit> entry : expected.entrySet()) {
      assertThat(entry.getKey().toString(), new TimeData(entry.getKey()).getBestUnit(),
          is(entry.getValue()));
    }
  }

  @Test
  public void getBestValue() {
    Map<Long, String> expected = new LinkedHashMap<Long, String>();
    expected.put(1L, "1.000");
    expected.put(999L, "999.000");
    expected.put(1000L, "1.000");
    expected.put(59999L, "59.999");
    expected.put(60000L, "1.00");
    expected.put(3599999L, "59.99");
    expected.put(3600000L, "1.00");
    expected.put(86399999L, "23.99");
    expected.put(86400000L, "1.00");
    expected.put(604799999L, "6.99");
    expected.put(604800000L, "1.00");
    expected.put(2627999999L, "4.34");
    expected.put(2628000000L, "0.99");
    expected.put(2628000001L, "1.00");
    expected.put(31535999999L, "11.99");
    expected.put(31536000000L, "1.00");
    expected.put(31536000000000L, "1000.00");
    for (Map.Entry<Long, String> entry : expected.entrySet()) {
      assertThat(entry.getKey().toString(), new TimeData(entry.getKey()).getBestValue(),
          is(new BigDecimal(entry.getValue())));
    }
  }

  @Test
  public void getBestDisplayValueOnly() {
    Map<Long, String> expected = new LinkedHashMap<Long, String>();
    expected.put(1L, "1");
    expected.put(999L, "999");
    expected.put(1000L, "1");
    expected.put(59999L, "59.999");
    expected.put(60000L, "1");
    expected.put(3599999L, "59.99");
    expected.put(3600000L, "1");
    expected.put(86399999L, "23.99");
    expected.put(86400000L, "1");
    expected.put(604799999L, "6.99");
    expected.put(604800000L, "1");
    expected.put(2627999999L, "4.34");
    expected.put(2628000000L, "0.99");
    expected.put(2628000001L, "1");
    expected.put(31535999999L, "11.99");
    expected.put(31536000000L, "1");
    expected.put(31536000000000L, "1000");
    for (Map.Entry<Long, String> entry : expected.entrySet()) {
      assertFormatValue(new TimeData(entry.getKey()).getBestDisplayValueOnly(), entry.getValue());
    }
  }

  @Test
  public void getBestDisplayValue() {
    Map<Long, String> expected = new LinkedHashMap<Long, String>();
    expected.put(1L, "1 ms");
    expected.put(999L, "999 ms");
    expected.put(1000L, "1 s");
    expected.put(59999L, "59.999 s");
    expected.put(60000L, "1 m");
    expected.put(3599999L, "59.99 m");
    expected.put(3600000L, "1 h");
    expected.put(86399999L, "23.99 h");
    expected.put(86400000L, "1 j");
    expected.put(604799999L, "6.99 j");
    expected.put(604800000L, "1 sem");
    expected.put(2627999999L, "4.34 sem");
    expected.put(2628000000L, "0.99 mois");
    expected.put(2628000001L, "1 mois");
    expected.put(31535999999L, "11.99 mois");
    expected.put(31536000000L, "1 ans");
    expected.put(31536000000000L, "1000 ans");
    for (Map.Entry<Long, String> entry : expected.entrySet()) {
      assertFormatValue(new TimeData(entry.getKey()).getBestDisplayValue(), entry.getValue());
    }
  }


  @Test
  public void getFormattedValueOnly() {
    TimeData timeData = createDefaultTimeData();
    Map<TimeUnit, String> expected = new LinkedHashMap<TimeUnit, String>();
    expected.put(TimeUnit.MILLI, getDefaultTime().toString());
    expected.put(TimeUnit.SEC, "2123546464564.987");
    expected.put(TimeUnit.MIN, "35392441076.08");
    expected.put(TimeUnit.HOUR, "589874017.93");
    expected.put(TimeUnit.DAY, "24578084.08");
    expected.put(TimeUnit.WEEK, "3511154.86");
    expected.put(TimeUnit.YEAR, "67337.21");
    for (Map.Entry<TimeUnit, String> entry : expected.entrySet()) {
      assertFormatValue(timeData.getFormattedValueOnly(entry.getKey()), entry.getValue());
    }
  }

  @Test
  public void getFormattedValue() {
    TimeData timeData = createDefaultTimeData();
    Map<TimeUnit, String> expected = new LinkedHashMap<TimeUnit, String>();
    expected.put(TimeUnit.MILLI, getDefaultTime().toString() + " ms");
    expected.put(TimeUnit.SEC, "2123546464564.987 s");
    expected.put(TimeUnit.MIN, "35392441076.08 m");
    expected.put(TimeUnit.HOUR, "589874017.93 h");
    expected.put(TimeUnit.DAY, "24578084.08 j");
    expected.put(TimeUnit.WEEK, "3511154.86 sem");
    expected.put(TimeUnit.YEAR, "67337.21 ans");
    for (Map.Entry<TimeUnit, String> entry : expected.entrySet()) {
      assertFormatValue(timeData.getFormattedValue(entry.getKey()), entry.getValue());
    }
  }

  /**
   * Centralized assert
   * @param test
   * @param expected
   */
  private void assertFormatValue(String test, String expected) {
    assertThat(test.replaceAll("[ \u00a0]", "").replace(',', '.'),
        is(expected.replaceAll(" ", "")));
  }


  /**
   * A default time data
   */
  private TimeData createDefaultTimeData() {
    return new TimeData(getDefaultTime());
  }


  /**
   * A default time
   */
  private BigDecimal getDefaultTime() {
    return new BigDecimal("2123546464564987");
  }
}