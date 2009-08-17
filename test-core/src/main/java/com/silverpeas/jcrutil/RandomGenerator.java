package com.silverpeas.jcrutil;

import java.util.Calendar;
import java.util.Random;

import org.apache.commons.lang.RandomStringUtils;

public class RandomGenerator {
  protected static final String[] LANGUAGES = new String[] { "fr", "en", "de",
      "ru", "cn", "se" };

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
    return calendar;
  }

  public static Calendar getFuturCalendar() {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DAY_OF_MONTH, 1 + random.nextInt(10));
    calendar.set(Calendar.HOUR_OF_DAY, getRandomHour());
    calendar.set(Calendar.MINUTE, getRandomMinutes());
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
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
    return calendar;
  }
}
