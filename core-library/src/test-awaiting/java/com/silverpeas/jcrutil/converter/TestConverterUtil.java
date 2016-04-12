/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.jcrutil.converter;

import java.text.ParseException;
import java.util.Calendar;

import org.junit.Test;
import org.silverpeas.core.persistence.jcr.JcrDataConverter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestConverterUtil {

  @Test
  public void testConvertToJcrPath() {
    assertEquals("/", JcrDataConverter.convertToJcrPath("/"));
    assertEquals("/toto", JcrDataConverter.convertToJcrPath("/toto"));
    assertEquals("/theme__test", JcrDataConverter.convertToJcrPath("/theme test"));
    assertEquals("/theme__test/sous__theme__test", JcrDataConverter
        .convertToJcrPath("/theme test/sous theme test"));
    assertEquals("/theme__test%7C", JcrDataConverter.convertToJcrPath("/theme test|"));
    assertEquals("/theme__test%09", JcrDataConverter.convertToJcrPath("/theme test\t"));
    assertEquals("/theme__test%0D", JcrDataConverter.convertToJcrPath("/theme test\r"));
    assertEquals("/theme__test%0A", JcrDataConverter.convertToJcrPath("/theme test\n"));
    assertEquals("t%2E", JcrDataConverter.convertToJcrPath("t."));
    assertEquals("theme.", JcrDataConverter.convertToJcrPath("theme."));
    assertEquals("/theme%2A", JcrDataConverter.convertToJcrPath("/theme*"));
    assertEquals("/theme__%5Btest%5D", JcrDataConverter.convertToJcrPath("/theme [test]"));
    assertEquals("/theme\"", JcrDataConverter.convertToJcrPath("/theme\""));
    assertEquals("/theme'", JcrDataConverter.convertToJcrPath("/theme'"));
    assertEquals("/theme__20%25", JcrDataConverter.convertToJcrPath("/theme 20%"));
    assertEquals("/theme%3Atest", JcrDataConverter.convertToJcrPath("/theme:test"));
  }

  @Test
  public void testConvertFromJcrPath() {
    assertEquals("/", JcrDataConverter.convertFromJcrPath("/"));
    assertEquals("/toto", JcrDataConverter.convertFromJcrPath("/toto"));
    assertEquals("/theme test", JcrDataConverter.convertFromJcrPath("/theme__test"));
    assertEquals("/theme test/sous theme test", JcrDataConverter
        .convertFromJcrPath("/theme__test/sous__theme__test"));
    assertEquals("/theme test|", JcrDataConverter.convertFromJcrPath("/theme__test%7C"));
    assertEquals("/theme test\t", JcrDataConverter.convertFromJcrPath("/theme__test%09"));
    assertEquals("/theme test\r", JcrDataConverter.convertFromJcrPath("/theme__test%0D"));
    assertEquals("/theme test\n", JcrDataConverter.convertFromJcrPath("/theme__test%0A"));
    assertEquals("t.", JcrDataConverter.convertFromJcrPath("t%2E"));
    assertEquals("theme.", JcrDataConverter.convertFromJcrPath("theme."));
    assertEquals("/theme*", JcrDataConverter.convertFromJcrPath("/theme%2A"));
    assertEquals("/theme [test]", JcrDataConverter.convertFromJcrPath("/theme__%5Btest%5D"));
    assertEquals("/theme\"", JcrDataConverter.convertFromJcrPath("/theme%22"));
    assertEquals("/theme'", JcrDataConverter.convertFromJcrPath("/theme%27"));
    assertEquals("/theme 20%", JcrDataConverter.convertFromJcrPath("/theme__20%25"));
    assertEquals("/theme:test", JcrDataConverter.convertFromJcrPath("/theme%3Atest"));
  }
  @Test
  public void testParseDate() throws Exception {
    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.HOUR_OF_DAY, 0);
    calend.set(Calendar.MINUTE, 0);
    calend.set(Calendar.SECOND, 0);
    calend.set(Calendar.MILLISECOND, 0);
    calend.set(Calendar.DAY_OF_MONTH, 17);
    calend.set(Calendar.MONTH, Calendar.FEBRUARY);
    calend.set(Calendar.YEAR, 1986);
    assertEquals(calend.getTime(), JcrDataConverter.parseDate("1986/02/17"));
    try {
      JcrDataConverter.parseDate("1986/15/17");
      fail();
    } catch (ParseException pex) {
    }
  }

  @Test
  public void testFormatDateDate() {
    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.HOUR_OF_DAY, 0);
    calend.set(Calendar.MINUTE, 0);
    calend.set(Calendar.SECOND, 0);
    calend.set(Calendar.MILLISECOND, 0);
    calend.set(Calendar.DAY_OF_MONTH, 17);
    calend.set(Calendar.MONTH, Calendar.FEBRUARY);
    calend.set(Calendar.YEAR, 1986);
    assertEquals("1986/02/17", JcrDataConverter.formatDate(calend.getTime()));
    calend.set(Calendar.MINUTE, 10);
    assertEquals("1986/02/17", JcrDataConverter.formatDate(calend.getTime()));
    calend.set(Calendar.MONTH, Calendar.DECEMBER);
    assertEquals("1986/12/17", JcrDataConverter.formatDate(calend.getTime()));
  }

  @Test
  public void testFormatDateCalendar() {
    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.HOUR_OF_DAY, 0);
    calend.set(Calendar.MINUTE, 0);
    calend.set(Calendar.SECOND, 0);
    calend.set(Calendar.MILLISECOND, 0);
    calend.set(Calendar.DAY_OF_MONTH, 17);
    calend.set(Calendar.MONTH, Calendar.FEBRUARY);
    calend.set(Calendar.YEAR, 1986);
    assertEquals("1986/02/17", JcrDataConverter.formatDate(calend));
    calend.set(Calendar.MINUTE, 10);
    assertEquals("1986/02/17", JcrDataConverter.formatDate(calend));
    calend.set(Calendar.MONTH, Calendar.DECEMBER);
    assertEquals("1986/12/17", JcrDataConverter.formatDate(calend));
  }

  @Test
  public void testFormatTimeCalendar() {
    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.HOUR_OF_DAY, 8);
    calend.set(Calendar.MINUTE, 15);
    calend.set(Calendar.SECOND, 0);
    calend.set(Calendar.MILLISECOND, 0);
    calend.set(Calendar.DAY_OF_MONTH, 17);
    calend.set(Calendar.MONTH, Calendar.FEBRUARY);
    calend.set(Calendar.YEAR, 1986);
    assertEquals("08:15", JcrDataConverter.formatTime(calend));
    calend.set(Calendar.MINUTE, 5);
    assertEquals("08:05", JcrDataConverter.formatTime(calend));
    calend.set(Calendar.MONTH, Calendar.DECEMBER);
    assertEquals("08:05", JcrDataConverter.formatTime(calend));
  }

  @Test
  public void testFormatTimeDate() {
    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.HOUR_OF_DAY, 8);
    calend.set(Calendar.MINUTE, 15);
    calend.set(Calendar.SECOND, 0);
    calend.set(Calendar.MILLISECOND, 0);
    calend.set(Calendar.DAY_OF_MONTH, 17);
    calend.set(Calendar.MONTH, Calendar.FEBRUARY);
    calend.set(Calendar.YEAR, 1986);
    assertEquals("08:15", JcrDataConverter.formatTime(calend.getTime()));
    calend.set(Calendar.MINUTE, 5);
    assertEquals("08:05", JcrDataConverter.formatTime(calend.getTime()));
    calend.set(Calendar.MONTH, Calendar.DECEMBER);
    assertEquals("08:05", JcrDataConverter.formatTime(calend.getTime()));
  }

  @Test
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
    JcrDataConverter.setTime(calend, "09:05");
    assertEquals(reference, calend);
    calend.set(Calendar.HOUR_OF_DAY, 8);
    calend.set(Calendar.MINUTE, 15);
    JcrDataConverter.setTime(calend, "9:05");
    assertEquals(reference, calend);
    calend.set(Calendar.HOUR_OF_DAY, 8);
    calend.set(Calendar.MINUTE, 15);
    JcrDataConverter.setTime(calend, "9:5");
    assertEquals(reference, calend);
  }

  @Test
  public void testFormatCalendarForXpath() {
    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.HOUR_OF_DAY, 8);
    calend.set(Calendar.MINUTE, 15);
    calend.set(Calendar.SECOND, 0);
    calend.set(Calendar.MILLISECOND, 0);
    calend.set(Calendar.DAY_OF_MONTH, 17);
    calend.set(Calendar.MONTH, Calendar.FEBRUARY);
    calend.set(Calendar.YEAR, 1986);
    assertEquals("xs:dateTime('1986-02-17T08:15:00.000+01:00')", JcrDataConverter.
        formatDateForXpath(calend.getTime()));
    calend.set(Calendar.MINUTE, 5);
    assertEquals("xs:dateTime('1986-02-17T08:05:00.000+01:00')", JcrDataConverter.
        formatDateForXpath(calend.getTime()));
    calend.set(Calendar.MONTH, Calendar.DECEMBER);
    assertEquals("xs:dateTime('1986-12-17T08:05:00.000+01:00')", JcrDataConverter.
        formatDateForXpath(calend.getTime()));
  }

  @Test
  public void testFormatDateForXpath() {
    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.HOUR_OF_DAY, 8);
    calend.set(Calendar.MINUTE, 15);
    calend.set(Calendar.SECOND, 0);
    calend.set(Calendar.MILLISECOND, 0);
    calend.set(Calendar.DAY_OF_MONTH, 17);
    calend.set(Calendar.MONTH, Calendar.FEBRUARY);
    calend.set(Calendar.YEAR, 1986);
    assertEquals("xs:dateTime('1986-02-17T08:15:00.000+01:00')", JcrDataConverter.
        formatDateForXpath(calend.getTime()));
    calend.set(Calendar.MINUTE, 5);
    assertEquals("xs:dateTime('1986-02-17T08:05:00.000+01:00')", JcrDataConverter.
        formatDateForXpath(calend.getTime()));
    calend.set(Calendar.MONTH, Calendar.DECEMBER);
    assertEquals("xs:dateTime('1986-12-17T08:05:00.000+01:00')", JcrDataConverter.
        formatDateForXpath(calend.getTime()));
  }
}
