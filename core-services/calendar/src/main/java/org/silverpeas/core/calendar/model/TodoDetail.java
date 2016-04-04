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

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class TodoDetail implements Serializable {

  private static final long serialVersionUID = 8827343178019140503L;
  public static final int PERCENT_UNDEFINED = -1;
  public static final float DURATION_UNDEFINED = -1;

  private String id = null;
  private String name = null;
  private String delegatorId = null;
  private List<Attendee> attendees = null;
  private String description = null;
  private Date startDate = null;
  private Date endDate = null;
  private int percentCompleted = PERCENT_UNDEFINED;
  private Date completedDate = null;
  private String componentId = null;
  private String spaceId = null;
  private String externalId = null;
  private float duration = DURATION_UNDEFINED;

  public static TodoDetail fromToDoHeader(final ToDoHeader header) {
    TodoDetail detail = new TodoDetail();
    detail.setName(header.getName());
    detail.setId(header.getId());
    detail.setDescription(header.getDescription());
    detail.setDelegatorId(header.getDelegatorId());
    // detail.setPriority(header.getPriority());
    detail.setStartDate(header.getStartDate());
    detail.setEndDate(header.getEndDate());
    detail.setDuration(header.getDuration());
    detail.setPercentCompleted(header.getPercentCompleted());
    detail.setComponentId(header.getComponentId());
    detail.setSpaceId(header.getSpaceId());
    detail.setExternalId(header.getExternalId());
    return detail;
  }

  public TodoDetail() {
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setDelegatorId(String delegatorId) {
    this.delegatorId = delegatorId;
  }

  public String getDelegatorId() {
    return delegatorId;
  }

  /**
   * @param attendees a list of Attendee objects
   */

  public void setAttendees(List<Attendee> attendees) {
    this.attendees = attendees;
  }

  /**
   * @return a list of Attendee objects
   */

  public List<Attendee> getAttendees() {
    return attendees;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date start) {
    this.startDate = start;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date end) {
    this.endDate = end;
  }

  public int getPercentCompleted() {
    return percentCompleted;
  }

  public void setPercentCompleted(int newValue) {
    percentCompleted = newValue;
  }

  public void setCompletedDate(Date date) {
    completedDate = date;
  }

  public Date getCompletedDate() {
    return completedDate;
  }

  public float getDuration() {
    return duration;
  }

  public void setDuration(float duration) {
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

}