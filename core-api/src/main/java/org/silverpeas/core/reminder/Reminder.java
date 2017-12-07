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
import org.silverpeas.core.contribution.ContributionManager;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.date.TimeUnit;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.TransactionRuntimeException;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.BasicJpaEntity;
import org.silverpeas.core.scheduler.SchedulerException;
import org.silverpeas.core.scheduler.SchedulerProvider;
import org.silverpeas.core.scheduler.trigger.JobTrigger;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import java.text.MessageFormat;
import java.time.OffsetDateTime;

/**
 * A reminder.
 * @author mmoquillon
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "reminderType")
public abstract class Reminder extends BasicJpaEntity<Reminder, UuidIdentifier> {

  private static final MessageFormat SCHEDULED_JOB_NAME = new MessageFormat("Reminder#{0}");

  private ContributionIdentifier contributionId;
  private String userId;
  private String text;

  /**
   * Constructs a new reminder about the given contribution for the specified user.
   * @param contributionId the unique identifier of a contribution.
   * @param user the user for which the reminder is aimed to.
   */
  protected Reminder(final ContributionIdentifier contributionId, final User user) {
    super();
    this.contributionId = contributionId;
    this.userId = user.getId();
  }

  protected Reminder() {
    super();
  }

  /**
   * Constructs a {@link ReminderBuilder} to build a new reminder about the specified contribution
   * and for the given user.
   * @return a {@link ReminderBuilder};
   */
  public static ReminderBuilder make(final ContributionIdentifier contribution, final User user) {
    return new ReminderBuilder().about(contribution).forUser(user);
  }

  /**
   * Sets a text with this reminder. The text will be sent with the notification to the user.
   * @param text a text to attach with the reminder.
   * @return itself.
   */
  public <T extends Reminder> T withText(final String text) {
    this.text = text;
    return (T) this;
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
   * Is this reminder scheduled? The reminder is scheduled if it is taken in charge by the
   * Silverpeas scheduler engine.
   * @return true if this reminder is scheduled to be triggered at its specified date time, false
   * otherwise.
   */
  public boolean isScheduled() {
    return SchedulerProvider.getScheduler()
        .isJobScheduled(SCHEDULED_JOB_NAME.format(new Object[]{getId()}));
  }

  /**
   * Schedules this reminder. It persists first the reminder properties and then starts its
   * scheduling according to its triggering rule.
   * @throws TransactionRuntimeException if the persistence or the scheduling fails.
   * @return itself.
   */
  public <T extends Reminder> T schedule() {
    OffsetDateTime dateTime = getTriggeringDate();
    assert dateTime != null;
    return Transaction.getTransaction().perform(() -> {
      if (isPersisted() && isScheduled()) {
        SchedulerProvider.getScheduler().unscheduleJob(getJobName());
      }
      Reminder me = ReminderRepository.get().save(this);
      JobTrigger trigger = JobTrigger.triggerAt(dateTime);
      SchedulerProvider.getScheduler().scheduleJob(getJobName(), trigger, ReminderProcess.get());
      return (T) me;
    });
  }

  /**
   * Gets the contribution related by this reminder.
   * @return a {@link Contribution} object.
   */
  protected Contribution getContribution() {
    return ContributionManager.get()
        .getById(this.contributionId)
        .orElseThrow(() -> new SilverpeasRuntimeException(
            SilverpeasExceptionMessages.failureOnGetting(contributionId.getType() + " contribution",
                contributionId.getLocalId())));
  }

  /**
   * Gets the date time at which this reminder has to be triggered. This date time is computed from
   * the triggering rule of this reminder.
   * @return an {@link OffsetDateTime} value.
   */
  protected abstract OffsetDateTime getTriggeringDate();

  private String getJobName() {
    return SCHEDULED_JOB_NAME.format(new Object[]{getId()});
  }

  /**
   * A builder of reminders. It builds the concrete reminder according to the type of triggering.
   */
  public static class ReminderBuilder {

    private ContributionIdentifier contribution;
    private User user;
    private String text;

    /**
     * The reminder is about the specified contribution.
     * @param contribution the unique identifier of a contribution.
     * @return itself.
     */
    public ReminderBuilder about(final ContributionIdentifier contribution) {
      this.contribution = contribution;
      return this;
    }

    /**
     * The reminders is for the specified user.
     * @param user the user aimed by the reminder.
     * @return itself.
     */
    public ReminderBuilder forUser(final User user) {
      this.user = user;
      return this;
    }

    /**
     * The reminder is annotated with the specified text.
     * @param text the text associated with the reminder.
     * @return itself.
     */
    public ReminderBuilder withText(final String text) {
      this.text = text;
      return this;
    }

    /**
     * Triggers the reminder at the specified duration before the start date of the related
     * contribution. This triggering is only valid for
     * {@link org.silverpeas.core.calendar.Plannable} contribution.
     * @param duration a duration value
     * @param timeUnit the time unit in which is expressed the duration.
     * @return a {@link DurationReminder} instance.
     */
    public DurationReminder triggerBefore(final int duration, final TimeUnit timeUnit)
        throws SchedulerException {
      return new DurationReminder(contribution, user).withText(text)
          .triggerBefore(duration, timeUnit);
    }

    /**
     * Triggers the reminder at the specified date time.
     * @param dateTime the {@link OffsetDateTime} at which the reminder will be triggered.
     * @return a {@link DateTimeReminder} instance.
     */
    public DateTimeReminder triggerAt(final OffsetDateTime dateTime) throws SchedulerException {
      return new DateTimeReminder(contribution, user).withText(text).triggerAt(dateTime);
    }
  }
}
  