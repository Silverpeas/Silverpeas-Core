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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.notification.user.delayed.scheduler;

import org.silverpeas.kernel.SilverpeasException;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.notification.user.delayed.delegate.DelayedNotificationDelegate;
import org.silverpeas.core.scheduler.SchedulerEvent;
import org.silverpeas.core.scheduler.SchedulerEventListener;
import org.silverpeas.kernel.logging.SilverLogger;

/**
 * @author Yohann Chastagnier
 */
@Service
public class DelayedNotificationListener implements SchedulerEventListener {

  /*
   * (non-Javadoc)
   * @see SchedulerEventListener#triggerFired(SchedulerEvent)
   */
  @Override
  public void triggerFired(final SchedulerEvent anEvent) throws SilverpeasException {
    try {
      DelayedNotificationDelegate.executeDelayedNotificationsSending(
          anEvent.getJobExecutionContext().getFireTime());
    } catch (Exception e) {
      throw new SilverpeasException(e);
    }
  }

  /*
   * (non-Javadoc)
   * @see SchedulerEventListener#jobSucceeded(SchedulerEvent)
   */
  @Override
  public void jobSucceeded(final SchedulerEvent anEvent) {
    // nothing to do
  }

  /*
   * (non-Javadoc)
   * @see SchedulerEventListener#jobFailed(SchedulerEvent)
   */
  @Override
  public void jobFailed(final SchedulerEvent anEvent) {
    SilverLogger.getLogger(this)
        .error(
            "The job '" + anEvent.getJobExecutionContext().getJobName() + "' was not successfull",
            anEvent.getJobThrowable());
  }
}
