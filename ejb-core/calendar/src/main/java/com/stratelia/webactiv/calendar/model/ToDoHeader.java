package com.stratelia.webactiv.calendar.model;

import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class ToDoHeader extends Schedulable implements Cloneable {
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
    completedDate = date;
  }

  public java.util.Date getCompletedDate() {
    return completedDate;
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
    if (completedDate == null)
      return null;
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

  public void setExternalId(String id) {
    externalId = id;
  }

  public String getExternalId() {
    return externalId;
  }

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
