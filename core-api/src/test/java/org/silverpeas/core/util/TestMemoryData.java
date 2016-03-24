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
import org.silverpeas.core.util.memory.MemoryData;
import org.silverpeas.core.util.memory.MemoryUnit;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * User: Yohann Chastagnier
 * Date: 14/11/13
 */
public class TestMemoryData extends AbstractUnitTest {

  @Test
  public void getSize() {
    MemoryData memoryData = createDefaultMemoryData();
    assertThat(memoryData.getSize(), is(getDefaultMemorySize()));
  }

  @Test
  public void getRoundedSizeConverted() {
    MemoryData memoryData = createDefaultMemoryData();
    Map<MemoryUnit, String> expected = new LinkedHashMap<MemoryUnit, String>();
    expected.put(MemoryUnit.B, getDefaultMemorySize().toString());
    expected.put(MemoryUnit.KB, "2073775844301");
    expected.put(MemoryUnit.MB, "2025171722.95");
    expected.put(MemoryUnit.GB, "1977706.76");
    expected.put(MemoryUnit.TB, "1931.35");
    for (Map.Entry<MemoryUnit, String> entry : expected.entrySet()) {
      assertThat(entry.getKey().name(), memoryData.getRoundedSizeConverted(entry.getKey()),
          is(new BigDecimal(entry.getValue())));
    }
  }

  @Test
  public void getSizeConverted() {
    MemoryData memoryData = createDefaultMemoryData();
    Map<MemoryUnit, String> expected = new LinkedHashMap<MemoryUnit, String>();
    expected.put(MemoryUnit.B, getDefaultMemorySize().toString());
    expected.put(MemoryUnit.KB, "2073775844301.7451171875");
    expected.put(MemoryUnit.MB, "2025171722.9509229660034");
    expected.put(MemoryUnit.GB, "1977706.7606942607089877");
    expected.put(MemoryUnit.TB, "1931.3542584904889736208");
    for (Map.Entry<MemoryUnit, String> entry : expected.entrySet()) {
      assertThat(entry.getKey().name(),
          StringUtils.substring(memoryData.getSizeConverted(entry.getKey()).toString(), 0, 24),
          is(entry.getValue()));
    }
  }

  @Test
  public void getBestUnit() {
    Map<Long, MemoryUnit> expected = new LinkedHashMap<Long, MemoryUnit>();
    expected.put(1L, MemoryUnit.B);
    expected.put(1023L, MemoryUnit.B);
    expected.put(1024L, MemoryUnit.KB);
    expected.put(2048L, MemoryUnit.KB);
    expected.put(1048575L, MemoryUnit.KB);
    expected.put(1048576L, MemoryUnit.MB);
    expected.put(1073741823L, MemoryUnit.MB);
    expected.put(1073741824L, MemoryUnit.GB);
    expected.put(1099511627775L, MemoryUnit.GB);
    expected.put(1099511627776L, MemoryUnit.TB);
    expected.put(1125899906842623L, MemoryUnit.TB);
    expected.put(1125899906842624L, MemoryUnit.TB);
    for (Map.Entry<Long, MemoryUnit> entry : expected.entrySet()) {
      assertThat(entry.getKey().toString(), new MemoryData(entry.getKey()).getBestUnit(),
          is(entry.getValue()));
    }
  }

  @Test
  public void getBestValue() {
    Map<Long, String> expected = new LinkedHashMap<Long, String>();
    expected.put(1L, "1");
    expected.put(1023L, "1023");
    expected.put(1024L, "1");
    expected.put(2048L, "2");
    expected.put(1048575L, "1023");
    expected.put(1048576L, "1.00");
    expected.put(1073741823L, "1023.99");
    expected.put(1073741824L, "1.00");
    expected.put(1099511627775L, "1023.99");
    expected.put(1099511627776L, "1.00");
    expected.put(1125899906842623L, "1023.99");
    expected.put(1125899906842624L, "1024.00");
    for (Map.Entry<Long, String> entry : expected.entrySet()) {
      assertThat(entry.getKey().toString(), new MemoryData(entry.getKey()).getBestValue(),
          is(new BigDecimal(entry.getValue())));
    }
  }

  @Test
  public void getBestDisplayValueOnly() {
    Map<Long, String> expected = new LinkedHashMap<Long, String>();
    expected.put(1L, "1");
    expected.put(1023L, "1023");
    expected.put(1024L, "1");
    expected.put(2048L, "2");
    expected.put(1048575L, "1023");
    expected.put(1048576L, "1");
    expected.put(1073741823L, "1023.99");
    expected.put(1073741824L, "1");
    expected.put(1099511627775L, "1023.99");
    expected.put(1099511627776L, "1");
    expected.put(1125899906842623L, "1023.99");
    expected.put(1125899906842624L, "1024");
    for (Map.Entry<Long, String> entry : expected.entrySet()) {
      assertFormatValue(new MemoryData(entry.getKey()).getBestDisplayValueOnly(), entry.getValue());
    }
  }

  @Test
  public void getBestDisplayValue() {
    Map<Long, String> expected = new LinkedHashMap<Long, String>();
    expected.put(1L, "1 Octets");
    expected.put(1023L, "1023 Octets");
    expected.put(1024L, "1 Ko");
    expected.put(2048L, "2 Ko");
    expected.put(1048575L, "1023 Ko");
    expected.put(1048576L, "1 Mo");
    expected.put(1073741823L, "1023.99 Mo");
    expected.put(1073741824L, "1 Go");
    expected.put(1099511627775L, "1023.99 Go");
    expected.put(1099511627776L, "1 To");
    expected.put(1125899906842623L, "1023.99 To");
    expected.put(1125899906842624L, "1024 To");
    for (Map.Entry<Long, String> entry : expected.entrySet()) {
      assertFormatValue(new MemoryData(entry.getKey()).getBestDisplayValue(), entry.getValue());
    }
  }


  @Test
  public void getFormattedValueOnly() {
    MemoryData memoryData = createDefaultMemoryData();
    Map<MemoryUnit, String> expected = new LinkedHashMap<MemoryUnit, String>();
    expected.put(MemoryUnit.B, getDefaultMemorySize().toString());
    expected.put(MemoryUnit.KB, "2073775844301");
    expected.put(MemoryUnit.MB, "2025171722.95");
    expected.put(MemoryUnit.GB, "1977706.76");
    expected.put(MemoryUnit.TB, "1931.35");
    for (Map.Entry<MemoryUnit, String> entry : expected.entrySet()) {
      assertFormatValue(memoryData.getFormattedValueOnly(entry.getKey()), entry.getValue());
    }
  }

  @Test
  public void getFormattedValue() {
    MemoryData memoryData = createDefaultMemoryData();
    Map<MemoryUnit, String> expected = new LinkedHashMap<MemoryUnit, String>();
    expected.put(MemoryUnit.B, getDefaultMemorySize().toString() + " Octets");
    expected.put(MemoryUnit.KB, "2073775844301 Ko");
    expected.put(MemoryUnit.MB, "2025171722.95 Mo");
    expected.put(MemoryUnit.GB, "1977706.76 Go");
    expected.put(MemoryUnit.TB, "1931.35 To");
    for (Map.Entry<MemoryUnit, String> entry : expected.entrySet()) {
      assertFormatValue(memoryData.getFormattedValue(entry.getKey()), entry.getValue());
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
   * A default memory data
   */
  private MemoryData createDefaultMemoryData() {
    return new MemoryData(getDefaultMemorySize());
  }


  /**
   * A default memory size
   */
  private BigDecimal getDefaultMemorySize() {
    return new BigDecimal("2123546464564987");
  }
}