/*
 * Copyright (C) 2000 - 2021 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.cmis.util;

import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * In CMIS the date and time are expressed in the Gregorian calendar.
 * @author mmoquillon
 */
public class CmisDateConverter {

  private CmisDateConverter() {

  }

  /**
   * Converts milliseconds from Epoch into a {@link GregorianCalendar} object, setting
   * the timezone to GMT and cutting milliseconds off.
   */
  public static GregorianCalendar millisToCalendar(long millis) {
    GregorianCalendar calendar = new GregorianCalendar();
    calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
    calendar.setTimeInMillis((long) (Math.ceil((double) millis / 1000) * 1000));
    return calendar;
  }

  /**
   * Converts a date in the GregorianCalendar calendar into the number of milliseconds from Epoch.
   * @param calendar a {@link GregorianCalendar} object.
   * @return the number of milliseconds from Epoch.
   */
  public static long calendarToMillis(GregorianCalendar calendar) {
    return calendar != null ? calendar.getTimeInMillis() : null;
  }
}
  