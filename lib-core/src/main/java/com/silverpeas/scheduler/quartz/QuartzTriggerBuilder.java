/*
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.scheduler.quartz;

import com.silverpeas.scheduler.trigger.CronJobTrigger;
import com.silverpeas.scheduler.trigger.FixedPeriodJobTrigger;
import com.silverpeas.scheduler.trigger.JobTriggerVisitor;
import java.text.ParseException;
import java.util.Date;
import org.quartz.CronTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;

/**
 * A builder of a Quartz trigger from the data defined in a QuartzSchedulerJob instance.
 *
 * A QuartzSchedulerJob is a job that will be indirectly scheduled by quartz and thus fired by
 * a Quartz trigger. As such a Quartz trigger is required for doing and it can be obtained from the
 * QuartzSchedulerJob information.
 */
public final class QuartzTriggerBuilder implements JobTriggerVisitor {

  private Trigger quartzTrigger = null;

  /**
   * Builds a Quartz trigger from the specified QuartzSchedulerJob instance.
   * @param the Quartz scheduler job with the data required to build a Quartz trigger that will fire
   * it.
   * @return a Quartz scheduler trigger.
   * @throws Exception if the build of a Quartz trigger failed.
   */
  public Trigger buildFrom(final QuartzSchedulerJob job) {
    job.getTrigger().accept(this);
    quartzTrigger.setName(job.getName());
    return quartzTrigger;
  }

  @Override
  public void visit(FixedPeriodJobTrigger trigger) {
    switch (trigger.getTimeUnit()) {
      case SECOND:
        quartzTrigger = TriggerUtils.makeSecondlyTrigger(trigger.getTimeInterval());
        break;
      case MINUTE:
        quartzTrigger = TriggerUtils.makeMinutelyTrigger(trigger.getTimeInterval());
        break;
      case HOUR:
        quartzTrigger = TriggerUtils.makeHourlyTrigger(trigger.getTimeInterval());
        break;
    }
    if (trigger.getStartDate() != null) {
      quartzTrigger.setStartTime(trigger.getStartDate());
    } else {
      quartzTrigger.setStartTime(quartzTrigger.getFireTimeAfter(new Date()));
    }
  }

  @Override
  public void visit(CronJobTrigger trigger) {
    try {
      CronTrigger cronTrigger = new CronTrigger();
      cronTrigger.setCronExpression("* " + trigger.getCronExpression());
      if (trigger.getStartDate() != null) {
        cronTrigger.setStartTime(trigger.getStartDate());
      }
      quartzTrigger = cronTrigger;
    } catch (ParseException ex) {
      throw new RuntimeException(ex.getMessage(), ex);
    }
  }
}
