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

import org.hamcrest.Matcher;
import org.hamcrest.Factory;
import com.silverpeas.calendar.CalendarEventRecurrence;
import com.silverpeas.calendar.CalendarEvent;
import com.silverpeas.calendar.RecurrencePeriod;
import org.silverpeas.core.date.Datable;
import com.silverpeas.calendar.CalendarEventCategories;
import org.silverpeas.core.date.Date;
import com.silverpeas.calendar.DayOfWeek;
import com.silverpeas.calendar.DayOfWeekOccurrence;
import java.util.List;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import static com.silverpeas.calendar.CalendarEventRecurrence.*;
import static com.silverpeas.calendar.DayOfWeekOccurrence.*;

/**
 * A matcher of a VEVENT instruction in an iCal calendar with a Silverpeas calendar event.
 */
public class CalendarEventMatcher extends TypeSafeMatcher<String> {

  private CalendarEvent expectedEvent;
  private StringBuilder failureMessage = new StringBuilder();

  @Factory
  public static Matcher<String> describes(final CalendarEvent event) {
    return new CalendarEventMatcher(event);
  }

  @Override
  public void describeTo(final Description description) {
    description.appendText(failureMessage.toString());
  }

  @Override
  public boolean matchesSafely(String actualICalContent) {
    if (!actualICalContent.contains("BEGIN:VEVENT")) {
      failureMessage.append("BEGIN:VEVENT");
      return false;
    }
    if (!actualICalContent.contains("END:VEVENT")) {
      failureMessage.append("END:VEVENT");
      return false;
    }
    if (!actualICalContent.contains("SUMMARY:" + expectedEvent.getTitle() + "\r\n")) {
      failureMessage.append("SUMMARY:").append(expectedEvent.getTitle());
      return false;
    }
    if (!actualICalContent.contains("DTSTART" + asIcalDate(expectedEvent.getStartDate()) + "\r\n")) {
      failureMessage.append("DTSTART").append(asIcalDate(expectedEvent.getStartDate()));
      return false;
    }
    if (!expectedEvent.isOnAllDay() &&
        !actualICalContent.contains("DTEND" + asIcalDate(expectedEvent.getEndDate()) + "\r\n")) {
      failureMessage.append("DTEND").append(asIcalDate(expectedEvent.getEndDate()));
      return false;
    }
//    String timeZone = expectedEvent.getStartDate().getTimeZone().getID();
//    if (!actualICalContent.contains("TZID:" + timeZone + "\r\n")) {
//      failureMessage.append("TZID:").append(timeZone);
//      return false;
//    }
    if (!actualICalContent.contains(asIcalCategories(expectedEvent.getCategories()))) {
      failureMessage.append(asIcalCategories(expectedEvent.getCategories()));
      return false;
    }
    for (String attendee : expectedEvent.getAttendees().asList()) {
      if (!actualICalContent.contains("ATTENDEE:" + attendee)) {
        failureMessage.append("ATTENDEE:").append(attendee);
        return false;
      }
    }
    if (expectedEvent.isRecurring()) {
      if (!actualICalContent.contains(extractRRule(expectedEvent.getRecurrence()))) {
        failureMessage.append(extractRRule(expectedEvent.getRecurrence()));
        return false;
      }
    }
    return true;
  }

  private CalendarEventMatcher(final CalendarEvent onEvent) {
    this.expectedEvent = onEvent;
  }

  private String extractRRule(final CalendarEventRecurrence recurrence) {
    StringBuilder rrule = new StringBuilder("RRULE:");
    rrule.append(asICalFrequency(recurrence.getFrequency()));
    if (recurrence.getRecurrenceCount() != NO_RECURRENCE_COUNT) {
      rrule.append(";COUNT=").append(recurrence.getRecurrenceCount());
    } else if (recurrence.getEndDate() != NO_RECURRENCE_END_DATE) {
      rrule.append(";UNTIL=").append(recurrence.getEndDate().toICalInUTC());
    }
    if (recurrence.getFrequency().getInterval() > 1) {
      rrule.append(";INTERVAL=").append(recurrence.getFrequency().getInterval());
    }
    List<DayOfWeekOccurrence> daysOfWeek = recurrence.getDaysOfWeek();
    if (!daysOfWeek.isEmpty()) {
      rrule.append(";BYDAY=");
      if (daysOfWeek.get(0).nth() != ALL_OCCURRENCES) {
        rrule.append(daysOfWeek.get(0).nth());
      }
      rrule.append(asICalWeekOfDay(daysOfWeek.get(0).dayOfWeek()));
      for (int i = 1; i < daysOfWeek.size(); i++) {
        rrule.append(",");
        if (daysOfWeek.get(i).nth() != ALL_OCCURRENCES) {
          rrule.append(daysOfWeek.get(i).nth());
        }
        rrule.append(asICalWeekOfDay(daysOfWeek.get(i).dayOfWeek()));
      }
    }
    return rrule.toString();
  }

  private String asICalFrequency(final RecurrencePeriod period) {
    String freq = "FREQ=";
    switch (period.getUnit()) {
      case SECOND:
        freq += "SECONDLY";
        break;
      case MINUTE:
        freq += "MINUTELY";
        break;
      case HOUR:
        freq += "HOURLY";
        break;
      case DAY:
        freq += "DAILY";
        break;
      case WEEK:
        freq += "WEEKLY";
        break;
      case MONTH:
        freq += "MONTHLY";
        break;
      case YEAR:
        freq += "YEARLY";
        break;
    }
    return freq;
  }

  private String asICalWeekOfDay(final DayOfWeek dayOfWeek) {
    String weekday = ";BYDAY=";
    switch (dayOfWeek) {
      case MONDAY:
        weekday = "MO";
        break;
      case TUESDAY:
        weekday = "TU";
        break;
      case WEDNESDAY:
        weekday = "WE";
        break;
      case THURSDAY:
        weekday = "TH";
        break;
      case FRIDAY:
        weekday = "FR";
        break;
      case SATURDAY:
        weekday = "SA";
        break;
      case SUNDAY:
        weekday = "SU";
        break;
    }
    return weekday;
  }

  private String asIcalDate(final Datable<?> aDate) {
    String icalDate;
    if (aDate instanceof Date) {
      icalDate = ";VALUE=DATE:";
    } else {
      icalDate = ";TZID=" + aDate.getTimeZone().getID() + ":";
    }
    icalDate += aDate.toICal();
    return icalDate;
  }

  private String asIcalCategories(final CalendarEventCategories eventCategories) {
    StringBuilder iCalCategories = new StringBuilder();
    if (!eventCategories.isEmpty()) {
      List<String> categories = expectedEvent.getCategories().asList();
      iCalCategories.append("CATEGORIES:").append(categories.get(0));
      for (int i = 1; i < categories.size(); i++) {
        iCalCategories.append(",").append(categories.get(i));
      }
    }
    iCalCategories.append("\r\n");
    return iCalCategories.toString();
  }
}
