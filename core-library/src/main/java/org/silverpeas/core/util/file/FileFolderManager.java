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

import org.apache.commons.io.IOCase;
import org.apache.commons.io.comparator.NameFileComparator;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.ImageUtil;
import org.silverpeas.core.util.UtilException;
import org.silverpeas.core.util.logging.SilverLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileFolderManager {

  private static final String NOT_A_DIRECTORY_MSG = " isn't a directory";

  /**
   * Gets all the folders (an only the folders, not the files) that are directly inside the
   * specified directory. Throws an {@link UtilException} if the
   * specified path doesn't denote a directory or if the listing of all of its directories fails.
   * @param path the path of the directory.
   * @return a collection of folders.
   */
  public static Collection<File> getAllSubFolder(final String path) {
    final List<File> result;
    final Path directory = Paths.get(path);
    if (directory.toFile().isDirectory()) {
      try (final Stream<Path> folders = Files.list(directory)) {
        result = folders.filter(p -> p.toFile().isDirectory())
            .map(Path::toFile)
            .collect(Collectors.toList());
      } catch (IOException e) {
        throw new UtilException(e);
      }
    } else {
      throw new UtilException(path + NOT_A_DIRECTORY_MSG);
    }
    return result;
  }

  /**
   * Returns all the files (and only the files, not the directories) that are directly inside the
   * given directory. Throws an {@link UtilException} if the
   * specified path doesn't denote a directory or if the listing of all of its files fails.
   * @param path the path of the directory
   * @return a collection of files
   */
  public static Collection<File> getAllFile(final String path) {
    final List<File> result;
    final Path directory = Paths.get(path);
    if (directory.toFile().isDirectory()) {
      try (final Stream<Path> folders = Files.list(directory)) {
        result = folders.filter(p -> p.toFile().isFile())
            .map(Path::toFile)
            .sorted(new NameFileComparator(IOCase.INSENSITIVE))
            .collect(Collectors.toList());
      } catch (IOException e) {
        throw new UtilException(e);
      }
    } else {
      throw new UtilException(path + NOT_A_DIRECTORY_MSG);
    }
    return result;
  }

  /**
   * Returns all the images that are inside the given directory and its subdirectories.
   * Throws an {@link UtilException} if the specified path doesn't
   * denote a directory or if the recursive listing of all of the images fails.
   * @param path the path of the directory.
   * @return a collection of image files.
   */
  public static Collection<File> getAllImages(final String path) {
    final List<File> result;
    final Path directory = Paths.get(path);
    if (directory.toFile().isDirectory()) {
      try (final Stream<Path> files = Files.walk(directory)) {
        result = files.filter(p -> Stream.of(ImageUtil.IMAGE_EXTENTIONS)
            .anyMatch(e -> p.toFile().getName().toLowerCase().endsWith(e.toLowerCase())))
            .map(Path::toFile)
            .collect(Collectors.toList());
      } catch (IOException e) {
        throw new UtilException(e);
      }
    } else {
      throw new UtilException(path + NOT_A_DIRECTORY_MSG);
    }
    return result;
  }

  /**
   * Gets all the web pages that are inside the specified directory and its subdirectories,
   * whatever their type (HTML, ...).
   * Throws an {@link UtilException} if the specified path doesn't
   * denote a directory or if the recursive listing of all of the web pages fails.
   * @param path the path of the directory containing web pages.
   * @return a collection of web pages.
   */
  public static Collection<File> getAllWebPages(final String path) {
    final List<File> result;
    final Path directory = Paths.get(path);
    if (directory.toFile().isDirectory()) {
      try (final Stream<Path> files = Files.walk(directory)) {
        result = files.map(Path::toFile).filter(File::isFile).collect(Collectors.toList());
      } catch (IOException e) {
        throw new UtilException(e);
      }
    } else {
      throw new UtilException(path + NOT_A_DIRECTORY_MSG);
    }
    return result;
  }

  /**
   * Gets all the HTML web pages that are inside the specified directory and its subdirectories.
   * Throws an {@link UtilException} if the specified path doesn't
   * denote a directory or if the recursive listing of all of the HTML web pages fails.
   * @param path the path of the directory containing web pages.
   * @return a collection of HTML pages.
   */
  public static Collection<File> getAllHTMLWebPages(final String path) {
    final List<File> result;
    final Path directory = Paths.get(path);
    if (directory.toFile().isDirectory()) {
      try (final Stream<Path> files = Files.walk(directory)) {
        result = files.map(Path::toFile)
            .filter(File::isFile)
            .filter(f -> f.getName().toLowerCase().endsWith(".html") ||
                f.getName().toLowerCase().endsWith(".htm"))
            .collect(Collectors.toList());
      } catch (IOException e) {
        throw new UtilException(e);
      }
    } else {
      throw new UtilException(path + NOT_A_DIRECTORY_MSG);
    }
    return result;
  }

  /**
   * Creates the specified folder.
   * If the specified path doesn't denote a directory, does nothing.
   * Throws an {@link UtilException} if the folder creation fails.
   * @param path the path of the folder to create.
   */
  public static void createFolder(final String path) {
    final File directory = new File(path);
    if (!directory.exists() || directory.isDirectory()) {
      createFolder(directory);
    }
  }

  /**
   * Creates the specified folder.
   * Throws an {@link UtilException} if the folder creation fails.
   * @param directory the folder.
   */
  public static void createFolder(final File directory) {
    try {
      Files.createDirectories(directory.toPath());
    } catch (IOException ioex) {
      throw new UtilException(ioex);
    }
  }

  /**
   * Moves or rename the specified folder to the new one. If the path and the new path denote the
   * same parent directory, then it means the folder will be renamed to the name ending the
   * specified <code>newPath</code>. Otherwise the folder located by the given path will be move
   * to the new path and will be renamed accordingly the name ending the <code>newPath</code>
   * parameter.
   * Throws an {@link UtilException} if the specified path doesn't
   * denote a directory or if the folder moving/renaming fails.
   * @param path the path of the folder to rename or to move.
   * @param newPath the new path of the folder.
   */
  public static void moveFolder(final String path, final String newPath) {
    final Path source = Paths.get(path);
    if (source.toFile().isDirectory()) {
      try {
        Files.move(source, Paths.get(newPath));
      } catch (IOException e) {
        throw new UtilException(e);
      }
    } else {
      throw new UtilException(path + NOT_A_DIRECTORY_MSG);
    }
  }

  /**
   * Deletes the specified directory recursively and quietly.
   * @param path the path of a directory
   */
  public static void deleteFolder(final String path) {
    try (final Stream<Path> paths = Files.walk(Paths.get(path))) {
      paths.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    } catch (IOException e) {
      SilverLogger.getLogger(FileFolderManager.class).warn(e);
    }
  }

  /**
   * Creates the specified file into the given directory with the specified content.
   * Throws an {@link UtilException} if the specified path doesn't
   * denote a directory or if file creation fails.
   * @param directoryPath the path of the directory into which the file will be created.
   * @param fileName the name of the file to create.
   * @param fileContent the content of the file. The content is expected to be encoded in UTF-8.
   */
  public static void createFile(final String directoryPath, final String fileName,
      final String fileContent) {
    final Path folder = Paths.get(directoryPath);
    if (folder.toFile().isDirectory()) {
      try {
        Files.write(folder.resolve(fileName), fileContent.getBytes(Charsets.UTF_8));
      } catch (IOException e) {
        throw new UtilException(e);
      }
    } else {
      throw new UtilException(directoryPath + NOT_A_DIRECTORY_MSG);
    }
  }

  /**
   * Renames the specified file in the given directory with the new specified name.
   * Throws an {@link UtilException} if the specified path doesn't
   * denote a directory or if the file in this directory isn't a file or if the renaming fails.
   * @param directoryPath the path of the directory containing the file to rename.
   * @param name the name of the file.
   * @param newName the new name of the file.
   */
  public static void renameFile(final String directoryPath, final String name,
      final String newName) {
    final Path directory = Paths.get(directoryPath);
    if (directory.toFile().isDirectory()) {
      final Path fileToRename = directory.resolve(name);
      if (fileToRename.toFile().isFile()) {
        try {
          Files.move(directory, directory.resolve(newName));
        } catch (IOException e) {
          throw new UtilException(e);
        }
      } else {
        throw new UtilException(fileToRename + " isn't a file");
      }
    } else {
      throw new UtilException(directoryPath + NOT_A_DIRECTORY_MSG);
    }
  }

  /**
   * Deletes the specified file.
   * Throws an {@link UtilException} if the deletion fails.
   * @param path the path of the file to delete.
   */
  public static void deleteFile(final String path) {
    try {
      Files.delete(Paths.get(path));
    } catch (IOException e) {
      throw new UtilException(e);
    }
  }

  /**
   * Gets the content of the specified file.
   * Throws an {@link UtilException} if the specified path doesn't
   * denote a directory or if the reading of the file content fails.
   * @param directoryPath the path of the directory containing the file to read.
   * @param fileName the name of the file.
   * @return the content of the whole file as a String instance. The content is expected to be
   * encoded in UTF-8.
   */
  public static Optional<String> getFileContent(final String directoryPath, final String fileName) {
    final Path directory = Paths.get(directoryPath);
    if (directory.toFile().isDirectory()) {
      String content = null;
      try {
        content = new String(Files.readAllBytes(directory.resolve(fileName)), Charsets.UTF_8);
      } catch (IOException e) {
        SilverLogger.getLogger(FileFolderManager.class)
            .debug(directory.resolve(fileName).toString() + "does not exist");
      }
      return Optional.ofNullable(content);
    } else {
      throw new UtilException(directoryPath + NOT_A_DIRECTORY_MSG);
    }
  }

  private FileFolderManager() {
  }
}
