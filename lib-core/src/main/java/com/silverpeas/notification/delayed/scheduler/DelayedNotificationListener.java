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
package com.silverpeas.notification.delayed.scheduler;

import com.silverpeas.notification.delayed.delegate.DelayedNotificationDelegate;
import com.silverpeas.scheduler.SchedulerEvent;
import com.silverpeas.scheduler.SchedulerEventListener;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * @author Yohann Chastagnier
 */
public class DelayedNotificationListener implements SchedulerEventListener {

  /*
   * (non-Javadoc)
   * @see com.silverpeas.scheduler.SchedulerEventListener#triggerFired(com.silverpeas.scheduler.SchedulerEvent)
   */
  @Override
  public void triggerFired(final SchedulerEvent anEvent) throws Exception {
    SilverTrace.debug("notification", "DelayedNotificationListener.handleSchedulerEvent", "The job '" +
        anEvent.getJobExecutionContext().getJobName() + "' is executing");
    
    DelayedNotificationDelegate.executeDelayedNotificationsSending(anEvent.getJobExecutionContext().getFireTime());
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.scheduler.SchedulerEventListener#jobSucceeded(com.silverpeas.scheduler.SchedulerEvent)
   */
  @Override
  public void jobSucceeded(final SchedulerEvent anEvent) {
    SilverTrace.debug("notification", "DelayedNotificationListener.handleSchedulerEvent", "The job '" +
        anEvent.getJobExecutionContext().getJobName() + "' was successfull");
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.scheduler.SchedulerEventListener#jobFailed(com.silverpeas.scheduler.SchedulerEvent)
   */
  @Override
  public void jobFailed(final SchedulerEvent anEvent) {
    SilverTrace.error("notification", "DelayedNotificationListener.handleSchedulerEvent", "The job '" +
        anEvent.getJobExecutionContext().getJobName() + "' was not successfull", anEvent.getJobThrowable());
  }
}
