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
package org.silverpeas.core.util.time;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.silverpeas.core.util.UnitUtil;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * User: Yohann Chastagnier
 * Date: 14/11/13
 */
public class TimeData {

  private static List<TimeUnit> orderedUnits =
      new ArrayList<TimeUnit>(EnumSet.allOf(TimeUnit.class));

  private static BigDecimal ONE = new BigDecimal("1");

  // Time in milliseconds.
  private final BigDecimal time;

  /**
   * Hidden constructor
   * @param time
   */
  public TimeData(final long time) {
    this.time = new BigDecimal(String.valueOf(time));
  }

  /**
   * Hidden constructor
   * @param time
   */
  public TimeData(final BigDecimal time) {
    this.time = time;
  }

  /**
   * Converting a value
   * @param value
   * @param from
   * @param to
   * @return
   */
  public static BigDecimal convertTo(BigDecimal value, final TimeUnit from, final TimeUnit to) {
    int fromIndex = orderedUnits.indexOf(from);
    int toIndex = orderedUnits.indexOf(to);
    final int offsetIndex = fromIndex - toIndex;
    if (offsetIndex > 0) {
      return value.multiply(from.getMultiplier(to)).setScale(15, BigDecimal.ROUND_DOWN);
    } else if (offsetIndex < 0) {
      return value.divide(from.getMultiplier(to), 15, BigDecimal.ROUND_DOWN);
    }
    return value;
  }

  /**
   * Gets the time in milliseconds.
   * @return
   */
  public BigDecimal getTime() {
    return getTimeConverted(TimeUnit.MILLI);
  }

  /**
   * Gets the time in milliseconds.
   * @return
   */
  public Long getTimeAsLong() {
    return getTimeConverted(TimeUnit.MILLI).longValue();
  }

  /**
   * Gets the time converted to desired unit.
   * @param to
   * @return
   */
  public BigDecimal getRoundedTimeConverted(final TimeUnit to) {
    BigDecimal convertedSize = getTimeConverted(to);
    int nbMaximumFractionDigits = 2;
    if (TimeUnit.MILLI == to || TimeUnit.SEC == to) {
      nbMaximumFractionDigits = 3;
    }
    return convertedSize.setScale(nbMaximumFractionDigits, BigDecimal.ROUND_DOWN);
  }

  /**
   * Gets the time converted to desired unit.
   * @param to
   * @return
   */
  public BigDecimal getTimeConverted(final TimeUnit to) {
    return convertTo(time, TimeUnit.MILLI, to);
  }

  /**
   * Gets the best unit (for display as example).
   * @return
   */
  public TimeUnit getBestUnit() {
    for (int i = 1; i < orderedUnits.size(); i++) {
      final TimeUnit nextUnit = orderedUnits.get(i);
      TimeUnit currentUnit = orderedUnits.get(i - 1);
      BigDecimal nextUnitLimit = convertTo(ONE, nextUnit, TimeUnit.MILLI);
      if (time.compareTo(nextUnitLimit) < 0) {
        // The current unit of the current unit value.
        return currentUnit;
      }
    }
    // The tallest unit.
    return orderedUnits.get(orderedUnits.size() - 1);
  }

  /**
   * Gets the best unformatted value (for display as example).
   * @return
   */
  public BigDecimal getBestValue() {
    TimeUnit bestUnit = getBestUnit();
    return getRoundedTimeConverted(bestUnit);
  }


  /**
   * Gets the best display value with or without the unit label.
   * @return
   */
  private String getBestDisplayValue(boolean valueOnly) {
    return getFormattedValue(getBestUnit(), valueOnly);
  }


  /**
   * Gets the best display value
   * @return
   */
  public String getBestDisplayValueOnly() {
    return getBestDisplayValue(true);
  }

  /**
   * Gets the best display value
   * @return
   */
  public String getBestDisplayValue() {
    return getBestDisplayValue(false);
  }


  /**
   * Gets the desired value with or without the unit label.
   * @return
   */
  private String getFormattedValue(final TimeUnit to, boolean valueOnly) {
    BigDecimal bestDisplayValue = getRoundedTimeConverted(to);
    final StringBuilder sb = new StringBuilder();
    sb.append(new DecimalFormat().format(bestDisplayValue));
    if (!valueOnly) {
      sb.append(" ");
      sb.append(to.getLabel());
    }
    return sb.toString();
  }


  /**
   * Gets the desired value only
   * @return
   */
  public String getFormattedValueOnly(final TimeUnit to) {
    return getFormattedValue(to, true);
  }

  /**
   * Gets the desired value with its label
   * @return
   */
  public String getFormattedValue(final TimeUnit to) {
    return getFormattedValue(to, false);
  }

  /**
   * @see #getFormattedDuration(String)
   */
  public String getFormattedDurationAsHMSM() {
    return getFormattedDuration("HH:mm:ss.SSS");
  }

  /**
   * @see #getFormattedDuration(String)
   */
  public String getFormattedDurationAsHMS() {
    TimeData roundedTimeData = UnitUtil
        .getTimeData(getRoundedTimeConverted(TimeUnit.SEC).setScale(0, BigDecimal.ROUND_HALF_DOWN),
            TimeUnit.SEC);
    return roundedTimeData.getFormattedDuration("HH:mm:ss");
  }

  /**
   * @see org.apache.commons.lang3.time.DurationFormatUtils#formatDuration
   */
  public String getFormattedDuration(String format) {
    return DurationFormatUtils.formatDuration(getTimeAsLong(), format);
  }
}
