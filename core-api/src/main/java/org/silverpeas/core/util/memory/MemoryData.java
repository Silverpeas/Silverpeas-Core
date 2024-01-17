/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.util.memory;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.EnumSet;

/**
 * User: Yohann Chastagnier
 * Date: 14/11/13
 */
public class MemoryData {

  private final BigDecimal byteSize;

  public MemoryData(final long byteSize) {
    this.byteSize = new BigDecimal(String.valueOf(byteSize));
  }

  public MemoryData(final BigDecimal byteSize) {
    this.byteSize = byteSize;
  }

  public static BigDecimal convertTo(BigDecimal value, final MemoryUnit from, final MemoryUnit to) {
    final int fromPower = from.getPower();
    final int toPower = to.getPower();
    final int offsetPower = fromPower - toPower;
    if (offsetPower > 0) {
      return value.multiply(MemoryUnit.byteMultiplier.pow(Math.abs(offsetPower)));
    } else if (offsetPower < 0) {
      return value
          .divide(MemoryUnit.byteMultiplier.pow(Math.abs(offsetPower)), 25, BigDecimal.ROUND_DOWN);
    }
    return value;
  }

  public BigDecimal getSize() {
    return getSizeConverted(MemoryUnit.B);
  }

  /**
   * Gets the size in bytes.
   * @return a long value.
   */
  public Long getSizeAsLong() {
    return getSize().longValue();
  }

  public BigDecimal getRoundedSizeConverted(final MemoryUnit to) {
    BigDecimal convertedSize = getSizeConverted(to);
    int nbMaximumFractionDigits = 2;
    if (EnumSet.of(MemoryUnit.B, MemoryUnit.KB).contains(to)) {
      nbMaximumFractionDigits = 0;
    }
    return convertedSize.setScale(nbMaximumFractionDigits, BigDecimal.ROUND_DOWN);
  }

  public BigDecimal getSizeConverted(final MemoryUnit to) {
    return convertTo(byteSize, MemoryUnit.B, to);
  }

  public MemoryUnit getBestUnit() {
    MemoryUnit to = MemoryUnit.values()[MemoryUnit.values().length - 1];
    for (final MemoryUnit currentUnit : MemoryUnit.values()) {
      if (currentUnit.getLimit().compareTo(byteSize) > 0) {
        to = currentUnit;
        break;
      }
    }
    return to;
  }

  public BigDecimal getBestValue() {
    MemoryUnit bestUnit = getBestUnit();
    return getRoundedSizeConverted(bestUnit);
  }


  private String getBestDisplayValue(boolean valueOnly) {
    return getFormattedValue(getBestUnit(), valueOnly);
  }


  public String getBestDisplayValueOnly() {
    return getBestDisplayValue(true);
  }

  public String getBestDisplayValue() {
    return getBestDisplayValue(false);
  }


  private String getFormattedValue(final MemoryUnit to, boolean valueOnly) {
    BigDecimal bestDisplayValue = getRoundedSizeConverted(to);
    final StringBuilder sb = new StringBuilder(128);
    sb.append(new DecimalFormat().format(bestDisplayValue));
    if (!valueOnly) {
      sb.append(" ");
      sb.append(to.getLabel());
    }
    return sb.toString();
  }


  public String getFormattedValueOnly(final MemoryUnit to) {
    return getFormattedValue(to, true);
  }

  public String getFormattedValue(final MemoryUnit to) {
    return getFormattedValue(to, false);
  }
}
