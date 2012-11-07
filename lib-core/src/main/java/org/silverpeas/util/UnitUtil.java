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

import static org.apache.commons.lang3.StringUtils.capitalize;

import java.math.BigDecimal;

import com.stratelia.webactiv.util.ResourceLocator;

/**
 * Unit values handling tools
 * @author Yohann Chastagnier
 */
public class UnitUtil {

  private static final ResourceLocator utilMessages = new ResourceLocator(
      "org.silverpeas.util.multilang.util", "");

  /* Byte, Kilo-Byte, Mega-Byte, ... */
  public static enum memUnit {
    B("o"), KB("ko"), MB("mo"), GB("go"), TB("to");

    private final String bundleKey;

    private memUnit(final String bundleKey) {
      this.bundleKey = bundleKey;
    }

    protected String getBundleKey() {
      return bundleKey;
    }
  };

  private static BigDecimal byteMultiplier = new BigDecimal(String.valueOf(1024));

  /**
   * Converting a computer data storage value
   * @param value
   * @param from
   * @param to
   * @return
   */
  public static long convertTo(final long value, final memUnit from, final memUnit to) {
    final BigDecimal decimalValue = convertTo(new BigDecimal(String.valueOf(value)), from, to);
    return decimalValue.setScale(0, BigDecimal.ROUND_HALF_DOWN).longValue();
  }

  /**
   * Converting a computer data storage value
   * @param value
   * @param from
   * @param to
   * @return
   */
  public static BigDecimal convertTo(BigDecimal value, final memUnit from, final memUnit to) {
    final int fromIndex = indexOf(from);
    final int toIndex = indexOf(to);
    final int inc = (fromIndex <= toIndex) ? 1 : -1;
    for (int i = fromIndex; i != toIndex; i += inc) {
      if (inc < 0) {
        value = value.multiply(byteMultiplier);
      } else {
        value = value.divide(byteMultiplier);
      }
    }
    return value;
  }

  /**
   * Centralizes enum index search
   * @param unit
   * @return
   */
  private static int indexOf(final memUnit unit) {
    int i = 0;
    for (final memUnit currentUnit : memUnit.values()) {
      if (currentUnit.equals(unit)) {
        break;
      }
      i++;
    }
    return i;
  }

  /**
   * Format a byte memory value
   * @param byteValue
   * @param to
   * @return
   */
  public static String formatValue(final long byteValue, final memUnit to) {
    return formatValue(new BigDecimal(String.valueOf(convertTo(byteValue, memUnit.B, to))), to, to);
  }

  /**
   * Format a byte memory value
   * @param byteValue
   * @param to
   * @return
   */
  public static String formatValue(final BigDecimal byteValue, final memUnit to) {
    final StringBuilder sb = new StringBuilder();
    sb.append(convertTo(byteValue, memUnit.B, to));
    sb.append(" ");
    sb.append(utilMessages.getString(to.getBundleKey(), capitalize(to.name().toLowerCase())));
    return sb.toString();
  }

  /**
   * Format a memory value
   * @param value
   * @param from
   * @param to
   * @return
   */
  public static String formatValue(final long value, final memUnit from, final memUnit to) {
    return formatValue(new BigDecimal(String.valueOf(convertTo(value, from, to))), to, to);
  }

  /**
   * Format a memory value
   * @param value
   * @param from
   * @param to
   * @return
   */
  public static String formatValue(final BigDecimal value, final memUnit from, final memUnit to) {
    final StringBuilder sb = new StringBuilder();
    sb.append(convertTo(value, from, to));
    sb.append(" ");
    sb.append(utilMessages.getString(to.getBundleKey(), capitalize(to.name().toLowerCase())));
    return sb.toString();
  }
}
