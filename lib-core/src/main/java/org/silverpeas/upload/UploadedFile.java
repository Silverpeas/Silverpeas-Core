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
package org.silverpeas.upload;

import com.stratelia.webactiv.util.FileRepositoryManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.PrefixFileFilter;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Collection;

/**
 * Representation of an uploaded file.
 * User: Yohann Chastagnier
 * Date: 18/03/13
 */
public class UploadedFile {
  private String fileUploadId;
  private File file;
  private String title;
  private String description;

  /**
   * Creates a representation of an uploaded file from HttpServletRequest and a given uploaded file
   * identifier.
   * @param request
   * @param uploadedFileId
   * @return
   */
  public static UploadedFile from(HttpServletRequest request, String uploadedFileId) {
    return new UploadedFile(uploadedFileId, getUploadedFileFromUploadId(uploadedFileId),
        (String) request.getParameter(uploadedFileId + "-title"),
        (String) request.getParameter(uploadedFileId + "-description"));
  }

  /**
   * Default constructor.
   * @param fileUploadId
   * @param file
   * @param title
   * @param description
   */
  private UploadedFile(final String fileUploadId, final File file, final String title,
      final String description) {
    this.fileUploadId = fileUploadId;
    this.file = file;
    this.title = title;
    this.description = description;
  }

  /**
   * Gets the identifier of the uploaded file.
   * @return
   */
  public String getFileUploadId() {
    return fileUploadId;
  }

  /**
   * Gets the uploaded file.
   * @return
   */
  public File getFile() {
    return file;
  }

  /**
   * Gets the title filled by the user for the uploaded file.
   * @return
   */
  public String getTitle() {
    return title;
  }

  /**
   * Gets the description filled by the user for the uploaded file.
   * @return
   */
  public String getDescription() {
    return description;
  }

  /**
   * Indicates that the uploaded file has been processed.
   * Uploaded physical file is deleted from its temporary upload repository.
   */
  public void markAsProcessed() {
    FileUtils.deleteQuietly(file);
  }

  /**
   * Gets an uploaded file from a given uploaded file identifier.
   * @param uploadedFileId
   * @return
   */
  private static File getUploadedFileFromUploadId(String uploadedFileId) {
    File tempDir = new File(FileRepositoryManager.getTemporaryPath());
    Collection<File> files =
        FileUtils.listFiles(tempDir, new PrefixFileFilter(uploadedFileId), FalseFileFilter.FALSE);
    if (files.isEmpty() || files.size() > 1) {
      return new File(tempDir, "unexistingFile");
    }
    return files.iterator().next();
  }
}
