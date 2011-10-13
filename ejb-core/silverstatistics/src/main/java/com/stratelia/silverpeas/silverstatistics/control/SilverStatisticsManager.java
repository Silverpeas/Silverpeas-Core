/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.silverpeas.silverstatistics.control;

import com.google.common.io.Closeables;
import com.silverpeas.scheduler.Job;
import com.silverpeas.scheduler.JobExecutionContext;
import com.silverpeas.scheduler.Scheduler;
import com.silverpeas.scheduler.SchedulerEvent;
import com.silverpeas.scheduler.SchedulerEventListener;
import com.silverpeas.scheduler.SchedulerException;
import com.silverpeas.scheduler.SchedulerFactory;
import com.silverpeas.scheduler.trigger.JobTrigger;
import com.silverpeas.util.FileUtil;
import com.stratelia.silverpeas.silverstatistics.model.SilverStatisticsConfigException;
import com.stratelia.silverpeas.silverstatistics.model.StatisticsConfig;
import com.stratelia.silverpeas.silverstatistics.util.StatType;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;

import javax.ejb.EJBException;
import java.io.File;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static com.stratelia.silverpeas.silverstatistics.control.SilverStatisticsConstants.SEPARATOR;
import static com.stratelia.silverpeas.silverstatistics.util.StatType.*;

/**
 * SilverStatisticsManager is the tool used in silverpeas to compute statistics for connexions,
 * files size and components access. This is a singleton class.
 *
 * @author Marc Guillemin
 */
public class SilverStatisticsManager implements SchedulerEventListener {

  private static final String STAT_SIZE_JOB_NAME = "SilverStatisticsSize";
  private static final String STAT_CUMUL_JOB_NAME = "SilverStatisticsCumul";
  private static final String STAT_VOLUME_JOB_NAME = "SilverStatisticsVolume";
  // Class variables
  // Singleton implementation
  private static SilverStatisticsManager myInstance = null;
  // Object variables
  // List of directory to compute size
  private List<String> directoryToScan = null;
  private SilverStatistics silverStatistics = null;
  private StatisticsConfig statsConfig = null;

  /**
   * Prevent the class from being instantiate (private)
   */
  private SilverStatisticsManager() {
  }

  /**
   * Init attributes
   */
  private void initSilverStatisticsManager() {
    directoryToScan = new ArrayList<String>();
    try {
      statsConfig = new StatisticsConfig();
      try {
        statsConfig.init();
        if (!statsConfig.isValidConfigFile()) {
          SilverTrace.error("silverstatistics", "SilverStatisticsManager.initSilverStatistics",
                  "silverstatistics.MSG_CONFIG_FILE");
        }
      } catch (SilverStatisticsConfigException e) {
        SilverTrace.error("silverstatistics", "SilverStatisticsManager.initSilverStatistics",
                "silverstatistics.MSG_CONFIG_FILE", e);
      }
      ResourceBundle resources = FileUtil.loadBundle(
              "com.stratelia.silverpeas.silverstatistics.SilverStatistics", Locale.getDefault());

      initSchedulerStatistics(resources.getString("scheduledGetStatVolumeTimeStamp"),
              STAT_VOLUME_JOB_NAME, "doGetStatVolume");
      initSchedulerStatistics(resources.getString("scheduledGetStatSizeTimeStamp"),
              STAT_SIZE_JOB_NAME, "doGetStatSize");
      initSchedulerStatistics(resources.getString("scheduledCumulStatTimeStamp"),
              STAT_CUMUL_JOB_NAME, "doCumulStat");
      initDirectoryToScan(resources);

    } catch (Exception ex) {
      SilverTrace.error("silverstatistics", "SilverStatisticsManager.initSilverStatistics",
              "root.EX_CLASS_NOT_INITIALIZED", ex);
    }
  }

  /**
   * SilverStatisticsManager is a singleton
   * @return 
   */
  public static synchronized SilverStatisticsManager getInstance() {
    if (myInstance == null) {
      myInstance = new SilverStatisticsManager();
      myInstance.initSilverStatisticsManager();
    }
    return myInstance;
  }

  /**
   * Sets up the scheduling of the specified statistics computation at given moments in time as
   * specified by the Unix-like cron expression.
   *
   * @param aCronString the cron expression.
   * @param jobName the name of the computation to schedule.
   * @param methodeName the name of the method that performs the computation.
   * @throws SchedulerException if the computation scheduling failed.
   * @throws ParseException if the cron expression is malformed.
   */
  public void initSchedulerStatistics(String aCronString, String jobName, String methodeName) throws
          SchedulerException, ParseException {
    SchedulerFactory schedulerFactory = SchedulerFactory.getFactory();
    Scheduler scheduler = schedulerFactory.getScheduler();
    scheduler.unscheduleJob(jobName);
    SilverTrace.info("silverstatistics", "SilverStatisticsManager.initSchedulerStatistics",
            "root.MSG_GEN_PARAM_VALUE", "jobName=" + jobName + ", aCronString=" + aCronString);
    JobTrigger trigger = JobTrigger.triggerAt(aCronString);
    Job job = createJobWith(jobName, methodeName);
    scheduler.scheduleJob(job, trigger, this);
  }

  /**
   * Method declaration
   *
   * @param currentDate
   * @see
   */
  public void doGetStatVolume(Date currentDate) {
    SilverTrace.debug("silverstatistics", "SilverStatisticsManager.doGetStatVolume",
            "currentDate=" + currentDate);
    try {
      getSilverStatistics().makeVolumeAlimentationForAllComponents();
    } catch (Exception ex) {
      SilverTrace.error("silverstatistics",
              "SilverStatisticsManager.doGetStatVolume", "root.EX_NO_MESSAGE", ex);
    }

  }

  /**
   * For each directory compute the size of all its files and the size of all its subdirectories
   * recursively.
   *
   * @param currentDate
   * @see
   */
  public void doGetStatSize(Date currentDate) {
    for (String aDirectoryToScan : directoryToScan) {
      addStatSize(currentDate, aDirectoryToScan, directorySize(aDirectoryToScan));
    }

  }

  /**
   * Method declaration
   *
   * @param currentDate
   * @see
   */
  public void doCumulStat(Date currentDate) {
    SilverTrace.debug("silverstatistics",
            "SilverStatisticsManager.doCumulStat", "currentDate=" + currentDate);
    try {
      getSilverStatistics().makeStatAllCumul();
    } catch (Exception ex) {
      SilverTrace.error("silverstatistics",
              "SilverStatisticsManager.doCumulStat", "root.EX_NO_MESSAGE", ex);
    }
  }

  /**
   * Method declaration
   *
   * @param resource
   * @see
   */
  private void initDirectoryToScan(java.util.ResourceBundle resource) {
    try {
      // read the directories
      // --------------------------
      int i = 0;
      String directoryPath = resource.getString("SilverPeasDataPath" + Integer.toString(i));
      // for each directory
      while (directoryPath != null) {
        // Test existence
        File dir = new File(directoryPath);
        if (!dir.isDirectory()) {
          throw new Exception("silverstatistics initDirectoryToScan" + directoryPath);
        }
        directoryToScan.add(directoryPath);
        i++;
        try {
          directoryPath = resource.getString("SilverPeasDataPath" + Integer.toString(i));
        } catch (MissingResourceException ex) {
          directoryPath = null;
        }
      }
    } catch (Exception e) {
      SilverTrace.error("silverstatistics", "SilverStatisticsManager.initDirectoryToScan()",
              "silvertrace.ERR_INIT_APPENDER_FROM_PROP", e);
    }
  }

  /**
   * Method declaration
   *
   * @param userId
   * @param volume
   * @param dateAccess
   * @param peasType
   * @param spaceId
   * @param componentId
   * @see
   */
  public void addStatVolume(String userId, int volume, Date dateAccess, String peasType,
          String spaceId, String componentId) {
    if (statsConfig.isRun(Volume)) {
      SilverTrace.debug("silverstatistics", "SilverStatistics.addStatVolume",
              " peasType=" + peasType + " spaceId=" + spaceId + " componentId=" + componentId);
      StringBuilder stat = new StringBuilder();
      stat.append(DateUtil.formatAsISO8601Day(dateAccess));
      stat.append(SEPARATOR);
      stat.append(userId); // userId
      stat.append(SEPARATOR);
      stat.append(peasType);
      stat.append(SEPARATOR);
      stat.append(spaceId);
      stat.append(SEPARATOR);
      stat.append(componentId);
      stat.append(SEPARATOR);
      stat.append((String.valueOf(volume)));
      if (isAsynchronStats(Volume)) {
        SilverStatisticsSender mySilverStatisticsSender = new SilverStatisticsSender();
        try {
          SilverTrace.info("silverstatistics", "SilverStatisticsManager.addStatVolume",
                  "root.MSG_GEN_PARAM_VALUE", "stat=" + stat.toString());
          mySilverStatisticsSender.send(Volume + SEPARATOR + stat.toString());
          SilverTrace.debug("silverstatistics",
                  "SilverStatisticsManager.addStatVolume", "after send");
        } catch (Exception e) {
          SilverTrace.error("silverstatistics", "SilverStatisticsManager.addStatVolume",
                  "SilverStatisticsSender ", e);
        } finally {
          Closeables.closeQuietly(mySilverStatisticsSender);
        }
      } // synchrone = appel d'une methode direct de l'ejb SilverStatisticsEJB
      else {
        try {
          SilverTrace.info("silverstatistics", "SilverStatisticsManager.addStatVolume",
                  "root.MSG_GEN_PARAM_VALUE", "stat=" + stat.toString());
          getSilverStatistics().putStats(Volume, stat.toString());
          SilverTrace.debug("silverstatistics", "SilverStatisticsManager.addStatVolume",
                  "after putStats");
        } catch (RemoteException ex) {
          SilverTrace.error("silverstatistics", "SilverStatisticsManager.addStatVolume",
                  "impossible de trouver " + JNDINames.SILVERSTATISTICS_EJBHOME, ex);
        }
      }
    }
  }

  /**
   * Method declaration
   *
   * @param userId
   * @param dateAccess
   * @param peasType
   * @param spaceId
   * @param componentId
   * @see
   */
  public void addStatAccess(String userId, Date dateAccess, String peasType, String spaceId,
          String componentId) {
    // should feed Access (see SilverStatistics.properties)
    if (statsConfig.isRun(Access)) {
      SilverTrace.debug("silverstatistics", "SilverStatistics.addStatAccess",
              " peasType=" + peasType + " spaceId=" + spaceId + " componentId="
              + componentId);
      // creation du stringbuffer correspondant au type Acces du
      // silverstatistics.properties
      StringBuilder stat = new StringBuilder();

      stat.append(DateUtil.formatAsISO8601Day(dateAccess));
      stat.append(SEPARATOR);
      stat.append(userId); // userId
      stat.append(SEPARATOR);
      stat.append(peasType);
      stat.append(SEPARATOR);
      stat.append(spaceId);
      stat.append(SEPARATOR);
      stat.append(componentId);
      stat.append(SEPARATOR);
      stat.append("1"); // countAccess

      // asynchrone : utilisation d'une queue avec jms
      if (isAsynchronStats(Access)) {
        SilverStatisticsSender mySilverStatisticsSender = new SilverStatisticsSender();
        try {
          SilverTrace.info("silverstatistics", "SilverStatisticsManager.addStatAccess",
                  "root.MSG_GEN_PARAM_VALUE", "stat=" + stat.toString());
          mySilverStatisticsSender.send(Access + SEPARATOR + stat.toString());
          SilverTrace.debug("silverstatistics", "SilverStatisticsManager.addStatAccess",
                  "after send");
          mySilverStatisticsSender.close();
        } catch (Exception e) {
          SilverTrace.error("silverstatistics", "SilverStatisticsManager.addStatAccess",
                  "SilverStatisticsSender ", e);
        } finally {
          Closeables.closeQuietly(mySilverStatisticsSender);
        }
      } // synchrone = appel d'une methode direct de l'ejb SilverStatisticsEJB
      else {
        try {
          SilverTrace.info("silverstatistics",
                  "SilverStatisticsManager.addStatAccess",
                  "root.MSG_GEN_PARAM_VALUE", "stat=" + stat.toString());
          getSilverStatistics().putStats(Access, stat.toString());
          SilverTrace.debug("silverstatistics",
                  "SilverStatisticsManager.addStatAccess", "after putStats");
        } catch (RemoteException ex) {
          SilverTrace.error("silverstatistics", "SilverStatisticsManager.addStatAccess",
                  "impossible de trouver " + JNDINames.SILVERSTATISTICS_EJBHOME, ex);
        }
      }
    }
  }

  /**
   * Method declaration
   *
   * @param userId
   * @param dateConnection
   * @param count
   * @param duration
   * @see
   */
  public void addStatConnection(String userId,
          Date dateConnection,
          int count,
          long duration) {
    // should feed connexion (see SilverStatistics.properties)
    if (statsConfig.isRun(Connexion)) {
      SilverTrace.debug("silverstatistics", "SilverStatistics.addStatConnection",
              " userId=" + userId + " count=" + count);
      // creation du stringbuffer correspondant au type Acces du
      // silverstatistics.properties

      StringBuilder stat = new StringBuilder();
      stat.append(DateUtil.formatAsISO8601Day(dateConnection)); // date
      // connexion
      stat.append(SEPARATOR);
      stat.append(userId); // userId
      stat.append(SEPARATOR);
      stat.append(Long.toString(count)); // countConnection
      stat.append(SEPARATOR);
      stat.append(Long.toString(duration)); // duration

      // asynchrone : utilisation d'une queue avec jms
      if (isAsynchronStats(Connexion)) {
        SilverStatisticsSender mySilverStatisticsSender = new SilverStatisticsSender();
        try {
          SilverTrace.info("silverstatistics", "SilverStatisticsManager.addStatConnection",
                  "root.MSG_GEN_PARAM_VALUE", "stat=" + stat.toString());
          mySilverStatisticsSender.send("Connexion" + SEPARATOR + stat.toString());
          SilverTrace.debug("silverstatistics",
                  "SilverStatisticsManager.addStatConnection", "after send");
          mySilverStatisticsSender.close();
        } catch (Exception e) {
          SilverTrace.error("silverstatistics", "SilverStatisticsManager.addStatConnection",
                  "SilverStatisticsSender ", e);
        } finally {
          Closeables.closeQuietly(mySilverStatisticsSender);

        }
      } // synchrone = appel d'une methode direct de l'ejb SilverStatisticsEJB
      else {
        try {
          SilverTrace.info("silverstatistics",
                  "SilverStatisticsManager.addStatConnection",
                  "root.MSG_GEN_PARAM_VALUE", "stat=" + stat.toString());
          getSilverStatistics().putStats(Connexion, stat.toString());
          SilverTrace.debug("silverstatistics",
                  "SilverStatisticsManager.addStatConnection", "after putStats");
        } catch (RemoteException ex) {
          SilverTrace.error("silverstatistics", "SilverStatisticsManager.addStatConnection",
                  "impossible de trouver " + JNDINames.SILVERSTATISTICS_EJBHOME, ex);
        }
      }
    }
  }

  /**
   * Method declaration
   *
   * @param date
   * @param dirName
   * @param dirSize
   * @see
   */
  public void addStatSize(Date date, String dirName, long dirSize) {
    if (statsConfig.isRun(Size)) {
      SilverTrace.debug("silverstatistics", "SilverStatistics.addStatSize",
              "dirName=" + dirName + " dirSize=" + dirSize);
      StringBuilder stat = new StringBuilder();
      stat.append(DateUtil.formatAsISO8601Day(date));
      stat.append(SEPARATOR);
      stat.append(dirName); // directoryName
      stat.append(SEPARATOR);
      stat.append(Long.toString(dirSize)); // directorySize

      // asynchrone : utilisation d'une queue avec jms
      if (isAsynchronStats(Size)) {
        SilverStatisticsSender mySilverStatisticsSender = new SilverStatisticsSender();
        try {
          SilverTrace.info("silverstatistics",
                  "SilverStatisticsManager.addStatSize",
                  "root.MSG_GEN_PARAM_VALUE", "stat=" + stat.toString());
          mySilverStatisticsSender.send(Size + SEPARATOR + stat.toString());
          SilverTrace.debug("silverstatistics",
                  "SilverStatisticsManager.addStatSize", "after send");
          mySilverStatisticsSender.close();
        } catch (Exception e) {
          SilverTrace.error("silverstatistics", "SilverStatisticsManager.addStatSize",
                  "SilverStatisticsSender ", e);
        } finally {
          Closeables.closeQuietly(mySilverStatisticsSender);
        }
      } // synchrone = appel d'une methode direct de l'ejb SilverStatisticsEJB
      else {
        try {
          SilverTrace.info("silverstatistics", "SilverStatisticsManager.addStatSize",
                  "root.MSG_GEN_PARAM_VALUE", "stat=" + stat.toString());
          getSilverStatistics().putStats(Size, stat.toString());
          SilverTrace.debug("silverstatistics",
                  "SilverStatisticsManager.addStatSize", "after putStats");
        } catch (RemoteException ex) {
          SilverTrace.error("silverstatistics", "SilverStatisticsManager.addStatSize",
                  "impossible de trouver " + JNDINames.SILVERSTATISTICS_EJBHOME, ex);
        }
      }
    }
  }

  private boolean isAsynchronStats(StatType typeStats) {
    try {
      if (statsConfig == null) {
        statsConfig = new StatisticsConfig();
        statsConfig.init();
      }
      if (statsConfig.isValidConfigFile()) {
        return statsConfig.isAsynchron(typeStats);
      }
    } catch (SilverStatisticsConfigException e) {
      SilverTrace.error("silverstatistics", "SilverStatisticsManager.isRun",
              "silverstatistics.MSG_CONFIG_FILE", e);
    }

    return false;
  }

  private SilverStatistics getSilverStatistics() {
    if (silverStatistics == null) {
      try {
        SilverStatisticsHome silverStatisticsHome = EJBUtilitaire.getEJBObjectRef(
                JNDINames.SILVERSTATISTICS_EJBHOME, SilverStatisticsHome.class);
        silverStatistics = silverStatisticsHome.create();
      } catch (Exception e) {
        throw new EJBException(e.getMessage());
      }
    }
    return silverStatistics;
  }

  /**
   * Method declaration
   *
   * @param directoryName
   * @return
   * @see
   */
  private long directorySize(String directoryName) {
    SilverTrace.debug("silverstatistics", "SilverStatisticsManager.directorySize",
            "directoryName=" + directoryName);
    File file = new File(directoryName);
    if (file.exists() && file.isDirectory()) {
      return returnSize(file);
    }
    return -1;
  }

  /**
   * Method declaration
   *
   * @param file
   * @return
   * @see
   */
  private long returnSize(File file) {
    if (file.isFile()) {
      return file.length();
    }
    File fDirContent[] = file.listFiles();
    long fileslength = 0L;
    for (File aFDirContent :
            fDirContent) {
      if (aFDirContent.isFile()) {
        fileslength = fileslength + aFDirContent.length();
      } else {
        fileslength = fileslength + returnSize(aFDirContent);
      }
    }
    return fileslength;
  }

  /**
   * Creates a job with the specified name and with the specified operation to execute.
   *
   * @param jobName the job name.
   * @param jobOperation the job operation.
   * @return a job wrapping the operation to schedule at given moments in time.
   * @throws SchedulerException if an error occurs while creating the job to schedule (for example,
   * the operation doesn't exist).
   */
  private Job createJobWith(final String jobName,
          final String jobOperation) throws SchedulerException {
    try {
      final Method operation = myInstance.getClass().getMethod(jobOperation, Date.class);
      return new Job(jobName) {

        @Override
        public void execute(JobExecutionContext context) throws Exception {
          Date date = context.getFireTime();
          operation.invoke(myInstance, date);
        }
      };
    } catch (Exception ex) {
      SilverTrace.error("silverstatistics", "SilverStatisticsManager.createJobWith", ex.getMessage(),
              ex);
      throw new SchedulerException(ex.getMessage());
    }
  }

  @Override
  public void triggerFired(SchedulerEvent anEvent) throws Exception {
    SilverTrace.debug("silverstatistics", "SilverStatisticsManager.handleSchedulerEvent",
            "The job '"
            + anEvent.getJobExecutionContext().getJobName() + "' is starting");
  }

  @Override
  public void jobSucceeded(SchedulerEvent anEvent) {
    SilverTrace.debug("silverstatistics", "SilverStatisticsManager.handleSchedulerEvent",
            "The job '"
            + anEvent.getJobExecutionContext().getJobName() + "' was successfull");
  }

  @Override
  public void jobFailed(SchedulerEvent anEvent) {
    SilverTrace.error("silverstatistics", "SilverStatisticsManager.handleSchedulerEvent",
            "The job '"
            + anEvent.getJobExecutionContext().getJobName() + "' was not successfull");
  }
}
