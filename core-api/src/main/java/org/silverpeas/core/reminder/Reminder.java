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
import org.silverpeas.core.calendar.Plannable;
import org.silverpeas.core.contribution.ContributionManager;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.date.TimeUnit;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.BasicJpaEntity;
import org.silverpeas.core.scheduler.SchedulerProvider;
import org.silverpeas.core.scheduler.trigger.JobTrigger;

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
  private String text;
  private int delay;
  private TimeUnit timeUnit;

  /**
   * Constructs a new reminder for the specified contribution.
   * @param contributionId the unique identifier of a contribution.
   */
  public Reminder(final ContributionIdentifier contributionId) {
    this.contributionId = contributionId;
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
   * Triggers this reminder at the specified delay. This type of trigger can only be used with
   * TimeLinePositionable object. The reminder is then scheduled and will be triggered at the
   * specified delay.
   * @param delay the time elapsed since the start date of a TimeLinePositionable object
   * @param timeUnit the time unit in which is expressed the delay.
   * @return itself.
   */
  public Reminder triggerBefore(final int delay, final TimeUnit timeUnit) {
    this.delay = delay;
    this.timeUnit = timeUnit;
    Plannable contribution = getPlannableContribution();
    Temporal startDate = contribution.getStartDate();
    startDate.minus(delay, timeUnit.toChronoUnit());
    return Transaction.getTransaction().perform(() -> {
      Reminder me = ReminderRepository.get().save(this);
      /*SchedulerProvider.getScheduler()
          .scheduleJob("Reminder#" + me.getId(), JobTrigger.tr) */
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
    return SchedulerProvider.getScheduler().isJobScheduled(SCHEDULED_JOB_NAME.format(getId()));
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
  