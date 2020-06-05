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

import org.silverpeas.core.backgroundprocess.AbstractBackgroundProcessRequest;
import org.silverpeas.core.backgroundprocess.BackgroundProcessTask;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.scheduler.SchedulerEvent;
import org.silverpeas.core.scheduler.SchedulerEventListener;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.silverpeas.core.reminder.BackgroundReminderProcess.Constants.PROCESS_NAME_SUFFIX;

/**
 * The process to send a notification to the user aimed by a reminder.
 * @author mmoquillon
 */
@Singleton
public class ReminderProcess implements SchedulerEventListener {

  @Inject
  private ReminderRepository repository;

  public static ReminderProcess get() {
    return ServiceProvider.getSingleton(ReminderProcess.class);
  }

  @Override
  public void triggerFired(final SchedulerEvent anEvent) {
    final String reminderId = anEvent.getJobExecutionContext().getJobName();
    final Reminder reminder = repository.getById(reminderId);
    reminder.triggered();
    notifyUserAbout(reminder);
    if (reminder.isSchedulable()) {
      Transaction.performInOne(() -> repository.save(reminder));
      reminder.schedule();
    } else if (reminder.isSystemUser()) {
      Transaction.performInOne(() -> {
        repository.delete(reminder);
        return null;
      });
    } else {
      Transaction.performInOne(() -> repository.save(reminder));
    }
  }

  @Override
  public void jobSucceeded(final SchedulerEvent anEvent) {
    final String reminderId = anEvent.getJobExecutionContext().getJobName();
    SilverLogger.getLogger(this).info("The reminder {0} was correctly fired", reminderId);
  }

  @Override
  public void jobFailed(final SchedulerEvent anEvent) {
    final String reminderId = anEvent.getJobExecutionContext().getJobName();
    if (anEvent.isExceptionThrown()) {
      final Throwable throwable = anEvent.getJobThrowable();
      SilverLogger.getLogger(this).error("The reminder " + reminderId + " firing failed", throwable);
    } else {
      SilverLogger.getLogger(this).error("The reminder " + reminderId + " firing failed");
    }
  }

  private void notifyUserAbout(final Reminder reminder) {
    final Reminder reminderCopy = reminder.clone();
    BackgroundProcessTask.push(new BackgroundReminderUserNotificationProcess(reminderCopy));
  }

  /**
   * Background process request which ensure that the reminder scheduler will not be disturbed
   * processes as they will be processed one by one.
   */
  private static class BackgroundReminderUserNotificationProcess extends AbstractBackgroundProcessRequest {

    private final Reminder reminder;

    private BackgroundReminderUserNotificationProcess(final Reminder reminder) {
      super();
      this.reminder = reminder;
    }

    @Override
    protected void process() {
      final String fullProcessName = reminder.getProcessName() + PROCESS_NAME_SUFFIX;
      final BackgroundReminderProcess process = ServiceProvider.getService(fullProcessName);
      process.performWith(reminder);
    }
  }
}
  