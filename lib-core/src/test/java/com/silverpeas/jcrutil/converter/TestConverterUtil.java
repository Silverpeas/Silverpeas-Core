/**
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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.silverpeas.jcrutil.converter;

import junit.framework.TestCase;

import java.text.ParseException;
import java.util.Calendar;


public class TestConverterUtil extends TestCase {

  public TestConverterUtil(String name) {
    super(name);
  }

  public void testConvertToJcrPath() {
    assertEquals("/", ConverterUtil.convertToJcrPath("/"));
    assertEquals("/toto", ConverterUtil.convertToJcrPath("/toto"));
    assertEquals("/theme__test", ConverterUtil.convertToJcrPath("/theme test"));
    assertEquals("/theme__test/sous__theme__test", ConverterUtil.convertToJcrPath(
        "/theme test/sous theme test"));
    assertEquals("/theme__test%7C", ConverterUtil.convertToJcrPath("/theme test|"));
    assertEquals("/theme__test%09", ConverterUtil.convertToJcrPath("/theme test\t"));
    assertEquals("/theme__test%0D", ConverterUtil.convertToJcrPath("/theme test\r"));
    assertEquals("/theme__test%0A", ConverterUtil.convertToJcrPath("/theme test\n"));
    assertEquals("t%2E", ConverterUtil.convertToJcrPath("t."));
    assertEquals("theme.", ConverterUtil.convertToJcrPath("theme."));
    assertEquals("/theme%2A", ConverterUtil.convertToJcrPath("/theme*"));
    assertEquals("/theme__%5Btest%5D", ConverterUtil.convertToJcrPath("/theme [test]"));
    assertEquals("/theme\"", ConverterUtil.convertToJcrPath("/theme\""));
    assertEquals("/theme'", ConverterUtil.convertToJcrPath("/theme'"));
    assertEquals("/theme__20%25", ConverterUtil.convertToJcrPath("/theme 20%"));
    assertEquals("/theme%3Atest", ConverterUtil.convertToJcrPath("/theme:test"));
  }

  public void testConvertFromJcrPath() {
    assertEquals("/", ConverterUtil.convertFromJcrPath("/"));
    assertEquals("/toto", ConverterUtil.convertFromJcrPath("/toto"));
    assertEquals("/theme test", ConverterUtil.convertFromJcrPath("/theme__test"));
    assertEquals("/theme test/sous theme test", ConverterUtil.convertFromJcrPath(
        "/theme__test/sous__theme__test"));
    assertEquals("/theme test|", ConverterUtil.convertFromJcrPath("/theme__test%7C"));
    assertEquals("/theme test\t", ConverterUtil.convertFromJcrPath("/theme__test%09"));
    assertEquals("/theme test\r", ConverterUtil.convertFromJcrPath("/theme__test%0D"));
    assertEquals("/theme test\n", ConverterUtil.convertFromJcrPath("/theme__test%0A"));
    assertEquals("t.", ConverterUtil.convertFromJcrPath("t%2E"));
    assertEquals("theme.", ConverterUtil.convertFromJcrPath("theme."));
    assertEquals("/theme*", ConverterUtil.convertFromJcrPath("/theme%2A"));
    assertEquals("/theme [test]", ConverterUtil.convertFromJcrPath("/theme__%5Btest%5D"));
    assertEquals("/theme\"", ConverterUtil.convertFromJcrPath("/theme%22"));
    assertEquals("/theme'", ConverterUtil.convertFromJcrPath("/theme%27"));
    assertEquals("/theme 20%", ConverterUtil.convertFromJcrPath("/theme__20%25"));
    assertEquals("/theme:test", ConverterUtil.convertFromJcrPath("/theme%3Atest"));
  }

  public void testParseDate() throws Exception {
    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.HOUR_OF_DAY, 0);
    calend.set(Calendar.MINUTE, 0);
    calend.set(Calendar.SECOND, 0);
    calend.set(Calendar.MILLISECOND, 0);
    calend.set(Calendar.DAY_OF_MONTH, 17);
    calend.set(Calendar.MONTH, Calendar.FEBRUARY);
    calend.set(Calendar.YEAR, 1986);
    assertEquals(calend.getTime(), ConverterUtil.parseDate("1986/02/17"));
    try {
      ConverterUtil.parseDate("1986/15/17");
      fail();
    } catch (ParseException pex) {

    }
  }

  public void testFormatDateDate() {
    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.HOUR_OF_DAY, 0);
    calend.set(Calendar.MINUTE, 0);
    calend.set(Calendar.SECOND, 0);
    calend.set(Calendar.MILLISECOND, 0);
    calend.set(Calendar.DAY_OF_MONTH, 17);
    calend.set(Calendar.MONTH, Calendar.FEBRUARY);
    calend.set(Calendar.YEAR, 1986);
    assertEquals("1986/02/17", ConverterUtil.formatDate(calend.getTime()));
    calend.set(Calendar.MINUTE, 10);
    assertEquals("1986/02/17", ConverterUtil.formatDate(calend.getTime()));
    calend.set(Calendar.MONTH, Calendar.DECEMBER);
    assertEquals("1986/12/17", ConverterUtil.formatDate(calend.getTime()));
  }

  public void testFormatDateCalendar() {
    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.HOUR_OF_DAY, 0);
    calend.set(Calendar.MINUTE, 0);
    calend.set(Calendar.SECOND, 0);
    calend.set(Calendar.MILLISECOND, 0);
    calend.set(Calendar.DAY_OF_MONTH, 17);
    calend.set(Calendar.MONTH, Calendar.FEBRUARY);
    calend.set(Calendar.YEAR, 1986);
    assertEquals("1986/02/17", ConverterUtil.formatDate(calend));
    calend.set(Calendar.MINUTE, 10);
    assertEquals("1986/02/17", ConverterUtil.formatDate(calend));
    calend.set(Calendar.MONTH, Calendar.DECEMBER);
    assertEquals("1986/12/17", ConverterUtil.formatDate(calend));
  }

  public void testFormatTimeCalendar() {
    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.HOUR_OF_DAY, 8);
    calend.set(Calendar.MINUTE, 15);
    calend.set(Calendar.SECOND, 0);
    calend.set(Calendar.MILLISECOND, 0);
    calend.set(Calendar.DAY_OF_MONTH, 17);
    calend.set(Calendar.MONTH, Calendar.FEBRUARY);
    calend.set(Calendar.YEAR, 1986);
    assertEquals("08:15", ConverterUtil.formatTime(calend));
    calend.set(Calendar.MINUTE, 5);
    assertEquals("08:05", ConverterUtil.formatTime(calend));
    calend.set(Calendar.MONTH, Calendar.DECEMBER);
    assertEquals("08:05", ConverterUtil.formatTime(calend));
  }

  public void testFormatTimeDate() {
    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.HOUR_OF_DAY, 8);
    calend.set(Calendar.MINUTE, 15);
    calend.set(Calendar.SECOND, 0);
    calend.set(Calendar.MILLISECOND, 0);
    calend.set(Calendar.DAY_OF_MONTH, 17);
    calend.set(Calendar.MONTH, Calendar.FEBRUARY);
    calend.set(Calendar.YEAR, 1986);
    assertEquals("08:15", ConverterUtil.formatTime(calend.getTime()));
    calend.set(Calendar.MINUTE, 5);
    assertEquals("08:05", ConverterUtil.formatTime(calend.getTime()));
    calend.set(Calendar.MONTH, Calendar.DECEMBER);
    assertEquals("08:05", ConverterUtil.formatTime(calend.getTime()));
  }

  public void testSetTime() throws Exception {
    Calendar reference = Calendar.getInstance();
    reference.set(Calendar.HOUR_OF_DAY, 9);
    reference.set(Calendar.MINUTE, 5);
    reference.set(Calendar.SECOND, 0);
    reference.set(Calendar.MILLISECOND, 0);
    reference.set(Calendar.DAY_OF_MONTH, 17);
    reference.set(Calendar.MONTH, Calendar.FEBRUARY);
    reference.set(Calendar.YEAR, 1986);
    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.HOUR_OF_DAY, 8);
    calend.set(Calendar.MINUTE, 15);
    calend.set(Calendar.SECOND, 0);
    calend.set(Calendar.MILLISECOND, 0);
    calend.set(Calendar.DAY_OF_MONTH, 17);
    calend.set(Calendar.MONTH, Calendar.FEBRUARY);
    calend.set(Calendar.YEAR, 1986);
    ConverterUtil.setTime(calend, "09:05");
    assertEquals(reference, calend);
    calend.set(Calendar.HOUR_OF_DAY, 8);
    calend.set(Calendar.MINUTE, 15);
    ConverterUtil.setTime(calend, "9:05");
    assertEquals(reference, calend);
    calend.set(Calendar.HOUR_OF_DAY, 8);
    calend.set(Calendar.MINUTE, 15);
    ConverterUtil.setTime(calend, "9:5");
    assertEquals(reference, calend);
  }

  public void testFormatCalendarForXpath() {
    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.HOUR_OF_DAY, 8);
    calend.set(Calendar.MINUTE, 15);
    calend.set(Calendar.SECOND, 0);
    calend.set(Calendar.MILLISECOND, 0);
    calend.set(Calendar.DAY_OF_MONTH, 17);
    calend.set(Calendar.MONTH, Calendar.FEBRUARY);
    calend.set(Calendar.YEAR, 1986);
    assertEquals("xs:dateTime('1986-02-17T08:15:00.000+01:00')", ConverterUtil
        .formatDateForXpath(calend.getTime()));
    calend.set(Calendar.MINUTE, 5);
    assertEquals("xs:dateTime('1986-02-17T08:05:00.000+01:00')", ConverterUtil
        .formatDateForXpath(calend.getTime()));
    calend.set(Calendar.MONTH, Calendar.DECEMBER);
    assertEquals("xs:dateTime('1986-12-17T08:05:00.000+01:00')", ConverterUtil
        .formatDateForXpath(calend.getTime()));
  }

  public void testFormatDateForXpath() {
    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.HOUR_OF_DAY, 8);
    calend.set(Calendar.MINUTE, 15);
    calend.set(Calendar.SECOND, 0);
    calend.set(Calendar.MILLISECOND, 0);
    calend.set(Calendar.DAY_OF_MONTH, 17);
    calend.set(Calendar.MONTH, Calendar.FEBRUARY);
    calend.set(Calendar.YEAR, 1986);
    assertEquals("xs:dateTime('1986-02-17T08:15:00.000+01:00')", ConverterUtil
        .formatDateForXpath(calend.getTime()));
    calend.set(Calendar.MINUTE, 5);
    assertEquals("xs:dateTime('1986-02-17T08:05:00.000+01:00')", ConverterUtil
        .formatDateForXpath(calend.getTime()));
    calend.set(Calendar.MONTH, Calendar.DECEMBER);
    assertEquals("xs:dateTime('1986-12-17T08:05:00.000+01:00')", ConverterUtil
        .formatDateForXpath(calend.getTime()));
  }
}
