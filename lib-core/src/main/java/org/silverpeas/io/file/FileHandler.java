/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.io.file;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.silverpeas.io.session.IOSession;

/**
 * @author Yohann Chastagnier
 */
public class FileHandler extends AbstractFileHandler {

  /**
   * Default constructor
   * @param session
   */
  protected FileHandler(final IOSession session) {
    super(session);
  }

  // -----------------------------------------------------------------------

  /**
   * @see FileUtils
   */
  public File getFile(final String... names) {
    return FileUtils.getFile(names);
  }

  /**
   * @see FileUtils
   */
  public File getFile(final File file, final String... names) {
    return FileUtils.getFile(file, names);
  }

  /**
   * @see FileUtils
   */
  public File getFile(final IOBasePath basePath, final String... names) {
    return getFile(basePath, new File(basePath.getPath()), names);
  }

  /**
   * @see FileUtils
   */
  public File getFile(final IOBasePath basePath, final File file, final String... names) {
    verify(basePath, file);
    return getExistingFile(basePath, FileUtils.getFile(file, names));
  }

  // -----------------------------------------------------------------------

  /**
   * @see FileUtils
   */
  public OutputStream openOutputStream(final IOBasePath basePath, final File file) throws Exception {
    return openOutputStream(basePath, file, false);
  }

  /**
   * @see FileUtils
   */
  public OutputStream openOutputStream(final IOBasePath basePath, final File file,
      final boolean append) throws Exception {
    verify(basePath, file);
    return FileUtils.openOutputStream(getFileForWriting(basePath, file, append), append);
  }

  /**
   * @see FileUtils
   */
  public InputStream openInputStream(final IOBasePath basePath, final File file) throws Exception {
    verify(basePath, file);
    final File sessionFile = translateToSessionPath(basePath, file);
    if (sessionFile.exists()) {
      return FileUtils.openInputStream(sessionFile);
    }
    return FileUtils.openInputStream(translateToRealPath(basePath, file));
  }

  /**
   * @see FileUtils
   */
  public void touch(final IOBasePath basePath, final File file, final String... names)
      throws Exception {
    verify(basePath, file);
    FileUtils.touch(getFileForWriting(basePath, FileUtils.getFile(file, names), true));
  }

  // -----------------------------------------------------------------------

  /**
   * Adding this simple method based on listFiles from @see FileUtils
   * @param basePath
   * @param directory
   * @param extensions
   * @return
   */
  public Collection<File> listFiles(final IOBasePath basePath, final File directory,
      final String... extensions) {
    return listFiles(basePath, directory, true, extensions);
  }

  /**
   * @see FileUtils
   */
  public Collection<File> listFiles(final IOBasePath basePath, final File directory,
      final boolean recursive, String... extensions) {
    verify(basePath, directory);
    if (extensions.length == 0) {
      extensions = null;
    }

    final File sessionDirectory = translateToSessionPath(basePath, directory);
    final File realDirectory = translateToRealPath(basePath, directory);

    Collection<File> listFiles = null;
    if (sessionDirectory.exists()) {
      listFiles = FileUtils.listFiles(sessionDirectory, extensions, recursive);
    }
    Collection<File> realFiles = null;
    if (realDirectory.exists()) {
      realFiles = FileUtils.listFiles(realDirectory, extensions, recursive);
    }

    return mergeFileLists(basePath, listFiles, realFiles);
  }

  /**
   * @see FileUtils
   */
  public Collection<File> listFiles(final IOBasePath basePath, final File directory,
      final IOFileFilter fileFilter, final IOFileFilter dirFilter) {
    verify(basePath, directory);

    final File sessionDirectory = translateToSessionPath(basePath, directory);
    final File realDirectory = translateToRealPath(basePath, directory);

    Collection<File> listFiles = null;
    if (sessionDirectory.exists()) {
      listFiles = FileUtils.listFiles(sessionDirectory, fileFilter, dirFilter);
    }
    Collection<File> realFiles = null;
    if (realDirectory.exists()) {
      realFiles = FileUtils.listFiles(realDirectory, fileFilter, dirFilter);
    }

    return mergeFileLists(basePath, listFiles, realFiles);
  }

  /**
   * Centralizes merging treatment between session path content and real path content
   * @param basePath
   * @param sessionFiles
   * @param realFiles
   * @return
   */
  private Collection<File> mergeFileLists(final IOBasePath basePath,
      final Collection<File> sessionFiles, final Collection<File> realFiles) {
    final Collection<File> listFiles = new LinkedList<File>();
    final Set<String> filePaths = new HashSet<String>();
    if (sessionFiles != null) {
      for (final File sessionFile : sessionFiles) {
        listFiles.add(sessionFile);
        filePaths.add(translateToRealPath(basePath, sessionFile).getPath());
      }
    }
    if (realFiles != null) {
      for (final File realFile : realFiles) {
        if (!filePaths.contains(realFile.getPath()) && !isMarkedToDelete(basePath, realFile)) {
          listFiles.add(realFile);
        }
      }
    }
    return listFiles;
  }

  // -----------------------------------------------------------------------

  /**
   * Both of given files are handled
   * @see FileUtils
   */
  public boolean contentEquals(final IOBasePath basePath, final File file1, final File file2)
      throws Exception {
    verify(basePath, file1);
    verify(basePath, file2);
    return FileUtils.contentEquals(getExistingFile(basePath, file1),
        getExistingFile(basePath, file2));
  }

  /**
   * The first given file is not handled. It have to exist in an other path than the one that is
   * handled.
   * The second given file is handled.
   * Both files are handled
   * @see FileUtils
   */
  public boolean contentEquals(final File file1, final IOBasePath basePath, final File file2)
      throws Exception {
    verify(basePath, file2);
    return FileUtils.contentEquals(file1, getExistingFile(basePath, file2));
  }

  // -----------------------------------------------------------------------

  /**
   * Both of given files are handled
   * @see FileUtils
   */
  public void copyFile(final IOBasePath basePath, final File srcFile, final File destFile)
      throws Exception {
    verify(basePath, srcFile);
    verify(basePath, destFile);
    FileUtils.copyFile(getExistingFile(basePath, srcFile), getFileForWriting(basePath, destFile));
  }

  /**
   * The first given file is not handled. It have to exist in an other path than the one that is
   * handled.
   * The second given file is handled.
   * Both files are handled
   * @see FileUtils
   */
  public void copyFile(final File srcFile, final IOBasePath basePath, final File destFile)
      throws Exception {
    verify(basePath, destFile);
    FileUtils.copyFile(srcFile, getFileForWriting(basePath, destFile));
  }

  /**
   * The given file is handled
   * @see FileUtils
   */
  public long copyFile(final IOBasePath basePath, final File input, final OutputStream output)
      throws Exception {
    verify(basePath, input);
    return FileUtils.copyFile(getExistingFile(basePath, input), output);
  }

  /**
   * The given file is handled
   * @see FileUtils
   */
  public void copyURLToFile(final URL source, final IOBasePath basePath, final File destination)
      throws Exception {
    verify(basePath, destination);
    FileUtils.copyURLToFile(source, getFileForWriting(basePath, destination));
  }

  // -----------------------------------------------------------------------

  /**
   * @see FileUtils
   */
  public boolean delete(final IOBasePath basePath, final File file) {
    verify(basePath, file);
    final boolean isFirstTimeMarkedToBeDeleted = markToDelete(basePath, file);
    final boolean isDeletedInSession =
        FileUtils.deleteQuietly(translateToSessionPath(basePath, file));
    return isFirstTimeMarkedToBeDeleted || isDeletedInSession;
  }

  /**
   * @see FileUtils
   */
  public void cleanDirectory(final IOBasePath basePath, final File directory) throws Exception {
    if (!directory.exists()) {
      final String message = directory + " does not exist";
      throw new IllegalArgumentException(message);
    }

    if (!directory.isDirectory()) {
      final String message = directory + " is not a directory";
      throw new IllegalArgumentException(message);
    }

    verify(basePath, directory);

    final File[] files = directory.listFiles();
    if (files == null) { // null if security restricted
      throw new Exception("Failed to list contents of " + directory);
    }

    for (final File file : files) {
      delete(basePath, file);
    }
  }

  // -----------------------------------------------------------------------

  /**
   * @see FileUtils
   */
  public boolean waitFor(final IOBasePath basePath, final File file, final int seconds) {
    verify(basePath, file);
    return FileUtils.waitFor(getExistingFile(basePath, file), seconds);
  }

  // -----------------------------------------------------------------------

  /**
   * @see FileUtils
   */
  public String readFileToString(final IOBasePath basePath, final File file) throws Exception {
    return readFileToString(basePath, file, null);
  }

  /**
   * @see FileUtils
   */
  public String readFileToString(final IOBasePath basePath, final File file, final String encoding)
      throws Exception {
    verify(basePath, file);
    return FileUtils.readFileToString(getExistingFile(basePath, file), encoding);
  }

  /**
   * @see FileUtils
   */
  public byte[] readFileToByteArray(final IOBasePath basePath, final File file) throws Exception {
    verify(basePath, file);
    return FileUtils.readFileToByteArray(getExistingFile(basePath, file));
  }

  /**
   * @see FileUtils
   */
  public List<String> readLines(final IOBasePath basePath, final File file) throws Exception {
    return readLines(basePath, file, null);
  }

  /**
   * @see FileUtils
   */
  public List<String> readLines(final IOBasePath basePath, final File file, final String encoding)
      throws Exception {
    verify(basePath, file);
    return FileUtils.readLines(getExistingFile(basePath, file), encoding);
  }

  // -----------------------------------------------------------------------

  /**
   * @see FileUtils
   */
  public void writeStringToFile(final IOBasePath basePath, final File file, final String data)
      throws Exception {
    writeStringToFile(basePath, file, data, null, false);
  }

  /**
   * @see FileUtils
   */
  public void writeStringToFile(final IOBasePath basePath, final File file, final String data,
      final String encoding) throws Exception {
    writeStringToFile(basePath, file, data, encoding, false);
  }

  /**
   * @see FileUtils
   */
  public void writeStringToFile(final IOBasePath basePath, final File file, final String data,
      final boolean append) throws Exception {
    writeStringToFile(basePath, file, data, null, append);
  }

  /**
   * @see FileUtils
   */
  public void writeStringToFile(final IOBasePath basePath, final File file, final String data,
      final String encoding, final boolean append) throws Exception {
    verify(basePath, file);
    FileUtils.writeStringToFile(getFileForWriting(basePath, file, append), data, encoding, append);
  }

  // -----------------------------------------------------------------------

  /**
   * @see FileUtils
   */
  public void write(final IOBasePath basePath, final File file, final CharSequence data)
      throws Exception {
    write(basePath, file, data, null, false);
  }

  /**
   * @see FileUtils
   */
  public void write(final IOBasePath basePath, final File file, final CharSequence data,
      final boolean append) throws Exception {
    write(basePath, file, data, null, append);
  }

  /**
   * @see FileUtils
   */
  public void write(final IOBasePath basePath, final File file, final CharSequence data,
      final String encoding) throws Exception {
    write(basePath, file, data, encoding, false);
  }

  /**
   * @see FileUtils
   */
  public void write(final IOBasePath basePath, final File file, final CharSequence data,
      final String encoding, final boolean append) throws Exception {
    verify(basePath, file);
    FileUtils.write(getFileForWriting(basePath, file, append), data, encoding, append);
  }

  // -----------------------------------------------------------------------

  /**
   * @see FileUtils
   */
  public void writeByteArrayToFile(final IOBasePath basePath, final File file, final byte[] data)
      throws Exception {
    writeByteArrayToFile(basePath, file, data, false);
  }

  /**
   * @see FileUtils
   */
  public void writeByteArrayToFile(final IOBasePath basePath, final File file, final byte[] data,
      final boolean append) throws Exception {
    verify(basePath, file);
    FileUtils.writeByteArrayToFile(getFileForWriting(basePath, file, append), data, append);
  }

  // -----------------------------------------------------------------------

  /**
   * @see FileUtils
   */
  public void writeLines(final IOBasePath basePath, final File file, final Collection<?> lines)
      throws Exception {
    writeLines(basePath, file, null, lines, null, false);
  }

  /**
   * @see FileUtils
   */
  public void writeLines(final IOBasePath basePath, final File file, final Collection<?> lines,
      final boolean append) throws Exception {
    writeLines(basePath, file, null, lines, null, append);
  }

  /**
   * @see FileUtils
   */
  public void writeLines(final IOBasePath basePath, final File file, final Collection<?> lines,
      final String lineEnding) throws Exception {
    writeLines(basePath, file, null, lines, lineEnding, false);
  }

  /**
   * @see FileUtils
   */
  public void writeLines(final IOBasePath basePath, final File file, final Collection<?> lines,
      final String lineEnding, final boolean append) throws Exception {
    writeLines(basePath, file, null, lines, lineEnding, append);
  }

  /**
   * @see FileUtils
   */
  public void writeLines(final IOBasePath basePath, final File file, final String encoding,
      final Collection<?> lines) throws Exception {
    writeLines(basePath, file, encoding, lines, null, false);
  }

  /**
   * @see FileUtils
   */
  public void writeLines(final IOBasePath basePath, final File file, final String encoding,
      final Collection<?> lines, final boolean append) throws Exception {
    writeLines(basePath, file, encoding, lines, null, append);
  }

  /**
   * @see FileUtils
   */
  public void writeLines(final IOBasePath basePath, final File file, final String encoding,
      final Collection<?> lines, final String lineEnding) throws Exception {
    writeLines(basePath, file, encoding, lines, lineEnding, false);
  }

  /**
   * @see FileUtils
   */
  public void writeLines(final IOBasePath basePath, final File file, final String encoding,
      final Collection<?> lines, final String lineEnding, final boolean append) throws Exception {
    verify(basePath, file);
    FileUtils.writeLines(getFileForWriting(basePath, file, append), encoding, lines, lineEnding,
        append);
  }

  // -----------------------------------------------------------------------

  /**
   * @see FileUtils
   */
  public long sizeOf(final IOBasePath basePath, File file) {
    verify(basePath, file);
    file = getExistingFile(basePath, file);
    if (file.isFile()) {
      return FileUtils.sizeOf(file);
    } else {
      return sizeOfDirectory(basePath, file);
    }
  }

  /**
   * @see FileUtils
   */
  public long sizeOfDirectory(final IOBasePath basePath, final File directory) {
    if (!directory.exists()) {
      final String message = directory + " does not exist";
      throw new IllegalArgumentException(message);
    }

    if (!directory.isDirectory()) {
      final String message = directory + " is not a directory";
      throw new IllegalArgumentException(message);
    }

    verify(basePath, directory);

    long size = 0;

    for (final File file : listFiles(basePath, directory)) {
      size += FileUtils.sizeOf(file);
    }

    return size;
  }

  // -----------------------------------------------------------------------

  /**
   * @see FileUtils
   */
  public boolean isFileNewer(final IOBasePath basePath, final File file, final File reference) {
    verify(basePath, file);
    verify(basePath, reference);
    return FileUtils.isFileNewer(getExistingFile(basePath, file),
        getExistingFile(basePath, reference));
  }

  /**
   * @see FileUtils
   */
  public boolean isFileNewer(final IOBasePath basePath, final File file, final Date date) {
    verify(basePath, file);
    return FileUtils.isFileNewer(getExistingFile(basePath, file), date);
  }

  /**
   * @see FileUtils
   */
  public boolean isFileNewer(final IOBasePath basePath, final File file, final long timeMillis) {
    verify(basePath, file);
    return FileUtils.isFileNewer(getExistingFile(basePath, file), timeMillis);
  }

  // -----------------------------------------------------------------------

  /**
   * @see FileUtils
   */
  public boolean isFileOlder(final IOBasePath basePath, final File file, final File reference) {
    verify(basePath, file);
    verify(basePath, reference);
    return FileUtils.isFileOlder(getExistingFile(basePath, file),
        getExistingFile(basePath, reference));
  }

  /**
   * @see FileUtils
   */
  public boolean isFileOlder(final IOBasePath basePath, final File file, final Date date) {
    verify(basePath, file);
    return FileUtils.isFileOlder(getExistingFile(basePath, file), date);
  }

  /**
   * @see FileUtils
   */
  public boolean isFileOlder(final IOBasePath basePath, final File file, final long timeMillis) {
    verify(basePath, file);
    return FileUtils.isFileOlder(getExistingFile(basePath, file), timeMillis);
  }
}
