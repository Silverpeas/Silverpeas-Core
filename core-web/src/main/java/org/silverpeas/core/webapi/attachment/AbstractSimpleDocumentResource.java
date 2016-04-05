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
package org.silverpeas.core.webapi.attachment;

import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.webapi.base.RESTWebService;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.apache.commons.io.FileUtils;
import org.silverpeas.core.notification.message.MessageNotifier;
import org.silverpeas.core.util.UnitUtil;
import org.silverpeas.core.util.error.SilverpeasTransverseErrorUtil;

import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.File;
import java.text.MessageFormat;

public class AbstractSimpleDocumentResource extends RESTWebService {

  @PathParam("componentId")
  private String componentId;

  @Override
  public String getComponentId() {
    return componentId;
  }

  @Override
  protected String getBundleLocation() {
    return "org.silverpeas.util.attachment.multilang.attachment";
  }

  /**
   * Check the file.
   * @param fileToCheck
   */
  protected void checkUploadedFile(File fileToCheck) {
    try {

      //check the file size
      long maximumFileSize = FileRepositoryManager.getUploadMaximumFileSize();
      long fileSize = fileToCheck.length();
      if (fileSize > maximumFileSize) {
        String errorMessage = getBundle().getString("attachment.dialog.errorFileSize") + " " +
            getBundle().getString("attachment.dialog.maximumFileSize") + " (" +
            UnitUtil.formatMemSize(maximumFileSize) + ")";
        errorMessage = MessageFormat.format(errorMessage, fileToCheck.getName());
        MessageNotifier.addError(errorMessage);
        throw new WebApplicationException(Response.status(Response.Status.PRECONDITION_FAILED)
            .entity(errorMessage).build());
      }

    } catch (RuntimeException re) {
      FileUtils.deleteQuietly(fileToCheck);
      throw re;
    }
  }

  /**
   * Manages the runtime errors.
   * @param re
   */
  protected void performRuntimeException(RuntimeException re) {
    String transverseExceptionMessage = SilverpeasTransverseErrorUtil
        .performExceptionMessage(re, getUserPreferences().getLanguage());
    if (StringUtil.isDefined(transverseExceptionMessage)) {
      throw new WebApplicationException(
          Response.status(Response.Status.PRECONDITION_FAILED).entity(transverseExceptionMessage)
              .build());
    }
    throw re;
  }
}
