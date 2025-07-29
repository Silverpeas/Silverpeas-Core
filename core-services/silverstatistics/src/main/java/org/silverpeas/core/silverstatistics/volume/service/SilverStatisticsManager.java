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
package org.silverpeas.core.silverstatistics.volume.service;

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.scheduler.*;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.core.silverstatistics.volume.model.SilverStatisticsConfigException;
import org.silverpeas.core.silverstatistics.volume.model.StatType;
import org.silverpeas.core.silverstatistics.volume.model.StatisticsConfig;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.kernel.SilverpeasException;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;
import org.silverpeas.kernel.logging.SilverLogger;

import javax.inject.Singleton;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import static org.silverpeas.core.silverstatistics.volume.model.SilverStatisticsConstants.SEPARATOR;
import static org.silverpeas.core.silverstatistics.volume.model.StatType.*;


/**
 * SilverStatisticsManager is the tool used in silverpeas to compute statistics for connexions,
 * files size and components access. This is a singleton class.
 * yet, the single instance is managed by the IoC container that will invoke the
 * <code>initSilverStatisticsManager()</code> method for initializing it.
 * @author Marc Guillemin
 */
@Service
@Singleton
public class SilverStatisticsManager implements Initialization {

  private static final String STAT_SIZE_JOB_NAME = "SilverStatisticsSize";
  private static final String STAT_CUMULI_JOB_NAME = "SilverStatisticsCumul";
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
  @Override
  public void init() {
    directoryToScan = new ArrayList<>();
    try {
      statsConfig = new StatisticsConfig();
      statsConfig.init();
      if (!statsConfig.isValidConfigFile()) {
        SilverLogger.getLogger(this).error("Statistics configuration is not valid");
      }
      SettingBundle settings =
          ResourceLocator.getSettingBundle("org.silverpeas.silverstatistics.SilverStatistics");
      final String sizeStatCron = settings.getString("scheduledGetStatSizeTimeStamp");
      final String volumeStatCron = settings.getString("scheduledGetStatVolumeTimeStamp");
      final String consolidationStatCron = settings.getString("scheduledCumulStatTimeStamp");
      initSchedulerStatistics(sizeStatCron, STAT_SIZE_JOB_NAME, this::doGetStatSize);
      initSchedulerStatistics(volumeStatCron, STAT_VOLUME_JOB_NAME, this::doGetStatVolume);
      initSchedulerStatistics(consolidationStatCron, STAT_CUMULI_JOB_NAME,
          this::doConsolidationStat);
      initDirectoryToScan(settings);
    } catch (SilverStatisticsConfigException e) {
      SilverLogger.getLogger(this).error("Initialization of statistics configuration failed", e);
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex);
    }
  }

  @Override
  public void release() throws SchedulerException {
    Scheduler scheduler = SchedulerProvider.getVolatileScheduler();
    scheduler.unscheduleJob(STAT_SIZE_JOB_NAME);
    scheduler.unscheduleJob(STAT_VOLUME_JOB_NAME);
    scheduler.unscheduleJob(STAT_CUMULI_JOB_NAME);
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
   * @param jobOperation the job operation.
   * @throws SilverpeasException if the computation scheduling failed or  if the cron expression
   * is malformed.
   */
  private void initSchedulerStatistics(String aCronString, String jobName,
      Consumer<Date> jobOperation) throws SilverpeasException {
    try {
      Scheduler scheduler = SchedulerProvider.getVolatileScheduler();
      scheduler.unscheduleJob(jobName);

      JobTrigger trigger = JobTrigger.triggerAt(aCronString);
      Job job = createJobWith(jobName, jobOperation);
      scheduler.scheduleJob(job, trigger);
    } catch (Exception e) {
      throw new SilverpeasException(e);
    }
  }

  /**
   * @param currentDate the date at which the method is called.
   */
  private void doGetStatVolume(Date currentDate) {
    try {
      getSilverStatistics().makeVolumeAlimentationForAllComponents();
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error("error during volume statistic computing started at {0}",
          new Object[]{currentDate}, ex);
    }

  }

  /**
   * For each directory computes the size of all its files and the size of all its subdirectories
   * recursively.
   * @param currentDate the date at which the method is called.
   */
  private void doGetStatSize(Date currentDate) {
    try {
      for (String aDirectoryToScan : directoryToScan) {
        DirectoryVolumeService service = new DirectoryVolumeService(new File(aDirectoryToScan));
        addStatSize(currentDate, aDirectoryToScan, service.getTotalSize());
      }
    } catch (Exception ex) {
      SilverLogger.getLogger(this)
          .error("error during size statistic computing started at {0}", new Object[]{currentDate},
              ex);
      if (ex instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
    }
  }

  /**
   * @param currentDate the date at which the method is called.
   */
  private void doConsolidationStat(Date currentDate) {
    try {
      getSilverStatistics().makeStatAllCumul();
    } catch (Exception ex) {
      SilverLogger.getLogger(this)
          .error("error during statistic consolidation computing started at {0}",
              new Object[]{currentDate}, ex);
    }
  }

  private void initDirectoryToScan(SettingBundle resource) {
    try {
      // read the directories
      int i = 0;
      String directoryPath = resource.getString("SilverPeasDataPath" + i, null);
      // for each directory
      while (directoryPath != null) {
        // Test existence
        File dir = new File(directoryPath);
        if (!dir.isDirectory()) {
          throw new SilverpeasException("SilverStatistics initDirectoryToScan " + directoryPath);
        }
        directoryToScan.add(directoryPath);
        i++;
        directoryPath = resource.getString("SilverPeasDataPath" + i, null);
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error("Statistics directory scanning in error", e);
    }
  }

  public void addStatVolume(String userId, long volume, Date dateAccess, String peasType,
      String spaceId, String componentId) {
    if (statsConfig.isRun(Volume)) {
      StringBuilder stat = buildStatisticData(userId, dateAccess, peasType, spaceId, componentId);
      stat.append(volume);
      sendStatistic(Volume, stat);
    }
  }

  private StringBuilder buildStatisticData(String userId, Date dateAccess, String peasType, String spaceId,
      String componentId) {
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
    return stat;
  }

  private void sendStatistic(StatType type, CharSequence stat) {
    if (isAsynchronStats(type)) {
      SilverStatisticsSender mySilverStatisticsSender = SilverStatisticsSender.get();
      try {
        mySilverStatisticsSender.send(type, stat.toString());
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }
    } else {
      // Synchronised way
      getSilverStatistics().putStats(type, stat.toString());
    }
  }

  public void addStatAccess(String userId, Date dateAccess, String peasType, String spaceId,
      String componentId) {
    // should feed Access (see SilverStatistics.properties)
    if (statsConfig.isRun(Access)) {
      StringBuilder stat = buildStatisticData(userId, dateAccess, peasType, spaceId, componentId);
      stat.append("1"); // countAccess
      sendStatistic(Access, stat);
    }
  }

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
      stat.append(duration); // duration

      sendStatistic(Connexion, stat);
    }
  }

  public void addStatSize(Date date, String dirName, long dirSize) {
    if (statsConfig.isRun(Size)) {
      StringBuilder stat = new StringBuilder();
      stat.append(DateUtil.formatAsISO8601Day(date));
      stat.append(SEPARATOR);
      // directoryName
      stat.append(dirName);
      stat.append(SEPARATOR);
      // directorySize
      stat.append(dirSize);
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
      SilverLogger.getLogger(this).error(e);
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
  private Job createJobWith(final String jobName, final Consumer<Date> jobOperation)
      throws SchedulerException {
    try {
      return new Job(jobName) {

        @Override
        public void execute(JobExecutionContext context) {
          Date date = context.getFireTime();
          jobOperation.accept(date);
        }
      };
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex);
      throw new SchedulerException(ex.getMessage());
    }
  }
}
