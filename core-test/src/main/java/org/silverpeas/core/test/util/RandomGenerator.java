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
package org.silverpeas.core.test.util;

import org.apache.commons.lang.RandomStringUtils;

import java.util.Calendar;
import java.util.Random;

public class RandomGenerator {

  protected static final String[] LANGUAGES = new String[]{"fr", "en", "de",
      "ru", "cn", "se"};
  protected static final Random random = new Random(10);

  /**
   * Generate a random int between 0 and 23.
   *
   * @return a random int between 0 and 23.
   */
  public static int getRandomHour() {
    return random.nextInt(24);
  }

  /**
   * Generate a random int between 0 and 59.
   *
   * @return a random int between 0 and 59.
   */
  public static int getRandomMinutes() {
    return random.nextInt(60);
  }

  /**
   * Generate a random int between 0 and 11.
   *
   * @return a random int between 0 and 11.
   */
  public static int getRandomMonth() {
    return random.nextInt(12);
  }

  /**
   * Generate a random int between 2019 and 2019.
   *
   * @return a random int between 2019 and 2019.
   */
  public static int getRandomYear() {
    return 2000 + random.nextInt(20);
  }

  /**
   * Generate a random int between 0 and 31.
   *
   * @return a random int between 0 and 31.
   */
  public static int getRandomDay() {
    return random.nextInt(32);
  }

  /**
   * Generate a random long.
   *
   * @return a random long.
   */
  public static long getRandomLong() {
    return random.nextLong();
  }

  /**
   * Generate a random float.
   *
   * @return a random float.
   */
  public static float getRandomFloat() {
    return random.nextFloat();
  }

  /**
   * Generate a random String of size 32.
   *
   * @return a random String of 32 chars.
   */
  public static String getRandomString() {
    return RandomStringUtils.random(32, true, true);
  }

  /**
   * Generate a random language
   *
   * @return a random valid language.
   */
  public static String getRandomLanguage() {
    return LANGUAGES[random.nextInt(LANGUAGES.length)];
  }

  /**
   * Generate a random boolean.
   *
   * @return a random boolean.
   */
  public static boolean getRandomBoolean() {
    return random.nextBoolean();
  }

  /**
   * Generate a random int.
   *
   * @return a random int.
   */
  public static int getRandomInt() {
    return random.nextInt();
  }

  /**
   * Generate a random int in the 0 inclusive max exclusive.
   *
   * @param max the exclusive maximum of the random int.
   * @return a random int.
   */
  public static int getRandomInt(int max) {
    return random.nextInt(max);
  }

  public static Calendar getOutdatedCalendar() {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DAY_OF_MONTH, -(1 + random.nextInt(10)));
    calendar.set(Calendar.HOUR_OF_DAY, getRandomHour());
    calendar.set(Calendar.MINUTE, getRandomMinutes());
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    calendar.setLenient(false);
    try {
      calendar.getTime();
    } catch (IllegalArgumentException ie) {
      return getOutdatedCalendar();
    }
    return calendar;
  }

  public static Calendar getFuturCalendar() {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DAY_OF_MONTH, 1 + random.nextInt(10));
    calendar.set(Calendar.HOUR_OF_DAY, getRandomHour());
    calendar.set(Calendar.MINUTE, getRandomMinutes());
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    calendar.setLenient(false);
    try {
      calendar.getTime();
    } catch (IllegalArgumentException ie) {
      return getFuturCalendar();
    }
    return calendar;
  }

  public static Calendar getRandomCalendar() {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.DAY_OF_MONTH, getRandomDay());
    calendar.set(Calendar.MONTH, getRandomMonth());
    calendar.set(Calendar.YEAR, getRandomYear());
    calendar.set(Calendar.HOUR_OF_DAY, getRandomHour());
    calendar.set(Calendar.MINUTE, getRandomMinutes());
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    calendar.setLenient(false);
    try {
      calendar.getTime();
    } catch (IllegalArgumentException ie) {
      return getRandomCalendar();
    }
    return calendar;
  }

  public static Calendar getCalendarAfter(Calendar date) {
    Calendar endDate = Calendar.getInstance();
    endDate.setTimeInMillis(date.getTimeInMillis());
    endDate.add(Calendar.DAY_OF_MONTH, 1 + random.nextInt(10));
    return endDate;
  }

  public static Calendar getCalendarBefore(Calendar date) {
    Calendar beforeDate = Calendar.getInstance();
    beforeDate.setTimeInMillis(date.getTimeInMillis());
    beforeDate.add(Calendar.DAY_OF_MONTH, -1 - random.nextInt(10));
    return beforeDate;
  }

  private RandomGenerator() {
  }
}
