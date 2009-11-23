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

import java.lang.reflect.*;

/**
 * This class extends the class 'SchedulerJob' for the functionality of a scheduled execution of a
 * class method.
 */
public class SchedulerMethodJob extends SchedulerJob {
  Object methodOwner;
  Method executionMethod;

  /**
   * The constructor has proteceted access, because the generation of jobs should be done in a
   * central way by the class 'SimpleScheduler'
   * @param aController The controller, that controls all job executions
   * @param aOwner The owner of the job
   * @param aJobName The name of the job
   * @param aLogBaseFile The log file for the job
   */
  protected SchedulerMethodJob(SimpleScheduler theJobController,
      SchedulerEventHandler theJobOwner, String theJobName)
      throws SchedulerException {
    super(theJobController, theJobOwner, theJobName);
  }

  /**
   * This method sets the execution parameter. The given execution method have to handle two
   * parameter (PrintStream, Date)
   * @param aMethodOwner The owner object of the execution method
   * @param aExecutionMethodName The name of a method for the execution logic (Arguments must be
   * PrintStream and Date)
   */
  protected synchronized void setExecutionParameter(Object aMethodOwner,
      String aExecutionMethodName) throws SchedulerException {
    // Check methodOwner
    if (aMethodOwner == null) {
      throw new SchedulerException(
          "SchedulerMethodJob.setExecutionParameter: Parameter 'aMethodOwner' is null");
    }

    // Check method
    if (aExecutionMethodName == null) {
      throw new SchedulerException(
          "SchedulerMethodJob.setExecutionParameter: Parameter 'aExecutionMethodName' is null");
    }

    // Set the method owner
    methodOwner = aMethodOwner;

    // Get the execution method
    try {
      Class argumentTypes[] = { Class.forName("java.util.Date") };
      executionMethod = methodOwner.getClass().getMethod(aExecutionMethodName,
          argumentTypes);
      if (executionMethod == null) {
        throw new Exception("There is no method '" + aExecutionMethodName
            + " (java.io.PrintStream,java.util.Date)' in the class '"
            + methodOwner.getClass().getName() + "'");
      }
    } catch (Exception aException) {
      throw new SchedulerException(
          "SchedulerMethodJob.setExecutionParameter: Getting the execution method fails (Msg: "
          + aException.toString() + ")");
    }
  }

  /**
   * This method implements the abstract method of the base class. It only routes the parameter the
   * the stored execution method.
   * @param log A PrintStream for text writings in the log file for this job
   * @param theExecutionDate The date of the execution
   */
  protected void execute(Date theExecutionDate) throws SchedulerException {
    Object parameters[] = { theExecutionDate };

    try {
      executionMethod.invoke(methodOwner, parameters);
    } catch (IllegalArgumentException eException) {
      throw new SchedulerException(
          "SchedulerMethodJob.execute: The arguments of the execution method have the wrong type");
    } catch (IllegalAccessException eException) {
      throw new SchedulerException(
          "SchedulerMethodJob.execute: The execution method can not be accessed");
    } catch (InvocationTargetException eException) {
      throw new SchedulerException(
          "SchedulerMethodJob.execute: Common invocation exception");
    }
  }
}
