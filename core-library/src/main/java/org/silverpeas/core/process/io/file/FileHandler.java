/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General protected License as
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
 * GNU Affero General protected License for more details.
 *
 * You should have received a copy of the GNU Affero General protected License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.process.io.file;

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
import org.silverpeas.core.process.session.ProcessSession;

/**
 * This is an handler that permits to perform transactional file manipulations. It has to be used
 * exclusively in classes that implements <code>SilverpeasProcess</code>. In a standard use,
 * <code>getHandledFile</code> method has to be called to obtain a <code>HandledFile</code> instance
 * (@see {@link HandledFile}).
 * @author Yohann Chastagnier
 */
public class FileHandler extends AbstractFileHandler {

  /**
   * Default constructor
   * @param session
   */
  protected FileHandler(final ProcessSession session) {
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
   * Gets a temporary file contained in session path. This file exists during the transaction and is
   * deleted after commit or rollback. Otherwise it represents the file from the destination path.
   */
  public File getSessionTemporaryFile(final String... names) {
    return FileUtils.getFile(getSessionTemporaryPath(), names);
  }

  /**
   * Gets the handled file that represents the file in session path if exists or if the file doesn't
   * exist into destination path. Otherwise it represents the file from the destination path.
   * @param basePath
   * @param names relative path from basePath param
   */
  public HandledFile getHandledFile(final FileBasePath basePath, final String... names) {
    return getHandledFile(basePath, new File(basePath.getPath()), names);
  }

  /**
   * Gets the handled file that represents the file in session path if exists or if the file doesn't
   * exist into destination path. Otherwise it represents the file from the destination path.
   * @param basePath
   * @param file the file or path whose the start of file path is equals to the root file path
   * defined by basePath param
   * @param names relative path from basePath param
   */
  public HandledFile getHandledFile(final FileBasePath basePath, final File file,
      final String... names) {
    verify(basePath, file);
    return new HandledFile(basePath, this,
        getExistingFile(basePath, FileUtils.getFile(file, names)));
  }

  /**
   * Indicates if the file exists in session or in real path.
   * @see FileUtils
   */
  protected boolean exists(final FileBasePath basePath, final File file, final String... names) {
    verify(basePath, file);
    return exists(getExistingFile(basePath, FileUtils.getFile(file, names)));
  }

  // -----------------------------------------------------------------------

  /**
   * @see FileUtils
   */
  protected OutputStream openOutputStream(final FileBasePath basePath, final File file)
      throws Exception {
    return openOutputStream(basePath, file, false);
  }

  /**
   * @see FileUtils
   */
  protected OutputStream openOutputStream(final FileBasePath basePath, final File file,
      final boolean append) throws Exception {
    verify(basePath, file);
    return FileUtils.openOutputStream(getFileForWriting(basePath, file, append), append);
  }

  /**
   * @see FileUtils
   */
  protected InputStream openInputStream(final FileBasePath basePath, final File file)
      throws Exception {
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
  protected void touch(final FileBasePath basePath, final File file, final String... names)
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
  protected Collection<File> listFiles(final FileBasePath basePath, final File directory,
      final String... extensions) {
    return listFiles(basePath, directory, true, extensions);
  }

  /**
   * @see FileUtils
   */
  protected Collection<File> listFiles(final FileBasePath basePath, final File directory,
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
  protected Collection<File> listFiles(final FileBasePath basePath, final File directory,
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
  private Collection<File> mergeFileLists(final FileBasePath basePath,
      final Collection<File> sessionFiles, final Collection<File> realFiles) {
    final Collection<File> listFiles = new LinkedList<>();
    final Set<String> filePaths = new HashSet<>();
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
  protected boolean contentEquals(final FileBasePath basePath, final File file1, final File file2)
      throws Exception {
    verify(basePath, file1);
    verify(basePath, file2);
    return FileUtils.contentEquals(getExistingFile(basePath, file1),
        getExistingFile(basePath, file2));
  }

  /**
   * The first given file is not handled. It have to exist in an other path than the one that is
   * handled. The second given file is handled. Both files are handled
   * @see FileUtils
   */
  protected boolean contentEquals(final File file1, final FileBasePath basePath, final File file2)
      throws Exception {
    verify(basePath, file2);
    return FileUtils.contentEquals(file1, getExistingFile(basePath, file2));
  }

  // -----------------------------------------------------------------------

  /**
   * Both of given files are handled
   * @see FileUtils
   */
  protected void copyFile(final FileBasePath basePath, final File srcFile, final File destFile)
      throws Exception {
    copyFile(basePath, srcFile, basePath, destFile);
  }

  /**
   * Both of given files are handled
   * @see FileUtils
   */
  protected void copyFile(final FileBasePath basePath, final File srcFile,
      final FileBasePath basePathDest, final File destFile) throws Exception {
    verify(basePath, srcFile);
    verify(basePathDest, destFile);
    FileUtils.copyFile(getExistingFile(basePath, srcFile),
        getFileForWriting(basePathDest, destFile));
  }

  /**
   * The first given file is not handled. It have to exist in an other path than the one that is
   * handled. The second given file is handled. Both files are handled
   * @see FileUtils
   */
  public void copyFile(final File srcFile, final HandledFile destFile) throws Exception {
    FileUtils.copyFile(srcFile, destFile.getFile());
  }

  /**
   * The given file is handled
   * @see FileUtils
   */
  protected long copyFile(final FileBasePath basePath, final File input, final OutputStream output)
      throws Exception {
    verify(basePath, input);
    return FileUtils.copyFile(getExistingFile(basePath, input), output);
  }

  /**
   * The given file is handled
   * @see FileUtils
   */
  protected void copyURLToFile(final URL source, final FileBasePath basePath, final File destination)
      throws Exception {
    verify(basePath, destination);
    FileUtils.copyURLToFile(source, getFileForWriting(basePath, destination));
  }

  // -----------------------------------------------------------------------

  /**
   * @see FileUtils
   */
  protected boolean delete(final FileBasePath basePath, final File file) throws Exception {
    verify(basePath, file);
    final boolean isFirstTimeMarkedToBeDeleted = markToDelete(basePath, file);
    final boolean isDeletedInSession =
        FileUtils.deleteQuietly(translateToSessionPath(basePath, file));
    return isFirstTimeMarkedToBeDeleted || isDeletedInSession;
  }

  /**
   * @see FileUtils
   */
  protected void cleanDirectory(final FileBasePath basePath, File directory) throws Exception {
    verify(basePath, directory);
    directory = translateToSessionPath(basePath, directory);

    if (!directory.exists()) {
      final String message = directory + " does not exist";
      throw new IllegalArgumentException(message);
    }

    if (!directory.isDirectory()) {
      final String message = directory + " is not a directory";
      throw new IllegalArgumentException(message);
    }

    final Collection<File> files = listFiles(basePath, directory);
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
  protected boolean waitFor(final FileBasePath basePath, final File file, final int seconds) {
    verify(basePath, file);
    return FileUtils.waitFor(getExistingFile(basePath, file), seconds);
  }

  // -----------------------------------------------------------------------

  /**
   * @see FileUtils
   */
  protected String readFileToString(final FileBasePath basePath, final File file) throws Exception {
    return readFileToString(basePath, file, null);
  }

  /**
   * @see FileUtils
   */
  protected String readFileToString(final FileBasePath basePath, final File file,
      final String encoding) throws Exception {
    verify(basePath, file);
    return FileUtils.readFileToString(getExistingFile(basePath, file), encoding);
  }

  /**
   * @see FileUtils
   */
  protected byte[] readFileToByteArray(final FileBasePath basePath, final File file)
      throws Exception {
    verify(basePath, file);
    return FileUtils.readFileToByteArray(getExistingFile(basePath, file));
  }

  /**
   * @see FileUtils
   */
  protected List<String> readLines(final FileBasePath basePath, final File file) throws Exception {
    return readLines(basePath, file, null);
  }

  /**
   * @see FileUtils
   */
  protected List<String> readLines(final FileBasePath basePath, final File file,
      final String encoding) throws Exception {
    verify(basePath, file);
    return FileUtils.readLines(getExistingFile(basePath, file), encoding);
  }

  // -----------------------------------------------------------------------

  /**
   * @see FileUtils
   */
  protected void writeStringToFile(final FileBasePath basePath, final File file, final String data)
      throws Exception {
    writeStringToFile(basePath, file, data, null, false);
  }

  /**
   * @see FileUtils
   */
  protected void writeStringToFile(final FileBasePath basePath, final File file, final String data,
      final String encoding) throws Exception {
    writeStringToFile(basePath, file, data, encoding, false);
  }

  /**
   * @see FileUtils
   */
  protected void writeStringToFile(final FileBasePath basePath, final File file, final String data,
      final boolean append) throws Exception {
    writeStringToFile(basePath, file, data, null, append);
  }

  /**
   * @see FileUtils
   */
  protected void writeStringToFile(final FileBasePath basePath, final File file, final String data,
      final String encoding, final boolean append) throws Exception {
    verify(basePath, file);
    FileUtils.writeStringToFile(getFileForWriting(basePath, file, append), data, encoding, append);
  }

  // -----------------------------------------------------------------------

  /**
   * @see FileUtils
   */
  protected void write(final FileBasePath basePath, final File file, final CharSequence data)
      throws Exception {
    write(basePath, file, data, null, false);
  }

  /**
   * @see FileUtils
   */
  protected void write(final FileBasePath basePath, final File file, final CharSequence data,
      final boolean append) throws Exception {
    write(basePath, file, data, null, append);
  }

  /**
   * @see FileUtils
   */
  protected void write(final FileBasePath basePath, final File file, final CharSequence data,
      final String encoding) throws Exception {
    write(basePath, file, data, encoding, false);
  }

  /**
   * @see FileUtils
   */
  protected void write(final FileBasePath basePath, final File file, final CharSequence data,
      final String encoding, final boolean append) throws Exception {
    verify(basePath, file);
    FileUtils.write(getFileForWriting(basePath, file, append), data, encoding, append);
  }

  // -----------------------------------------------------------------------

  /**
   * @see FileUtils
   */
  protected void writeByteArrayToFile(final FileBasePath basePath, final File file,
      final byte[] data) throws Exception {
    writeByteArrayToFile(basePath, file, data, false);
  }

  /**
   * @see FileUtils
   */
  protected void writeByteArrayToFile(final FileBasePath basePath, final File file,
      final byte[] data, final boolean append) throws Exception {
    verify(basePath, file);
    FileUtils.writeByteArrayToFile(getFileForWriting(basePath, file, append), data, append);
  }

  /**
   * @see FileUtils
   */
  protected void copyInputStreamToFile(final FileBasePath basePath, final File file,
      final InputStream inputStream, final boolean append) throws Exception {
    verify(basePath, file);
    FileUtils.copyInputStreamToFile(inputStream, getFileForWriting(basePath, file, append));
  }

  // -----------------------------------------------------------------------

  /**
   * @see FileUtils
   */
  protected void writeLines(final FileBasePath basePath, final File file, final Collection<?> lines)
      throws Exception {
    writeLines(basePath, file, null, lines, null, false);
  }

  /**
   * @see FileUtils
   */
  protected void writeLines(final FileBasePath basePath, final File file,
      final Collection<?> lines, final boolean append) throws Exception {
    writeLines(basePath, file, null, lines, null, append);
  }

  /**
   * @see FileUtils
   */
  protected void writeLines(final FileBasePath basePath, final File file,
      final Collection<?> lines, final String lineEnding) throws Exception {
    writeLines(basePath, file, null, lines, lineEnding, false);
  }

  /**
   * @see FileUtils
   */
  protected void writeLines(final FileBasePath basePath, final File file,
      final Collection<?> lines, final String lineEnding, final boolean append) throws Exception {
    writeLines(basePath, file, null, lines, lineEnding, append);
  }

  /**
   * @see FileUtils
   */
  protected void writeLines(final FileBasePath basePath, final File file, final String encoding,
      final Collection<?> lines) throws Exception {
    writeLines(basePath, file, encoding, lines, null, false);
  }

  /**
   * @see FileUtils
   */
  protected void writeLines(final FileBasePath basePath, final File file, final String encoding,
      final Collection<?> lines, final boolean append) throws Exception {
    writeLines(basePath, file, encoding, lines, null, append);
  }

  /**
   * @see FileUtils
   */
  protected void writeLines(final FileBasePath basePath, final File file, final String encoding,
      final Collection<?> lines, final String lineEnding) throws Exception {
    writeLines(basePath, file, encoding, lines, lineEnding, false);
  }

  /**
   * @see FileUtils
   */
  protected void writeLines(final FileBasePath basePath, final File file, final String encoding,
      final Collection<?> lines, final String lineEnding, final boolean append) throws Exception {
    verify(basePath, file);
    FileUtils.writeLines(getFileForWriting(basePath, file, append), encoding, lines, lineEnding,
        append);
  }

  // -----------------------------------------------------------------------

  /**
   * The first given file is not handled. It have to exist in an other path than the one that is
   * handled. The second given file is handled. Both files are handled
   * @see FileUtils
   */
  public void moveFile(final File srcFile, final HandledFile destFile) throws Exception {
    FileUtils.moveFile(srcFile, destFile.getFile());
  }

  /**
   * Both of given files are handled
   * @see FileUtils
   */
  protected void moveFile(final FileBasePath basePath, final File srcFile, final File destFile)
      throws Exception {
    moveFile(basePath, srcFile, basePath, destFile);
  }

  /**
   * Both of given files are handled
   * @see FileUtils
   */
  protected void moveFile(final FileBasePath basePath, final File srcFile,
      final FileBasePath baseDestPath, final File destFile) throws Exception {
    verify(basePath, srcFile);
    verify(baseDestPath, destFile);
    FileUtils.moveFile(getFileForWriting(basePath, srcFile, true),
        getFileForWriting(baseDestPath, destFile));
  }

  // -----------------------------------------------------------------------

  /**
   * @see FileUtils
   */
  protected long sizeOf(final FileBasePath basePath, File file) {
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
  protected long sizeOfDirectory(final FileBasePath basePath, final File directory) {
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
  protected boolean isFileNewer(final FileBasePath basePath, final File file, final File reference) {
    verify(basePath, file);
    verify(basePath, reference);
    return FileUtils.isFileNewer(getExistingFile(basePath, file),
        getExistingFile(basePath, reference));
  }

  /**
   * @see FileUtils
   */
  protected boolean isFileNewer(final FileBasePath basePath, final File file, final Date date) {
    verify(basePath, file);
    return FileUtils.isFileNewer(getExistingFile(basePath, file), date);
  }

  /**
   * @see FileUtils
   */
  protected boolean isFileNewer(final FileBasePath basePath, final File file, final long timeMillis) {
    verify(basePath, file);
    return FileUtils.isFileNewer(getExistingFile(basePath, file), timeMillis);
  }

  // -----------------------------------------------------------------------

  /**
   * @see FileUtils
   */
  protected boolean isFileOlder(final FileBasePath basePath, final File file, final File reference) {
    verify(basePath, file);
    verify(basePath, reference);
    return FileUtils.isFileOlder(getExistingFile(basePath, file),
        getExistingFile(basePath, reference));
  }

  /**
   * @see FileUtils
   */
  protected boolean isFileOlder(final FileBasePath basePath, final File file, final Date date) {
    verify(basePath, file);
    return FileUtils.isFileOlder(getExistingFile(basePath, file), date);
  }

  /**
   * @see FileUtils
   */
  protected boolean isFileOlder(final FileBasePath basePath, final File file, final long timeMillis) {
    verify(basePath, file);
    return FileUtils.isFileOlder(getExistingFile(basePath, file), timeMillis);
  }
}
