/**
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.silverpeas.scheduler;

import java.util.*;

/**
 * This class extends the class 'SchedulerJob' for the functionality of a
 * scheduled execution of a class method.
 */
public class SchedulerMethodJobMinute extends SchedulerMethodJob {
  long m_iMs = 1000;

  /**
   * The constructor has proteceted access, because the generation of jobs
   * should be done in a central way by the class 'SimpleScheduler'
   * 
   * @param aController
   *          The controller, that controls all job executions
   * @param aOwner
   *          The owner of the job
   * @param aJobName
   *          The name of the job
   * @param aLogBaseFile
   *          The log file for the job
   */
  protected SchedulerMethodJobMinute(SimpleScheduler theJobController,
      SchedulerEventHandler theJobOwner, String theJobName, int iMinutes)
      throws SchedulerException {
    super(theJobController, theJobOwner, theJobName);
    m_iMs = (long) iMinutes * (long) 60 * (long) 1000;
  }

  protected long getNextTimeStamp() {
    return ((new Date()).getTime() + m_iMs);
  }
}
