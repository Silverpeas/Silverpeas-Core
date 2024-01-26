/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.reminder;

import org.silverpeas.core.ApplicationService;
import org.silverpeas.core.SilverpeasExceptionMessages;
import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.model.NoSuchPropertyException;
import org.silverpeas.core.date.TimeUnit;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.TransactionRuntimeException;
import org.silverpeas.core.persistence.datasource.model.jpa.BasicJpaEntity;
import org.silverpeas.core.scheduler.Scheduler;
import org.silverpeas.core.scheduler.SchedulerProvider;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.core.util.filter.Filter;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

/**
 * A reminder. A reminder is a notification that is sent to a given user at a specific datetime.
 * The accuracy of the reminding triggering is about 5mn meaning a reminder shouldn't be lesser
 * than this value. Nevertheless we recommend strongly to set a reminder at a time far greater than
 * 15mn otherwise expecting behaviour might occurred (this isn't constrained by the reminder).
 * A reminder can be automatically rescheduled, meaning that it is rescheduled at another datetime
 * at each of its triggering until that another datetime isn't defined. This capability depends on
 * the concrete type of the used reminder and it is based upon the return of the
 * {@link #isSchedulable()} method.
 * @author mmoquillon
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "reminderType")
@Table(name = "sb_reminder")
@NamedQuery(name = "remindersByUserId",
    query = "select r from Reminder r where r.userId = :userId")
@NamedQuery(name = "remindersByContributionId",
    query = "select r from Reminder r where r.contributionId = :contributionId")
 @NamedQuery(name = "remindersByContributionIdAndUserId",
    query = "select r from Reminder r where r.userId = :userId and r.contributionId = :contributionId")
public abstract class Reminder extends BasicJpaEntity<Reminder, ReminderIdentifier> {
  private static final long serialVersionUID = -7921844697973849535L;

  @Embedded
  private ContributionIdentifier contributionId;
  @Column(name = "userId", nullable = false, length = 40)
  private String userId;
  @Column(name = "process_name", nullable = false, length = 200)
  private String processName;
  @Column(name = "text")
  private String text;
  @Column(name = "triggered")
  private boolean triggered;
  @Column(name = "trigger_datetime", nullable = false)
  private Instant triggerDateTime;
  @Column(name = "trigger_prop")
  private String contributionProperty;
  @Transient
  private transient OffsetDateTime nextTriggeringDate;

  /**
   * Gets the reminders linked to a contribution represented by the given identifier.
   * @param contributionId the identifier of a contribution.
   * @return a list of reminders related to the specified contribution, empty if no such reminders.
   */
  public static List<Reminder> getByContribution(final ContributionIdentifier contributionId) {
    return ReminderRepository.get().findByContributionId(contributionId);
  }

  /**
   * Gets the reminders set by the specified user for himself.
   * @param user the user.
   * @return a list of the user's reminders, empty if no such reminders.
   */
  public static List<Reminder> getByUser(final User user) {
    return ReminderRepository.get().findByUserId(user.getId());
  }

  /**
   * Gets the reminders that was set by the specified user for himself and that are about the
   * specified contribution.
   * @param contributionId the unique identifier of a contribution.
   * @param user the user.
   * @return a list of the user's reminders related to the contribution, empty if no such reminders.
   */
  public static List<Reminder> getByContributionAndUser(final ContributionIdentifier contributionId,
      final User user) {
    return ReminderRepository.get().findByContributionAndUserIds(contributionId, user.getId());
  }

  /**
   * Gets a reminder by its identifier.
   * @param reminderId the identifier of a reminder.
   * @return the right reminder, null otherwise.
   */
  public static Reminder getById(final String reminderId) {
    return ReminderRepository.get().getById(reminderId);
  }

  /**
   * Copies this reminder to another one. This method expects the concrete class extending the
   * {@link Reminder} abstract one implements a default constructor as it is used to constructs an
   * empty {@link Reminder} instance before setting the attributes. Any concrete reminders should
   * override this method in order to set their specifics attributes.
   * @return a copy of this reminder.
   */
  public Reminder copy() {
    Reminder copy = newEmptyReminder();
    copy.userId = userId;
    copy.contributionId = contributionId;
    copy.processName = getProcessName();
    copy.triggerDateTime = triggerDateTime;
    copy.text = text;
    copy.contributionProperty = contributionProperty;
    copy.nextTriggeringDate = nextTriggeringDate;
    copy.triggered = triggered;
    return copy;
  }

  private Reminder newEmptyReminder() {
    try {
      Constructor<? extends Reminder> constructor = getClass().getDeclaredConstructor();
      if (! constructor.canAccess(null)) {
        constructor.trySetAccessible();
      }
      return constructor.newInstance();
    } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  /**
   * Constructs a new reminder about the given contribution for the system.
   * @param contributionId the unique identifier of a contribution.
   * @param processName the name of the process the reminder MUST perform when triggered.
   */
  protected Reminder(final ContributionIdentifier contributionId,
      final ReminderProcessName processName) {
    super();
    this.contributionId = contributionId;
    this.userId = "-1";
    this.processName = processName.asString();
  }

  /**
   * Constructs a new reminder about the given contribution for the specified user.
   * @param contributionId the unique identifier of a contribution.
   * @param user the user for which the reminder is aimed to.
   * @param processName the name of the process the reminder MUST perform when triggered.
   */
  protected Reminder(final ContributionIdentifier contributionId, final User user,
      final ReminderProcessName processName) {
    super();
    this.contributionId = contributionId;
    this.userId = user.getId();
    this.processName = processName.asString();
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
  @SuppressWarnings("unchecked")
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
   * Indicates if the user id represents the system.
   * @return the identifier of a user as a String
   */
  public boolean isSystemUser() {
    return "-1".equals(userId);
  }

  /**
   * Gets the name of the process the reminder MUST perform when triggered.
   * @return the name of the process as string.
   */
  public String getProcessName() {
    return processName;
  }

  /**
   * Gets the reminder text.
   * @return the text associated with this reminder.
   */
  public String getText() {
    return text;
  }

  /**
   * Gets the datetime at which this reminder is scheduled. If this reminder isn't yet scheduled,
   * the datetime returned is null, even if its triggering rule is set. The datetime is in
   * UTC/Greenwich.
   * @return a {@link OffsetDateTime} value or null if the reminder isn't yet scheduled.
   */
  public OffsetDateTime getScheduledDateTime() {
    return this.triggerDateTime.atOffset(ZoneOffset.UTC);
  }

  /**
   * Gets the temporal property of the contribution to which this reminder is related.
   * @return the name of a temporal business property of the contribution.
   */
  public String getContributionProperty() {
    return contributionProperty;
  }

  @SuppressWarnings("unchecked")
  <T extends Reminder> T withContributionProperty(final String temporalProperty) {
    this.contributionProperty = temporalProperty;
    return (T) this;
  }

  /**
   * Is this reminder scheduled? The reminder is scheduled if it is taken in charge by the
   * Silverpeas scheduler engine.
   * @return true if this reminder is scheduled to be triggered at its specified date time, false
   * otherwise.
   */
  public boolean isScheduled() {
    return isScheduledWith(getScheduler());
  }

  /**
   * Is this reminder was triggered?
   * @return true if this reminder was already fired, false otherwise.
   */
  public boolean isTriggered() {
    return triggered;
  }

  /**
   * This reminder is currently being triggered.
   */
  protected void triggered() {
    this.triggered = true;
  }

  /**
   * Schedules this reminder. It persists first the reminder properties and then starts its
   * scheduling according to its triggering rule. If this reminder is already scheduled, it is
   * rescheduled with its new triggering rules and any of its properties changes are persisted.
   * Hence, this method can be used both for scheduling and for rescheduling a reminder.
   * @throws TransactionRuntimeException if the persistence or the scheduling fails.
   * @throws IllegalStateException if no trigger was set or the triggering date is null.
   * @return itself.
   */
  @SuppressWarnings("unchecked")
  public <T extends Reminder> T schedule() {
    OffsetDateTime triggeringDateTime = computeTriggeringDate();
    checkTriggeringDate(triggeringDateTime);
    this.triggerDateTime = triggeringDateTime.toInstant();
    Scheduler scheduler = getScheduler();
    return Transaction.performInOne(() -> {
      if (isPersisted() && isScheduledWith(scheduler)) {
        scheduler.unscheduleJob(getJobName());
      }
      Reminder me = ReminderRepository.get().save(this);
      JobTrigger trigger = JobTrigger.triggerAt(triggeringDateTime);
      scheduler.scheduleJob(me.getJobName(), trigger, ReminderProcess.get());
      return (T) me;
    });
  }

  /**
   * Unschedules this reminder. The reminder won't be anymore scheduled and it will be also removed
   * from the persistence context.
   * @throws TransactionRuntimeException if the persistence or the scheduling fails.
   */
  public void unschedule() {
    unschedule(true);
  }

  /**
   * Unschedules this reminder. The reminder won't be anymore scheduled and it will be also removed
   * from the persistence context according to given indicator.
   * @param deleteReminder if true the reminder is also removed from the persistence, if false
   * only the trigger is unscheduled.
   * @throws TransactionRuntimeException if the persistence or the scheduling fails.
   */
  void unschedule(boolean deleteReminder) {
    Scheduler scheduler = getScheduler();
    Transaction.performInOne(() -> {
      scheduler.unscheduleJob(getJobName());
      if (deleteReminder) {
        ReminderRepository.get().delete(this);
      }
      return null;
    });
  }

  /**
   * Is this reminder schedulable? A reminder is schedulable if its trigger is correctly set and it
   * matches the expectation of the concrete reminder.
   * By default a reminder is schedulable or reschedulable if the temporal property of the related
   * contribution from which the triggering date is computed is non null and after now.
   * @return true if this reminder can be scheduled, false otherwise.
   */
  public boolean isSchedulable() {
    try {
      nextTriggeringDate = computeTriggeringDate();
      return nextTriggeringDate != null;
    } catch (IllegalArgumentException | NoSuchPropertyException e) {
      return false;
    }
  }

  @Override
  public boolean equals(final Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  /**
   * Gets the contribution related by this reminder.
   * @return a {@link Contribution} object.
   */
  protected Contribution getContribution() {
    return ApplicationService.getInstance(contributionId.getComponentInstanceId())
        .getContributionById(this.contributionId)
        .orElseThrow(() -> new SilverpeasRuntimeException(
            SilverpeasExceptionMessages.failureOnGetting(contributionId.getType() + " contribution",
                contributionId.getLocalId())));
  }

  /**
   * Computes the date time at which this reminder will be triggered. This method is invoked in
   * {@link Reminder#schedule()} in order to plan the trigger of this reminder in the timeline.
   * The triggering datetime is computed from the triggering rule that is specific to the
   * concrete type of this reminder. The timezone of the returned triggering date has to be set
   * with the timezone of the user behind the reminder.
   * @return an {@link OffsetDateTime} value.
   */
  protected OffsetDateTime computeTriggeringDate() {
    if (nextTriggeringDate != null && !nextTriggeringDate.isBefore(OffsetDateTime.now())) {
      return nextTriggeringDate;
    }
    return null;
  }

  ZoneId getUserZoneId() {
    return isSystemUser()
        ? ZoneId.systemDefault()
        : User.getById(getUserId()).getUserPreferences().getZoneId();
  }

  OffsetDateTime applyFilterOnTemporalType(final Filter<Class<?>, Object> filter,
      ZoneId withZoneId) {
    final ZoneId platformZoneId = ZoneId.systemDefault();
    final ZonedDateTime platformZonedTriggeringDate =
        filter.matchFirst(Date.class::isAssignableFrom,
            d -> ZonedDateTime.ofInstant(((Date) d).toInstant(), platformZoneId))
            .matchFirst(Instant.class::equals,
                d -> ZonedDateTime.ofInstant((Instant) d, platformZoneId))
            .matchFirst(OffsetDateTime.class::equals,
                d -> ((OffsetDateTime) d).atZoneSameInstant(platformZoneId))
            .matchFirst(LocalDate.class::equals,
                d -> ((LocalDate) d).atStartOfDay(withZoneId).withZoneSameInstant(platformZoneId))
            .matchFirst(LocalDateTime.class::equals,
                d -> ((LocalDateTime) d).atZone(platformZoneId))
            .matchFirst(ZonedDateTime.class::equals,
                d -> ((ZonedDateTime) d).withZoneSameInstant(platformZoneId))
            .result()
            .orElseThrow(() -> new IllegalArgumentException(
                "The property " + getContributionProperty() + " isn't a date or a date time"));
    return platformZonedTriggeringDate.toOffsetDateTime();
  }

  private String getJobName() {
    return this.getId();
  }

  private boolean isScheduledWith(final Scheduler scheduler) {
    return scheduler.isJobScheduled(this.getJobName());
  }
  
  private Scheduler getScheduler() {
    return SchedulerProvider.getPersistentScheduler();
  }

  private void checkTriggeringDate(final OffsetDateTime dateTime) {
    if (dateTime == null) {
      throw new IllegalStateException(
          "The triggering rule is invalid: the computed triggering date is null!");
    }
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
     * @param processName the name of the process the reminder MUST perform when triggered.
     * @return a {@link DurationReminder} instance.
     */
    public DurationReminder triggerBefore(final int duration, final TimeUnit timeUnit,
        final String temporalProperty, final ReminderProcessName processName) {
      return new DurationReminder(contribution, user, processName).withText(text)
          .triggerBefore(duration, timeUnit, temporalProperty);
    }

    /**
     * Triggers the reminder at the specified date time.
     * @param dateTime the {@link OffsetDateTime} at which the reminder will be triggered.
     * @param processName the name of the process the reminder MUST perform when triggered.
     * @return a {@link DateTimeReminder} instance.
     */
    public DateTimeReminder triggerAt(final OffsetDateTime dateTime,
        final ReminderProcessName processName) {
      return new DateTimeReminder(contribution, user, processName).withText(text).triggerAt(dateTime);
    }

    /**
     * Triggers the reminder from the temporal property of the contribution.
     * @param temporalProperty the temporal property of the contribution.
     * @param processName the name of the process the reminder MUST perform when triggered.
     * @return a {@link DateTimeReminder} instance.
     */
    public DateTimeReminder triggerFrom(final String temporalProperty,
        final ReminderProcessName processName) {
      return new DateTimeReminder(contribution, user, processName).withText(text).triggerFrom(temporalProperty);
    }
  }
}
