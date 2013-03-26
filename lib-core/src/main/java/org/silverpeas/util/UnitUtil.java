/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.EnumSet;

import com.stratelia.webactiv.util.ResourceLocator;

/**
 * Unit values handling tools
 *
 * @author Yohann Chastagnier
 */
public class UnitUtil {

  private static final ResourceLocator utilMessages = new ResourceLocator(
      "org.silverpeas.util.multilang.util", "");

  /* Byte, Kilo-Byte, Mega-Byte, ... */
  public static enum memUnit {

    B(1, "o", "bytes"), KB(2, "ko", "Kb"), MB(3, "mo", "Mb"), GB(4, "go", "Gb"), TB(5, "to", "Tb");
    private final String bundleKey;
    private final String bundleDefault;
    private final BigDecimal limit;
    private final int power;

    private memUnit(int power, final String bundleKey, final String bundleDefault) {
      this.bundleKey = bundleKey;
      this.bundleDefault = bundleDefault;
      this.limit = byteMultiplier.pow(power);
      this.power = power;
    }

    protected String getBundleKey() {
      return bundleKey;
    }

    protected String getBundleDefault() {
      return bundleDefault;
    }

    public BigDecimal getLimit() {
      return limit;
    }

    public int getPower() {
      return power;
    }
  }
  private static BigDecimal byteMultiplier = new BigDecimal(String.valueOf(1024));

  /**
   * Converting a computer data storage value
   *
   * @param value
   * @param from
   * @param to
   * @return
   */
  public static long convertTo(final long value, final memUnit from, final memUnit to) {
    final BigDecimal decimalValue = convertTo(new BigDecimal(String.valueOf(value)), from, to);
    return decimalValue.setScale(0, BigDecimal.ROUND_HALF_UP).longValue();
  }

  /**
   * Converting a computer data storage value
   *
   * @param value
   * @param from
   * @param to
   * @return
   */
  public static BigDecimal convertTo(BigDecimal value, final memUnit from, final memUnit to) {
    final int fromPower = from.getPower();
    final int toPower = to.getPower();
    final int offsetPower = fromPower - toPower;
    if (offsetPower > 0) {
      return value.multiply(byteMultiplier.pow(Math.abs(offsetPower)));
    } else if (offsetPower < 0) {
      return value.divide(byteMultiplier.pow(Math.abs(offsetPower)));
    }
    return value;
  }

  /**
   * Format a byte memory value
   *
   * @param byteValue
   * @param to
   * @return
   */
  public static String formatValue(final long byteValue, final memUnit to) {
    return formatValue(new BigDecimal(String.valueOf(byteValue)), memUnit.B, to);
  }

  /**
   * Format a byte memory value
   *
   * @param byteValue
   * @param to
   * @return
   */
  public static String formatValue(final BigDecimal byteValue, final memUnit to) {
    return formatValue(byteValue, memUnit.B, to);
  }

  /**
   * Format a memory value
   *
   * @param value
   * @param from
   * @param to
   * @return
   */
  public static String formatValue(final long value, final memUnit from, final memUnit to) {
    return formatValue(new BigDecimal(String.valueOf(value)), from, to);
  }

  /**
   * Format a memory value
   *
   * @param value
   * @param from
   * @param to
   * @return formated value
   */
  public static String formatValue(final BigDecimal value, final memUnit from, final memUnit to) {
    final StringBuilder sb = new StringBuilder(128);
    BigDecimal convertedValue = convertTo(value, from, to);
    int nbMaximumFractionDigits = 2;
    if (EnumSet.of(memUnit.B, memUnit.KB).contains(to)) {
      nbMaximumFractionDigits = 0;
    }
    convertedValue = convertedValue.setScale(nbMaximumFractionDigits, BigDecimal.ROUND_HALF_UP);
    sb.append(new DecimalFormat().format(convertedValue));
    sb.append(" ");
    sb.append(utilMessages.getString(to.getBundleKey(), to.getBundleDefault()));
    return sb.toString();
  }

  /**
   * Get the memory size with the suitable unit
   *
   * @param memSize size in bytes
   * @return String
   */
  public static String formatMemSize(final long memSize) {
    return formatMemSize(memSize, memUnit.B);
  }

  /**
   * Get the memory size with the suitable unit
   *
   * @param memSize size in bytes
   * @return String
   */
  public static String formatMemSize(final BigDecimal memSize) {
    return formatMemSize(memSize, memUnit.B);
  }

  /**
   * Get the memory size with the suitable unit
   *
   * @param memSize size
   * @param from the unit of the given size
   * @return String
   */
  public static String formatMemSize(final long memSize, final memUnit from) {
    return formatMemSize(new BigDecimal(String.valueOf(memSize)), from);
  }

  /**
   * Get the memory size with the suitable unit
   *
   * @param memSize size
   * @param from the unit of the given size
   * @return String
   */
  public static String formatMemSize(final BigDecimal memSize, final memUnit from) {
    BigDecimal byteMemSize = convertTo(memSize, from, memUnit.B);
    memUnit to = memUnit.values()[memUnit.values().length - 1];
    for (final memUnit currentUnit : memUnit.values()) {
      if (currentUnit.getLimit().compareTo(byteMemSize) > 0) {
        to = currentUnit;
        break;
      }
    }
    return formatValue(byteMemSize, memUnit.B, to);
  }
}
