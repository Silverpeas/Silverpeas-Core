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
import org.silverpeas.core.calendar.Plannable;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.date.TemporalConverter;
import org.silverpeas.core.date.TimeUnit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * A reminder that is triggered at a given duration before the start date of the {@link Plannable}
 * relarted contribution.
 * @author mmoquillon
 */
@Entity
@DiscriminatorValue("duration")
public class DurationReminder extends Reminder {

  private Integer duration;
  private TimeUnit timeUnit;

  /**
   * Constructs a new reminder about the specified contribution and for the given user.
   * @param contributionId the unique identifier of the contribution.
   * @param user the user aimed by this reminder.
   */
  public DurationReminder(final ContributionIdentifier contributionId, final User user) {
    super(contributionId, user);
  }

  /**
   * Empty constructors for the persistence engine.
   */
  protected DurationReminder() {
    super();
  }

  @Override
  public final DurationReminder withText(final String text) {
    return super.withText(text);
  }

  /**
   * Gets the duration before the start date of a {@link Plannable} object this remainder has to be
   * triggered.
   * @return a duration value.
   */
  public int getDuration() {
    return duration;
  }

  /**
   * Gets the unit of time the duration is expressed.
   * @return a time unit value.
   */
  public TimeUnit getTimeUnit() {
    return timeUnit;
  }

  /**
   * Triggers this reminder the specified duration before the start date of the plannable
   * contribution. This type of trigger can only be used with {@link Plannable} object. The
   * reminder, once scheduled, will be triggered at the specified duration prior the start date
   * of the plannable object.
   * @param duration the duration value prior to the start date of a {@link Plannable} object
   * @param timeUnit the time unit in which is expressed the duration.
   * @return itself.
   */
  public DurationReminder triggerBefore(final int duration, final TimeUnit timeUnit) {
    this.duration = duration;
    this.timeUnit = timeUnit;
    return this;
  }

  @Override
  protected OffsetDateTime getTriggeringDate() {
    Plannable contribution = getPlannableContribution();
    return TemporalConverter.applyByType(contribution.getStartDate(),
        d -> d.atStartOfDay().atZone(ZoneId.systemDefault()).toOffsetDateTime(), dt -> dt)
        .minus(duration, timeUnit.toChronoUnit());
  }

  private Plannable getPlannableContribution() {
    Contribution contribution = getContribution();
    if (contribution instanceof Plannable) {
      return (Plannable) contribution;
    }
    throw new IllegalArgumentException(
        "The " + getContributionId().getType() + " contribution isn't plannable!");
  }
}
  