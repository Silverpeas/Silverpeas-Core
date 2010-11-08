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
 * FLOSS exception.  You should have received a copy of the text describing
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

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <P>
 * This is the controlling class for all scheduling jobs. It works like a factory. The only way to
 * create a job is the usage of the method 'scheduleJob'. The parameter signature controls the needed job
 * type (script, method or event). An example:
 * </P>
 * <CODE>
 *     .....<BR>
 *     SimpleScheduler.scheduleJob (jobOwner, "Hello", "0 2 * * *");<BR>
 *     .....<BR>
 * </CODE>
 * <P>
 * This simple line creates a new controlling instance (only once) and a new event job. The job
 * owner (or creator) have to handle SchedulerEvents of the type 'EXECUTION'. In this example is it
 * the object 'jobOwner', that gets ervery day at 2:00 events from the job with the name 'Hello'.
 * The timestamp of execution is described by a cron string (here: '0 2 * * *'). This simple string
 * is divided into five fields (separated by spaces): minute, hour, day of month, month and day of
 * week. The '*' meens <B>every</B> minute, hour,.... So it is a wildcard. If you do not like this
 * method, every job type has a generator method, that uses five Vectors to describe a timestamp.
 * Have a look at the methods for the details.
 * </P>
 * <P>
 * Let us have a look at the script jobs:
 * </P>
 * <CODE>
 *     .....<BR>
 *     SimpleScheduler.scheduleJob (jobOwner, "Hello", "0 2 * * *", "/home/tb/data/Project/java/SimpleScheduler/prj/test.sh");<BR>
 *     .....<BR>
 * </CODE>
 * <P>
 * There is only one additional parameter: The path to the script. Additional parameter to the
 * script must be separated by spaces. It is not a shell, so piping, redirections etc. given as
 * parameter are not implemented. If you need this, you have to realize it inside the sript.
 * </P>
 * <P>
 * What about the method jobs?
 * </P>
 * <CODE>
 *    .....<BR>
 * 	SimpleScheduler.scheduleJob (jobOwner, "Moin1", "0 2 * * *", methodImplementer, "test");<BR>
 *     .....<BR>
 * </CODE>
 * <P>
 * Looks more complicated? No, it isn't. The first three parameter are identical with the
 * corresponding parameter of the script job. The following parameter stand for the 'action'. The
 * object 'methodImplementer' holds a method with the name 'test'. This method has to have two
 * parameters (argumenttypes: PrintStream, Date). And? Is it realy complicated?
 * </P>
 * <P>
 * This class creates a log file for every job. The location of the files could be controlled by a
 * property file ('SimpleScheduler.properties'). It is searched in the home directories of the
 * application and the current user. Additionaly there is the method 'setLogDirectory'. This method
 * has to be called <B>before</B> any job is created. If nothing works, the log files will be placed
 * in [user home]/.SimpleScheduler/logs.
 * </P>
 */
public class SimpleScheduler {

  private static SimpleScheduler theSimpleScheduler;
  // Object variables
  private final Map<SchedulerEventHandler, List<SchedulerJob>> htJobs;
  private final Set<String> jobNames = new HashSet<String>();

  private static void initScheduler() {
    synchronized (SimpleScheduler.class) {
      if (theSimpleScheduler == null) {
        theSimpleScheduler = new SimpleScheduler();
      }
    }
  }

  /**
   * This method creates a job that fires a SchedulerEvent of the type 'EXECUTION'. The timestamp is
   * given in minutes
   * @param aJobOwner The owner of the created job
   * @param aJobName The name of the created job
   * @param iMinutes The minutes
   * @return A new job
   * @throws SchedulerException
   */
  public static SchedulerJob scheduleJob(SchedulerEventHandler aJobOwner,
      String aJobName, int iMinutes) throws SchedulerException {
    initScheduler();
    if (aJobOwner == null) {
      throw new SchedulerException(
          "SimpleScheduler.getJob: Parameter 'aJobOwner' is null");
    }
    if (aJobName == null) {
      throw new SchedulerException(
          "SimpleScheduler.getJob: Parameter 'aJobName' is null");
    }
    if (!theSimpleScheduler.checkJobName(aJobName)) {
      throw new SchedulerException(
          "SimpleScheduler.getJob: Parameter 'aJobName' conflicts with other job names");
    }
    SchedulerEventJob newJob = new SchedulerEventJobMinute(theSimpleScheduler, aJobOwner,
        aJobName, iMinutes);
    return theSimpleScheduler.addJob(aJobOwner, newJob);
  }

  /**
   * This method creates a job that fires a SchedulerEvent of the type 'EXECUTION'. The timestamp is
   * given by a cron like string (currently ranges are not allowed). So the string '* 3,21 * 3 0'
   * starts the execution of the given command every Sunday in March at 03:00 and 21:00. The allowed
   * ranges are: minutes (0-59), hours (0-23), days of a month (1-31), months (1-12; starts with 1
   * for January), day of a week (0-6; starts with 0 for Sunday). Currently the parser for the cron
   * string is not very flexible, so have a look at the syntax.
   * @param aJobOwner The owner of the created job
   * @param aJobName The name of the created job
   * @param aCronString A cron like string ([*|NUM{,NUM}] [*|NUM{,NUM}] [*|NUM{,NUM}] [*|NUM{,NUM}]
   * [*|NUM{,NUM}])
   * @return A new job
   * @throws SchedulerException 
   */
  public static SchedulerJob scheduleJob(SchedulerEventHandler aJobOwner,
      String aJobName, String aCronString) throws SchedulerException {
    initScheduler();
    if (aJobOwner == null) {
      throw new SchedulerException(
          "SimpleScheduler.getJob: Parameter 'aJobOwner' is null");
    }
    if (aJobName == null) {
      throw new SchedulerException(
          "SimpleScheduler.getJob: Parameter 'aJobName' is null");
    }
    if (!theSimpleScheduler.checkJobName(aJobName)) {
      throw new SchedulerException(
          "SimpleScheduler.getJob: Parameter 'aJobName' conflicts with other job names");
    }
    SchedulerEventJob newJob = new SchedulerEventJob(theSimpleScheduler, aJobOwner, aJobName);
    newJob.setSchedulingParameter(aCronString);
    return theSimpleScheduler.addJob(aJobOwner, newJob);
  }

  /**
   * This method creates a job that fires a SchedulerEvent of the type 'EXECUTION'. The time
   * settings are given by vectors. Each vector holds a list of Integer objects. Every Integer
   * represents a element of a timestamp (cron like). Emty Vectors matches all elements of the
   * domain.
   * @param aJobOwner The owner of the created job
   * @param aJobName The name of the created job
   * @param startMinutes A list of minutes (0-59)
   * @param startHours A list of hours (0-23)
   * @param startDaysOfMonth A list of days of a month (1-31)
   * @param startMonths A list of months (1-12; starts with 1 for January)
   * @param startDaysOfWeek A list of day of a week (0-6; starts with 0 for Sunday)
   * @return A new job
   * @throws SchedulerException
   */
  public static SchedulerJob scheduleJob(SchedulerEventHandler aJobOwner,
      String aJobName, List<Integer> startMinutes, List<Integer> startHours,
      List<Integer> startDaysOfMonth, List<Integer> startMonths, List<Integer> startDaysOfWeek)
      throws SchedulerException {
    initScheduler();
    if (aJobOwner == null) {
      throw new SchedulerException(
          "SimpleScheduler.getJob: Parameter 'aJobOwner' is null");
    }
    if (aJobName == null) {
      throw new SchedulerException(
          "SimpleScheduler.getJob: Parameter 'aJobName' is null");
    }
    if (!theSimpleScheduler.checkJobName(aJobName)) {
      throw new SchedulerException(
          "SimpleScheduler.getJob: Parameter 'aJobName' conflicts with internal names");
    }
    SchedulerEventJob newJob = new SchedulerEventJob(theSimpleScheduler, aJobOwner, aJobName);
    newJob.setSchedulingParameter(startMinutes, startHours, startDaysOfMonth, startMonths,
        startDaysOfWeek);
    return theSimpleScheduler.addJob(aJobOwner, newJob);
  }

  /**
   * This method creates a job that executes a class method. The timestamp is given by a cron like
   * string (currently ranges are not allowed). So the string '* 3,21 * 3 0' starts the given method
   * every Sunday in March at 03:00 and 21:00. The allowed ranges are: minutes (0-59), hours (0-23),
   * days of a month (1-31), months (1-12; starts with 1 for January), day of a week (0-6; starts
   * with 0 for Sunday). Currently the parser for the cron string is not very flexible, so have a
   * look at the syntax.
   * @param aJobOwner The owner of the created job
   * @param aJobName The name of the created job
   * @param aCronString A cron like string ([*|NUM{,NUM}] [*|NUM{,NUM}] [*|NUM{,NUM}] [*|NUM{,NUM}]
   * [*|NUM{,NUM}])
   * @param aMethodOwner The owner object of the execution method
   * @param aExecutionMethodName The name of a method for the execution logic (Arguments must be
   * PrintStream and Date)
   * @return A new job
   * @throws SchedulerException
   */
  public static SchedulerJob scheduleJob(SchedulerEventHandler aJobOwner, String aJobName,
      String aCronString, Object aMethodOwner, String aExecutionMethodName)
      throws SchedulerException {
    initScheduler();
    if (aJobOwner == null) {
      throw new SchedulerException(
          "SimpleScheduler.getJob: Parameter 'aJobOwner' is null");
    }
    if (aJobName == null) {
      throw new SchedulerException(
          "SimpleScheduler.getJob: Parameter 'aJobName' is null");
    }
    if (!theSimpleScheduler.checkJobName(aJobName)) {
      throw new SchedulerException(
          "SimpleScheduler.getJob: Parameter 'aJobName' conflicts with other job names");
    }
    SchedulerMethodJob newJob = new SchedulerMethodJob(theSimpleScheduler, aJobOwner, aJobName);
    newJob.setSchedulingParameter(aCronString);
    newJob.setExecutionParameter(aMethodOwner, aExecutionMethodName);
    return theSimpleScheduler.addJob(aJobOwner, newJob);
  }

  /**
   * Same as previous but the job's first nextTime can be initialized or 0
   * @param aJobOwner
   * @param aJobName
   * @param aCronString
   * @param initialNextTime
   * @param aExecutionMethodName
   * @param aMethodOwner
   * @return
   * @throws SchedulerException
   */
  public static SchedulerJob scheduleJob(SchedulerEventHandler aJobOwner, String aJobName,
      String aCronString, Object aMethodOwner, String aExecutionMethodName, long initialNextTime)
      throws SchedulerException {
    initScheduler();
    if (aJobOwner == null) {
      throw new SchedulerException(
          "SimpleScheduler.getJob: Parameter 'aJobOwner' is null");
    }
    if (aJobName == null) {
      throw new SchedulerException(
          "SimpleScheduler.getJob: Parameter 'aJobName' is null");
    }
    if (!theSimpleScheduler.checkJobName(aJobName)) {
      throw new SchedulerException(
          "SimpleScheduler.getJob: Parameter 'aJobName' conflicts with other job names");
    }
    SchedulerMethodJob newJob = new SchedulerMethodJob(theSimpleScheduler, aJobOwner, aJobName);
    newJob.setSchedulingParameter(aCronString);
    newJob.setExecutionParameter(aMethodOwner, aExecutionMethodName);
    newJob.initTimeStamp(initialNextTime);
    return theSimpleScheduler.addJob(aJobOwner, newJob);
  }

  /**
   * This method creates a job that executes a class method. The time settings are given by minute.
   * The given execution method has to handle two parameter (PrintStream, Date)
   * @param aJobOwner The owner of the created job
   * @param aJobName The name of the created job
   * @param iMinutes
   * @param aMethodOwner The owner object of the execution method
   * @param aExecutionMethodName The name of a method for the execution logic (Arguments must be
   * PrintStream and Date)
   * @return A new job
   * @throws SchedulerException
   */
  public static SchedulerJob scheduleJob(SchedulerEventHandler aJobOwner,
      String aJobName, int iMinutes, Object aMethodOwner,
      String aExecutionMethodName) throws SchedulerException {
    initScheduler();
    if (aJobOwner == null) {
      throw new SchedulerException(
          "SimpleScheduler.getJob: Parameter 'aJobOwner' is null");
    }
    if (aJobName == null) {
      throw new SchedulerException(
          "SimpleScheduler.getJob: Parameter 'aJobName' is null");
    }
    if (!theSimpleScheduler.checkJobName(aJobName)) {
      throw new SchedulerException(
          "SimpleScheduler.getJob: Parameter 'aJobName' conflicts with internal names");
    }
    SchedulerMethodJob newJob = new SchedulerMethodJobMinute(theSimpleScheduler, aJobOwner,
        aJobName, iMinutes);
    newJob.setExecutionParameter(aMethodOwner, aExecutionMethodName);
    return theSimpleScheduler.addJob(aJobOwner, newJob);
  }

  /**
   * This method creates a job that executes a class method. The time settings are given by vectors.
   * Each vector holds a list of Integer objects. Every Integer represents a element of a timestamp
   * (cron like). Emty Vectors matches all elements of the domain. The given execution method has to
   * handle two parameter (PrintStream, Date)
   * @param aJobOwner The owner of the created job
   * @param aJobName The name of the created job
   * @param startMinutes A list of minutes (0-59)
   * @param startHours A list of hours (0-23)
   * @param startDaysOfMonth A list of days of a month (1-31)
   * @param startMonths A list of months (1-12; starts with 1 for January)
   * @param startDaysOfWeek A list of day of a week (0-6; starts with 0 for Sunday)
   * @param aMethodOwner The owner object of the execution method
   * @param aExecutionMethodName The name of a method for the execution logic (Arguments must be
   * PrintStream and Date)
   * @return A new job
   * @throws SchedulerException
   */
  public static SchedulerJob scheduleJob(SchedulerEventHandler aJobOwner, String aJobName,
      List<Integer> startMinutes, List<Integer> startHours, List<Integer> startDaysOfMonth,
      List<Integer> startMonths, List<Integer> startDaysOfWeek, Object aMethodOwner,
      String aExecutionMethodName) throws SchedulerException {
    initScheduler();

    if (aJobOwner == null) {
      throw new SchedulerException(
          "SimpleScheduler.getJob: Parameter 'aJobOwner' is null");
    }
    if (aJobName == null) {
      throw new SchedulerException(
          "SimpleScheduler.getJob: Parameter 'aJobName' is null");
    }
    if (!theSimpleScheduler.checkJobName(aJobName)) {
      throw new SchedulerException(
          "SimpleScheduler.getJob: Parameter 'aJobName' conflicts with internal names");
    }
    SchedulerMethodJob newJob = new SchedulerMethodJob(theSimpleScheduler, aJobOwner, aJobName);
    newJob.setSchedulingParameter(startMinutes, startHours, startDaysOfMonth,
        startMonths, startDaysOfWeek);
    newJob.setExecutionParameter(aMethodOwner, aExecutionMethodName);
    return theSimpleScheduler.addJob(aJobOwner, newJob);
  }

  /**
   * This method returns a list of the jobs of the given job owner
   * @param aJobOwner A job owner
   * @return A list of the jobs of the given job owner
   * @throws SchedulerException
   */
  public static List<SchedulerJob> getJobList(SchedulerEventHandler aJobOwner)
      throws SchedulerException {
    initScheduler();
    synchronized (SimpleScheduler.class) {
      if (theSimpleScheduler.htJobs.get(aJobOwner) != null) {
        return new ArrayList<SchedulerJob>(theSimpleScheduler.htJobs.get(aJobOwner));
      }
      return new ArrayList<SchedulerJob>();
    }
  }

  /**
   * This method removes a job
   * @param aJobOwner A job owner
   * @param aJobName A job name
   */
  public static void unscheduleJob(SchedulerEventHandler aJobOwner, String aJobName) {
    initScheduler();
    synchronized (SimpleScheduler.class) {
      List<SchedulerJob> jobList = theSimpleScheduler.htJobs.get(aJobOwner);
      if (jobList != null && !jobList.isEmpty()) {
        for (SchedulerJob workJob : jobList) {
          if (workJob.getJobName().equals(aJobName)) {
            theSimpleScheduler.removeJob(workJob);
            return;
          }
        }
      }
    }
  }

  /**
   * This method removes a job
   * @param aJobOwner A job owner
   * @param aJob the job to be removed.
   */
  public static void unscheduleJob(SchedulerEventHandler aJobOwner, SchedulerJob aJob) {
    initScheduler();
    theSimpleScheduler.removeJob(aJob);
  }

  /**
   * This method removes all jobs of the given job owner
   * @param aJobOwner The job owner, whitch jobs should be removed
   */
  public static void unscheduleAllJobs(SchedulerEventHandler aJobOwner) {
    initScheduler();
    synchronized (SimpleScheduler.class) {
      List<SchedulerJob> jobList = theSimpleScheduler.htJobs.get(aJobOwner);
      if (jobList != null) {
        while (!jobList.isEmpty()) {
          theSimpleScheduler.removeJob(jobList.get(0));
        }
      }
    }
  }

  /**
   * This method kills all active jobs. The unique instance of the SimpleScheduler will be
   * destroyed.
   */
  public synchronized static void shutdown() {
    SilverTrace.debug("scheduler", "SimpleScheduler",
        "-------------------- SimpleScheduler shutdown --------------------",
        new Exception("ForStack"));
    if (theSimpleScheduler != null) {
      theSimpleScheduler.stopAllJobs();
      theSimpleScheduler = null;
    }
  }

  /**
   * The constructor is private because it will be created internal.
   */
  private SimpleScheduler() {
    this(null);
  }

  /**
   * The constructor is private because it will be created internal
   * @param aBasePath The path to the directory where the logfiles will be created
   */
  private SimpleScheduler(File aBasePath) {
    htJobs = new HashMap<SchedulerEventHandler, List<SchedulerJob>>();
    SilverTrace.debug("scheduler", "SimpleScheduler",
        "-------------------- SimpleScheduler started --------------------");
  }

  /**
   * This method adds a job to the internal list of jobs an starts the job
   * @param aJobOwner A job owner
   * @param aNewJob A new job
   */
  private synchronized SchedulerJob addJob(SchedulerEventHandler aJobOwner, SchedulerJob aNewJob) {
    List<SchedulerJob> jobList = htJobs.get(aJobOwner);
    if (jobList == null) {
      jobList = new ArrayList<SchedulerJob>();
      jobList.add(aNewJob);
      htJobs.put(aJobOwner, jobList);
    } else {
      jobList.add(aNewJob);
    }
    jobNames.add(aNewJob.getName());
    aNewJob.start();
    return aNewJob;
  }

  /**
   * This method removes a job
   * @param aJob A job object
   */
  private synchronized void removeJob(SchedulerJob aJob) {
    jobNames.remove(aJob.getName());
    SchedulerEventHandler event = null;
    for (Map.Entry<SchedulerEventHandler, List<SchedulerJob>> jobs : htJobs.entrySet()) {
      Iterator<SchedulerJob> workIter = jobs.getValue().iterator();
      while (workIter.hasNext()) {
        SchedulerJob workJob = workIter.next();
        if (workJob == aJob) {
          workJob.stopThread();
          jobNames.remove(workJob.getName());
          workIter.remove();
          // log ("Job '" + workJob.getJobName () + "' removed");
          break;
        }
      }
      // Is job list for owner empty?
      if (jobs.getValue().isEmpty()) {
        event = jobs.getKey();
      }
    }
    if (event != null) {
      htJobs.remove(event);
    }
  }

  /**
   * This method checks the existence of the given job name
   * @param aJobName A new job name
   * @return True, if the name does not exist
   */
  private synchronized boolean checkJobName(String aJobName) {
    return !jobNames.contains(aJobName);
  }

  /**
   * This method stops all scheduling jobs
   */
  private synchronized void stopAllJobs() {
    for (List<SchedulerJob> jobs : htJobs.values()) {
      for (SchedulerJob workJob : jobs) {
        workJob.stopThread();
      }
      // Clear references
      jobs.clear();
      htJobs.clear();
      jobNames.clear();
    }
  }
}
