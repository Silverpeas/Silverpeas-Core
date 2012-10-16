/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.silverpeas.notification.delayed.scheduler;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import com.silverpeas.scheduler.Scheduler;
import com.silverpeas.scheduler.trigger.JobTrigger;
import com.stratelia.silverpeas.notificationManager.AbstractNotification;

/**
 * @author Yohann Chastagnier
 */
@Named("delayedNotificationSchedulerInitializer")
public class DelayedNotificationSchedulerInitializer extends AbstractNotification {

  public static final String JOB_NAME = "DelayedNotificationJob";

  @Inject
  private Scheduler scheduler;

  @PostConstruct
  public void initialize() throws Exception {
    final String cron = getNotificationResources().getString("cronDelayedNotificationSending");
    if (StringUtils.isNotBlank(cron)) {
      scheduler.scheduleJob(JOB_NAME, JobTrigger.triggerAt(cron), new DelayedNotificationListener());
    }
  }
}
