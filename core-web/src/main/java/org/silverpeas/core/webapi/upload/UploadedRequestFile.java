/*
 * Copyright (C) 2000 - 2014 Silverpeas
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

import org.silverpeas.core.web.http.RequestFile;

import javax.ws.rs.FormParam;

import static org.silverpeas.core.web.util.IFrameAjaxTransportUtil.X_REQUESTED_WITH;

/**
 * @author Yohann Chastagnier
 */
public class UploadedRequestFile {

  /**
   * A parameter indicating from which the upload was performed. It is valued
   * with the identifier of the HTML or javascript component at the origin of the uploading.
   * According to his value, the expected response can be different.
   */
  @FormParam(X_REQUESTED_WITH)
  private String xRequestedWith;

  /**
   * The upload session identifier
   */
  @FormParam(FileUploadData.X_UPLOAD_SESSION)
  private String uploadSessionId;

  /**
   * Detail about the uploaded file like the filename for example.
   * It provides the input stream from which the content of the file can be read.
   */
  @FormParam("file_upload")
  private RequestFile requestFile;

  /**
   * The component instance identifier
   */
  @FormParam(FileUploadData.X_COMPONENT_INSTANCE_ID)
  private String componentInstanceId;

  /**
   * @see #xRequestedWith
   */
  public String getXRequestedWith() {
    return xRequestedWith;
  }

  /**
   * @see #uploadSessionId
   */
  public String getUploadSessionId() {
    return uploadSessionId;
  }

  /**
   * @see #requestFile
   */
  public RequestFile getRequestFile() {
    return requestFile;
  }

  /**
   * @see #componentInstanceId
   */
  public String getComponentInstanceId() {
    return componentInstanceId;
  }
}
