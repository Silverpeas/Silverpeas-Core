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
package org.silverpeas.core.process.io.file;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

/**
 * This class permits to manipulate files (read/write/delete/...) into transactional processes. It
 * encapsulates a <code>FileHandler</code> instance and knows the root path repository into that it
 * have to work. <code>FileHandler.getHandledFile</code> method has to be called to obtain a
 * <code>HandledFile</code> instance. With <code>HandledFile</code> instance, file manipulations are
 * easier and lighter due to the hiding of internal mechanism.
 * @author Yohann Chastagnier
 */
public class HandledFile {

  private final FileBasePath basePath;
  protected final FileHandler fileHandler;
  protected final File file;

  /**
   * Default constructor
   * @param basePath
   * @param fileHandler
   * @param file
   */
  protected HandledFile(final FileBasePath basePath, final FileHandler fileHandler, final File file) {
    this.basePath = basePath;
    this.fileHandler = fileHandler;
    this.file = file;
  }

  /**
   * @return the basePath
   */
  public FileBasePath getBasePath() {
    return basePath;
  }

  /**
   * @return the fileHandler
   */
  public FileHandler getFileHandler() {
    return fileHandler;
  }

  /**
   * Gets the real path of the file, even if the file exists only in the session path
   */
  public String getRealPath() {
    return fileHandler.translateToRealPath(basePath, file).getPath();
  }

  /**
   * @return the file
   */
  public File getFile() {
    return fileHandler.getExistingFile(basePath, file);
  }

  /**
   * Indicates if the file exists in session or in real path.
   * @see FileUtils
   */
  public boolean exists(final String... names) {
    return fileHandler.exists(basePath, file, names);
  }

  // -----------------------------------------------------------------------

  /**
   * Gets a sub file from the current HandledFile. If no parameter is given, then the current
   * HandledFile is returned.
   * @see FileUtils
   */
  public HandledFile getHandledFile(final String... names) {
    if (names == null) {
      return this;
    }
    return fileHandler.getHandledFile(basePath, file, names);
  }

  /**
   * Gets a sub file from the parent of the current HandledFile. If no parameter is given, then the
   * parent of current HandledFile is returned.
   * @see FileUtils
   */
  public HandledFile getParentHandledFile(final String... names) {
    return fileHandler.getHandledFile(basePath, file.getParentFile(), names);
  }

  // -----------------------------------------------------------------------

  /**
   * @see fileHandler
   */
  public OutputStream openOutputStream() throws Exception {
    return openOutputStream(false);
  }

  /**
   * @see fileHandler
   */
  public OutputStream openOutputStream(final boolean append) throws Exception {
    return fileHandler.openOutputStream(basePath, file, append);
  }

  /**
   * @see fileHandler
   */
  public InputStream openInputStream() throws Exception {
    return fileHandler.openInputStream(basePath, file);
  }

  /**
   * @see fileHandler
   */
  public void touch(final String... names) throws Exception {
    fileHandler.touch(basePath, file, names);
  }

  // -----------------------------------------------------------------------

  /**
   * @see fileHandler
   */
  public Collection<HandledFile> listFiles(final String... extensions) {
    return listFiles(true, extensions);
  }

  /**
   * @see fileHandler
   */
  public Collection<HandledFile> listFiles(final boolean recursive, final String... extensions) {
    return toIOFiles(fileHandler.listFiles(basePath, file, recursive, extensions));
  }

  /**
   * @see fileHandler
   */
  public Collection<HandledFile> listFiles(final IOFileFilter fileFilter,
      final IOFileFilter dirFilter) {
    return toIOFiles(fileHandler.listFiles(basePath, file, fileFilter, dirFilter));
  }

  /**
   * Centralized method to transform collections of File into collections of IOFile
   * @param files
   * @return
   */
  private Collection<HandledFile> toIOFiles(final Collection<File> files) {
    final Collection<HandledFile> result = new LinkedList<>();
    if (files != null) {
      for (final File file : files) {
        result.add(new HandledFile(basePath, fileHandler, file));
      }
    }
    return result;
  }

  // -----------------------------------------------------------------------

  /**
   * The given file is handled
   * @see fileHandler
   */
  public boolean contentEquals(final HandledFile otherFile) throws Exception {
    return fileHandler.contentEquals(basePath, file, otherFile.file);
  }

  // -----------------------------------------------------------------------

  /**
   * The given file is handled
   * @see fileHandler
   */
  public void copyFile(final HandledFile destFile) throws Exception {
    fileHandler.copyFile(basePath, file, destFile.basePath, destFile.file);
  }

  /**
   * The given file is handled
   * @see fileHandler
   */
  public long copyFile(final OutputStream output) throws Exception {
    return fileHandler.copyFile(basePath, file, output);
  }

  /**
   * The given file is handled
   * @see fileHandler
   */
  public void copyURLToFile(final URL source) throws Exception {
    fileHandler.copyURLToFile(source, basePath, file);
  }

  // -----------------------------------------------------------------------

  /**
   * @see fileHandler
   */
  public boolean delete() throws Exception {
    return fileHandler.delete(basePath, file);
  }

  /**
   * @see fileHandler
   */
  public void cleanDirectory() throws Exception {
    fileHandler.cleanDirectory(basePath, file);
  }

  // -----------------------------------------------------------------------

  /**
   * @see fileHandler
   */
  public boolean waitFor(final int seconds) {
    return fileHandler.waitFor(basePath, file, seconds);
  }

  // -----------------------------------------------------------------------

  /**
   * @see fileHandler
   */
  public String readFileToString() throws Exception {
    return readFileToString(null);
  }

  /**
   * @see fileHandler
   */
  public String readFileToString(final String encoding) throws Exception {
    return fileHandler.readFileToString(basePath, file, encoding);
  }

  /**
   * @see fileHandler
   */
  public byte[] readFileToByteArray() throws Exception {
    return fileHandler.readFileToByteArray(basePath, file);
  }

  /**
   * @see fileHandler
   */
  public List<String> readLines() throws Exception {
    return readLines(null);
  }

  /**
   * @see fileHandler
   */
  public List<String> readLines(final String encoding) throws Exception {
    return fileHandler.readLines(basePath, file, encoding);
  }

  // -----------------------------------------------------------------------

  /**
   * @see fileHandler
   */
  public void writeStringToFile(final String data) throws Exception {
    writeStringToFile(data, null, false);
  }

  /**
   * @see fileHandler
   */
  public void writeStringToFile(final String data, final String encoding) throws Exception {
    writeStringToFile(data, encoding, false);
  }

  /**
   * @see fileHandler
   */
  public void writeStringToFile(final String data, final boolean append) throws Exception {
    writeStringToFile(data, null, append);
  }

  /**
   * @see fileHandler
   */
  public void writeStringToFile(final String data, final String encoding, final boolean append)
      throws Exception {
    fileHandler.writeStringToFile(basePath, file, data, encoding, append);
  }

  // -----------------------------------------------------------------------

  /**
   * @see fileHandler
   */
  public void write(final CharSequence data) throws Exception {
    write(data, null, false);
  }

  /**
   * @see fileHandler
   */
  public void write(final CharSequence data, final boolean append) throws Exception {
    write(data, null, append);
  }

  /**
   * @see fileHandler
   */
  public void write(final CharSequence data, final String encoding) throws Exception {
    write(data, encoding, false);
  }

  /**
   * @see fileHandler
   */
  public void write(final CharSequence data, final String encoding, final boolean append)
      throws Exception {
    fileHandler.write(basePath, file, data, encoding, append);
  }

  // -----------------------------------------------------------------------

  /**
   * @see fileHandler
   */
  public void writeByteArrayToFile(final byte[] data) throws Exception {
    writeByteArrayToFile(data, false);
  }

  /**
   * @see fileHandler
   */
  public void writeByteArrayToFile(final byte[] data, final boolean append) throws Exception {
    fileHandler.writeByteArrayToFile(basePath, file, data, append);
  }

  /**
   * Use this method prior to writeByteArrayToFile in order to process very large file
   * @param inputStream the input stream to write
   * @throws Exception
   */
  public void copyInputStreamToFile(final InputStream inputStream) throws Exception {
    fileHandler.copyInputStreamToFile(basePath, file, inputStream, false);
  }

  // -----------------------------------------------------------------------

  /**
   * @see fileHandler
   */
  public void writeLines(final Collection<?> lines) throws Exception {
    writeLines(null, lines, null, false);
  }

  /**
   * @see fileHandler
   */
  public void writeLines(final Collection<?> lines, final boolean append) throws Exception {
    writeLines(null, lines, null, append);
  }

  /**
   * @see fileHandler
   */
  public void writeLines(final Collection<?> lines, final String lineEnding) throws Exception {
    writeLines(null, lines, lineEnding, false);
  }

  /**
   * @see fileHandler
   */
  public void writeLines(final Collection<?> lines, final String lineEnding, final boolean append)
      throws Exception {
    writeLines(null, lines, lineEnding, append);
  }

  /**
   * @see fileHandler
   */
  public void writeLines(final String encoding, final Collection<?> lines) throws Exception {
    writeLines(encoding, lines, null, false);
  }

  /**
   * @see fileHandler
   */
  public void writeLines(final String encoding, final Collection<?> lines, final boolean append)
      throws Exception {
    writeLines(encoding, lines, null, append);
  }

  /**
   * @see fileHandler
   */
  public void writeLines(final String encoding, final Collection<?> lines, final String lineEnding)
      throws Exception {
    writeLines(encoding, lines, lineEnding, false);
  }

  /**
   * @see fileHandler
   */
  public void writeLines(final String encoding, final Collection<?> lines, final String lineEnding,
      final boolean append) throws Exception {
    fileHandler.writeLines(basePath, file, encoding, lines, lineEnding, append);
  }

  // -----------------------------------------------------------------------

  /**
   * Given file is handled
   * @see fileHandler
   */
  public void moveFile(final HandledFile destFile) throws Exception {
    fileHandler.moveFile(basePath, file, destFile.basePath, destFile.file);
  }

  // -----------------------------------------------------------------------

  /**
   * @see fileHandler
   */
  public long size() {
    return fileHandler.sizeOf(basePath, file);
  }

  // -----------------------------------------------------------------------

  /**
   * @see fileHandler
   */
  public boolean isFileNewer(final HandledFile reference) {
    return isFileNewer(reference.file);
  }

  /**
   * @see fileHandler
   */
  public boolean isFileNewer(final File reference) {
    return fileHandler.isFileNewer(basePath, file, reference);
  }

  /**
   * @see fileHandler
   */
  public boolean isFileNewer(final Date date) {
    return fileHandler.isFileNewer(basePath, file, date);
  }

  /**
   * @see fileHandler
   */
  public boolean isFileNewer(final long timeMillis) {
    return fileHandler.isFileNewer(basePath, file, timeMillis);
  }

  // -----------------------------------------------------------------------
  /**
   * @see fileHandler
   */
  public boolean isFileOlder(final HandledFile reference) {
    return isFileOlder(reference.file);
  }

  /**
   * @see fileHandler
   */
  public boolean isFileOlder(final File reference) {
    return fileHandler.isFileOlder(basePath, file, reference);
  }

  /**
   * @see fileHandler
   */
  public boolean isFileOlder(final Date date) {
    return fileHandler.isFileOlder(basePath, file, date);
  }

  /**
   * @see fileHandler
   */
  public boolean isFileOlder(final long timeMillis) {
    return fileHandler.isFileOlder(basePath, file, timeMillis);
  }
}
