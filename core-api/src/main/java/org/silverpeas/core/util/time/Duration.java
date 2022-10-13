/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.util.time;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.silverpeas.core.date.TimeUnit;
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
public class Duration {

  private static final List<TimeUnit> orderedUnits = new ArrayList<>(EnumSet.allOf(TimeUnit.class));

  private static final BigDecimal ONE = new BigDecimal("1");

  // Time in milliseconds.
  private final BigDecimal time;

  public Duration(final long time) {
    this.time = new BigDecimal(String.valueOf(time));
  }

  public Duration(final BigDecimal time) {
    this.time = time;
  }

  /**
   * Converts a {@link TimeUnit} into a {@link DurationUnit}.
   * @param timeUnit a {@link TimeUnit}.
   * @return a {@link DurationUnit}.
   */
  private static DurationUnit d(final TimeUnit timeUnit) {
    return DurationUnit.from(timeUnit);
  }

  public static BigDecimal convertTo(BigDecimal value, final TimeUnit from, final TimeUnit to) {
    int fromIndex = orderedUnits.indexOf(from);
    int toIndex = orderedUnits.indexOf(to);
    final int offsetIndex = fromIndex - toIndex;
    if (offsetIndex > 0) {
      return value.multiply(d(from).getMultiplier(d(to))).setScale(15, BigDecimal.ROUND_DOWN);
    } else if (offsetIndex < 0) {
      return value.divide(d(from).getMultiplier(d(to)), 15, BigDecimal.ROUND_DOWN);
    }
    return value;
  }

  /**
   * Gets the time in milliseconds.
   * @return the time in milliseconds
   */
  public BigDecimal getTime() {
    return getTimeConverted(TimeUnit.MILLISECOND);
  }

  /**
   * Gets the time in milliseconds.
   * @return the time in milliseconds.
   */
  public Long getTimeAsLong() {
    return getTimeConverted(TimeUnit.MILLISECOND).longValue();
  }

  /**
   * Gets the time converted to desired unit.
   * @param to a time unit
   * @return the time converted to desired unit.
   */
  public BigDecimal getRoundedTimeConverted(final TimeUnit to) {
    BigDecimal convertedSize = getTimeConverted(to);
    int nbMaximumFractionDigits = 2;
    if (TimeUnit.MILLISECOND == to || TimeUnit.SECOND == to) {
      nbMaximumFractionDigits = 3;
    }
    return convertedSize.setScale(nbMaximumFractionDigits, BigDecimal.ROUND_DOWN);
  }

  /**
   * Gets the time converted to desired unit.
   * @param to a time unit
   * @return the time converted to desired unit.
   */
  public BigDecimal getTimeConverted(final TimeUnit to) {
    return convertTo(time, TimeUnit.MILLISECOND, to);
  }

  /**
   * Gets the best unit (for display as example).
   * @return the best unit
   */
  public TimeUnit getBestUnit() {
    for (int i = 1; i < orderedUnits.size(); i++) {
      final TimeUnit nextUnit = orderedUnits.get(i);
      TimeUnit currentUnit = orderedUnits.get(i - 1);
      BigDecimal nextUnitLimit = convertTo(ONE, nextUnit, TimeUnit.MILLISECOND);
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
   * @return the best unformatted value
   */
  public BigDecimal getBestValue() {
    TimeUnit bestUnit = getBestUnit();
    return getRoundedTimeConverted(bestUnit);
  }

  /**
   * Gets the best display value with or without the unit label.
   * @return the best display value
   */
  private String getBestDisplayValue(boolean valueOnly) {
    return getFormattedValue(getBestUnit(), valueOnly);
  }

  /**
   * Gets the best display value
   * @return the best display value
   */
  public String getBestDisplayValueOnly() {
    return getBestDisplayValue(true);
  }

  /**
   * Gets the best display value
   * @return the best display value
   */
  public String getBestDisplayValue() {
    return getBestDisplayValue(false);
  }

  private String getFormattedValue(final TimeUnit to, boolean valueOnly) {
    BigDecimal bestDisplayValue = getRoundedTimeConverted(to);
    final StringBuilder sb = new StringBuilder();
    sb.append(new DecimalFormat().format(bestDisplayValue));
    if (!valueOnly) {
      sb.append(" ");
      sb.append(d(to).getLabel());
    }
    return sb.toString();
  }

  public String getFormattedValueOnly(final TimeUnit to) {
    return getFormattedValue(to, true);
  }

  public String getFormattedValue(final TimeUnit to) {
    return getFormattedValue(to, false);
  }

  public String getFormattedDurationAsHMSM() {
    return getFormattedDuration("HH:mm:ss.SSS");
  }

  public String getFormattedDurationAsHMS() {
    Duration roundedDuration = UnitUtil
        .getDuration(getRoundedTimeConverted(TimeUnit.SECOND).setScale(0, BigDecimal.ROUND_HALF_DOWN),
            TimeUnit.SECOND);
    return roundedDuration.getFormattedDuration("HH:mm:ss");
  }

  public String getFormattedDuration(String format) {
    return DurationFormatUtils.formatDuration(getTimeAsLong(), format);
  }
}
