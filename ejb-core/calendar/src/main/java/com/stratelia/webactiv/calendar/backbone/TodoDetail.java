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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.stratelia.webactiv.calendar.backbone;

import java.io.Serializable;
import java.util.Date;
import java.util.Vector;

public class TodoDetail implements Serializable {

  public static final int PERCENT_UNDEFINED = -1;
  public static final float DURATION_UNDEFINED = -1;

  private String id = null;
  private String name = null;
  private String delegatorId = null;
  private Vector attendees = null;
  private String description = null;
  private Date startDate = null;
  private Date endDate = null;
  private int percentCompleted = PERCENT_UNDEFINED;
  private java.util.Date completedDate = null;
  private String componentId = null;
  private String spaceId = null;
  private String externalId = null;
  private float duration = DURATION_UNDEFINED;

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
   * @param attendees
   *          a list of Attendee objects
   */

  public void setAttendees(Vector attendees) {
    this.attendees = attendees;
  }

  /**
   * @return a list of Attendee objects
   */

  public Vector getAttendees() {
    return attendees;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public java.util.Date getStartDate() {
    return startDate;
  }

  public void setStartDate(java.util.Date start) {
    this.startDate = start;
  }

  public java.util.Date getEndDate() {
    return endDate;
  }

  public void setEndDate(java.util.Date end) {
    this.endDate = end;
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