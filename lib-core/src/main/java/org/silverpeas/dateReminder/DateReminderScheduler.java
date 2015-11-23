/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.dateReminder;

import com.silverpeas.scheduler.Scheduler;
import com.silverpeas.scheduler.SchedulerEvent;
import com.silverpeas.scheduler.SchedulerEventListener;
import com.silverpeas.scheduler.SchedulerProvider;
import com.silverpeas.scheduler.trigger.JobTrigger;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.initialization.Initialization;
import org.silverpeas.util.ResourceLocator;
import org.silverpeas.EntityReference;
import org.silverpeas.dateReminder.exception.DateReminderException;
import org.silverpeas.dateReminder.persistent.DateReminderDetail;
import org.silverpeas.dateReminder.persistent.PersistentResourceDateReminder;
import org.silverpeas.dateReminder.persistent.service.DateReminderServiceProvider;
import org.silverpeas.dateReminder.provider.DateReminderProcess;
import org.silverpeas.dateReminder.provider.DateReminderProcessRegistration;
import org.silverpeas.util.SettingBundle;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Scheduler for processing <code>DateReminderProcess</code> instances.
 * @author Cécile Bonin
 * @see org.silverpeas.dateReminder.provider.DateReminderProcess
 * @see org.silverpeas.dateReminder.provider.DateReminderProcessRegistration
 */
public class DateReminderScheduler implements SchedulerEventListener, Initialization {

  public static final String DATEREMINDER_JOB_NAME_PROCESS = "A_ProcessDateReminder";

  @Override
  public void init() throws Exception {
    try {
      SettingBundle settings = ResourceLocator.getSettingBundle("org.silverpeas.dateReminder.settings.dateReminderSettings");
      String cron = settings.getString("cronScheduledDateReminder");
      Logger.getLogger(getClass().getSimpleName())
          .log(Level.INFO, "Date reminder Processor scheduled with cron ''{0}''", cron);
      Scheduler scheduler = SchedulerProvider.getScheduler();
      scheduler.unscheduleJob(DATEREMINDER_JOB_NAME_PROCESS);
      JobTrigger trigger = JobTrigger.triggerAt(cron);
      scheduler.scheduleJob(DATEREMINDER_JOB_NAME_PROCESS, trigger, this);
    } catch (Exception e) {
      SilverTrace.error("dateReminder", "ScheduledDateReminderService.initialize()",
          "dateReminder.EX_CANT_INIT_SCHEDULED_DATEREMINDER", e);
    }
  }

  /**
   * Schedule the date reminder process
   */
  public void doScheduledDateReminder() throws DateReminderException {
    SilverTrace.info("dateReminder", "ScheduledDateReminderService.doScheduledDateReminder()",
        "root.MSG_GEN_ENTER_METHOD");

    Calendar calendar = Calendar.getInstance(Locale.FRENCH);
    calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
    calendar.set(java.util.Calendar.MINUTE, 0);
    calendar.set(java.util.Calendar.SECOND, 0);
    calendar.set(java.util.Calendar.MILLISECOND, 0);
    Date deadLine = calendar.getTime();
    SilverTrace.info("dateReminder", "ScheduledDateReminderService.doScheduledDateReminder()",
        "root.MSG_GEN_PARAM_VALUE", "deadLine = " + deadLine.toString());

    Collection<PersistentResourceDateReminder> listResourceDateReminder =
        DateReminderServiceProvider.getDateReminderService().listAllDateReminderMaturing(deadLine);
    SilverTrace.info("dateReminder", "ScheduledDateReminderService.doScheduledDateReminder()",
        "root.MSG_GEN_PARAM_VALUE", "ResourceDateReminder = " + listResourceDateReminder.size());

    boolean performed = false;
    EntityReference entityReference = null;
    for (PersistentResourceDateReminder resourceDateReminder : listResourceDateReminder) {
      performed = false;

      for (DateReminderProcess dateReminderProcess : DateReminderProcessRegistration
          .getProcesses(resourceDateReminder)) {

        performed = false;
        try {
          entityReference = dateReminderProcess.perform(resourceDateReminder);
          performed = true;
        } catch (NotificationManagerException e) {
          SilverTrace
              .error("dateReminder", "ScheduledDateReminderService.doScheduledDateReminder()",
                  "dateReminder.EX_ERROR_WHILE_PERFORMING_DATEREMINDER",
                  "Type = " + resourceDateReminder.getResourceType() + ", ResourceId = " +
                      resourceDateReminder.getId());
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

    SilverTrace.info("dateReminder", "ScheduledDateReminderService.doScheduledDateReminder()",
        "root.MSG_GEN_EXIT_METHOD");
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
    SilverTrace.error("dateReminder", "ScheduledDateReminderService.jobFailed",
        "The job '" + anEvent.getJobExecutionContext().getJobName() + "' failed");
  }
}