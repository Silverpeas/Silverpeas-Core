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
package org.silverpeas.servlet;

import com.silverpeas.util.web.servlet.FileUploadUtil;
import org.apache.commons.fileupload.FileItem;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * User: Yohann Chastagnier
 * Date: 06/04/13
 */
public class HttpServletRequestWrapper extends ServletRequestWrapper {

  /**
   * Default constructor.
   * @param request
   */
  public HttpServletRequestWrapper(final HttpServletRequest request, final String language) {
    super(request, language);
  }

  @Override
  protected HttpServletRequest getRequest() {
    return (HttpServletRequest) super.getRequest();
  }

  /**
   * Get file items.
   * {@link org.apache.commons.fileupload.FileUpload#parseRequest(org.apache.commons.fileupload
   * .RequestContext)}
   * @return
   */
  public List<FileItem> getFileItems() {
    return FileUploadUtil.parseRequest(getRequest());
  }
}
