/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.core.datereminder.persistence;

import java.util.Date;

/**
 * The information details of date reminder
 *
 * @author Cécile Bonin
 */
public class DateReminderDetail {

  public static final int REMINDER_NOT_PROCESSED = 0;
  public static final int REMINDER_PROCESSED = 1;

  private Date dateReminder;

  private String message;

  private int processStatus = DateReminderDetail.REMINDER_NOT_PROCESSED;

  private String creatorId;

  private String updaterId;

  public DateReminderDetail(Date dateReminder, String message, int processStatus, String creatorId, String updatedId) {
    this.dateReminder = dateReminder;
    this.message = message;
    this.processStatus = processStatus;
    this.creatorId = creatorId;
    this.updaterId = updatedId;
  }

  public Date getDateReminder() {
    return dateReminder;
  }

  public void setDateReminder(final Date dateReminder) {

    this.dateReminder = dateReminder;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(final String message) {
    this.message = message;
  }

  public int getProcessStatus() {
    return processStatus;
  }

  public void setProcessStatus(final int processStatus) {
    this.processStatus = processStatus;
  }

  public String getCreatorId() {
    return creatorId;
  }

  public void setCreatorId(final String creatorId) {
    this.creatorId = creatorId;
  }

  public String getUpdaterId() {
    return updaterId;
  }

  public void setUpdaterId(final String updaterId) {
    this.updaterId = updaterId;
  }
}