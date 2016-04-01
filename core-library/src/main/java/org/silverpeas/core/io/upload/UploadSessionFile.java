/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.core.io.upload;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class represents a file which will be uploaded, or, has been uploaded, on the server.
 * @author Yohann Chastagnier
 */
public class UploadSessionFile {

  private UploadSession uploadSession;
  private String fullPath;
  private File serverFile;

  /**
   * Hidden constructor.
   * @param uploadSession the upload session.
   * @param fullPath the full path into the session.
   * @param serverFile the (temporary) file on the server filesystem.
   */
  UploadSessionFile(UploadSession uploadSession, String fullPath, File serverFile) {
    this.uploadSession = uploadSession;
    this.fullPath = fullPath;
    this.serverFile = serverFile;
  }

  public UploadSession getUploadSession() {
    return uploadSession;
  }

  public String getFullPath() {
    return fullPath;
  }

  /**
   * Loads the data to access the file on server if it has not been done, noting is performed
   * otherwise.
   * @return the file on the server.
   * @throws Exception
   */
  public File getServerFile() {
    return serverFile;
  }

  /**
   * Writes the given input stream into the physical file.<br/>
   * Closes the input stream at the end.
   * @param uploadedInputStream
   * @throws IOException
   */
  public void write(InputStream uploadedInputStream) throws IOException {
    getUploadSession().markFileWritingInProgress(this);
    try {
      FileOutputStream fOS = FileUtils.openOutputStream(getServerFile());
      try {
        IOUtils.copy(uploadedInputStream, fOS);
      } finally {
        IOUtils.closeQuietly(fOS);
      }
    } finally {
      IOUtils.closeQuietly(uploadedInputStream);
      getUploadSession().markFileWritingDone(this);
    }
  }
}
