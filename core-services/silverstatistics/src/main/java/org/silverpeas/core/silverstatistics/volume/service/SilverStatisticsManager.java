/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.silverstatistics.volume.service;

import org.silverpeas.core.scheduler.Job;
import org.silverpeas.core.scheduler.JobExecutionContext;
import org.silverpeas.core.scheduler.Scheduler;
import org.silverpeas.core.scheduler.SchedulerException;
import org.silverpeas.core.scheduler.SchedulerProvider;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.core.silverstatistics.volume.model.SilverStatisticsConfigException;
import org.silverpeas.core.silverstatistics.volume.model.StatisticsConfig;
import org.silverpeas.core.silverstatistics.volume.model.StatType;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.SettingBundle;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.io.File;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.silverpeas.core.silverstatistics.volume.model.SilverStatisticsConstants.SEPARATOR;


import static org.silverpeas.core.silverstatistics.volume.model.StatType.*;


/**
 * SilverStatisticsManager is the tool used in silverpeas to compute statistics for connexions,
 * files size and components access. This is a singleton class.
 * yet, the single instance is managed by the IoC container that will invoke the
 * <code>initSilverStatisticsManager()</code> method for initializing it.
 * @author Marc Guillemin
 */
@Singleton
public class SilverStatisticsManager {

  private static final String STAT_SIZE_JOB_NAME = "SilverStatisticsSize";
  private static final String STAT_CUMUL_JOB_NAME = "SilverStatisticsCumul";
  private static final String STAT_VOLUME_JOB_NAME = "SilverStatisticsVolume";
  // List of directory to compute size
  private List<String> directoryToScan = null;
  private StatisticsConfig statsConfig = null;

  /**
   * Prevent the class from being instantiate (private)
   */
  private SilverStatisticsManager() {
  }

  /**
   * Init attributes.
   * This method is invoked by the IoC container. Don't invoke it!
   */
  @PostConstruct
  public void initSilverStatisticsManager() {
    directoryToScan = new ArrayList<>();
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
      SettingBundle settings =
          ResourceLocator.getSettingBundle("org.silverpeas.silverstatistics.SilverStatistics");
      initSchedulerStatistics(settings.getString("scheduledGetStatVolumeTimeStamp"),
          STAT_VOLUME_JOB_NAME, "doGetStatVolume");
      initSchedulerStatistics(settings.getString("scheduledGetStatSizeTimeStamp"),
          STAT_SIZE_JOB_NAME, "doGetStatSize");
      initSchedulerStatistics(settings.getString("scheduledCumulStatTimeStamp"),
          STAT_CUMUL_JOB_NAME, "doCumulStat");
      initDirectoryToScan(settings);

    } catch (Exception ex) {
      SilverTrace.error("silverstatistics", "SilverStatisticsManager.initSilverStatistics",
          "root.EX_CLASS_NOT_INITIALIZED", ex);
    }
  }

  /**
   * SilverStatisticsManager is a singleton
   * @return the instance of silver statistics manager
   */
  public static SilverStatisticsManager getInstance() {
    return ServiceProvider.getService(SilverStatisticsManager.class);
  }

  /**
   * Sets up the scheduling of the specified statistics computation at given moments in time as
   * specified by the Unix-like cron expression.
   * @param aCronString the cron expression.
   * @param jobName the name of the computation to schedule.
   * @param methodeName the name of the method that performs the computation.
   * @throws SchedulerException if the computation scheduling failed.
   * @throws ParseException if the cron expression is malformed.
   */
  public void initSchedulerStatistics(String aCronString, String jobName, String methodeName)
      throws SchedulerException, ParseException {
    Scheduler scheduler = SchedulerProvider.getScheduler();
    scheduler.unscheduleJob(jobName);

    JobTrigger trigger = JobTrigger.triggerAt(aCronString);
    Job job = createJobWith(jobName, methodeName);
    scheduler.scheduleJob(job, trigger);
  }

  /**
   * @param currentDate
   */
  public void doGetStatVolume(Date currentDate) {
    try {
      getSilverStatistics().makeVolumeAlimentationForAllComponents();
    } catch (Exception ex) {
      SilverTrace.error("silverstatistics", "SilverStatisticsManager.doGetStatVolume",
          "root.EX_NO_MESSAGE", ex);
    }

  }

  /**
   * For each directory compute the size of all its files and the size of all its subdirectories
   * recursively.
   * @param currentDate
   */
  public void doGetStatSize(Date currentDate) throws ExecutionException, InterruptedException {
    for (String aDirectoryToScan : directoryToScan) {
      DirectoryVolumeService service = new DirectoryVolumeService(new File(aDirectoryToScan));
      addStatSize(currentDate, aDirectoryToScan, service.getTotalSize(null));
    }

  }

  /**
   * @param currentDate
   */
  public void doCumulStat(Date currentDate) {
    try {
      getSilverStatistics().makeStatAllCumul();
    } catch (Exception ex) {
      SilverTrace
          .error("silverstatistics", "SilverStatisticsManager.doCumulStat", "root.EX_NO_MESSAGE",
              ex);
    }
  }

  /**
   * @param resource
   */
  private void initDirectoryToScan(SettingBundle resource) {
    try {
      // read the directories
      int i = 0;
      String directoryPath = resource.getString("SilverPeasDataPath" + Integer.toString(i), null);
      // for each directory
      while (directoryPath != null) {
        // Test existence
        File dir = new File(directoryPath);
        if (!dir.isDirectory()) {
          throw new Exception("silverstatistics initDirectoryToScan" + directoryPath);
        }
        directoryToScan.add(directoryPath);
        i++;
        directoryPath = resource.getString("SilverPeasDataPath" + Integer.toString(i), null);
      }
    } catch (Exception e) {
      SilverTrace.error("silverstatistics", "SilverStatisticsManager.initDirectoryToScan()",
          "silvertrace.ERR_INIT_APPENDER_FROM_PROP", e);
    }
  }

  /**
   * @param userId the user identifier
   * @param volume
   * @param dateAccess the access date
   * @param peasType
   * @param spaceId the space identifier
   * @param componentId the component instance identifier (silverpeas application)
   */
  public void addStatVolume(String userId, long volume, Date dateAccess, String peasType,
      String spaceId, String componentId) {
    if (statsConfig.isRun(Volume)) {
      StringBuilder stat = new StringBuilder();
      stat.append(DateUtil.formatAsISO8601Day(dateAccess));
      stat.append(SEPARATOR);
      stat.append(userId);
      stat.append(SEPARATOR);
      stat.append(peasType);
      stat.append(SEPARATOR);
      stat.append(spaceId);
      stat.append(SEPARATOR);
      stat.append(componentId);
      stat.append(SEPARATOR);
      stat.append((String.valueOf(volume)));
      sendStatistic(Volume, stat);
    }
  }

  private void sendStatistic(StatType type, CharSequence stat) {
    if (isAsynchronStats(type)) {
      SilverStatisticsSender mySilverStatisticsSender = SilverStatisticsSender.get();
      try {

        mySilverStatisticsSender.send(type, stat.toString());
      } catch (Exception e) {
        SilverTrace.error("silverstatistics", "SilverStatisticsManager.sendStatistic",
            "SilverStatisticsSender ", e);
      }
    } // synchrone
    else {

      getSilverStatistics().putStats(type, stat.toString());
    }
  }

  /**
   * Add access statistic
   * @param userId the user identifier
   * @param dateAccess the access date
   * @param peasType
   * @param spaceId the space identifier
   * @param componentId the component instance identifier (silverpeas application)
   */
  public void addStatAccess(String userId, Date dateAccess, String peasType, String spaceId,
      String componentId) {
    // should feed Access (see SilverStatistics.properties)
    if (statsConfig.isRun(Access)) {
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
      sendStatistic(Access, stat);
    }
  }

  /**
   * Add connection statistic
   * @param userId the user identifier
   * @param dateConnection connection date
   * @param count
   * @param duration the connection duration
   */
  public void addStatConnection(String userId, Date dateConnection, int count, long duration) {
    // should feed connexion (see SilverStatistics.properties)
    if (statsConfig.isRun(Connexion)) {
      StringBuilder stat = new StringBuilder();
      stat.append(DateUtil.formatAsISO8601Day(dateConnection)); // date
      // connexion
      stat.append(SEPARATOR);
      stat.append(userId); // userId
      stat.append(SEPARATOR);
      stat.append(Long.toString(count)); // countConnection
      stat.append(SEPARATOR);
      stat.append(Long.toString(duration)); // duration

      sendStatistic(Connexion, stat);
    }
  }

  /**
   * Add statistics size
   * @param date
   * @param dirName
   * @param dirSize
   */
  public void addStatSize(Date date, String dirName, long dirSize) {
    if (statsConfig.isRun(Size)) {
      StringBuilder stat = new StringBuilder();
      stat.append(DateUtil.formatAsISO8601Day(date));
      stat.append(SEPARATOR);
      // directoryName
      stat.append(dirName);
      stat.append(SEPARATOR);
      // directorySize
      stat.append(Long.toString(dirSize));
      sendStatistic(Size, stat);
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
    return SilverStatisticsProvider.getSilverStatistics();
  }

  /**
   * Creates a job with the specified name and with the specified operation to execute.
   * @param jobName the job name.
   * @param jobOperation the job operation.
   * @return a job wrapping the operation to schedule at given moments in time.
   * @throws SchedulerException if an error occurs while creating the job to schedule (for example,
   * the operation doesn't exist).
   */
  private Job createJobWith(final String jobName, final String jobOperation)
      throws SchedulerException {
    try {
      final Method operation = getClass().getMethod(jobOperation, Date.class);
      return new Job(jobName) {

        @Override
        public void execute(JobExecutionContext context) throws Exception {
          Date date = context.getFireTime();
          operation.invoke(this, date);
        }
      };
    } catch (Exception ex) {
      SilverTrace
          .error("silverstatistics", "SilverStatisticsManager.createJobWith", ex.getMessage(), ex);
      throw new SchedulerException(ex.getMessage());
    }
  }
}
