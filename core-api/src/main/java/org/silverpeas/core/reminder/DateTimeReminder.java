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
package org.silverpeas.core.reminder;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.scheduler.SchedulerException;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.time.OffsetDateTime;

/**
 * A reminder about any contribution that is triggered at a specified date time.
 * @author mmoquillon
 */
@Entity
@DiscriminatorValue("datetime")
public class DateTimeReminder extends Reminder {

  @Column(name = "dateTime", nullable = false)
  private OffsetDateTime dateTime;

  /**
   * Constructs a new reminder about the specified contribution and for the given user.
   * @param contributionId the unique identifier of the contribution.
   * @param user the user aimed by this reminder.
   */
  public DateTimeReminder(final ContributionIdentifier contributionId, final User user) {
    super(contributionId, user);
  }

  /**
   * Empty constructors for the persistence engine.
   */
  protected DateTimeReminder() {
    super();
  }

  @Override
  public DateTimeReminder withText(final String text) {
    return super.withText(text);
  }

  /**
   * Gets the date time at which this reminder will be triggered.
   * @return a {@link OffsetDateTime} value.
   */
  public OffsetDateTime getDateTime() {
    return dateTime;
  }

  /**
   * Sets a new date time at which this reminder has to be triggered.
   * @param dateTime a new date time.
   * @return itself.
   */
  public DateTimeReminder setDateTime(final OffsetDateTime dateTime) {
    this.dateTime = dateTime;
    return this;
  }

  /**
   * Triggers this reminder at the specified date time.
   * @param dateTime the date time at which this reminder will be triggered.
   * @return itself.
   * @throws SchedulerException is an error occurs while scheduling this reminder.
   */
  public DateTimeReminder triggerAt(final OffsetDateTime dateTime) throws SchedulerException {
    this.dateTime = dateTime;
    return scheduleAt(dateTime);
  }
}
  