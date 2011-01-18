/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.calendar.model;

import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class ToDoHeader extends Schedulable implements Cloneable {

  private static final long serialVersionUID = -8938831646589260261L;
  public static final int PERCENT_UNDEFINED = -1;
  public static final int DURATION_UNDEFINED = -1;

  private int percentCompleted = PERCENT_UNDEFINED;
  private java.util.Date completedDate = null;
  private String componentId = null;
  private String spaceId = null;
  private String externalId = null;
  private int duration = DURATION_UNDEFINED;

  public ToDoHeader() {
  }

  public ToDoHeader(String name, String organizerId) {
    super(name, organizerId);
  }

  public ToDoHeader(String id, String name, String organizerId) {
    super(id, name, organizerId);
  }

  public int getPercentCompleted() {
    return percentCompleted;
  }

  public void setPercentCompleted(int newValue) {
    percentCompleted = newValue;
  }

  public void setCompletedDate(java.util.Date date) {
    completedDate = new java.util.Date(date.getTime());
  }

  public java.util.Date getCompletedDate() {
    return new java.util.Date(completedDate.getTime());
  }

  public void setCompletedDay(String day) {
    try {
      completedDate = dateFormat.parse(day);
    } catch (Exception e) {
      SilverTrace.warn("calendar", "ToDoHeader.setCompletedDay(String day)",
          "calendar_MSG_NOT_PARSE_DATE", "return => completedDate=null");
      completedDate = null;
    }
  }

  public String getCompletedDay() {
    if (completedDate == null) {
      return null;
    }
    return dateFormat.format(completedDate);
  }

  public int getDuration() {
    return duration;
  }

  public void setDuration(int duration) {
    this.duration = duration;
  }

  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

  public String getComponentId() {
    return componentId;
  }

  public void setSpaceId(String id) {
    spaceId = id;
  }

  public String getSpaceId() {
    return spaceId;
  }

  @Override
  public void setExternalId(String id) {
    externalId = id;
  }

  @Override
  public String getExternalId() {
    return externalId;
  }

  @Override
  public Schedulable getCopy() {
    try {
      return (ToDoHeader) this.clone();
    } catch (Exception e) {
      SilverTrace.warn("calendar", "ToDoHeader.getCopy",
          "calendar_MSG_NOT_Get_COPY", "return => Schedulable=null");
      return null;
    }
  }
}
