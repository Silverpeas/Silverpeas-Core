package com.stratelia.webactiv.calendar.model;

import java.util.Vector;

public class SchedulableList {

  protected Vector content = new Vector();
  private String day;

  public SchedulableList() {
  }

  public SchedulableList(String day) {
    this.day = day;
  }

  public SchedulableList(String day, Vector elements) {
    this(day);
    for (int i = 0; i < elements.size(); i++)
      add((Schedulable) elements.elementAt(i));
  }

  public Vector getContent() {
    return content;
  }

  public void add(Schedulable schedule) {
    if (schedule.getStartDay() == null)
      return;
    if (schedule.getStartDay().compareTo(day) != 0)
      return;

    for (int i = 0; i < content.size(); i++) {
      Object obj = content.elementAt(i);
      if (obj instanceof Schedulable) {
        if (schedule.isOver((Schedulable) obj)) {
          content.remove(obj);
          SchedulableGroup group = new SchedulableGroup();
          group.add(schedule);
          group.add((Schedulable) obj);
          add(group);
          return;
        }
      }
      if (obj instanceof SchedulableGroup) {
        SchedulableGroup objGroup = (SchedulableGroup) obj;
        if (objGroup.isOver(schedule)) {
          content.remove(objGroup);
          objGroup.add(schedule);
          this.add(objGroup);
          return;
        }
      }
    }
    content.add(schedule);
  }

  public void add(SchedulableGroup group) {
    for (int i = 0; i < content.size(); i++) {
      Object obj = content.elementAt(i);
      if (obj instanceof Schedulable) {
        if (group.isOver((Schedulable) obj)) {
          content.remove(obj);
          group.add((Schedulable) obj);
          this.add(group);
          return;
        }
      }
      if (obj instanceof SchedulableGroup) {
        SchedulableGroup objGroup = (SchedulableGroup) obj;
        if (objGroup.isOver(group)) {
          objGroup.add(group);
          return;
        }
      }
    }
    content.add(group);
  }

  public Vector getStartingSchedules(String startHour, String endHour) {
    Vector result = new Vector();

    for (int i = 0; i < content.size(); i++) {
      Object obj = content.elementAt(i);
      if (obj instanceof Schedulable) {
        Schedulable schedule = (Schedulable) obj;
        // if (day.equals(schedule.getStartDay()))
        if ((schedule.getStartHour() != null)
            && (schedule.getEndHour() != null))
          if ((startHour.compareTo(schedule.getStartHour()) <= 0)
              && (endHour.compareTo(schedule.getStartHour()) > 0))
            result.add(schedule);
      } else if (obj instanceof SchedulableGroup) {
        SchedulableGroup group = (SchedulableGroup) obj;
        // if (day.equals(group.getStartDay()))
        if ((group.getStartHour() != null) && (group.getEndHour() != null))
          if ((startHour.compareTo(group.getStartHour()) <= 0)
              && (endHour.compareTo(group.getStartHour()) > 0))
            result.add(group);
      }
    }
    return result;
  }

  public Vector getWithoutHourSchedules() {
    Vector result = new Vector();
    for (int i = 0; i < content.size(); i++) {
      if (content.elementAt(i) instanceof Schedulable) {
        Schedulable schedule = (Schedulable) content.elementAt(i);
        if ((schedule.getStartHour() == null)
            && (schedule.getEndHour() == null))
          result.addElement(schedule);
      }
    }
    return result;
  }

  public Vector getGoOnSchedules(String startHour, String endHour) {
    Vector result = new Vector();

    for (int i = 0; i < getContent().size(); i++) {
      Object obj = getContent().elementAt(i);
      if (obj instanceof Schedulable) {
        Schedulable schedule = (Schedulable) obj;
        if ((schedule.getStartHour() != null)
            && (schedule.getEndHour() != null))
          // if ((hour.compareTo(schedule.getStartHour()) >= 0) &&
          // (hour.compareTo(schedule.getEndHour()) < 0))
          if (((startHour.compareTo(schedule.getStartHour()) >= 0) && (startHour
              .compareTo(schedule.getEndHour()) < 0))
              || ((endHour.compareTo(schedule.getStartHour()) > 0) && (endHour
                  .compareTo(schedule.getEndHour()) <= 0))
              || ((startHour.compareTo(schedule.getStartHour()) <= 0) && (endHour
                  .compareTo(schedule.getStartHour()) > 0))
              || ((startHour.compareTo(schedule.getEndHour()) < 0) && (endHour
                  .compareTo(schedule.getEndHour()) >= 0)))
            result.add(schedule);
      } else if (obj instanceof SchedulableGroup) {
        SchedulableGroup schedule = (SchedulableGroup) obj;
        if ((schedule.getStartHour() != null)
            && (schedule.getEndHour() != null))
          if (((startHour.compareTo(schedule.getStartHour()) >= 0) && (startHour
              .compareTo(schedule.getEndHour()) < 0))
              || ((endHour.compareTo(schedule.getStartHour()) > 0) && (endHour
                  .compareTo(schedule.getEndHour()) <= 0))
              || ((startHour.compareTo(schedule.getStartHour()) <= 0) && (endHour
                  .compareTo(schedule.getStartHour()) > 0))
              || ((startHour.compareTo(schedule.getEndHour()) < 0) && (endHour
                  .compareTo(schedule.getEndHour()) >= 0)))
            result.add(schedule);
      }
    }
    return result;
  }

}
