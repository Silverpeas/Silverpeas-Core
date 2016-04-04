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

package org.silverpeas.core.calendar.model;

import org.silverpeas.core.util.DateUtil;

import java.util.Vector;

public class SchedulableGroup extends SchedulableList {

  public SchedulableGroup() {
  }

  public boolean isOver(Schedulable schedule) {
    for (int i = 0; i < content.size(); i++) {
      Schedulable sched = (Schedulable) content.elementAt(i);
      if (sched.isOver(schedule)) {
        return true;
      }
    }
    return false;
  }

  public boolean isOver(SchedulableGroup group) {
    for (int i = 0; i < content.size(); i++) {
      Schedulable sched = (Schedulable) content.elementAt(i);
      if (group.isOver(sched)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void add(Schedulable schedule) {
    content.add(schedule);
  }

  @Override
  public void add(SchedulableGroup group) {
    Vector toAdd = group.getContent();
    for (int i = 0; i < toAdd.size(); i++) {
      content.add((Schedulable) toAdd.elementAt(i));
    }
  }

  public String getStartHour() {
    String result = null;
    for (int i = 0; i < content.size(); i++) {
      Schedulable schedule = (Schedulable) content.elementAt(i);
      if (result == null) {
        result = schedule.getStartHour();
      } else if (schedule.getStartHour() != null) {
        if (schedule.getStartHour().compareTo(result) < 0) {
          result = schedule.getStartHour();
        }
      }
    }
    // Debug.println("Group.startHour = " + result);
    return result;
  }

  public String getEndHour() {
    String result = null;
    for (int i = 0; i < content.size(); i++) {
      Schedulable schedule = (Schedulable) content.elementAt(i);
      if (result == null) {
        result = schedule.getEndHour();
      } else if (schedule.getEndHour() != null) {
        if (schedule.getEndHour().compareTo(result) > 0) {
          result = schedule.getEndHour();
        }
      }
    }
    // Debug.println("Group.endHour = " + result);
    return result;
  }

  public Vector getStartingSchedules(String hour) {
    Vector result = new Vector();

    for (int i = 0; i < content.size(); i++) {
      Object obj = content.elementAt(i);
      if (obj instanceof Schedulable) {
        Schedulable schedule = (Schedulable) obj;
        // if (day.equals(schedule.getStartDay()))
        if (hour.equals(schedule.getStartHour())) {
          result.add(schedule);
        }
      }
    }
    return result;
  }

  public int getMinuteDuration() {
    try {
      java.util.Date startDate = DateUtil.parseDateTime("2000/01/01 " + getStartHour());
      java.util.Date endDate = DateUtil.parseDateTime("2000/01/01 " + getEndHour());
      long ms = endDate.getTime() - startDate.getTime();
      return (int) (ms / (60000));
    } catch (Exception e) {
      return 0;
    }
  }
}
