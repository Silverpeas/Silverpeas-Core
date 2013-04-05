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
import java.util.Locale;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.silverpeas.util.UnitUtil.*;

public class UnitUtilTest {

  private static Locale currentLocale;

  @BeforeClass
  public static void forceDefaultLocale() {
    currentLocale = Locale.getDefault();
    Locale.setDefault(Locale.FRANCE);
  }

  @AfterClass
  public static void restoreDefaultLocale() {
    Locale.setDefault(currentLocale);
  }

  @Test
  public void testConvertToFromBigDecimal() {

    assertThat(convertTo(new BigDecimal("1"), UnitUtil.memUnit.B, UnitUtil.memUnit.B), is(
        new BigDecimal("1")));

    assertThat(convertTo(new BigDecimal("1"), UnitUtil.memUnit.KB, UnitUtil.memUnit.B), is(
        new BigDecimal("1024")));

    assertThat(
        convertTo(new BigDecimal("2"), UnitUtil.memUnit.KB, UnitUtil.memUnit.B),
        is(new BigDecimal("2048")));

    assertThat(convertTo(new BigDecimal("2.4"), UnitUtil.memUnit.KB, UnitUtil.memUnit.B),
        is(new BigDecimal("2457.6")));

    assertThat(convertTo(new BigDecimal("1"), UnitUtil.memUnit.MB, UnitUtil.memUnit.B), is(
        new BigDecimal("1048576")));

    assertThat(convertTo(new BigDecimal("10"), UnitUtil.memUnit.MB, UnitUtil.memUnit.B), is(
        new BigDecimal("10485760")));

    assertThat(convertTo(new BigDecimal("1024"), UnitUtil.memUnit.B, UnitUtil.memUnit.KB), is(
        new BigDecimal("1")));

    assertThat(convertTo(new BigDecimal("1048576"), UnitUtil.memUnit.B, UnitUtil.memUnit.MB), is(
        new BigDecimal("1")));

    assertThat(convertTo(new BigDecimal("1073741824"), UnitUtil.memUnit.B, UnitUtil.memUnit.GB), is(
        new BigDecimal("1")));
  }

  @Test
  public void testConvertToFromLong() {
    assertThat(convertTo(1L, UnitUtil.memUnit.KB, UnitUtil.memUnit.B), is(1024L));
    assertThat(convertTo(1L, UnitUtil.memUnit.B, UnitUtil.memUnit.KB), is(0L));
    assertThat(convertTo(512L, UnitUtil.memUnit.B, UnitUtil.memUnit.KB), is(1L));
    assertThat(convertTo(513L, UnitUtil.memUnit.B, UnitUtil.memUnit.KB), is(1L));
  }

  @Test
  public void testFormatValueFromBigDecimal() {
    assertFormatValue(formatValue(new BigDecimal("1"), UnitUtil.memUnit.B, UnitUtil.memUnit.B),
        "1 Octets");
    assertFormatValue(formatValue(new BigDecimal("1"), UnitUtil.memUnit.KB, UnitUtil.memUnit.B),
        "1024 Octets");
    assertFormatValue(formatValue(new BigDecimal("2"), UnitUtil.memUnit.KB, UnitUtil.memUnit.B),
        "2048 Octets");
    assertFormatValue(formatValue(new BigDecimal("2.4"), UnitUtil.memUnit.KB, UnitUtil.memUnit.B),
        "2458 Octets");
    assertFormatValue(formatValue(new BigDecimal("2.4"), UnitUtil.memUnit.GB, UnitUtil.memUnit.MB),
        "2457.6 Mo");
    assertFormatValue(formatValue(new BigDecimal("1"), UnitUtil.memUnit.MB, UnitUtil.memUnit.B),
        "1048576 Octets");
    assertFormatValue(formatValue(new BigDecimal("10"), UnitUtil.memUnit.MB, UnitUtil.memUnit.B),
        "10485760 Octets");
    assertFormatValue(formatValue(new BigDecimal("1024"), UnitUtil.memUnit.B, UnitUtil.memUnit.KB),
        "1 Ko");
    assertFormatValue(
        formatValue(new BigDecimal("1048576"), UnitUtil.memUnit.B, UnitUtil.memUnit.MB), "1 Mo");
    assertFormatValue(formatValue(new BigDecimal("1073741824"), UnitUtil.memUnit.B,
        UnitUtil.memUnit.GB), "1 Gb");
  }

  @Test
  public void testFormatValueFromLong() {
    assertFormatValue(formatValue(1L, UnitUtil.memUnit.KB, UnitUtil.memUnit.B), "1024 Octets");
    assertFormatValue(formatValue(1L, UnitUtil.memUnit.B, UnitUtil.memUnit.KB), "0 Ko");
    assertFormatValue(formatValue(512L, UnitUtil.memUnit.B, UnitUtil.memUnit.KB), "1 Ko");
    assertFormatValue(formatValue(513L, UnitUtil.memUnit.B, UnitUtil.memUnit.KB), "1 Ko");
  }

  @Test
  public void testFormatValueFromBigDecimalAroundLimits() {
    assertFormatValue(formatValue(new BigDecimal("1024"), UnitUtil.memUnit.KB), "1 Ko");
    assertFormatValue(formatValue(new BigDecimal("1048576"), UnitUtil.memUnit.MB), "1 Mo");
    assertFormatValue(formatValue(new BigDecimal("1073741824"), UnitUtil.memUnit.GB), "1 Gb");
  }

  @Test
  public void testFormatValueFromLongAroundLimits() {
    assertFormatValue(formatValue(513L, UnitUtil.memUnit.KB), "1 Ko");
  }

  @Test
  public void testFormatMemSize() {
    assertFormatValue(formatMemSize(new BigDecimal("1"), UnitUtil.memUnit.B), "1 Octets");
    assertFormatValue(formatMemSize(new BigDecimal("1"), UnitUtil.memUnit.KB), "1 Ko");
    assertFormatValue(formatMemSize(new BigDecimal("2"), UnitUtil.memUnit.KB), "2 Ko");
    assertFormatValue(formatMemSize(new BigDecimal("2.4"), UnitUtil.memUnit.KB), "2 Ko");
    assertFormatValue(formatMemSize(new BigDecimal("2.4"), UnitUtil.memUnit.GB), "2.4 Gb");
    assertFormatValue(formatMemSize(new BigDecimal("1"), UnitUtil.memUnit.MB), "1 Mo");
    assertFormatValue(formatMemSize(new BigDecimal("10"), UnitUtil.memUnit.MB), "10 Mo");
    assertFormatValue(formatMemSize(new BigDecimal("1024"), UnitUtil.memUnit.B), "1 Ko");
    assertFormatValue(formatMemSize(new BigDecimal("1048576"), UnitUtil.memUnit.B), "1 Mo");
    assertFormatValue(formatMemSize(new BigDecimal("1073741824"), UnitUtil.memUnit.B), "1 Gb");
    assertFormatValue(formatMemSize(new BigDecimal("1024"), UnitUtil.memUnit.GB), "1 Tb");
    assertFormatValue(formatMemSize(new BigDecimal("1023"), UnitUtil.memUnit.GB), "1023 Gb");
  }

  /**
   * Centralized assert
   *
   * @param test
   * @param expected
   */
  private void assertFormatValue(String test, String expected) {
    assertThat(test.replaceAll("[ \u00a0]", "").replace(',', '.'), is(expected.replaceAll(" ", "")));
  }
}
