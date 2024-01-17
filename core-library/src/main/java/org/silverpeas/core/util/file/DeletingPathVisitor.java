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

package org.silverpeas.core.util.file;

import org.silverpeas.core.util.logging.SilverLogger;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Visitor that allows to delete quietly (no exceptions) the root path and its content.
 * @author silveryocha
 */
public class DeletingPathVisitor extends SimpleFileVisitor<Path> {

  /**
   * Deletes quietly (no exceptions)
   * @param path the root path to delete.
   * @return boolean that indicates an effective deletion.
   */
  public static boolean deleteQuietly(final Path path) {
    try {
      if (Files.exists(path)) {
        Files.walkFileTree(path, new DeletingPathVisitor());
      }
    } catch (IOException e) {
      SilverLogger.getLogger(DeletingPathVisitor.class).error(e);
      return false;
    }
    return true;
  }

  @Override
  public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
      throws IOException {
    try {
      return super.visitFile(file, attrs);
    } finally {
      Files.delete(file);
    }
  }

  @Override
  public FileVisitResult postVisitDirectory(final Path dir, final IOException exc)
      throws IOException {
    try {
      return super.postVisitDirectory(dir, exc);
    } finally {
      Files.delete(dir);
    }
  }
}
