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
 * FLOSS exception.  You should have received a copy of the text describing
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
package org.silverpeas.core.personalorganizer.model;

import org.silverpeas.core.util.logging.SilverLogger;

import java.text.ParseException;
import java.util.Date;

import static org.silverpeas.core.util.DateUtil.*;
import static org.silverpeas.core.util.StringUtil.isDefined;

public abstract class Schedulable implements java.io.Serializable {

  private static final long serialVersionUID = -4783278450365830294L;
  private static final int MILLIS_IN_MINUTE = 60000;
  private static final int MILLIS_IN_HOUR = 3600000;
  private static final int SECONDS_IN_MINUTE = 60;
  private static final int DIGIT_COUNT = 2;
  private String id = null;
  private String name = null;
  private String delegatorId = null;
  private String description = null;
  private Classification classification = null;
  private String startDate = null;
  private String startHour = null;
  private String endDate = null;
  private String endHour = null;
  private Priority priority = null;
  private String externalId = null;

  public Schedulable() {
  }

  public Schedulable(String name, String delegatorId) {
    if (isDefined(name)) {
      this.name = name;
    }
    if (isDefined(delegatorId)) {
      this.delegatorId = delegatorId;
    }
  }

  public Schedulable(String id, String name, String delegatorId) {
    this(name, delegatorId);
    this.id = id;
  }

  public abstract Schedulable getCopy();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    if (isDefined(name)) {
      this.name = name;
    } else {
      this.name = null;
    }
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    if (isDefined(id)) {
      this.id = id;
    } else {
      this.id = null;
    }
  }

  public void setDelegatorId(String delegatorId) {
    if (isDefined(delegatorId)) {
      this.delegatorId = delegatorId;
    } else {
      this.delegatorId = null;
    }
  }

  public String getDelegatorId() {
    return delegatorId;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public Classification getClassification() {
    if (classification == null) {
      classification = new Classification();
    }
    return classification;
  }

  public Priority getPriority() {
    if (priority == null) {
      priority = new Priority();
    }
    return priority;
  }

  public void setStartDay(String date) throws java.text.ParseException {
    if (date == null) {
      startDate = null;
      // this is a normal case
      return;
    }
    if (date.length() == 0) {
      startDate = null;
      // this is also a normal case
      return;
    }
    parseDate(date);
    this.startDate = date;
  }

  public void setStartHour(String hour) throws java.text.ParseException {
    if (hour == null) {
      startHour = null;
      // this is a normal case
      return;
    }
    if (hour.length() == 0) {
      startHour = null;
      // this is also a normal case
      return;
    }
    parseTime(hour);
    this.startHour = hour;
  }

  public String getStartDay() {
    return startDate;
  }

  public Date getStartDate() {
    if (getStartDay() == null) {
      return null;
    }
    try {
      if (getStartHour() == null) {
        return parseDate(getStartDay());
      } else {
        return parseDateTime(getStartDay() + " " + getStartHour());
      }
    } catch (java.text.ParseException e) {
      return null;
    }
  }

  public void setStartDate(Date start) {
    if (start == null) {
      startDate = null;
      return;
    }
    startDate = formatDate(start);
  }

  public String getStartHour() {
    return startHour;
  }

  public void setEndDay(String date) throws java.text.ParseException {
    if (!isDefined(date)) {
      endDate = null;
      return;
    }
    parseDate(date);
    this.endDate = date;
  }

  public void setEndHour(String hour) throws java.text.ParseException {
    if (hour == null) {
      endHour = null;
      // this is a normal case
      return;
    }
    if (hour.length() == 0) {
      endHour = null;
      // this is also a normal case
      return;
    }
    parseTime(hour);

    this.endHour = hour;
  }

  public String getEndDay() {
    return endDate;
  }

  public String getEndHour() {
    return endHour;
  }

  public Date getEndDate() {
    if (getEndDay() == null) {
      return null;
    }
    try {
      if (getEndHour() != null) {
        return parseDateTime(getEndDay() + " " + getEndHour());
      } else {
        return parseDate(getEndDay());
      }
    } catch (java.text.ParseException e) {
      return null;
    }
  }

  public void setEndDate(Date end) {
    if (end == null) {
      endDate = null;
      return;
    }
    endDate = formatDate(end);
  }

  public String getStringDuration() {
    try {
      Date aStartDate = parseDateTime(getStartDay() + " " + getStartHour());
      Date anEndDate = parseDateTime(getEndDay() + " " + getEndHour());
      long ms = anEndDate.getTime() - aStartDate.getTime();
      return Schedulable.hourMinuteToString((int) ((ms / MILLIS_IN_MINUTE) % SECONDS_IN_MINUTE),
          (int) (ms / MILLIS_IN_HOUR));
    } catch (ParseException e) {
      SilverLogger.getLogger(this)
          .warn("Error while parsing the time. Return 00:00. Error is: " + e.getMessage());
      return "00:00";
    }
  }

  public int getMinuteDuration() {
    try {
      Date aStartDate = parseDateTime(getStartDay() + " " + getStartHour());
      Date anEndDate = parseDateTime(getEndDay() + " " + getEndHour());
      long ms = anEndDate.getTime() - aStartDate.getTime();
      return (int) (ms / MILLIS_IN_MINUTE);
    } catch (ParseException e) {
      SilverLogger.getLogger(this)
          .warn("Error while parsing the minutes. Return 0. Error is: " + e.getMessage());
      return 0;
    }
  }

  public boolean isOver(Schedulable schedule) {
    if (isOverHour(schedule)) {
      return false;
    }
    if ((getStartDate().compareTo(schedule.getStartDate()) <= 0)
        && (getEndDate().compareTo(schedule.getStartDate()) > 0)) {
      return true;
    }
    if ((getStartDate().compareTo(schedule.getEndDate()) < 0)
        && (getEndDate().compareTo(schedule.getEndDate()) >= 0)) {
      return true;
    }
    if ((schedule.getStartDate().compareTo(getStartDate()) <= 0)
        && (schedule.getEndDate().compareTo(getStartDate()) > 0)) {
      return true;
    }
    return schedule.getStartDate().compareTo(getEndDate()) < 0 &&
        schedule.getEndDate().compareTo(getEndDate()) >= 0;
  }

  private boolean isOverHour(final Schedulable schedule) {
    if ((getStartHour() == null) || (getEndHour() == null)) {
      return true;
    }
    return schedule.getStartHour() == null || schedule.getEndHour() == null;
  }

  @Override
  public String toString() {
    return " id = " + getId() + " name = " + getName()
        + " delegatorId = " + getDelegatorId() + " description = "
        + getDescription() + " startDay = " + getStartDay() + " startHour = "
        + getStartHour() + " endDay = " + getEndDay() + " endHour = "
        + getEndHour() + " externalId = " + getExternalId();
  }

  public static String hourMinuteToString(int hour, int minute) {
    String h = String.valueOf(hour);
    if (h.length() < DIGIT_COUNT) {
      h = "0" + h;
    }
    String m = String.valueOf(minute);
    if (m.length() < DIGIT_COUNT) {
      m = "0" + m;
    }
    return h + ":" + m;
  }

  public static String quarterCountToHourString(int quarterCount) {
    final int by2 = 2;
    final int mask = 3;
    final int quarter = 15;
    String hour = String.valueOf(quarterCount >> by2);
    if (hour.length() < DIGIT_COUNT) {
      hour = "0" + hour;
    }

    String minute = String.valueOf((quarterCount & mask) * quarter);
    if (minute.length() < DIGIT_COUNT) {
      minute = "0" + minute;
    }

    return hour + ":" + minute;
  }

  public String getExternalId() {
    return externalId;
  }

  public void setExternalId(String outlookId) {
    this.externalId = outlookId;
  }

}
