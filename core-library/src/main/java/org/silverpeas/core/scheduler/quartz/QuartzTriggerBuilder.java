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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.scheduler.quartz;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.silverpeas.core.scheduler.trigger.CronJobTrigger;
import org.silverpeas.core.scheduler.trigger.FixedDateTimeJobTrigger;
import org.silverpeas.core.scheduler.trigger.FixedPeriodJobTrigger;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.core.scheduler.trigger.JobTriggerVisitor;

import static org.quartz.TriggerBuilder.newTrigger;

/**
 * A builder of a Quartz {@link Trigger} for a given job and from a {@link JobTrigger} instance
 * representing a triggering rules in Silverpeas. In Quartz, a trigger is always related to a
 * given job and a job can have one or more triggers. In Silverpeas, a trigger isn't related to
 * a specific job and it represents only a job triggering rule that can be used to schedule one or
 * more job; hence a job in Silverpeas is always related to one and only one trigger. The builder
 * converts the {@link JobTrigger} representation in Silverpeas into a {@link Trigger}
 * representation in Quartz for the job the trigger will be used to schedule it.
 */
public final class QuartzTriggerBuilder implements JobTriggerVisitor {

  private Trigger quartzTrigger = null;
  private final String jobName;

  /**
   * Constructs a new Quartz {@link Trigger} builder for the specified job name to which it will
   * be related in the Quartz scheduler.
   * @param jobName the name of a job to schedule.
   * @return a {@link QuartzTriggerBuilder} instance.
   */
  public static QuartzTriggerBuilder forJob(final String jobName) {
    return new QuartzTriggerBuilder(jobName);
  }

  /**
   * Builds from the {@link JobTrigger} instance a {@link Trigger} object to be used to schedule
   * the underlying job with a Quartz scheduler.
   * @param jobTrigger the {@link JobTrigger} to convert.
   * @return a {@link Trigger} object.
   */
  public Trigger buildFrom(final JobTrigger jobTrigger) {
    jobTrigger.accept(this);
    return quartzTrigger;
  }

  @Override
  public void visit(FixedPeriodJobTrigger trigger) {
    TriggerBuilder<Trigger> triggerBuilder = newTrigger().withIdentity(jobName);
    switch (trigger.getTimeUnit()) {
      case SECOND:
        triggerBuilder.withSchedule(
            SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(trigger.getTimeInterval())
                .repeatForever());
        break;
      case MINUTE:
        triggerBuilder.withSchedule(
            SimpleScheduleBuilder.simpleSchedule().withIntervalInMinutes(trigger.getTimeInterval())
                .repeatForever());
        break;
      case HOUR:
        triggerBuilder.withSchedule(
            SimpleScheduleBuilder.simpleSchedule().withIntervalInHours(trigger.getTimeInterval())
                .repeatForever());
        break;
      default:
        // no repeat scheduling
        break;
    }
    if (trigger.getStartDate() != null) {
      triggerBuilder.startAt(trigger.getStartDate());
    } else {
      triggerBuilder.startNow();
    }
    quartzTrigger = triggerBuilder.forJob(jobName).build();
  }

  @Override
  public void visit(CronJobTrigger trigger) {
    QuartzCronExpression cronExpression = (QuartzCronExpression)trigger.getCronExpression();
    TriggerBuilder<CronTrigger> triggerBuilder = newTrigger().withIdentity(jobName).forJob
        (jobName).withSchedule(CronScheduleBuilder.cronSchedule(cronExpression.getExpression()));
    if (trigger.getStartDate() != null) {
      triggerBuilder.startAt(trigger.getStartDate());
    }
    quartzTrigger = triggerBuilder.build();
  }

  @Override
  public void visit(final FixedDateTimeJobTrigger trigger) {
    TriggerBuilder triggerBuilder = newTrigger().withIdentity(jobName).forJob(jobName).startAt
        (trigger.getStartDate());
    quartzTrigger = triggerBuilder.build();
  }

  private QuartzTriggerBuilder(String jobName) {
    this.jobName = jobName;
  }
}
