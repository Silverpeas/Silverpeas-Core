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
package org.silverpeas.core.io.temp;

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.scheduler.Job;
import org.silverpeas.core.scheduler.JobExecutionContext;
import org.silverpeas.core.scheduler.SchedulingInitializer;
import org.silverpeas.core.thread.ManagedThreadPool;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.kernel.annotation.NonNull;
import org.silverpeas.kernel.logging.SilverLogger;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static java.text.MessageFormat.format;
import static java.util.Optional.ofNullable;
import static org.silverpeas.core.io.temp.TemporaryDataManagementSetting.getTimeAfterThatFilesMustBeDeletedAtServerStart;

/**
 * @author Yohann Chastagnier
 */
@Service
@Singleton
public class TemporaryDataCleanerSchedulerInitializer extends SchedulingInitializer {

  protected static final String JOB_NAME = "TemporayDataCleanerJob";
  private static final File tempPath = new File(FileRepositoryManager.getTemporaryPath());

  private Future<Void> startTask;
  private final TemporaryDataCleanerJob job = new TemporaryDataCleanerJob();

  @Override
  protected void preSchedule() {
    // Cleaning temporary data at start if requested
    startTask = job.startCleanProcess(getTimeAfterThatFilesMustBeDeletedAtServerStart());
  }

  @NonNull
  @Override
  protected String getCron() {
    return TemporaryDataManagementSetting.getJobCron();
  }

  @NonNull
  @Override
  protected Job getJob() {
    return job;
  }

  @Override
  protected boolean isSchedulingEnabled() {
    return true;
  }

  Optional<Future<Void>> getStartTask() {
    return ofNullable(startTask);
  }

  /**
   * Temporary data cleaner.
   * @author Yohann Chastagnier
   */
  static class TemporaryDataCleanerJob extends Job {

    private final Object mutex = new Object();
    private final AtomicInteger nbAttempts = new AtomicInteger(0);
    private Future<Void> currentTask;

    /**
     * Default constructor.
     */
    public TemporaryDataCleanerJob() {
      super(JOB_NAME);
    }

    @Override
    public void execute(final JobExecutionContext context) {
      // 1 hour minimum or each time
      long nbMilliseconds = TemporaryDataManagementSetting.getTimeAfterThatFilesMustBeDeleted();
      if (nbMilliseconds < 0) {
        nbMilliseconds = 0;
      }
      startCleanProcess(nbMilliseconds);
    }

    /**
     * Starting the cleaning processing.
     * <p>
     *   This method ensures not having two threads running at same time. If this method is
     *   invoked while a cleaning process is yet running, then nothing is done.
     * </p>
     * <p>
     *   When the first time a new clean process is started while an other one is yet running, it
     *   is ignored.
     * </p>
     * <p>
     *   When for a second time a new clean process is started while the other one is yet running
     *   (the same than the first time), the one running is aborted (because it could be a
     *   technical problem) and the new one processes.
     * </p>
     * @param nbMilliseconds : age of files that don't have to be deleted (in milliseconds)
     * @return the {@link Future} of the current process which can be the same as a previous JOB
     * invocation.
     */
    Future<Void> startCleanProcess(final long nbMilliseconds) {
      synchronized (mutex) {
        if (!tempPath.exists() || nbMilliseconds < 0) {
          return null;
        }
        Future<Void> task = getSafelyCurrentTask();
        if (task != null && !task.isDone()) {
          if (nbAttempts.incrementAndGet() > 1) {
            nbAttempts.setRelease(0);
            SilverLogger.getLogger(this)
                .warn("Attempt for a second time to start a new cleaning process " +
                    "whereas an other one is not yet finished. " +
                    "Aborting the previous one to create a new process");
            task.cancel(true);
          } else {
            SilverLogger.getLogger(this)
                .warn("Attempt to start a new cleaning process " +
                    "whereas an other one is not yet finished. New cleaning process is ignored");
            return task;
          }
        }
        try {
          task = ManagedThreadPool.getPool().invoke(new CleaningProcess(nbMilliseconds));
          this.currentTask = task;
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          SilverLogger.getLogger(this).error(e);
        }
        return task;
      }
    }

    private Future<Void> getSafelyCurrentTask() {
      synchronized (mutex) {
        return currentTask;
      }
    }
  }

  private static class CleaningProcess implements Callable<Void> {
    private final long millisecondOffset;

    private CleaningProcess(final long millisecondOffset) {
      this.millisecondOffset = millisecondOffset;
    }

    @Override
    public Void call() {
      // Calculating the date from which files and directories should be deleted from their date
      // of last modification. (from offset specified in milliseconds)
      final ZonedDateTime age = ZonedDateTime.now().minus(millisecondOffset, ChronoUnit.MILLIS);
      final CleaningObsoleteFileAndDirectoriesVisitor visitor = new CleaningObsoleteFileAndDirectoriesVisitor(age);
      try {
        Files.walkFileTree(tempPath.toPath(), visitor);
        SilverLogger.getLogger(this)
            .info(format("{0} file(s) and {1} directorie(s) deleted.", visitor.getNbDeletedFiles(),
                visitor.getNbDeletedDirectories()));
      } catch (IOException e) {
        SilverLogger.getLogger(this).error(e);
      }
      return null;
    }
  }

  static class CleaningObsoleteFileAndDirectoriesVisitor extends SimpleFileVisitor<Path> {
    private final Instant age;
    private final Set<Path> directoriesThatCanBeDeleted = new HashSet<>();
    private final SilverLogger logger = SilverLogger.getLogger(this);
    private boolean rootVisited = false;
    private long nbDeletedFiles = 0;
    private long nbDeletedDirectories = 0;

    public CleaningObsoleteFileAndDirectoriesVisitor(final ZonedDateTime age) {
      this.age = age.toInstant();
    }

    public long getNbDeletedFiles() {
      return nbDeletedFiles;
    }

    public long getNbDeletedDirectories() {
      return nbDeletedDirectories;
    }

    @NonNull
    @Override
    public FileVisitResult preVisitDirectory(@NonNull final Path dir,
        @NonNull final BasicFileAttributes attrs) throws IOException {
      try {
        return super.preVisitDirectory(dir, attrs);
      } finally {
        if(rootVisited && isObsolete(attrs)) {
          directoriesThatCanBeDeleted.add(dir);
        }
        rootVisited = true;
      }
    }

    @NonNull
    @Override
    public FileVisitResult visitFile(@NonNull final Path file,
        @NonNull final BasicFileAttributes attrs) throws IOException {
      try {
        return super.visitFile(file, attrs);
      } finally {
        if (isObsolete(attrs)) {
          if(Files.deleteIfExists(file)) {
            nbDeletedFiles++;
            logger.debug(() -> format("Deleting file {0}", file.toFile().getPath()));
          } else {
            logger.warn(
                format("Detecting file [{0}] for deletion, but it is not present when deleting it!",
                    file.toFile().getPath()));
          }
        }
      }
    }

    @NonNull
    @Override
    public FileVisitResult postVisitDirectory(@NonNull final Path dir, final IOException exc)
        throws IOException {
      try {
        return super.postVisitDirectory(dir, exc);
      } finally {
        if (directoriesThatCanBeDeleted.remove(dir)) {
          try {
            // deleting a directory only if it is empty
            if (Files.deleteIfExists(dir)) {
              nbDeletedDirectories++;
              logger.debug(() -> format("Deleting directory {0}", dir.toFile().getPath()));
            } else {
              logger.warn(format(
                  "Detecting directory [{0}] for deletion, but it is not present when deleting it!",
                      dir.toFile().getPath()));
            }
          } catch (DirectoryNotEmptyException e) {
            // ignoring this exception as it is a functional case
            logger.silent(e);
          }
        }
      }
    }

    private boolean isObsolete(final BasicFileAttributes attrs) {
      return attrs.lastModifiedTime().toInstant().isBefore(age);
    }
  }
}
