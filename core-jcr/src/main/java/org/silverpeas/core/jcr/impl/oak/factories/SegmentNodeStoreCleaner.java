/*
 * Copyright (C) 2000 - 2023 Silverpeas
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

package org.silverpeas.core.jcr.impl.oak.factories;

import org.apache.jackrabbit.oak.segment.file.FileStore;
import org.silverpeas.core.SilverpeasException;
import org.silverpeas.core.backgroundprocess.RunnableBackgroundProcess;
import org.silverpeas.core.jcr.impl.oak.configuration.SegmentNodeStoreConfiguration;
import org.silverpeas.core.scheduler.Job;
import org.silverpeas.core.scheduler.JobExecutionContext;
import org.silverpeas.core.util.logging.SilverLogger;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;

import static java.text.MessageFormat.format;

/**
 * This Job implementation is in charge of cleaning the segment data which is obsolete.
 * <p>
 *   In a first time, a full garbage collection is performed (in a JCR meaning):
 *   <ul>
 *     <li>estimation</li>
 *     <li>compaction</li>
 *     <li>cleanup</li>
 *   </ul>
 * </p>
 * <p>
 *   In a second time, all the backup files created in a read only context are deleted (*.bak).
 * </p>
 * <p>
 *   The processing is performed into a {@link RunnableBackgroundProcess} ensuring to have
 *   only one process at a time.
 * </p>
 * @author silveryocha
 */
public class SegmentNodeStoreCleaner extends Job {

  private static final String JOB_NAME = SegmentNodeStoreCleaner.class.getSimpleName();

  private final Path segmentPath;
  private final FileStore fs;
  private final SegmentNodeStoreConfiguration parameters;

  /**
   * Creates a new job with the specified name.
   * @param segmentPath the root {@link Path} of the segment data.
   * @param fs the {@link FileStore} initialized with Silverpeas's configuration.
   * @param parameters a {@link SegmentNodeStoreConfiguration} instance centralizing all segment
   * store configuration.
   */
  public SegmentNodeStoreCleaner(final Path segmentPath, final FileStore fs,
      final SegmentNodeStoreConfiguration parameters) {
    super(JOB_NAME);
    this.segmentPath = segmentPath;
    this.fs = fs;
    this.parameters = parameters;
  }

  @Override
  public void execute(final JobExecutionContext context) throws SilverpeasException {
    RunnableBackgroundProcess.register(() -> {
      fullGC();
      cleanBackupFiles();
    });
  }

  private void fullGC() {
    try {
      fs.fullGC();
    } catch (IOException e) {
      SilverLogger.getLogger(this).error(e);
    }
  }

  private void cleanBackupFiles() {
    final int backupFileAgeThreshold = parameters.getBackupFileAgeThreshold();
    if (backupFileAgeThreshold >= 0) {
      try {
        final BackupFileDeleter visitor = new BackupFileDeleter(backupFileAgeThreshold);
        Files.walkFileTree(segmentPath, visitor);
        SilverLogger.getLogger(this)
            .info(format("{0} backup file(s) have been deleted.", visitor.getDeletionCount()));
      } catch (IOException e) {
        SilverLogger.getLogger(this).error(e);
      }
    } else {
      SilverLogger.getLogger(this).info(format("Backup file deletion is not enabled."));
    }
  }

  private static class BackupFileDeleter extends SimpleFileVisitor<Path> {

    private final Instant now = Instant.now();
    private final int backupFileAgeThreshold;
    private int deletionCount = 0;

    public BackupFileDeleter(final int backupFileAgeThreshold) {
      this.backupFileAgeThreshold = backupFileAgeThreshold;
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
        throws IOException {
      if (file.getFileName().toString().endsWith(".bak") && (backupFileAgeThreshold == 0 ||
          Duration.between(attrs.lastModifiedTime().toInstant(), now).toDays() >= backupFileAgeThreshold)) {
        Files.delete(file);
        deletionCount++;
      }
      return super.visitFile(file, attrs);
    }

    public int getDeletionCount() {
      return deletionCount;
    }
  }
}
