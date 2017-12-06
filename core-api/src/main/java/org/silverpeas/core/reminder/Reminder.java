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

import org.silverpeas.core.SilverpeasExceptionMessages;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.calendar.Plannable;
import org.silverpeas.core.contribution.ContributionManager;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.date.TimeUnit;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.BasicJpaEntity;
import org.silverpeas.core.scheduler.SchedulerProvider;

import javax.persistence.Entity;
import java.text.MessageFormat;
import java.time.temporal.Temporal;

/**
 * A reminder.
 * @author mmoquillon
 */
@Entity
public class Reminder extends BasicJpaEntity<Reminder, UuidIdentifier> {

  private static final MessageFormat SCHEDULED_JOB_NAME = new MessageFormat("Reminder#{0}");

  private final ContributionIdentifier contributionId;
  private final String userId;
  private String text;
  private int duration;
  private TimeUnit timeUnit;

  /**
   * Constructs a new reminder about the given contribution for the specified user.
   * @param contributionId the unique identifier of a contribution.
   * @param user the user for which the reminder is aimed to.
   */
  public Reminder(final ContributionIdentifier contributionId, final User user) {
    this.contributionId = contributionId;
    this.userId = user.getId();
  }

  /**
   * Sets a text with this reminder. The text will be sent with the notification to the user.
   * @param text a text to attach with the reminder.
   * @return itself.
   */
  public Reminder withText(final String text) {
    this.text = text;
    return this;
  }

  /**
   * Gets the unique identifier of the contribution this remainder is set of.
   * @return the contribution unique identifier.
   */
  public ContributionIdentifier getContributionId() {
    return contributionId;
  }

  /**
   * Gets the unique identifier of the user against which this reminder is aimed.
   * @return the identifier of a user as a String
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Gets the reminder text.
   * @return the text associated with this reminder.
   */
  public String getText() {
    return text;
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
   * reminder is then scheduled and will be triggered at the specified duration prior the plannable
   * object.
   * @param duration the duration value prior to the start date of a {@link Plannable} object
   * @param timeUnit the time unit in which is expressed the duration.
   * @return itself.
   */
  public Reminder triggerBefore(final int duration, final TimeUnit timeUnit) {
    this.duration = duration;
    this.timeUnit = timeUnit;
    Plannable contribution = getPlannableContribution();
    Temporal startDate = contribution.getStartDate();
    startDate.minus(duration, timeUnit.toChronoUnit());
    return Transaction.getTransaction().perform(() -> {
      Reminder me = ReminderRepository.get().save(this);

      /*SchedulerProvider.getScheduler()
          .scheduleJob("Reminder#" + me.getId(), JobTrigger.tr)*/
      return me;
    });
  }

  /**
   * Is this reminder scheduled? The reminder is scheduled if it is taken in charge by the
   * Silverpeas scheduler engine.
   * @return true if this reminder is scheduled to be triggered at its specified date time, false
   * otherwise.
   */
  public boolean isScheduled() {
    return SchedulerProvider.getScheduler()
        .isJobScheduled(SCHEDULED_JOB_NAME.format(new Object[]{getId()}));
  }

  private Contribution getContribution() {
    return ContributionManager.get()
        .getById(this.contributionId)
        .orElseThrow(() -> new SilverpeasRuntimeException(
            SilverpeasExceptionMessages.failureOnGetting(contributionId.getType() + " contribution",
                contributionId.getLocalId())));
  }

  private Plannable getPlannableContribution() {
    Contribution contribution = getContribution();
    if (contribution instanceof Plannable) {
      return (Plannable) contribution;
    }
    throw new IllegalArgumentException(
        "The " + contributionId.getType() + " contribution isn't plannable!");
  }
}
  