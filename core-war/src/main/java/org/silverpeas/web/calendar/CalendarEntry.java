/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.calendar;

public class CalendarEntry {

  private static final long serialVersionUID = 6712231674470680038L;

  /**
   * The minimum allowable priority value. Please note that this refers to the case where no
   * priority is set. This case happens to be represented by the value 0 which is the lowest integer
   * value allowable.
   */
  private static final int MINIMUM_PRIORITY = 0;

  /**
   * The maximum alloable priority value. Please note that this refers to the highest possible
   * integer value for priority. When interpreting this value it is seen as the lowest priority
   * because the value 1 is the highest priority value.
   */
  private static final int MAXIMUM_PRIORITY = 9;


  private static final String PRIVATE = "private";
  private static final String PUBLIC = "public";
  private static final String CONFIDENTIAL = "confidential";

  private String id = null;
  private String name = null;
  private String delegatorId = null;
  private String description = null;
  private String classification = PRIVATE;
  private String startDate = null;
  private String startHour = null;
  private String endDate = null;
  private String endHour = null;
  private int priority = 2;
  private String externalId = null;

  public CalendarEntry() {
  }

  public String getEndDay() {
    if (endDate == null) {
      return getStartDay();
    }
    return endDate;
  }

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

  public String getClassification() {
    return classification;
  }

  public void setClassification(String classification) {
    if (classification == null) {
      return;
    }
    if (PRIVATE.equalsIgnoreCase(classification)) {
      this.classification = PRIVATE;
    } else if (PUBLIC.equalsIgnoreCase(classification)) {
      this.classification = PUBLIC;
    } else if (CONFIDENTIAL.equalsIgnoreCase(classification)) {
      this.classification = CONFIDENTIAL;
    }
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int newPriority) {
    if (newPriority > MAXIMUM_PRIORITY) {
      priority = MAXIMUM_PRIORITY;
    } else if (newPriority < MINIMUM_PRIORITY) {
      priority = MINIMUM_PRIORITY;
    } else {
      priority = newPriority;
    }
  }

  public void setStartDay(String date) {
    if (date == null) {
      startDate = null;
      return; // this is a normal case
    }
    this.startDate = date;
  }

  public void setStartHour(String hour) {
    this.startHour = hour;
  }

  public String getStartDay() {
    return startDate;
  }

  public String getStartHour() {
    return startHour;
  }

  public void setEndDay(String date) {
    if (date == null) {
      endDate = null;
      return;
    }
    this.endDate = date;
  }

  public void setEndHour(String hour) {
    if (hour == null) {
      endHour = null;
      return;
    }
    this.endHour = hour;
  }

  public String getEndHour() {
    return endHour;
  }

  public String getExternalId() {
    return externalId;
  }

  public void setExternalId(String outlookId) {
    this.externalId = outlookId;
  }

  private boolean isDefined(String parameter) {
    return (parameter != null && !parameter.trim().isEmpty() && !"null".equalsIgnoreCase(parameter));
  }


  @Override
  public String toString() {
    return "JournalHeader{" + "id=" + id + ", name=" + name + ", delegatorId=" + delegatorId
        + ", description=" + description + ", classification=" + classification + ", startDate="
        + startDate + ", startHour=" + startHour + ", endDate=" + endDate + ", endHour=" + endHour
        + ", priority=" + priority + ", externalId=" + externalId + '}';
  }
}
