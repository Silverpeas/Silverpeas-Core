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
package org.silverpeas.core.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.notification.message.MessageManager;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.silverpeas.core.util.memory.MemoryUnit;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.silverpeas.core.util.UnitUtil.*;

@EnableSilverTestEnv
class TestUnitUtil extends AbstractUnitTest {

  @BeforeEach
  void setUpMessageManager() {
    MessageManager.initialize();
    MessageManager.setLanguage("fr");
  }

  @Test
  void testConvertToFromBigDecimal() {

    assertThat(convertTo(new BigDecimal("1"), MemoryUnit.B, MemoryUnit.B), is(
        new BigDecimal("1")));

    assertThat(convertTo(new BigDecimal("1"), MemoryUnit.KB, MemoryUnit.B), is(
        new BigDecimal("1024")));

    assertThat(
        convertTo(new BigDecimal("2"), MemoryUnit.KB, MemoryUnit.B),
        is(new BigDecimal("2048")));

    assertThat(convertTo(new BigDecimal("2.4"), MemoryUnit.KB, MemoryUnit.B),
        is(new BigDecimal("2457.6")));

    assertThat(convertTo(new BigDecimal("1"), MemoryUnit.MB, MemoryUnit.B), is(
        new BigDecimal("1048576")));

    assertThat(convertTo(new BigDecimal("10"), MemoryUnit.MB, MemoryUnit.B), is(
        new BigDecimal("10485760")));

    assertThat(convertTo(new BigDecimal("1024"), MemoryUnit.B, MemoryUnit.KB), is(
        new BigDecimal("1.0000000000000000000000000")));

    assertThat(convertTo(new BigDecimal("1048576"), MemoryUnit.B, MemoryUnit.MB), is(
        new BigDecimal("1.0000000000000000000000000")));

    assertThat(convertTo(new BigDecimal("1073741824"), MemoryUnit.B, MemoryUnit.GB), is(
        new BigDecimal("1.0000000000000000000000000")));
  }

  @Test
  void testConvertToFromLong() {
    assertThat(convertTo(1L, MemoryUnit.KB, MemoryUnit.B), is(1024L));
    assertThat(convertTo(1L, MemoryUnit.B, MemoryUnit.KB), is(0L));
    assertThat(convertTo(512L, MemoryUnit.B, MemoryUnit.KB), is(0L));
    assertThat(convertTo(513L, MemoryUnit.B, MemoryUnit.KB), is(0L));
    assertThat(convertTo(1023L, MemoryUnit.B, MemoryUnit.KB), is(0L));
    assertThat(convertTo(1024L, MemoryUnit.B, MemoryUnit.KB), is(1L));
  }

  @Test
  void testFormatValueFromBigDecimal() {
    assertFormatValue(formatValue(new BigDecimal("1"), MemoryUnit.B, MemoryUnit.B),
        "1 Octets");
    assertFormatValue(formatValue(new BigDecimal("1"), MemoryUnit.KB, MemoryUnit.B),
        "1024 Octets");
    assertFormatValue(formatValue(new BigDecimal("2"), MemoryUnit.KB, MemoryUnit.B),
        "2048 Octets");
    assertFormatValue(formatValue(new BigDecimal("2.4"), MemoryUnit.KB, MemoryUnit.B),
        "2457 Octets");
    assertFormatValue(formatValue(new BigDecimal("2.4"), MemoryUnit.GB, MemoryUnit.MB),
        "2457.6 Mo");
    assertFormatValue(formatValue(new BigDecimal("1"), MemoryUnit.MB, MemoryUnit.B),
        "1048576 Octets");
    assertFormatValue(formatValue(new BigDecimal("10"), MemoryUnit.MB, MemoryUnit.B),
        "10485760 Octets");
    assertFormatValue(formatValue(new BigDecimal("1024"), MemoryUnit.B, MemoryUnit.KB),
        "1 Ko");
    assertFormatValue(
        formatValue(new BigDecimal("1048576"), MemoryUnit.B, MemoryUnit.MB), "1 Mo");
    assertFormatValue(formatValue(new BigDecimal("1073741824"), MemoryUnit.B,
        MemoryUnit.GB), "1 Go");
  }

  @Test
  void testFormatValueFromLong() {
    assertFormatValue(formatValue(1L, MemoryUnit.KB, MemoryUnit.B), "1024 Octets");
    assertFormatValue(formatValue(1L, MemoryUnit.B, MemoryUnit.KB), "0 Ko");
    assertFormatValue(formatValue(512L, MemoryUnit.B, MemoryUnit.KB), "0 Ko");
    assertFormatValue(formatValue(513L, MemoryUnit.B, MemoryUnit.KB), "0 Ko");
    assertFormatValue(formatValue(1023L, MemoryUnit.B, MemoryUnit.KB), "0 Ko");
    assertFormatValue(formatValue(1024L, MemoryUnit.B, MemoryUnit.KB), "1 Ko");
  }

  @Test
  void testFormatValueFromBigDecimalAroundLimits() {
    assertFormatValue(formatValue(new BigDecimal("1024"), MemoryUnit.KB), "1 Ko");
    assertFormatValue(formatValue(new BigDecimal("1048576"), MemoryUnit.MB), "1 Mo");
    assertFormatValue(formatValue(new BigDecimal("1073741824"), MemoryUnit.GB), "1 Go");
  }

  @Test
  void testFormatValueFromLongAroundLimits() {
    assertFormatValue(formatValue(513L, MemoryUnit.KB), "0 Ko");
    assertFormatValue(formatValue(1023L, MemoryUnit.KB), "0 Ko");
    assertFormatValue(formatValue(1024L, MemoryUnit.KB), "1 Ko");
  }

  @Test
  void testFormatMemSize() {
    assertFormatValue(formatMemSize(new BigDecimal("1"), MemoryUnit.B), "1 Octets");
    assertFormatValue(formatMemSize(new BigDecimal("1"), MemoryUnit.KB), "1 Ko");
    assertFormatValue(formatMemSize(new BigDecimal("2"), MemoryUnit.KB), "2 Ko");
    assertFormatValue(formatMemSize(new BigDecimal("2.4"), MemoryUnit.KB), "2 Ko");
    assertFormatValue(formatMemSize(new BigDecimal("2.4"), MemoryUnit.GB), "2.4 Go");
    assertFormatValue(formatMemSize(new BigDecimal("1"), MemoryUnit.MB), "1 Mo");
    assertFormatValue(formatMemSize(new BigDecimal("10"), MemoryUnit.MB), "10 Mo");
    assertFormatValue(formatMemSize(new BigDecimal("1024"), MemoryUnit.B), "1 Ko");
    assertFormatValue(formatMemSize(new BigDecimal("1048576"), MemoryUnit.B), "1 Mo");
    assertFormatValue(formatMemSize(new BigDecimal("1073741824"), MemoryUnit.B), "1 Go");
    assertFormatValue(formatMemSize(new BigDecimal("1024"), MemoryUnit.GB), "1 To");
    assertFormatValue(formatMemSize(new BigDecimal("1023"), MemoryUnit.GB), "1023 Go");
  }

  private void assertFormatValue(String test, String expected) {
    assertThat(test.replaceAll("[ \u00a0]", "").replace(',', '.'), is(expected.replaceAll(" ", "")));
  }
}
