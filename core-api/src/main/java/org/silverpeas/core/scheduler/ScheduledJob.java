/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.scheduler;

import java.util.Date;

/**
 * A job that is scheduled in the scheduler. A job registered in the scheduler is instanciated into
 * a ScheduledJob object that carries all of the information required by the scheduler to perform
 * its task. The implementation of this interface depends upon the scheduling backend in use.
 */
public interface ScheduledJob {

  /**
   * Gets the name under which the job is registered into the scheduler.
   * @return the name of the job.
   */
  String getName();

  /**
   * Gets the next time at which the execution of this job will be fired. The next execution time
   * depends upon the trigger with which it is registered in the scheduler.
   * @return the next time at which this job should be executed.
   */
  Date getNextExecutionTime();

}
