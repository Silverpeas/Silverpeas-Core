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
package org.silverpeas.web.attachment;

import org.silverpeas.core.notification.user.UserSubscriptionNotificationSendingHandler;
import org.silverpeas.core.web.mvc.webcomponent.SilverpeasAuthenticatedHttpServlet;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang3.CharEncoding;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.io.upload.UploadSession;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.error.SilverpeasTransverseErrorUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import static org.silverpeas.core.importexport.control.RepositoriesTypeManager.handleFileToAttach;
import static org.silverpeas.core.util.StringUtil.getBooleanValue;
import static org.silverpeas.core.i18n.I18NHelper.checkLanguage;

/**
 * Servlet used whith the drag and drop applet to import non-versioned documents.
 */
public class DragAndDrop extends SilverpeasAuthenticatedHttpServlet {
  private static final long serialVersionUID = 4084217276750892258L;

  /**
   * Method declaration
   * @param req
   * @param res
   * @throws IOException
   * @throws ServletException
   * @see
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    doPost(req, res);
  }

  /**
   * Method declaration
   * @param req
   * @param res
   * @throws IOException
   * @throws ServletException
   * @see
   */
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {

    final String versionType = req.getParameter("Type");


    final HttpRequest request = HttpRequest.decorate(req);
    request.setCharacterEncoding(CharEncoding.UTF_8);

    final UserDetail currentUser = UserDetail.getCurrentRequester();
    final String userLanguage = currentUser.getUserPreferences().getLanguage();
    final UploadSession uploadSession = UploadSession.from(request);

    try {
      final String componentId = request.getParameter("ComponentId");

      if (!uploadSession.isUserAuthorized(componentId)) {
        throwHttpForbiddenError();
      }

      UserSubscriptionNotificationSendingHandler.verifyRequest(req);

      final String resourceId = request.getParameter("ResourceId");
      final String contentLanguage = checkLanguage(request.getParameter("ContentLanguage"));
      final DocumentType documentType = determineDocumentType(request);
      final boolean hasToBeIndexed = getBooleanValue(request.getParameter("IndexIt"));
      final boolean versionControlActivated = !getBooleanValue(
          uploadSession.getComponentInstanceParameterValue("publicationAlwaysVisible")) &&
          getBooleanValue(uploadSession.getComponentInstanceParameterValue("versionControl"));
      final boolean publicVersion =
          StringUtil.isDefined(versionType) && !getBooleanValue(versionType);

      final File rootUploadFolder = uploadSession.getRootFolder();
      final Date creationDate = new Date();

      for (final File file : FileUtils.listFiles(rootUploadFolder, FileFilterUtils.fileFileFilter(),
          FileFilterUtils.trueFileFilter())) {

        handleFileToAttach(currentUser, componentId, resourceId, request.getParameter("DocumentId"),
            documentType, file, contentLanguage, creationDate, hasToBeIndexed,
            versionControlActivated, publicVersion);

      }
    } catch (Exception ex) {
      SilverTrace.error(StringUtil.isNotDefined(versionType) ? "attachment" : "versioningPeas",
          "DragAndDrop.doPost", "ERREUR", ex);
      SilverpeasTransverseErrorUtil.throwTransverseErrorIfAny(ex, userLanguage);
      throw new ServletException(ex);
    } finally {
      uploadSession.clear();
    }
  }

  /**
   * Determines from the request parameters which type of attachment it must be performed.
   */
  private DocumentType determineDocumentType(HttpServletRequest req) {
    String documentType = req.getParameter("DocumentType");
    DocumentType type = DocumentType.attachment;
    if (StringUtil.isDefined(documentType)) {
      try {
        type = DocumentType.valueOf(documentType);
      } catch (IllegalArgumentException ex) {
        //wrong parameter value, we keep with the default context.
      }
    }
    return type;
  }
}
