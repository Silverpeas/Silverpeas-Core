/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
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
package org.silverpeas.core.util;

import org.silverpeas.core.util.logging.SilverLogger;

/**
 * This class embeds a logic of reattempting the execution of jobs when an exception is thrown
 * during a job execution.
 * It provides a simple retry mechanism.
 */
public class ExecutionAttempts {

  /**
   * Wraps a job with a retry mechanism.
   * @param maxAttempts the maximum number of execution attempts that are authorized before
   * throwing really the exception.
   * @param aJob the job to run within a retry mechanism.
   * @throws Exception the exception the job has thrown when the maximum execution attempts has been reached.
   */
  public static void retry(int maxAttempts, final Job aJob) throws Exception {
    int attempts = 1;
    while (true) {
      try {
        aJob.execute();
        break;
      } catch (Exception ex) {
        attempts++;
        SilverLogger.getLogger(ExecutionAttempts.class).warn("Execution of job {0} failed. Try once more",
            aJob.getClass().getSimpleName());
        if (attempts > maxAttempts) {
          throw ex;
        }
      }
    }
  }

  /**
   * The interface a job within a retry mechanism should implements.
   */
  public interface Job {

    /**
     * Executes the job.
     * @throws Exception if an error occurs while executing the job.
     */
    void execute() throws Exception;
  }
}
