/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.core.io.upload;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class represents a file which will be uploaded, or, has been uploaded, on the server within
 * the context of a files upload session (represented by an {@link UploadSession} instance.
 * @author Yohann Chastagnier
 */
public class UploadSessionFile {

  private final UploadSession uploadSession;
  private final String fullPath;
  private final File serverFile;

  /**
   * Hidden constructor.
   * @param uploadSession the files upload session.
   * @param fullPath the full path relative to the root folder of the given session.
   * @param serverFile the (temporary) physical file on the server filesystem.
   */
  UploadSessionFile(UploadSession uploadSession, String fullPath, File serverFile) {
    this.uploadSession = uploadSession;
    this.fullPath = fullPath;
    this.serverFile = serverFile;
  }

  /**
   * Gets the files upload session for which this file is defined.
   * @return the {@link UploadSession} instance to which this file belongs.
   */
  public UploadSession getUploadSession() {
    return uploadSession;
  }

  /**
   * Gets the full path of this file relative to the root folder of the underlying files upload
   * session.
   * @return the path of the file relative to the upload session's root folder.
   */
  public String getFullPath() {
    return fullPath;
  }

  /**
   * Gets the physical representation on the server filesystem of this file.
   * @return the file on the Silverpeas server.
   */
  public File getServerFile() {
    return serverFile;
  }

  /**
   * Writes the given input stream into the physical file referred by the
   * {@link UploadSessionFile#getServerFile()} method.<br>
   * Closes the input stream at the end.
   * @param uploadedInputStream an input stream on a content to read.
   * @throws IOException if an error occurs while reading the input stream and writing the content
   * onto the physical file.
   */
  public void write(InputStream uploadedInputStream) throws IOException {
    getUploadSession().markFileWritingInProgress(this);
    try {
      try(BufferedInputStream bIs = new BufferedInputStream(uploadedInputStream);
          FileOutputStream fOS = FileUtils.openOutputStream(getServerFile())) {
        IOUtils.copy(bIs, fOS);
      }
    } finally {
      getUploadSession().markFileWritingDone(this);
    }
  }
}
