/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
import org.silverpeas.core.contribution.model.ContributionModel;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * A reminder about any contribution that is triggered at a specified date time.
 * @author mmoquillon
 */
@Entity
@DiscriminatorValue("datetime")
public class DateTimeReminder extends Reminder {
  private static final long serialVersionUID = 472505709526636072L;

  @Transient
  private transient OffsetDateTime dateTime;

  @Override
  public Reminder copy() {
    DateTimeReminder copy = (DateTimeReminder) super.copy();
    copy.dateTime = dateTime;
    return copy;
  }

  /**
   * Constructs a new reminder about the given contribution for the system.
   * @param contributionId the unique identifier of a contribution.
   * @param processName the name of the process the reminder MUST perform when triggered.
   */
  public DateTimeReminder(final ContributionIdentifier contributionId,
      final ReminderProcessName processName) {
    super(contributionId, processName);
  }

  /**
   * Constructs a new reminder about the specified contribution and for the given user.
   * @param contributionId the unique identifier of the contribution.
   * @param user the user aimed by this reminder.
   * @param processName the name of the process the reminder MUST perform when triggered.
   */
  public DateTimeReminder(final ContributionIdentifier contributionId, final User user,
      final ReminderProcessName processName) {
    super(contributionId, user, processName);
  }

  /**
   * Empty constructors for the persistence engine.
   */
  protected DateTimeReminder() {
    super();
  }

  @SuppressWarnings("unchecked")
  @Override
  public final DateTimeReminder withText(final String text) {
    return super.withText(text);
  }


  /**
   * Triggers this reminder at the specified date time. The timezone of the specified date time
   * will be set in the timezone of the user behind this reminder.
   * @param dateTime the date time at which this reminder will be triggered once scheduled.
   * @return itself.
   */
  public DateTimeReminder triggerAt(final OffsetDateTime dateTime) {
    final ZoneId userZoneId = getUserZoneId();
    this.dateTime = dateTime.atZoneSameInstant(userZoneId).toOffsetDateTime();
    return this;
  }


  /**
   * Triggers this reminder from the temporal property of the contribution. The timezone of the
   * computed date time will be set in the timezone of the user behind this reminder.
   * @param temporalProperty the temporal property of the contribution.
   * @return itself.
   */
  public DateTimeReminder triggerFrom(final String temporalProperty) {
    withContributionProperty(temporalProperty);
    return this;
  }

  /**
   * Gets the datetime at which the trigger of this reminder is set. The returned datetime is
   * based upon the timezone of the user behind this reminder.
   * @return the datetime of this reminder's trigger.
   */
  public OffsetDateTime getDateTime() {
    return isScheduled() ? getScheduledDateTime() : dateTime;
  }

  @Override
  public boolean equals(final Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  protected OffsetDateTime computeTriggeringDate() {
    OffsetDateTime computedDate = super.computeTriggeringDate();
    if (computedDate == null && isDefined(getContributionProperty())) {
      final ContributionModel model = getContribution().getModel();
      final ZoneId userZoneId = getUserZoneId();
      final OffsetDateTime propertyDateTime = applyFilterOnTemporalType(model.filterByType(getContributionProperty()), userZoneId);
      computedDate = !propertyDateTime.isBefore(OffsetDateTime.now()) ? propertyDateTime : null;
    } else {
      computedDate = dateTime;
    }
    return computedDate;
  }
}
  