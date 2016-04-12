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
package org.silverpeas.core.webapi.upload;

import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.StringUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.net.URLDecoder;

/**
 * @author Yohann Chastagnier
 */
public class FileUploadData {

  final static String X_COMPONENT_INSTANCE_ID = "X-COMPONENT-INSTANCE-ID";
  final static String X_UPLOAD_SESSION = "X-UPLOAD-SESSION";
  final static String X_FULL_PATH = "X-FULL-PATH";

  private String uploadSessionId;
  private String fullPath;
  private String name;
  private String componentInstanceId;

  /**
   * Hidden constructor.
   * @param uploadSessionId
   * @param fullPath
   * @param componentInstanceId
   */
  private FileUploadData(String uploadSessionId, String fullPath,
      final String componentInstanceId) {
    this.uploadSessionId = uploadSessionId;
    this.fullPath = fullPath;
    this.name = StringUtil.isDefined(fullPath) ? new File(fullPath).getName() : "";
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
   * @throws Exception
   */
  public static FileUploadData from(HttpServletRequest request) throws Exception {
    String brutFullPath = request.getHeader(X_FULL_PATH);
    if (StringUtil.isNotDefined(brutFullPath)) {
      brutFullPath = "";
    }
    String fullPath = URLDecoder.decode(brutFullPath, Charsets.UTF_8.name());
    return new FileUploadData(request.getHeader(X_UPLOAD_SESSION), fullPath,
        request.getHeader(X_COMPONENT_INSTANCE_ID));
  }

  /**
   * Initializes an instance from {@link UploadedRequestFile}. (FormData)
   * @param uploadedRequestFile the upload file that represents the form data.
   * @return a new initialized instance.
   * @throws Exception
   */
  public static FileUploadData from(UploadedRequestFile uploadedRequestFile) {
    return new FileUploadData(uploadedRequestFile.getUploadSessionId(),
        uploadedRequestFile.getRequestFile().getName(),
        uploadedRequestFile.getComponentInstanceId());
  }
}
