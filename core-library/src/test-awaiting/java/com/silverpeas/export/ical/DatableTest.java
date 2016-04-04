/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package com.silverpeas.export.ical;

import org.silverpeas.core.date.DateTime;
import org.silverpeas.core.date.Datable;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 * Unit tests on datable objects.
 */
public class DatableTest {

  @Test
  public void aDateTimeToISO8601() {
    java.util.Date now = new java.util.Date();
    SimpleDateFormat format = new SimpleDateFormat(Datable.ISO_8601_PATTERN);
    String expected = format.format(now);

    DateTime dateTime = new DateTime(now);
    String actual = dateTime.toISO8601();
    System.out.println("Date in ISO 8601: " + actual);
    assertThat(actual, equalToIgnoringCase(expected));
  }

  @Test
  public void aDateTimeToICal() {
    java.util.Date now = new java.util.Date();
    SimpleDateFormat format = new SimpleDateFormat(Datable.ICAL_PATTERN);
    String expected = format.format(now);

    DateTime dateTime = new DateTime(now);
    String actual = dateTime.toICal();
    System.out.println("Date in iCal: " + actual);
    assertThat(actual, equalToIgnoringCase(expected));
  }

  @Test
  public void aDateTimeInUTCToICal() {
    java.util.Date now = new java.util.Date();
    SimpleDateFormat format = new SimpleDateFormat(Datable.ICAL_UTC_PATTERN);
    format.setTimeZone(TimeZone.getTimeZone("UTC"));
    String expected = format.format(now);

    DateTime dateTime = new DateTime(now);
    String actual = dateTime.toICalInUTC();
    System.out.println("UTC date in iCal: " + actual);
    assertThat(actual, equalToIgnoringCase(expected));
  }
}
