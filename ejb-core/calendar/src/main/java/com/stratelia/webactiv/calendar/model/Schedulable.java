package com.stratelia.webactiv.calendar.model;

import com.stratelia.silverpeas.silvertrace.SilverTrace;

public abstract class Schedulable implements java.io.Serializable {
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

  protected static final java.text.SimpleDateFormat completeFormat = new java.text.SimpleDateFormat(
      "yyyy/MM/dd HH:mm");
  protected static final java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat(
      "yyyy/MM/dd");
  protected static final java.text.SimpleDateFormat hourFormat = new java.text.SimpleDateFormat(
      "HH:mm");

  public Schedulable() {
  }

  public Schedulable(String name, String delegatorId) {
    setName(name);
    setDelegatorId(delegatorId);
  }

  public Schedulable(String id, String name, String delegatorId) {
    this(name, delegatorId);
    this.id = id;
  }

  abstract public Schedulable getCopy();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
    if (name != null)
      if (name.length() == 0)
        this.name = null;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    if (id != null)
      if (id.length() == 0)
        this.id = null;
    this.id = id;
  }

  public void setDelegatorId(String delegatorId) {
    this.delegatorId = delegatorId;
    if (delegatorId != null)
      if (delegatorId.length() == 0)
        this.delegatorId = null;
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
    if (classification == null)
      classification = new Classification();
    return classification;
  }

  public Priority getPriority() {
    if (priority == null)
      priority = new Priority();
    return priority;
  }

  public void setStartDay(String date) throws java.text.ParseException {
    if (date == null) {
      startDate = null;
      return; // this is a normal case
    }
    if (date.length() == 0) {
      startDate = null;
      return; // this is also a normal case
    }

    dateFormat.parse(date);

    this.startDate = date;
  }

  public void setStartHour(String hour) throws java.text.ParseException {
    if (hour == null) {
      startHour = null;
      return; // this is a normal case
    }
    if (hour.length() == 0) {
      startHour = null;
      return; // this is also a normal case
    }
    hourFormat.parse(hour);
    this.startHour = hour;
  }

  public String getStartDay() {
    return startDate;
  }

  public java.util.Date getStartDate() {
    if (getStartDay() == null)
      return null;
    try {
      if (getStartHour() == null)
        return dateFormat.parse(getStartDay());
      else
        return completeFormat.parse(getStartDay() + " " + getStartHour());
    } catch (java.text.ParseException e) {
      return null;
    }
  }

  public void setStartDate(java.util.Date start) {
    if (start == null) {
      startDate = null;
      return;
    }
    try {
      setStartDay(dateFormat.format(start));
    } catch (java.text.ParseException e) {
      SilverTrace.warn("calendar",
          "Schedulable.setStartDate(setStartDate(java.util.Date start)",
          "calendar_MSG_NOT_PARSE_DATE");
    }
  }

  public String getStartHour() {
    return startHour;
  }

  public void setEndDay(String date) throws java.text.ParseException {
    if (date == null) {
      endDate = null;
      return;
    }
    if (date.length() == 0) {
      endDate = null;
      return; // this is also a normal case
    }
    dateFormat.parse(date);
    this.endDate = date;
  }

  public void setEndHour(String hour) throws java.text.ParseException {
    if (hour == null) {
      endHour = null;
      return; // this is a normal case
    }
    if (hour.length() == 0) {
      endHour = null;
      return; // this is also a normal case
    }
    hourFormat.parse(hour);

    this.endHour = hour;
  }

  public String getEndDay() {
    return endDate;
  }

  public String getEndHour() {
    return endHour;
  }

  public java.util.Date getEndDate() {
    if (getEndDay() == null)
      return null;
    try {
      if (getEndHour() != null)
        return completeFormat.parse(getEndDay() + " " + getEndHour());
      else
        return dateFormat.parse(getEndDay());
    } catch (java.text.ParseException e) {
      return null;
    }
  }

  public void setEndDate(java.util.Date end) {
    if (end == null) {
      endDate = null;
      return;
    }
    try {
      setEndDay(dateFormat.format(end));
    } catch (java.text.ParseException e) {
      SilverTrace.warn("calendar",
          "Schedulable.setStartDate(setStartDate(java.util.Date start)",
          "calendar_MSG_NOT_PARSE_DATE");
    }
  }

  public String getStringDuration() {
    try {
      java.util.Date startDate = completeFormat.parse(getStartDay() + " "
          + getStartHour());
      java.util.Date endDate = completeFormat.parse(getEndDay() + " "
          + getEndHour());
      long ms = endDate.getTime() - startDate.getTime();
      return Schedulable.hourMinuteToString((int) ((ms / (60000)) % 60),
          (int) (ms / 3600000));
    } catch (Exception e) {
      SilverTrace.warn("calendar", "Schedulable.getStringDuration",
          "calendar_MSG_NOT_SCEDULE", "return = 00:00");
      return "00:00";
    }
  }

  public int getMinuteDuration() {
    try {
      java.util.Date startDate = completeFormat.parse(getStartDay() + " "
          + getStartHour());
      java.util.Date endDate = completeFormat.parse(getEndDay() + " "
          + getEndHour());
      long ms = endDate.getTime() - startDate.getTime();
      return (int) (ms / (60000));
    } catch (Exception e) {
      SilverTrace.warn("calendar", "Schedulable.getMinuteDuration() ",
          "calendar_MSG_NOT_SCEDULE", "return = 0");
      return 0;
    }
  }

  public boolean isOver(Schedulable schedule) {
    if ((getStartHour() == null) || (getEndHour() == null))
      return false;
    if ((schedule.getStartHour() == null) || (schedule.getEndHour() == null))
      return false;
    if ((getStartDate().compareTo(schedule.getStartDate()) <= 0)
        && (getEndDate().compareTo(schedule.getStartDate()) > 0))
      return true;
    if ((getStartDate().compareTo(schedule.getEndDate()) < 0)
        && (getEndDate().compareTo(schedule.getEndDate()) >= 0))
      return true;
    if ((schedule.getStartDate().compareTo(getStartDate()) <= 0)
        && (schedule.getEndDate().compareTo(getStartDate()) > 0))
      return true;
    if ((schedule.getStartDate().compareTo(getEndDate()) < 0)
        && (schedule.getEndDate().compareTo(getEndDate()) >= 0))
      return true;
    // }
    return false;
  }

  public String toString() {
    String result = " id = " + getId() + " name = " + getName()
        + " delegatorId = " + getDelegatorId() + " description = "
        + getDescription() + " startDay = " + getStartDay() + " startHour = "
        + getStartHour() + " endDay = " + getEndDay() + " endHour = "
        + getEndHour() + " externalId = " + getExternalId();
    return result;
  }

  public static String hourMinuteToString(int hour, int minute) {
    String h = String.valueOf(hour);
    if (h.length() < 2)
      h = "0" + h;
    String m = String.valueOf(minute);
    if (m.length() < 2)
      m = "0" + m;
    return h + ":" + m;
  }

  static public String quaterCountToHourString(int quaterCount) {
    String hour = String.valueOf(quaterCount >> 2);
    if (hour.length() < 2)
      hour = "0" + hour;

    String minute = String.valueOf((quaterCount & 3) * 15);
    if (minute.length() < 2)
      minute = "0" + minute;

    return hour + ":" + minute;
  }

  public String getExternalId() {
    return externalId;
  }

  public void setExternalId(String outlookId) {
    this.externalId = outlookId;
  }

}
