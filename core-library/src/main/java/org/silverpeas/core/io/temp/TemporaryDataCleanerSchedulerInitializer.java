/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.core.io.temp;

import org.silverpeas.core.scheduler.Job;
import org.silverpeas.core.scheduler.JobExecutionContext;
import org.silverpeas.core.scheduler.Scheduler;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.AgeFileFilter;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.thread.ManagedThreadPool;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.StringUtil;

import javax.inject.Inject;
import java.io.File;
import java.util.Collection;

import static org.apache.commons.io.FileUtils.*;

/**
 * @author Yohann Chastagnier
 */
public class TemporaryDataCleanerSchedulerInitializer implements Initialization {

  protected static final String JOB_NAME = "TemporayDataCleanerJob";
  private static final File tempPath = new File(FileRepositoryManager.getTemporaryPath());

  @Inject
  private Scheduler scheduler;

  Thread startTask;

  @Override
  public void init() throws Exception{

    // Job instance
    final TemporaryDataCleanerJob temporaryDataCleanerJob = new TemporaryDataCleanerJob();

    // Cleaning temporary data at start if requested
    startTask = ManagedThreadPool.invoke(() -> {
      temporaryDataCleanerJob.clean(
          TemporaryDataManagementSetting.getTimeAfterThatFilesMustBeDeletedAtServerStart());
    }).iterator().next();

    // Setting CRON
    final String cron = TemporaryDataManagementSetting.getJobCron();
    scheduler.unscheduleJob(JOB_NAME);
    if (StringUtil.isDefined(cron)) {
      scheduler.scheduleJob(temporaryDataCleanerJob, JobTrigger.triggerAt(cron));
    }
  }

  /**
   * Temporary data cleaner.
   * @author Yohann Chastagnier
   */
  private class TemporaryDataCleanerJob extends Job {

    /**
     * Default constructor.
     */
    public TemporaryDataCleanerJob() {
      super(JOB_NAME);
    }

    @Override
    public void execute(final JobExecutionContext context) throws Exception {

      // 1 hour minimum or each time
      long nbMilliseconds = TemporaryDataManagementSetting.getTimeAfterThatFilesMustBeDeleted();
      if (nbMilliseconds < 0) {
        nbMilliseconds = 0;
      }
      clean(nbMilliseconds);
    }

    /**
     * Cleaning treatment
     * @param nbMilliseconds : age of files that don't have to be deleted (in milliseconds)
     */
    private synchronized void clean(final long nbMilliseconds) {

      // Temporary temp directory
      if (tempPath.exists()) {

        // Nothing to do if fileAge is negative
        if (nbMilliseconds >= 0) {

          // Calculating the date from which files should be deleted from their date of last
          // modification. (in milliseconds)
          long fileAge = System.currentTimeMillis() - nbMilliseconds;

          // List all files to clean in temp directory and its subdirectories if any
          delete(listFiles(tempPath, new AgeFileFilter(fileAge), TrueFileFilter.TRUE));

          // Calculating the date from which empty directories should be deleted from their date of
          // last modification. (in milliseconds)
          fileAge = System.currentTimeMillis() - nbMilliseconds;

          // Deleting all empty subdirectories
          delete(listFilesAndDirs(tempPath, FalseFileFilter.FALSE,
              new AndFileFilter(new AgeFileFilter(fileAge), new AbstractFileFilter() {

                @Override
                public boolean accept(final File file, final String name) {
                  // Checks subdirectories
                  return listFiles(file, TrueFileFilter.TRUE, TrueFileFilter.TRUE).size() == 0;
                }
              })));
        }
      }
    }

    /**
     * Deleting files
     * @param filesToDelete
     */
    private void delete(final Collection<File> filesToDelete) {
      if (filesToDelete != null) {
        filesToDelete.remove(tempPath);
        for (final File fileToDelete : filesToDelete) {
          deleteQuietly(fileToDelete);
        }
      }
    }
  }
}
