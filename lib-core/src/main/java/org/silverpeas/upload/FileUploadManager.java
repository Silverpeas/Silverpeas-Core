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

import com.stratelia.silverpeas.silvertrace.SilverTrace;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * This manager permits to retrieve from {@link HttpServletRequest} a collection of
 * {@link UploadedFile}.
 * User: Yohann Chastagnier
 * Date: 18/03/13
 */
public class FileUploadManager {
  private static final String UPLOADED_FILE_PREFIX_ID = "uploaded-file";

  /**
   * Retrieves from {@link HttpServletRequest} a collection of {@link UploadedFile}
   * @param request
   * @return
   */
  @SuppressWarnings("unchecked")
  public static Collection<UploadedFile> getUploadedFiles(HttpServletRequest request) {
    Collection<UploadedFile> uploadedFiles = new ArrayList<UploadedFile>();
    if (request != null) {
      Enumeration<String> attributeNames = request.getParameterNames();
      String attributeName;
      while (attributeNames.hasMoreElements()) {
        attributeName = attributeNames.nextElement();
        if (attributeName.startsWith(UPLOADED_FILE_PREFIX_ID)) {

          // If an attribute name starts with {@link UPLOADED_FILE_PREFIX_ID} an {@link
          // UploadedFile} is performed.
          uploadedFiles
              .add(UploadedFile.from(request, (String) request.getParameter(attributeName)));
        }
      }

      // Security : unexisting uploaded files are removed from the result.
      uploadedFiles.remove(null);
      Iterator<UploadedFile> it = uploadedFiles.iterator();
      while (it.hasNext()) {
        UploadedFile uploadedFile = it.next();
        if (!uploadedFile.getFile().exists()) {
          it.remove();
          SilverTrace.warn("upload", "FileUploadManager.getUploadedFiles", "EX_FILE_DOES_NOT_EXIST",
              "FileUploadId: " + uploadedFile.getFileUploadId() + " - FileName: " +
                  uploadedFile.getFile().getName());
        }
      }
    }
    return uploadedFiles;
  }
}
