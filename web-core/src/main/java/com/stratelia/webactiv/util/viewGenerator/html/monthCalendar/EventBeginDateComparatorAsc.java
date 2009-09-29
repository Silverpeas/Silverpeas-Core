package com.stratelia.webactiv.util.viewGenerator.html.monthCalendar;

import java.util.Calendar;
import java.util.Comparator;

public class EventBeginDateComparatorAsc implements Comparator {
  static public EventBeginDateComparatorAsc comparator = new EventBeginDateComparatorAsc();

  public int compare(Object o1, Object o2) {
    Calendar date1 = Calendar.getInstance();
    Event e1 = (Event) o1;
    date1.setTime(e1.getStartDate());

    String startHour = e1.getStartHour(); // 12:30
    String separator = ":";
    if (startHour != null && startHour.length() > 0
        && startHour.lastIndexOf(separator) != -1) {
      int hour = Integer.parseInt(startHour.substring(0, startHour
          .lastIndexOf(separator)));
      int minutes = Integer.parseInt(startHour.substring(startHour
          .lastIndexOf(separator) + 1, startHour.length()));
      date1.set(Calendar.HOUR_OF_DAY, hour);
      date1.set(Calendar.MINUTE, minutes);
    }

    Calendar date2 = Calendar.getInstance();
    Event e2 = (Event) o2;
    date2.setTime(e2.getStartDate());
    startHour = e2.getStartHour();
    if (startHour != null && startHour.length() > 0
        && startHour.lastIndexOf(separator) != -1) {
      int hour = Integer.parseInt(startHour.substring(0, startHour
          .lastIndexOf(separator)));
      int minutes = Integer.parseInt(startHour.substring(startHour
          .lastIndexOf(separator) + 1, startHour.length()));
      date2.set(Calendar.HOUR_OF_DAY, hour);
      date2.set(Calendar.MINUTE, minutes);
    }

    int compareResult = (new Long(date1.getTimeInMillis())).compareTo(new Long(
        (date2.getTimeInMillis())));

    return compareResult;
  }

  public boolean equals(Object o) {
    return o == this;
  }
}