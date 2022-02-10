/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This manager allows to retrieve from a {@link HttpServletRequest} or a dictionary of parameters
 * a collection of {@link UploadedFile}.
 * <p>
 * This class must be used when <code>silverpeas-fileUpload</code> Silverpeas Javascript Plugin is
 * used at the client-side.
 * </p>
 * @author Yohann Chastagnier
 */
public class FileUploadManager {

  private static final String UPLOADED_FILE_PREFIX_ID = "uploaded-file";

  private FileUploadManager() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Retrieves from the given {@link HttpServletRequest} instance a collection of
   * {@link UploadedFile} objects.
   * @param request an incoming HTTP request.
   * @param uploader the user behind the upload of files.
   */
  public static List<UploadedFile> getUploadedFiles(HttpServletRequest request,
      final User uploader) {
    Map<String, String[]> parameters = null;
    if (request != null) {
      parameters = request.getParameterMap();
    }
    return getUploadedFiles(parameters, uploader);
  }

  /**
   * Retrieves from the given dictionary of uploaded files parameters a collection of
   * {@link UploadedFile} objects.
   * @param parameters a dictionary of files parameters (title, description, ...)
   * @param uploader the user behind the upload of files.
   */
  public static List<UploadedFile> getUploadedFiles(Map<String, String[]> parameters,
      final User uploader) {
    List<UploadedFile> uploadedFiles = new ArrayList<>();
    if (parameters != null) {
      parameters.forEach((name, value) -> {
        if (name.startsWith(UPLOADED_FILE_PREFIX_ID)) {
          // If an attribute name starts with {@link UPLOADED_FILE_PREFIX_ID} an {@link
          // UploadedFile} is performed.
          uploadedFiles.add(UploadedFile.from(parameters, value[0], uploader));
        }
      });

      // Security : non-existing uploaded files are removed from the result.
      uploadedFiles.remove(null);
      Iterator<UploadedFile> it = uploadedFiles.iterator();
      while (it.hasNext()) {
        UploadedFile uploadedFile = it.next();
        if (!uploadedFile.getFile().exists()) {
          it.remove();
          SilverLogger.getLogger(FileUploadManager.class)
              .warn("file does not exist (UploadSessionId={0}, FileName={1})",
                  uploadedFile.getUploadSession().getId(), uploadedFile.getFile().getName());
        }
      }
    }
    return uploadedFiles;
  }
}
