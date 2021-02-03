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
package org.silverpeas.core.webapi.upload;

import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.LocalDateTime;

import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * @author Yohann Chastagnier
 */
public class FileUploadData {

  static final String X_COMPONENT_INSTANCE_ID = "X-COMPONENT-INSTANCE-ID";
  static final String X_UPLOAD_SESSION = "X-UPLOAD-SESSION";
  static final String X_FULL_PATH = "X-FULL-PATH";

  private String uploadSessionId;
  private String fullPath;
  private String name;
  private String componentInstanceId;

  /**
   * Hidden constructor.
   */
  private FileUploadData(String uploadSessionId, String fullPath,
      final String componentInstanceId) {
    if (isDefined(fullPath) && fullPath.contains("..")) {
      SilverLogger.getLogger("silverpeas.core.security")
          .error("Path Traversal attack detected at {0}", LocalDateTime.now().toString());
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    this.uploadSessionId = uploadSessionId;
    this.fullPath = fullPath;
    this.name = isDefined(fullPath) ? new File(fullPath).getName() : "";
    this.componentInstanceId = componentInstanceId;
  }

  public String getUploadSessionId() {
    return uploadSessionId;
  }

  public String getFullPath() {
    return fullPath;
  }

  public String getName() {
    return name;
  }

  public String getComponentInstanceId() {
    return componentInstanceId;
  }

  /**
   * Initializes an instance from the request directly. (OCTET STREAM UPLOAD)
   * @param request the current http request.
   * @return a new initialized instance.
   * @throws UnsupportedEncodingException on encoding error.
   */
  public static FileUploadData from(HttpServletRequest request)
      throws UnsupportedEncodingException {
    String brutFullPath = request.getHeader(X_FULL_PATH);
    if (StringUtil.isNotDefined(brutFullPath)) {
      brutFullPath = "";
    }
    String fullPath = URLDecoder.decode(brutFullPath, Charsets.UTF_8.name());
    fullPath = StringUtil.normalize(fullPath);
    return new FileUploadData(request.getHeader(X_UPLOAD_SESSION), fullPath,
        request.getHeader(X_COMPONENT_INSTANCE_ID));
  }

  /**
   * Initializes an instance from {@link UploadedRequestFile}. (FormData)
   * @param uploadedRequestFile the upload file that represents the form data.
   * @return a new initialized instance.
   */
  public static FileUploadData from(UploadedRequestFile uploadedRequestFile) {
    return new FileUploadData(uploadedRequestFile.getUploadSessionId(),
        uploadedRequestFile.getRequestFile().getName(),
        uploadedRequestFile.getComponentInstanceId());
  }
}
