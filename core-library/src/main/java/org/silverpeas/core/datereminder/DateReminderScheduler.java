/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.datereminder;

import org.silverpeas.core.scheduler.Scheduler;
import org.silverpeas.core.scheduler.SchedulerEvent;
import org.silverpeas.core.scheduler.SchedulerEventListener;
import org.silverpeas.core.scheduler.SchedulerProvider;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.core.notification.user.client.NotificationManagerException;
import org.silverpeas.core.datereminder.exception.DateReminderException;
import org.silverpeas.core.datereminder.persistence.DateReminderDetail;
import org.silverpeas.core.datereminder.provider.DateReminderProcessRegistration;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.persistence.EntityReference;
import org.silverpeas.core.datereminder.persistence.PersistentResourceDateReminder;
import org.silverpeas.core.datereminder.persistence.service.DateReminderServiceProvider;
import org.silverpeas.core.datereminder.provider.DateReminderProcess;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

/**
 * Scheduler for processing <code>DateReminderProcess</code> instances.
 * @author Cécile Bonin
 * @see DateReminderProcess
 * @see DateReminderProcessRegistration
 */
public class DateReminderScheduler implements SchedulerEventListener, Initialization {

  public static final String DATEREMINDER_JOB_NAME_PROCESS = "A_ProcessDateReminder";

  @Override
  public void init() throws Exception {
    try {
      SettingBundle settings = ResourceLocator.getSettingBundle("org.silverpeas.dateReminder.settings.dateReminderSettings");
      String cron = settings.getString("cronScheduledDateReminder");
      SilverLogger.getLogger(this).info("Date reminder Processor scheduled with cron ''{0}''", cron);
      Scheduler scheduler = SchedulerProvider.getScheduler();
      scheduler.unscheduleJob(DATEREMINDER_JOB_NAME_PROCESS);
      JobTrigger trigger = JobTrigger.triggerAt(cron);
      scheduler.scheduleJob(DATEREMINDER_JOB_NAME_PROCESS, trigger, this);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error("Cannot schedule date reminder", e);
    }
  }

  /**
   * Schedule the date reminder process
   */
  public void doScheduledDateReminder() throws DateReminderException {
    Calendar calendar = Calendar.getInstance(Locale.FRENCH);
    calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
    calendar.set(java.util.Calendar.MINUTE, 0);
    calendar.set(java.util.Calendar.SECOND, 0);
    calendar.set(java.util.Calendar.MILLISECOND, 0);
    Date deadLine = calendar.getTime();
    Collection<PersistentResourceDateReminder> listResourceDateReminder =
        DateReminderServiceProvider.getDateReminderService().listAllDateReminderMaturing(deadLine);
    boolean performed = false;
    EntityReference entityReference = null;
    for (PersistentResourceDateReminder resourceDateReminder : listResourceDateReminder) {
      for (DateReminderProcess dateReminderProcess : DateReminderProcessRegistration
          .getProcesses(resourceDateReminder)) {
        try {
          entityReference = dateReminderProcess.perform(resourceDateReminder);
          performed = true;
        } catch (NotificationManagerException e) {
          SilverLogger.getLogger(this).error("Date reminder failure for type = {0}, resource = {1}",
              new String[] {resourceDateReminder.getResourceType(), resourceDateReminder.getId()},
              e);
          performed = false;
        }

        if (performed) {
          //set processStatus to 1
          DateReminderDetail dateReminderDetail = resourceDateReminder.getDateReminder();
          dateReminderDetail.setProcessStatus(DateReminderDetail.REMINDER_PROCESSED);
          DateReminderServiceProvider.getDateReminderService()
              .set(entityReference, dateReminderDetail);
        }
      }
    }

  }

  @Override
  public void triggerFired(SchedulerEvent anEvent) throws Exception {
    doScheduledDateReminder();
  }

  @Override
  public void jobSucceeded(SchedulerEvent anEvent) {
  }

  @Override
  public void jobFailed(SchedulerEvent anEvent) {
    SilverLogger.getLogger(this).error("The job {0} failed",
        anEvent.getJobExecutionContext().getJobName());
  }
}