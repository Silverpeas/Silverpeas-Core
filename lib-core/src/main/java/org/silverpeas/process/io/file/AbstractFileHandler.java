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
package org.silverpeas.process.io.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.silverpeas.process.io.IOAccess;
import org.silverpeas.process.io.file.exception.FileHandlerException;
import org.silverpeas.process.management.ProcessManagement;
import org.silverpeas.process.session.ProcessSession;

import com.stratelia.webactiv.util.FileRepositoryManager;

/**
 * Bases of file handler functionnalities whose a lot of these are protected and only usable by
 * extended classes and by <code>ProcessManagement</code> services (@see {@link ProcessManagement}).
 * This class contains the transactionnal mechanism of file system manipulations.
 * @author Yohann Chastagnier
 */
public abstract class AbstractFileHandler {

  private static final Set<FileBasePath> handledBasePath = new HashSet<FileBasePath>();
  static {
    handledBasePath.add(FileBasePath.UPLOAD_PATH);
  }

  private final File sessionRootPath = new File(FileRepositoryManager.getTemporaryPath());
  private final ProcessSession session;
  private final Map<FileBasePath, Set<File>> toDelete = new HashMap<FileBasePath, Set<File>>();
  private IOAccess ioAccess = IOAccess.READ_ONLY;

  /**
   * Default constructor
   * @param session
   */
  protected AbstractFileHandler(final ProcessSession session) {
    this.session = session;
  }

  /**
   * @return the session
   */
  private ProcessSession getSession() {
    return session;
  }

  /**
   * Mark the given file to be deleted.
   * @param basePath
   * @param file
   * @return true if the file exists and when it is the first time that it is registred to be
   * deleted
   */
  protected boolean markToDelete(final FileBasePath basePath, File file) {
    if (isHandledPath(basePath)) {
      file = translateToRealPath(basePath, file);
      if (file.exists()) {
        if (getIoAccess().equals(IOAccess.READ_ONLY)) {
          setIoAccess(IOAccess.DELETE_ONLY);
        }
        return getMarkedToDelete(basePath).add(file);
      }
    }
    return false;
  }

  /**
   * Indicates if the file will be deleted
   * @param basePath
   * @param file
   * @return
   */
  protected boolean isMarkedToDelete(final FileBasePath basePath, File file) {
    if (isHandledPath(basePath)) {
      file = translateToRealPath(basePath, file);
      for (final File fileToDelete : getMarkedToDelete(basePath)) {
        if (file.getPath().startsWith(fileToDelete.getPath())) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Gets the container that holds references on files to delete
   * @param basePath
   * @return
   */
  Set<File> getMarkedToDelete(final FileBasePath basePath) {
    Set<File> toDeleteContainer = toDelete.get(basePath);
    if (toDeleteContainer == null) {
      toDeleteContainer = new HashSet<File>();
      toDelete.put(basePath, toDeleteContainer);
    }
    return toDeleteContainer;
  }

  /**
   * Translate to real.
   * Attention please : verify has to be called before.
   * @param basePath
   * @param file
   * @return
   */
  protected File translateToRealPath(final FileBasePath basePath, File file) {
    if (isHandledPath(basePath)) {
      final File sessionPath = getSessionPath(basePath);
      if (file.getPath().startsWith(sessionPath.getPath())) {
        final String endOfFileName = file.getPath().substring(sessionPath.getPath().length());
        file = new File(basePath.getPath() + endOfFileName);
      }
    }
    return file;
  }

  /**
   * Translate to session path.
   * Attention please : verify has to be called before.
   * @param basePath
   * @param file
   * @return
   */
  protected File translateToSessionPath(final FileBasePath basePath, File file) {
    if (isHandledPath(basePath) && file.getPath().startsWith(basePath.getPath())) {
      final String endOfFileName = file.getPath().substring(basePath.getPath().length());
      file = new File(getSessionPath(basePath).getPath() + endOfFileName);
    }
    return file;
  }

  /**
   * If the given file doesn't exist in session path, then the file existing in real path is
   * returned.
   * If the given file doesn't exist in real path, then the new session file is returned.
   * @param basePath
   * @param file
   * @return
   */
  protected File getExistingFile(final FileBasePath basePath, final File file) {
    final File sessionFile = translateToSessionPath(basePath, file);
    final File realFile = translateToRealPath(basePath, file);
    return (!sessionFile.exists() && realFile.exists() && !isMarkedToDelete(basePath, realFile))
        ? realFile : sessionFile;
  }

  /**
   * If the given file doesn't exist in session path, then the file existing in real path is
   * returned.
   * If the given file doesn't exist in real path, then the new session file is returned.
   * @param basePath
   * @param file
   * @return
   */
  protected File getFileForWriting(final FileBasePath basePath, final File file) throws Exception {
    return getFileForWriting(basePath, file, false);
  }

  /**
   * If the given file doesn't exist in session path, then the file existing in real path is
   * returned.
   * If the given file doesn't exist in real path, then the new session file is returned.
   * @param basePath
   * @param file
   * @param append
   * @return
   */
  protected File getFileForWriting(final FileBasePath basePath, final File file,
      final boolean append) throws Exception {
    final File sessionFile = translateToSessionPath(basePath, file);
    if (append && !isMarkedToDelete(basePath, file)) {
      final File realFile = translateToRealPath(basePath, file);
      if (realFile.exists()) {
        FileUtils.copyFile(realFile, sessionFile);
      }
    }
    markToDelete(basePath, file);
    setIoAccess(IOAccess.READ_WRITE);
    return sessionFile;
  }

  /**
   * Build session path
   * @param basePath
   * @return
   */
  File getSessionPath(final FileBasePath basePath) {
    if (!isHandledPath(basePath)) {
      throw new FileHandlerException("EX_GETTING_SESSION_PATH_IS_NOT_POSSIBLE");
    }
    return FileUtils.getFile(sessionRootPath, getSession().getId(), basePath.getHandledNodeName());
  }

  /**
   * Build session temporary path
   * @param basePath
   * @return
   */
  File getSessionTemporaryPath() {
    return FileUtils.getFile(sessionRootPath, getSession().getId(), "@#@work@#@");
  }

  /**
   * This method calculates the size of files contained in the given relative root path from the
   * session and subtracts from the previous result the size of files marked to be deleted.
   */
  public long sizeOfSessionWorkingPath(final String... relativeRootPath) {
    long size = 0;
    for (final FileBasePath basePath : handledBasePath) {
      size += sizeOfSessionWorkingPath(basePath, relativeRootPath);
    }
    return size;
  }

  /**
   * This method calculates the size of files contained in the given relative root path from the
   * session and subtracts from the previous result the size of files marked to be deleted.
   */
  protected long sizeOfSessionWorkingPath(final FileBasePath basePath,
      final String... relativeRootPath) {

    // Computing root path from which the size has to be calculated
    final List<String> rootPathParts = new ArrayList<String>();
    rootPathParts.add(getSession().getId());
    rootPathParts.add(basePath.getHandledNodeName());
    if (relativeRootPath != null) {
      rootPathParts.addAll(Arrays.asList(relativeRootPath));
    }

    // Root path (or file ?)
    final File rootPath =
        FileUtils.getFile(sessionRootPath, rootPathParts.toArray(new String[] {}));

    // Size of not deleted files
    long size = (rootPath.exists()) ? FileUtils.sizeOf(rootPath) : 0;

    // Remove the size of deleted files from previous result
    final String realRootPath = translateToRealPath(basePath, rootPath).getPath();
    for (final Map.Entry<FileBasePath, Set<File>> entry : toDelete.entrySet()) {
      for (final File fileToDelete : entry.getValue()) {
        if (fileToDelete.getPath().startsWith(realRootPath) && fileToDelete.exists()) {
          size -= FileUtils.sizeOf(fileToDelete);
        }
      }
    }
    return size;
  }

  /**
   * Delete session path
   * @param basePath
   * @return
   */
  protected void deleteSessionWorkingPath() {
    FileUtils.deleteQuietly(FileUtils.getFile(sessionRootPath, getSession().getId()));
    toDelete.clear();
  }

  /**
   * Checkin session path
   * @return
   */
  protected void checkinSessionWorkingPath() throws Exception {

    // Deleting files
    for (final Map.Entry<FileBasePath, Set<File>> entry : toDelete.entrySet()) {
      for (final File fileToDelete : entry.getValue()) {
        if (fileToDelete.exists()) {
          FileUtils.deleteQuietly(fileToDelete);
        }
      }
    }

    // Cleaning file to delete container
    toDelete.clear();

    // Copying files from the working session path
    for (final FileBasePath basePath : handledBasePath) {
      copyFiles(basePath, getSessionPath(basePath));
    }

    // Cleaning
    final File sessionPath = FileUtils.getFile(sessionRootPath, getSession().getId());
    if (sessionPath.exists()) {
      for (final File file : sessionPath.listFiles()) {
        FileUtils.deleteQuietly(file);
      }
    }
  }

  /**
   * Recursive file copying
   * @param basePath
   * @param file
   * @throws IOException
   */
  private void copyFiles(final FileBasePath basePath, final File file) throws IOException {
    if (file.exists()) {
      final LinkedList<File> fifo = new LinkedList<File>();
      fifo.add(file);
      File currentFile;
      while ((currentFile = fifo.poll()) != null) {
        if (currentFile.isDirectory()) {
          for (final File fileUnderDirectoty : currentFile.listFiles()) {
            fifo.add(fileUnderDirectoty);
          }
        } else {
          FileUtils.copyFile(currentFile, translateToRealPath(basePath, currentFile));
        }
      }
    }
  }

  /**
   * Verify the integrity between handled path and file
   * @param basePath
   * @param file
   */
  protected void verify(final FileBasePath basePath, final File file) {
    verify(basePath, file, false);
  }

  /**
   * Verify the integrity between handled path and file
   * @param basePath
   * @param file
   * @param isReadOnly
   */
  protected void verify(final FileBasePath basePath, final File file, final boolean isReadOnly) {
    if (basePath == null || (!isReadOnly && isHandledPath(basePath))) {
      if (basePath != null &&
          (file.getPath().startsWith(getSessionPath(basePath).getPath()) || file.getPath()
              .startsWith(basePath.getPath()))) {
        return;
      }
      throw new FileHandlerException("EX_VERIFY_ERROR");
    }
  }

  /**
   * Verify if the given file exists
   * @param file
   * @return
   */
  protected static boolean exists(final File file) {
    return file.exists();
  }

  /**
   * Indicates if the given path is handled or not
   * @param basePath
   * @return
   */
  protected boolean isHandledPath(final FileBasePath basePath) {
    return handledBasePath.contains(basePath);
  }

  /**
   * @return the ioAccess
   */
  public IOAccess getIoAccess() {
    return ioAccess;
  }

  /**
   * @param ioAccess the ioAccess to set
   */
  private void setIoAccess(final IOAccess ioAccess) {
    this.ioAccess = ioAccess;
  }
}
