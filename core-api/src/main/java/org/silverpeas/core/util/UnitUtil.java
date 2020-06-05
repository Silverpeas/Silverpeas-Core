/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.util;

import org.silverpeas.core.date.TimeUnit;
import org.silverpeas.core.util.memory.MemoryData;
import org.silverpeas.core.util.memory.MemoryUnit;
import org.silverpeas.core.util.time.Duration;

import java.math.BigDecimal;

/**
 * Unit values handling tools
 *
 * @author Yohann Chastagnier
 */
public class UnitUtil {

  private UnitUtil() {
    throw new IllegalStateException("Utility class");
  }

  public static long convertTo(final long byteValue, final MemoryUnit to) {
    return getMemData(byteValue).getRoundedSizeConverted(to).longValue();
  }

  public static BigDecimal convertTo(BigDecimal byteValue, final MemoryUnit to) {
    return getMemData(byteValue).getSizeConverted(to);
  }

  public static long convertTo(final long value, final MemoryUnit from, final MemoryUnit to) {
    return getMemData(value, from).getRoundedSizeConverted(to).longValue();
  }

  public static BigDecimal convertTo(BigDecimal value, final MemoryUnit from, final MemoryUnit to) {
    return getMemData(value, from).getSizeConverted(to);
  }

  public static BigDecimal convertAndRoundTo(MemoryData memoryData, final MemoryUnit to) {
    return memoryData.getRoundedSizeConverted(to);
  }

  public static String formatValue(final long byteValue, final MemoryUnit to) {
    return getMemData(byteValue).getFormattedValue(to);
  }

  public static String formatValue(final BigDecimal byteValue, final MemoryUnit to) {
    return getMemData(byteValue).getFormattedValue(to);
  }

  public static String formatValue(final long value, final MemoryUnit from, final MemoryUnit to) {
    return getMemData(value, from).getFormattedValue(to);
  }

  public static String formatValue(final BigDecimal value, final MemoryUnit from,
      final MemoryUnit to) {
    return getMemData(value, from).getFormattedValue(to);
  }

  /**
   * Get the memory size with the suitable unit
   *
   * @param memSize size in bytes
   * @return String
   */
  public static String formatMemSize(final long memSize) {
    return getMemData(memSize).getBestDisplayValue();
  }

  /**
   * Get the memory size with the suitable unit
   *
   * @param memSize size in bytes
   * @return String
   */
  public static String formatMemSize(final BigDecimal memSize) {
    return getMemData(memSize).getBestDisplayValue();
  }

  /**
   * Get the memory size with the suitable unit
   *
   * @param memSize size
   * @param from the unit of the given size
   * @return String
   */
  public static String formatMemSize(final long memSize, final MemoryUnit from) {
    return getMemData(memSize, from).getBestDisplayValue();
  }

  /**
   * Get the memory size with the suitable unit
   *
   * @param memSize size
   * @param from the unit of the given size
   * @return String
   */
  public static String formatMemSize(final BigDecimal memSize, final MemoryUnit from) {
    return getMemData(memSize, from).getBestDisplayValue();
  }

  /**
   * Get the memory data
   * @param memSize size in bytes
   * @return MemoryData
   */
  public static MemoryData getMemData(final long memSize) {
    return getMemData(memSize, MemoryUnit.B);
  }

  /**
   * Get the memory data
   * @param memSize size in bytes
   * @return MemoryData
   */
  public static MemoryData getMemData(final BigDecimal memSize) {
    return getMemData(memSize, MemoryUnit.B);
  }

  /**
   * Get the memory data
   * @param memSize size
   * @param from the unit of the given size
   * @return MemoryData
   */
  public static MemoryData getMemData(final long memSize, final MemoryUnit from) {
    BigDecimal byteMemSize =
        MemoryData.convertTo(new BigDecimal(String.valueOf(memSize)), from, MemoryUnit.B);
    return new MemoryData(byteMemSize.setScale(0, BigDecimal.ROUND_HALF_UP).longValue());
  }

  /**
   * Get the memory data
   * @param memSize size
   * @param from the unit of the given size
   * @return MemoryData
   */
  public static MemoryData getMemData(final BigDecimal memSize, final MemoryUnit from) {
    BigDecimal byteMemSize = MemoryData.convertTo(memSize, from, MemoryUnit.B);
    return new MemoryData(byteMemSize);
  }

  public static long convertTo(final long millisecondValue, final TimeUnit to) {
    return getDuration(millisecondValue).getRoundedTimeConverted(to).longValue();
  }

  public static BigDecimal convertTo(BigDecimal millisecondValue, final TimeUnit to) {
    return getDuration(millisecondValue).getTimeConverted(to);
  }

  public static long convertTo(final long value, final TimeUnit from, final TimeUnit to) {
    return getDuration(value, from).getRoundedTimeConverted(to).longValue();
  }

  public static BigDecimal convertTo(BigDecimal value, final TimeUnit from, final TimeUnit to) {
    return getDuration(value, from).getTimeConverted(to);
  }

  public static BigDecimal convertAndRoundTo(Duration duration, final TimeUnit to) {
    return duration.getRoundedTimeConverted(to);
  }

  public static String formatValue(final long millisecondValue, final TimeUnit to) {
    return getDuration(millisecondValue).getFormattedValue(to);
  }

  public static String formatValue(final BigDecimal millisecondValue, final TimeUnit to) {
    return getDuration(millisecondValue).getFormattedValue(to);
  }

  public static String formatValue(final long value, final TimeUnit from, final TimeUnit to) {
    return getDuration(value, from).getFormattedValue(to);
  }

  public static String formatValue(final BigDecimal value, final TimeUnit from,
      final TimeUnit to) {
    return getDuration(value, from).getFormattedValue(to);
  }

  /**
   * Get the time with the suitable unit
   *
   * @param time in milliseconds
   * @return String
   */
  public static String formatTime(final long time) {
    return getDuration(time).getBestDisplayValue();
  }

  /**
   * Get the time with the suitable unit
   *
   * @param time in milliseconds
   * @return String
   */
  public static String formatTime(final BigDecimal time) {
    return getDuration(time).getBestDisplayValue();
  }

  /**
   * Get the time with the suitable unit
   *
   * @param time in milliseconds
   * @param from the unit of the given size
   * @return String
   */
  public static String formatTime(final long time, final TimeUnit from) {
    return getDuration(time, from).getBestDisplayValue();
  }

  /**
   * Get the time with the suitable unit
   *
   * @param time in milliseconds
   * @param from the unit of the given size
   * @return String
   */
  public static String formatTime(final BigDecimal time, final TimeUnit from) {
    return getDuration(time, from).getBestDisplayValue();
  }

  /**
   * Get the time data
   * @param time in milliseconds
   * @return Duration
   */
  public static Duration getDuration(final long time) {
    return getDuration(time, TimeUnit.MILLISECOND);
  }

  /**
   * Get the time data
   * @param time in milliseconds
   * @return Duration
   */
  public static Duration getDuration(final BigDecimal time) {
    return getDuration(time, TimeUnit.MILLISECOND);
  }

  /**
   * Get the time data
   * @param time in milliseconds
   * @param from the unit of the given size
   * @return Duration
   */
  public static Duration getDuration(final long time, final TimeUnit from) {
    BigDecimal millisecondTime =
        Duration.convertTo(new BigDecimal(String.valueOf(time)), from, TimeUnit.MILLISECOND);
    return new Duration(millisecondTime.setScale(0, BigDecimal.ROUND_HALF_UP).longValue());
  }

  /**
   * Get the time data
   * @param time in milliseconds
   * @param from the unit of the given size
   * @return Duration
   */
  public static Duration getDuration(final BigDecimal time, final TimeUnit from) {
    BigDecimal millisecondTime = Duration.convertTo(time, from, TimeUnit.MILLISECOND);
    return new Duration(millisecondTime);
  }
}
