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

package org.silverpeas.core.scheduler.quartz;

import org.silverpeas.core.scheduler.trigger.CronJobTrigger;
import org.silverpeas.core.scheduler.trigger.FixedPeriodJobTrigger;
import org.silverpeas.core.scheduler.trigger.JobTriggerVisitor;
import org.quartz.CronScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.util.Date;

import static org.quartz.TriggerBuilder.newTrigger;

/**
 * A builder of a Quartz trigger from the data defined in a QuartzSchedulerJob instance. A
 * QuartzSchedulerJob is a job that will be indirectly scheduled by quartz and thus fired by a
 * Quartz trigger. As such a Quartz trigger is required for doing and it can be obtained from the
 * QuartzSchedulerJob information.
 */
public final class QuartzTriggerBuilder implements JobTriggerVisitor {

  private Trigger quartzTrigger = null;
  private String jobName = null;

  /**
   * Builds a Quartz trigger from the specified QuartzSchedulerJob instance.
   * @param job the scheduler job implementation for Quartz.
   * @return a Quartz scheduler trigger.
   */
  public static Trigger buildFrom(final QuartzSchedulerJob job) {
    QuartzTriggerBuilder visitor = new QuartzTriggerBuilder(job.getName());
    job.getTrigger().accept(visitor);
    return visitor.quartzTrigger;
  }

  @Override
  public void visit(FixedPeriodJobTrigger trigger) {
    TriggerBuilder triggerBuilder = newTrigger().withIdentity(jobName);
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
    }
    if (trigger.getStartDate() != null) {
      triggerBuilder.startAt(trigger.getStartDate());
    } else {
      triggerBuilder.startAt(quartzTrigger.getFireTimeAfter(new Date()));
    }
    quartzTrigger = triggerBuilder.build();
  }

  @Override
  public void visit(CronJobTrigger trigger) {
    QuartzCronExpression cronExpression = (QuartzCronExpression)trigger.getCronExpression();
    TriggerBuilder triggerBuilder = newTrigger().withIdentity(jobName)
        .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression.getExpression()));
    if (trigger.getStartDate() != null) {
      triggerBuilder.startAt(trigger.getStartDate());
    }
    quartzTrigger = triggerBuilder.build();
  }

  private QuartzTriggerBuilder(String jobName) {
    this.jobName = jobName;
  }
}
