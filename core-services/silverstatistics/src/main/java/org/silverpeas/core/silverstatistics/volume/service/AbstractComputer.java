/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.silverstatistics.volume.service;

import org.silverpeas.core.admin.component.model.SilverpeasComponent;
import org.silverpeas.core.silverstatistics.volume.model.DirectoryStats;
import org.silverpeas.core.util.logging.SilverLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * @author silveryocha
 */
abstract class AbstractComputer implements Callable<DirectoryStats> {

  protected final File directory;
  private final boolean onlyComponentData;

  AbstractComputer(File directory, final boolean onlyComponentData) {
    this.directory = directory;
    this.onlyComponentData = onlyComponentData;
  }

  @SuppressWarnings("unchecked")
  @Override
  public final DirectoryStats call() {
    final DirectoryStats result = new DirectoryStats(directory.getName());
    final Optional<SilverpeasComponent> component = SilverpeasComponent
        .getByInstanceId(directory.getName());
    if (onlyComponentData && !component.isPresent()) {
      // root directory is not a component instance one, no computing is performed
      return result;
    }
    final FileVisitorOfTransverseServices fileVisitor = new FileVisitorOfTransverseServices(this,
        onlyComponentData);
    // transverse files
    try {
      Files.walkFileTree(Paths.get(directory.getPath()), fileVisitor);
    } catch (IOException e) {
      SilverLogger.getLogger(this).error(e);
    }
    setTransverseResult(result);
    // specific files
    component
        .ifPresent(
            c -> ComponentStatisticsProvider.getByComponentName(c.getName())
              .ifPresent(
                  s -> setSpecificResult(result, s)));
    return result;
  }

  protected abstract void handleTransverseFile(final Path file, final BasicFileAttributes attrs);

  protected abstract void setTransverseResult(final DirectoryStats result);

  protected abstract void setSpecificResult(final DirectoryStats result,
      final ComponentStatisticsProvider componentStatistics);

  private static class FileVisitorOfTransverseServices<C extends AbstractComputer>
      implements FileVisitor<Path> {

    private final C computer;
    private final boolean onlyComponentData;
    private boolean canVisitFile;

    private FileVisitorOfTransverseServices(final C computer, final boolean onlyComponentData) {
      this.computer = computer;
      this.onlyComponentData = onlyComponentData;
      this.canVisitFile = !onlyComponentData;
    }

    @Override
    public final FileVisitResult preVisitDirectory(final Path dir,
        final BasicFileAttributes attrs) {
      if (onlyComponentData && isAttachmentRepository(dir)) {
        canVisitFile = true;
      }
      return FileVisitResult.CONTINUE;
    }

    @Override
    public final FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
      if (canVisitFile && !isWysiwygContent(file)) {
        computer.handleTransverseFile(file, attrs);
      }
      return FileVisitResult.CONTINUE;
    }

    @Override
    public final FileVisitResult visitFileFailed(final Path file, final IOException exc) {
      SilverLogger.getLogger(this).warn(exc);
      return FileVisitResult.CONTINUE;
    }

    @Override
    public final FileVisitResult postVisitDirectory(final Path dir, final IOException exc) {
      if (onlyComponentData && isAttachmentRepository(dir)) {
        canVisitFile = false;
      }
      return FileVisitResult.CONTINUE;
    }

    private boolean isAttachmentRepository(final Path dir) {
      return dir.getFileName().toString().startsWith("simpledoc_");
    }

    private boolean isWysiwygContent(final Path file) {
      return file.getFileName().toString().matches(".+wysiwyg_[a-z]+[.]txt");
    }
  }
}
