package com.stratelia.webactiv.util.indexEngine;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatter {

  private static SimpleDateFormat indexFormatter = new SimpleDateFormat(
      "yyyyMMdd");
  public static String nullBeginDate = "00000000";
  public static String nullEndDate = "99999999";

  public static String date2IndexFormat(Date date) {
    return indexFormatter.format(date);
  }

  public static Date string2Date(String date) {
    try {
      return indexFormatter.parse(date);
    } catch (ParseException e) {
      return null;
    }
  }
}
