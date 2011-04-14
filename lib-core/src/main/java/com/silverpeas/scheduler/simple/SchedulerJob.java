/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
package com.silverpeas.scheduler.simple;

import com.silverpeas.scheduler.trigger.CronJobTrigger;
import com.silverpeas.scheduler.JobExecutionContext;
import com.silverpeas.scheduler.ScheduledJob;
import com.silverpeas.scheduler.SchedulerEvent;
import com.silverpeas.scheduler.SchedulerEventListener;
import com.silverpeas.scheduler.SchedulerException;
import com.silverpeas.scheduler.trigger.JobTrigger;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import java.text.Format;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.commons.lang.time.FastDateFormat;
import static com.silverpeas.scheduler.SchedulerFactory.*;

/**
 * This is the base class of all scheduler job classes. This class is abstract. If you will
 * implement your own special job class, you have to overrite the method 'execute' and add your own
 * job generation method in the class 'SimpleScheduler'
 */
@Deprecated
abstract public class SchedulerJob
    implements Runnable,
    ScheduledJob {
  // Environment variables

  protected Format logDateFormat = FastDateFormat.getInstance("yyyy-MM-dd HH:mm");
  private SchedulerEventListener theOwner;
  // private File theLogBaseFile;
  private String sJobName;
  // private String sJobLogFileName;
  // Converted cron string
  private List<Integer> vMinutes;
  private List<Integer> vHours;
  private List<Integer> vDaysOfMonth;
  private List<Integer> vMonths;
  private List<Integer> vDaysOfWeek;
  // Next timestamp
  private Integer currentMinute;
  private Integer currentHour;
  private Integer currentDayOfMonth;
  private Integer currentMonth;
  private Integer currentYear;
  // Runtime variables
  private long nextTimeStamp = 0;
  private volatile boolean bRunnable;
  private JobTrigger trigger;

  /**
   * This method returns the owner (or creator) of the job
   * @return The owner of the job
   */
  public SchedulerEventListener getOwner() {
    return theOwner;
  }

  public void initTimeStamp(long nextTime) {
    if (nextTime != 0) {
      nextTimeStamp = nextTime;
    } else {
      nextTimeStamp = getNextTimeStamp();
    }
  }

  public long readNextTimeStamp() {
    return nextTimeStamp;
  }

  @Override
  public Date getNextExecutionTime() {
    return new Date(nextTimeStamp);
  }

  @Override
  public long getNexExecutionTimeInMillis() {
    return nextTimeStamp;
  }

  /**
   * This method returns the name of the job
   * @return The name of the job
   */
  public String getJobName() {
    return sJobName;
  }

  /**
   * This method handles the thread execution
   */
  @Override
  public void run() {
    long sleepTime;

    // Get next schedule time
    if (nextTimeStamp == 0) {
      nextTimeStamp = getNextTimeStamp();
    }

    SilverTrace.info(MODULE_NAME, "SchedulerJob.run",
        "root.MSG_GEN_PARAM_VALUE", ": Job '" + sJobName
        + "' starts without errors.");
    SilverTrace.info(MODULE_NAME, "SchedulerJob.run",
        "root.MSG_GEN_PARAM_VALUE", ": Next schedule time: "
        + logDateFormat.format(new Date(nextTimeStamp)));

    while (bRunnable) {
      try {
        // Calculate the delay time
        sleepTime = nextTimeStamp - (new Date()).getTime();
        SilverTrace.info(MODULE_NAME, "SchedulerJob.run",
            "root.MSG_GEN_PARAM_VALUE", ": Sleeptime = " + sleepTime);
        if (sleepTime < 0) {
          // Yields if there are problems with the date. Should normaly not
          // occour.
          Thread.currentThread().sleep(0);
        } else {
          // Sleeps up to the next schedule time
          Thread.currentThread().sleep(sleepTime);
        }
      } catch (InterruptedException aException) {
      }

      if (bRunnable && ((new Date()).getTime() >= nextTimeStamp)) {
        try {
          SilverTrace.info(MODULE_NAME, "SchedulerJob.run",
              "root.MSG_GEN_PARAM_VALUE", ": ---------------- Start of job '"
              + sJobName + "' -------------------");
          // Execute the functionality of the job and gets a new schedule time
          nextTimeStamp = getNextTimeStamp();
          Date now = new Date();
          JobExecutionContext ctx = JobExecutionContext.createWith(sJobName, now);
          try {
            // execute (logStream, new Date ());
            execute(now);
            // logStream.flush ();
            theOwner.jobSucceeded(SchedulerEvent.jobSucceeded(ctx));
          } catch (SchedulerException aException) {
            theOwner.jobFailed(SchedulerEvent.jobFailed(ctx, aException));
          }

          SilverTrace.info(MODULE_NAME, "SchedulerJob.run",
              "root.MSG_GEN_PARAM_VALUE", ": ---------------- End of job '"
              + sJobName + "' -------------------");
          SilverTrace.info(MODULE_NAME, "SchedulerJob.run",
              "root.MSG_GEN_PARAM_VALUE", ": Next schedule time: "
              + logDateFormat.format(new Date(nextTimeStamp)));
        } catch (Exception aException) {
          SilverTrace.error(MODULE_NAME, "SchedulerJob.run",
              "root.EX_NO_MESSAGE", aException);
        }
      }
    }
    theOwner = null;
    sJobName = null;
  }

  /**
   * Stops the scheduling of the job
   */
  public synchronized void stop() {
    bRunnable = false;
  }

  /**
   * This method holds the logic of the job. It has to be overwriten in subclasses of this class
   * @param log A PrintStream for text writings in the log file for this job
   * @param theExecutionDate The date of the execution
   * @throws SchedulerException
   */
  abstract protected void execute(Date theExecutionDate)
      throws SchedulerException;

  /**
   * The constructor has proteceted access, because the generation of jobs should be done in a
   * central way by the class 'SimpleScheduler'
   * @param aController The controller, that controls all job executions
   * @param aOwner The owner of the job
   * @param aJobName The name of the job
   * @param aLogBaseFile The log file for the job
   */
  protected SchedulerJob(SimpleScheduler aController,
      SchedulerEventListener aOwner,
      String aJobName) throws SchedulerException {
    if (aController == null) {
      throw new SchedulerException(
          "SchedulerJob.SchedulerJob: Parameter 'aController' is null");
    }

    if (aOwner == null) {
      throw new SchedulerException(
          "SchedulerJob.SchedulerJob: Parameter 'aOwner' is null");
    }

    if (aJobName == null) {
      throw new SchedulerException(
          "SchedulerJob.SchedulerJob: Parameter 'aJobName' is null");
    }

    theOwner = aOwner;

    sJobName = aJobName;
    vMinutes = new ArrayList<Integer>();
    vHours = new ArrayList<Integer>();
    vDaysOfMonth = new ArrayList<Integer>();
    vMonths = new ArrayList<Integer>();
    vDaysOfWeek = new ArrayList<Integer>();
    // Instead
    Calendar calInit = Calendar.getInstance();
    currentMinute = 0;
    currentHour = 0;
    if (calInit.getActualMinimum(Calendar.DAY_OF_MONTH) == calInit.get(Calendar.DAY_OF_MONTH)) {
      currentDayOfMonth = calInit.getActualMaximum(Calendar.DAY_OF_MONTH);
      if (calInit.getActualMinimum(Calendar.MONTH) == calInit.get(Calendar.MONTH)) {
        currentMonth = calInit.getActualMaximum(Calendar.MONTH);
        currentYear = calInit.get(Calendar.YEAR) - 1;
      } else {
        currentMonth = calInit.get(Calendar.MONTH) - 1;
        currentYear = calInit.get(Calendar.YEAR);
      }
    } else {
      currentDayOfMonth = calInit.get(Calendar.DAY_OF_MONTH) - 1;
      currentMonth = calInit.get(Calendar.MONTH);
      currentYear = calInit.get(Calendar.YEAR);
    }
    if (calInit.getActualMinimum(Calendar.MONTH) == currentMonth.intValue()) {
      currentMonth = calInit.getActualMaximum(Calendar.MONTH);
      currentYear = currentYear.intValue() - 1;
    } else {
      currentMonth = currentMonth.intValue() - 1;
    }
    nextTimeStamp = 0;
    bRunnable = true;
  }

  /**
   * This method sets the scheduling parameter. The time settings are given by vectors. Each vector
   * holds a list of Integer objects (currently ordered). Every Integer represents a element of a
   * timestamp (cron like).
   * @param startMinutes A list of minutes (0-59)
   * @param startHours A list of hours (0-23)
   * @param startDaysOfMonth A list of days of a month (1-31)
   * @param startMonths A list of months (1-12; starts with 1 for January)
   * @param startDaysOfWeek A list of day of a week (0-6; starts with 0 for Sunday)
   */
  protected synchronized void setSchedulingParameter(List<Integer> startMinutes,
      List<Integer> startHours,
      List<Integer> startDaysOfMonth,
      List<Integer> startMonths,
      List<Integer> startDaysOfWeek) throws SchedulerException {
    Enumeration vectorEnumerator;

    List<Integer> workVector;
    int workInt;

    // Check minute values
    if (startMinutes == null) {
      startMinutes = new ArrayList<Integer>();
    }

    for (Integer minute : startMinutes) {
      try {
        workInt = minute.intValue();

        if ((workInt < 0) || (workInt > 59)) {
          throw new SchedulerException(
              "SchedulerMethodJob.setParameter: A minute value is out of range");
        }
      } catch (ClassCastException aException) {
        throw new SchedulerException(
            "SchedulerMethodJob.setParameter: Can't convert a minute value");
      }
    }

    // Check hour values
    if (startHours == null) {
      startHours = new ArrayList<Integer>();
    }

    for (Integer hours : startHours) {
      try {
        workInt = hours.intValue();

        if ((workInt < 0) || (workInt > 23)) {
          throw new SchedulerException(
              "SchedulerMethodJob.setParameter: A hour value is out of range");
        }
      } catch (ClassCastException aException) {
        throw new SchedulerException(
            "SchedulerMethodJob.setParameter: Can't convert a hour value");
      }
    }

    // Check day of month values
    if (startDaysOfMonth == null) {
      startDaysOfMonth = new ArrayList<Integer>();
    }

    for (Integer days : startDaysOfMonth) {
      try {
        workInt = days.intValue();

        if ((workInt < 1) || (workInt > 31)) {
          throw new SchedulerException(
              "SchedulerMethodJob.setParameter: A day of month value is out of range");
        }
      } catch (ClassCastException aException) {
        throw new SchedulerException(
            "SchedulerMethodJob.setParameter: Can't convert a day of month value");
      }
    }

    // Check month values and normalize them for internal usage
    if (startMonths == null) {
      startMonths = new ArrayList<Integer>();
    }

    workVector = new ArrayList<Integer>();
    for (Integer month : startMonths) {
      try {
        workInt = month.intValue();

        if ((workInt < 1) || (workInt > 12)) {
          throw new SchedulerException(
              "SchedulerMethodJob.setParameter: A month value is out of range");
        }

        workVector.add(workInt - 1); // Internal: zero based
      } catch (ClassCastException aException) {
        throw new SchedulerException(
            "SchedulerMethodJob.setParameter: Can't convert a month value");
      }
    }
    startMonths = workVector;

    // Check day of week values
    if (startDaysOfWeek == null) {
      startDaysOfWeek = new ArrayList<Integer>();
    }

    workVector = new ArrayList<Integer>();
    for (Integer daysOfWeek : startDaysOfWeek) {
      try {
        workInt = daysOfWeek.intValue();

        if ((workInt < 0) || (workInt > 6)) {
          throw new SchedulerException(
              "SchedulerMethodJob.setParameter: A day of week value is out of range");
        }

        // Conversion not realy necessary, but what if SUN changes the
        // implementation .... :-))
        switch (workInt) {
          case 0:
            workVector.add(Integer.valueOf(Calendar.SUNDAY));
            break;
          case 1:
            workVector.add(Integer.valueOf(Calendar.MONDAY));
            break;
          case 2:
            workVector.add(Integer.valueOf(Calendar.TUESDAY));
            break;
          case 3:
            workVector.add(Integer.valueOf(Calendar.WEDNESDAY));
            break;
          case 4:
            workVector.add(Integer.valueOf(Calendar.THURSDAY));
            break;
          case 5:
            workVector.add(Integer.valueOf(Calendar.FRIDAY));
            break;
          case 6:
            workVector.add(Integer.valueOf(Calendar.SATURDAY));
            break;
        }
      } catch (ClassCastException aException) {
        throw new SchedulerException(
            "SchedulerMethodJob.setParameter: Can't convert a day of week value");
      }
    }
    startDaysOfWeek = workVector;

    // Assign the calculated values
    vMinutes = startMinutes;
    vHours = startHours;
    vDaysOfMonth = startDaysOfMonth;
    vMonths = startMonths;
    vDaysOfWeek = startDaysOfWeek;

    // Sort the calculated vectors
    sortCronVectors();
  }

  /**
   * This method sets the scheduling parameter. It is given by a cron like string (currently ranges
   * are not allowed). So the string '* 3,21 * 3 0' starts the execution every Sunday in March on
   * 03:00 and 21:00. The allowed ranges are: minutes (0-59), hours (0-23), days of a month (1-31),
   * months (1-12; starts with 1 for January), day of a week (0-6; starts with 0 for Sunday).
   * Currently the parsing of the cron string ist not done by a state machine but by
   * StringTokenizers so this method is <B>very</B> sensitive for syntax failures!
   * @param aCronString
   */
  protected synchronized void setSchedulingParameter(CronJobTrigger trigger) throws
      SchedulerException {
    setTrigger(trigger);
    String aCronString = trigger.getCronExpression();
    StringTokenizer fieldSeparator;
    StringTokenizer fieldContentSeparator;
    String workString;
    int workInt;

    if (aCronString == null) {
      throw new SchedulerException(
          "SchedulerShellJob.setCronString: Parameter 'aCronString' is null");
    }
    if (aCronString.contains("-") || aCronString.contains("/")) {
      throw new SchedulerException(
          "This scheduler backend doesn't support yet the range and the increment instructions "
          + "in cron expressions");
    }
    aCronString = aCronString.replaceAll("\\?", "*");

    // Reset current values
    vMinutes = new ArrayList<Integer>();
    vHours = new ArrayList<Integer>();
    vDaysOfMonth = new ArrayList<Integer>();
    vMonths = new ArrayList<Integer>();
    vDaysOfWeek = new ArrayList<Integer>();

    // This StringTokenizer splits the cron string into time fields
    fieldSeparator = new StringTokenizer(aCronString);

    // Get minute values
    if (fieldSeparator.hasMoreTokens()) {
      // This StringTokenizer splits each timefield list into single numbers
      fieldContentSeparator = new StringTokenizer(fieldSeparator.nextToken(), ",");
      while (fieldContentSeparator.hasMoreTokens()) {
        workString = fieldContentSeparator.nextToken();

        // Check ingnore token
        if (workString.equals("*")) {
          vMinutes = new ArrayList<Integer>();
          break;
        }

        // Check integer value
        try {
          workInt = Integer.parseInt(workString);
        } catch (NumberFormatException aException) {
          throw new SchedulerException(
              "SchedulerShellJob.setCronString: Can't convert a minute value");
        }

        if ((workInt < 0) || (workInt > 59)) {
          throw new SchedulerException(
              "SchedulerShellJob.setCronString: A minute value is out of range");
        }

        vMinutes.add(workInt);
      }
    }

    // Get hour values
    if (fieldSeparator.hasMoreTokens()) {
      fieldContentSeparator = new StringTokenizer(fieldSeparator.nextToken(), ",");
      while (fieldContentSeparator.hasMoreTokens()) {
        workString = fieldContentSeparator.nextToken();

        // Check ingnore token
        if (workString.equals("*")) {
          vHours = new ArrayList<Integer>();
          break;
        }

        // Check iteger value
        try {
          workInt = Integer.parseInt(workString);
        } catch (NumberFormatException aException) {
          throw new SchedulerException(
              "SchedulerShellJob.setCronString: Can't convert a hour value");
        }

        if ((workInt < 0) || (workInt > 23)) {
          throw new SchedulerException(
              "SchedulerShellJob.setCronString: A hour value is out of range");
        }

        vHours.add(workInt);
      }
    }

    // Get day of month values and normalize them for internal usage
    if (fieldSeparator.hasMoreTokens()) {
      fieldContentSeparator = new StringTokenizer(fieldSeparator.nextToken(), ",");
      while (fieldContentSeparator.hasMoreTokens()) {
        workString = fieldContentSeparator.nextToken();

        // Check ingnore token
        if (workString.equals("*")) {
          vDaysOfMonth = new ArrayList<Integer>();
          break;
        }

        // Check iteger value
        try {
          workInt = Integer.parseInt(workString);
        } catch (NumberFormatException aException) {
          throw new SchedulerException(
              "SchedulerShellJob.setCronString: Can't convert a day of month value");
        }

        if ((workInt < 1) || (workInt > 31)) {
          throw new SchedulerException(
              "SchedulerShellJob.setCronString: A day of month value is out of range");
        }

        vDaysOfMonth.add(workInt);
      }
    }

    // Get month values
    if (fieldSeparator.hasMoreTokens()) {
      fieldContentSeparator = new StringTokenizer(fieldSeparator.nextToken(), ",");
      while (fieldContentSeparator.hasMoreTokens()) {
        workString = fieldContentSeparator.nextToken();

        // Check ingnore token
        if (workString.equals("*")) {
          vMonths = new ArrayList<Integer>();
          break;
        }

        // Check iteger value
        try {
          workInt = Integer.parseInt(workString);
        } catch (NumberFormatException aException) {
          throw new SchedulerException(
              "SchedulerShellJob.setCronString: Can't convert a month value");
        }

        if ((workInt < 1) || (workInt > 12)) {
          throw new SchedulerException(
              "SchedulerShellJob.setCronString: A month value is out of range");
        }

        vMonths.add(workInt - 1); // Internal: zero based
      }
    }

    // Get day of week values
    if (fieldSeparator.hasMoreTokens()) {
      fieldContentSeparator = new StringTokenizer(fieldSeparator.nextToken(),
          ",");
      while (fieldContentSeparator.hasMoreTokens()) {
        workString = fieldContentSeparator.nextToken();

        // Check ingnore token
        if (workString.equals("*")) {
          vDaysOfWeek = new ArrayList<Integer>();
          break;
        }

        // Check iteger value
        try {
          workInt = Integer.parseInt(workString);
        } catch (NumberFormatException aException) {
          throw new SchedulerException(
              "SchedulerShellJob.setCronString: Can't convert a day of week value");
        }

        if ((workInt < 0) || (workInt > 6)) {
          throw new SchedulerException(
              "SchedulerShellJob.setCronString: A day of week value is out of range");
        }

        switch (workInt) {
          case 0:
            vDaysOfWeek.add(Calendar.SUNDAY);
            break;
          case 1:
            vDaysOfWeek.add(Calendar.MONDAY);
            break;
          case 2:
            vDaysOfWeek.add(Calendar.TUESDAY);
            break;
          case 3:
            vDaysOfWeek.add(Calendar.WEDNESDAY);
            break;
          case 4:
            vDaysOfWeek.add(Calendar.THURSDAY);
            break;
          case 5:
            vDaysOfWeek.add(Calendar.FRIDAY);
            break;
          case 6:
            vDaysOfWeek.add(Calendar.SATURDAY);
            break;
        }
      }
    }

    if (fieldSeparator.hasMoreTokens()) {
      throw new SchedulerException(
          "SchedulerShellJob.setCronString: Too much time fields in cron string");
    }

    if (vDaysOfWeek == null) {
      throw new SchedulerException(
          "SchedulerShellJob.setCronString: Not enough time fields in cron string");
    }

    // Sort the calculated vectors
    sortCronVectors();
  }

  /**
   * Generates a new timestamp
   * @return
   */
  protected long getNextTimeStamp() {
    Calendar calcCalendar;
    long currentTime;
    boolean validTimeStamp;
    boolean carryMinute;
    boolean carryHour;
    boolean carryDayOfMonth;
    boolean carryMonth;
    boolean firstYearAccess;

    calcCalendar = Calendar.getInstance();

    SilverTrace.debug(MODULE_NAME, "SchedulerJob.getNextTimeStamp",
        "Current TimeStamp: "
        + logDateFormat.format(new Date(
        getMillisecondsOfCalendar(calcCalendar))));

    currentTime = getMillisecondsOfCalendar(calcCalendar);

    calcCalendar.set(Calendar.YEAR, currentYear.intValue());
    calcCalendar.set(Calendar.MONTH, currentMonth.intValue());
    calcCalendar.set(Calendar.DAY_OF_MONTH, currentDayOfMonth.intValue());
    calcCalendar.set(Calendar.HOUR_OF_DAY, currentHour.intValue());
    calcCalendar.set(Calendar.MINUTE, currentMinute.intValue());

    SilverTrace.debug(MODULE_NAME, "SchedulerJob.getNextTimeStamp",
        "Start TimeStamp: "
        + logDateFormat.format(new Date(
        getMillisecondsOfCalendar(calcCalendar))));

    // !!!!!! The values must be ordered ascend !!!!!
    validTimeStamp = false;
    carryMinute = false;
    carryHour = false;
    carryDayOfMonth = false;
    carryMonth = false;
    firstYearAccess = true;

    while (!validTimeStamp) {
      // Get new minute
      if (vMinutes.isEmpty()) {
        // Default ('*') -> Hit every minute
        // If the cron setting for minutes is *, we don't have to care about
        // incrementing minutes
        // So do a carryHour
        carryMinute = true;
      } else {
        // Special handling for lists with one element
        if (vMinutes.size() == 1) {
          currentMinute = vMinutes.get(0);
          carryMinute = !carryMinute;
          if (!carryMinute) {
            carryHour = false;
            carryDayOfMonth = false;
            carryMonth = false;
          }
        } else {
          int indexOfMinutes = vMinutes.indexOf(currentMinute);
          if ((indexOfMinutes == -1)
              || (indexOfMinutes == (vMinutes.size() - 1))) {
            currentMinute = vMinutes.get(0);
            carryMinute = true;
          } else {
            currentMinute = vMinutes.get(indexOfMinutes + 1);
            carryMinute = false;
            carryHour = false;
            carryDayOfMonth = false;
            carryMonth = false;
          }
        }
      }
      calcCalendar.set(Calendar.MINUTE, currentMinute.intValue());

      // Get new hour
      if (carryMinute) {
        if (vHours.isEmpty()) // Default ('*') -> Hit every hour
        {
          int maxHour = calcCalendar.getActualMaximum(Calendar.HOUR_OF_DAY);
          if (currentHour.intValue() < maxHour) {
            currentHour = currentHour.intValue() + 1;
            carryHour = false;
            carryDayOfMonth = false;
            carryMonth = false;
          } else {
            currentHour = calcCalendar.getActualMinimum(Calendar.HOUR_OF_DAY);
            carryHour = true;
          }
        } else {
          // Special handling for lists with one element
          if (vHours.size() == 1) {
            currentHour = vHours.get(0);
            carryHour = !carryHour;
            if (!carryHour) {
              carryDayOfMonth = false;
              carryMonth = false;
            }
          } else {
            int indexOfHours = vHours.indexOf(currentHour);
            if ((indexOfHours == -1) || (indexOfHours == (vHours.size() - 1))) {
              currentHour = vHours.get(0);
              carryHour = true;
            } else {
              currentHour = vHours.get(indexOfHours + 1);
              carryHour = false;
              carryDayOfMonth = false;
              carryMonth = false;
            }
          }
        }
        calcCalendar.set(Calendar.HOUR_OF_DAY, currentHour.intValue());
      }

      // Get new day of month
      if (carryHour) {
        if (vDaysOfMonth.isEmpty()) // Default ('*') -> Hit every month
        {
          int maxMonth = calcCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
          if (currentDayOfMonth.intValue() < maxMonth) {
            currentDayOfMonth = currentDayOfMonth.intValue() + 1;
            carryDayOfMonth = false;
            carryMonth = false;
          } else {
            currentDayOfMonth = calcCalendar.getActualMinimum(Calendar.DAY_OF_MONTH);
            carryDayOfMonth = true;
          }
        } else {
          // Special handling for lists with one element
          if (vMinutes.size() == 1) {
            currentDayOfMonth = vDaysOfMonth.get(0);
            carryDayOfMonth = !carryDayOfMonth;
            if (!carryDayOfMonth) {
              carryMonth = false;
            }
          } else {
            int indexOfMonths = vDaysOfMonth.indexOf(currentDayOfMonth);
            if ((indexOfMonths == -1) || (indexOfMonths == (vDaysOfMonth.size() - 1))) {
              currentDayOfMonth = vDaysOfMonth.get(0);
              carryDayOfMonth = true;
            } else {
              currentDayOfMonth = vDaysOfMonth.get(indexOfMonths + 1);
              carryDayOfMonth = false;
              carryMonth = false;
            }
          }
        }
        calcCalendar.set(Calendar.DAY_OF_MONTH, currentDayOfMonth.intValue());
      }

      // Get new month
      if (carryDayOfMonth) {
        if (vMonths.isEmpty()) // Default ('*') -> Hit every month
        {
          int maxMonth = calcCalendar.getActualMaximum(Calendar.MONTH);
          if (currentMonth.intValue() < maxMonth) {
            currentMonth = currentMonth.intValue() + 1;
            carryMonth = false;
          } else {
            currentMonth = calcCalendar.getActualMinimum(Calendar.MONTH);
            carryMonth = true;
          }
        } else {
          // Special handling for lists with one element
          if (vMinutes.size() == 1) {
            currentMonth = vMonths.get(0);
            carryMonth = !carryMonth;
          } else {
            int indexOfMonths = vMonths.indexOf(currentMonth);
            if ((indexOfMonths == -1)
                || (indexOfMonths == (vMonths.size() - 1))) {
              currentMonth = vMonths.get(0);
              carryMonth = true;
            } else {
              currentMonth = vMonths.get(indexOfMonths + 1);
              carryMonth = false;
            }
          }
        }
        calcCalendar.set(Calendar.MONTH, currentMonth.intValue());
      }

      // Get new year
      if (carryMonth) {
        // Prevent Check for the 'ever carry' of one element lists
        if ((!firstYearAccess)
            || ((currentMinute.intValue() == 0)
            && (currentHour.intValue() == 0)
            && (currentDayOfMonth.intValue() == 1) && (currentMonth.intValue() == 0))) {
          // Hit every year
          currentYear = currentYear.intValue() + 1;
          calcCalendar.set(Calendar.YEAR, currentYear.intValue());
        }

        firstYearAccess = false;
      }

      // If time stamp is greater than the current time check the day of week
      if (getMillisecondsOfCalendar(calcCalendar) > currentTime) {
        // Check eventualy day movement while calculations
        if (calcCalendar.get(Calendar.DAY_OF_MONTH) == currentDayOfMonth.intValue()) {
          // Check for correct day of week
          if (vDaysOfWeek.isEmpty()) {
            validTimeStamp = true;
          } else {
            for (Integer dayOfWeek : vDaysOfWeek) {
              if (calcCalendar.get(Calendar.DAY_OF_WEEK) == dayOfWeek.intValue()) {
                validTimeStamp = true;
                break;
              }
            }
          }
        }
      }
    } // while (getMillisecondsOfCurrentTimeStamp (calcCalendar) < currentTime)

    SilverTrace.debug(MODULE_NAME, "SchedulerJob.getNextTimeStamp", "New TimeStamp: "
        + logDateFormat.format(new Date(getMillisecondsOfCalendar(calcCalendar))));
    return getMillisecondsOfCalendar(calcCalendar);
  }

  /**
   * Wraps calender date access
   * @param aCalendar
   * @return
   */
  protected long getMillisecondsOfCalendar(Calendar aCalendar) {
    return aCalendar.getTime().getTime();
  }

  /**
   * Sorts the internal cron vectors nad remove doubled entries. This is necessary to calculate the
   * correct schedule time
   */
  private void sortCronVectors() {
    Collections.sort(vMinutes);
    Collections.sort(vHours);
    Collections.sort(vDaysOfMonth);
    Collections.sort(vMonths);
    Collections.sort(vDaysOfWeek);

    removeDoubled(vMinutes);
    removeDoubled(vHours);
    removeDoubled(vDaysOfMonth);
    removeDoubled(vMonths);
    removeDoubled(vDaysOfWeek);
  }

  /**
   * Removes doubled entries (Comparable) , if the list is sorted
   */
  private void removeDoubled(List<? extends Comparable> aList) {
    Comparable currentComparable;

    Comparable lastComparable = null;
    for (Iterator listIterator = aList.iterator(); listIterator.hasNext();) {
      try {
        if (lastComparable == null) {
          lastComparable = (Comparable) listIterator.next();
        } else {
          currentComparable = (Comparable) listIterator.next();
          if (lastComparable.compareTo(currentComparable) == 0) {
            listIterator.remove();
          } else {
            lastComparable = currentComparable;
          }
        }
      } catch (Exception aException) {
        // Unequal
      }
    }
  }

  @Override
  public String getName() {
    return getJobName();
  }

  @Override
  public void execute(JobExecutionContext context) throws Exception {
    execute(new Date());
  }

  @Override
  public SchedulerEventListener getSchedulerEventListener() {
    return this.getOwner();
  }

  @Override
  public JobTrigger getTrigger() {
    return this.trigger;
  }

  protected void setTrigger(final JobTrigger trigger) {
    this.trigger = trigger;
  }
}
