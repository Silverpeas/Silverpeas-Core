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
import org.silverpeas.core.scheduler.Scheduler;
import org.silverpeas.core.scheduler.SchedulerProvider;
import org.silverpeas.core.scheduler.trigger.JobTrigger;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import java.text.MessageFormat;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

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
   * Gets the list of reminders linked to a contribution represented by the given identifier.
   * @param contributionId the identifier of a contribution.
   * @return a list of reminder, empty if no reminder.
   */
  public static List<Reminder> getByContributionId(final ContributionIdentifier contributionId) {
    // TODO when persistence is ready
    return Collections.emptyList();
  }

  /**
   * Gets a reminder by its identifier.
   * @param reminderId the identifier of a reminder.
   * @return the right reminder, null otherwise.
   */
  public static Reminder getById(final String reminderId) {
    // TODO when persistence is ready
    return null;
  }

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
    return scheduler()
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
        scheduler().unscheduleJob(getJobName());
      }
      Reminder me = ReminderRepository.get().save(this);
      JobTrigger trigger = JobTrigger.triggerAt(dateTime);
      scheduler().scheduleJob(getJobName(), trigger, ReminderProcess.get());
      return (T) me;
    });
  }

  /**
   * Unscheduling this reminder.
   * @throws TransactionRuntimeException if the persistence or the scheduling fails.
   */
  public void unschedule() {
    // TODO
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
  
  private Scheduler scheduler() {
    return SchedulerProvider.getPersistentScheduler();
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
     * Triggers this reminder the specified duration before the given property of the contribution.
     * The property must represents either a date or a date time whose the value is a
     * {@link java.time.temporal.Temporal} object. For example the start
     * date of an event or the end date of the visibility of a publication.
     * @param duration the duration value prior to the temporal property of the contribution.
     * @param timeUnit the time unit in which is expressed the duration.
     * @param temporalProperty the temporal property of the contribution.
     * @return a {@link DurationReminder} instance.
     */
    public DurationReminder triggerBefore(final int duration, final TimeUnit timeUnit,
        final String temporalProperty) {
      return new DurationReminder(contribution, user).withText(text)
          .triggerBefore(duration, timeUnit, temporalProperty);
    }

    /**
     * Triggers the reminder at the specified date time.
     * @param dateTime the {@link OffsetDateTime} at which the reminder will be triggered.
     * @return a {@link DateTimeReminder} instance.
     */
    public DateTimeReminder triggerAt(final OffsetDateTime dateTime) {
      return new DateTimeReminder(contribution, user).withText(text).triggerAt(dateTime);
    }
  }
}
