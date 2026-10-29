/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.servlets;

import org.silverpeas.core.contribution.attachment.AttachmentService;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.io.file.SilverpeasFile;
import org.silverpeas.core.io.file.SilverpeasFileDescriptor;
import org.silverpeas.core.io.file.SilverpeasFileProvider;
import org.silverpeas.core.security.authorization.*;
import org.silverpeas.core.web.mvc.AbstractFileSender;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.kernel.util.StringUtil;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static org.silverpeas.core.admin.space.SpaceInst.SPACE_KEY_PREFIX;

/**
 * Servlet sending the files stored either in the upload directory of a component instance or of a
 * space (look), or as an attachment of a contribution. Only the users authenticated and authorized
 * to access the resource the file belongs to are allowed to get it.
 */
public class OnlineFileServer extends AbstractFileSender {

  private static final long serialVersionUID = -6153872618631360113L;
  private static final String SPACE_RESOURCE_PREFIX = "Space";

  @Inject
  private AttachmentService attachmentService;
  @Inject
  private ComponentAccessControl componentAccessControl;
  @Inject
  private SpaceAccessControl spaceAccessControl;
  @Inject
  private SimpleDocumentAccessControl documentAccessControl;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) {
    doPost(req, res);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse response) {
    final MainSessionController mainSessionCtrl = getMainSessionController(req);
    if (mainSessionCtrl == null) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }
    final String userId = mainSessionCtrl.getUserId();

    String mimeType = req.getParameter("MimeType");
    String sourceFile = req.getParameter("SourceFile");
    String directory = req.getParameter("Directory");
    String componentId = req.getParameter("ComponentId");
    String language = req.getParameter("lang");
    SilverpeasFileDescriptor ref;
    boolean isAllowed;

    SimpleDocument document = getWantedDocument(req, componentId, language);
    if (document != null) {
      isAllowed = documentAccessControl.isUserAuthorized(userId, document,
          AccessControlContext.init().onOperationsOf(AccessControlOperation.DOWNLOAD));
      ref = new SilverpeasFileDescriptor(document.getInstanceId())
          .mimeType(document.getContentType())
          .fileName(document.getAttachmentPath())
          .absolutePath();
    } else {
      isAllowed = isUserAllowed(userId, componentId);
      ref = new SilverpeasFileDescriptor(componentId).mimeType(mimeType).fileName(sourceFile)
          .parentDirectory(directory);
    }

    if (!isAllowed) {
      SilverLogger.getLogger(this)
          .warn("Forbidden file access from ''{0}'' by user ''{1}''", req.getRequestURI(), userId);
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    try {
      SilverpeasFile onlineFile = SilverpeasFileProvider.getFile(ref);
      sendFile(req, response, onlineFile);
    } catch (IOException e) {
      SilverLogger.getLogger(this).error(e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  private MainSessionController getMainSessionController(final HttpServletRequest req) {
    final HttpSession session = req.getSession(false);
    return session == null ? null : (MainSessionController) session.getAttribute(
        MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
  }

  /**
   * Gets the document, either a version of a versioned document or an attachment, that is asked
   * for in the request, if any.
   */
  private SimpleDocument getWantedDocument(final HttpServletRequest req, final String componentId,
      final String language) {
    SimpleDocument document = null;
    String documentId = req.getParameter("DocumentId");
    String attachmentId = req.getParameter("attachmentId");
    if (StringUtil.isDefined(documentId)) {
      String versionId = req.getParameter("VersionId");
      document = attachmentService.searchDocumentById(new SimpleDocumentPK(versionId, componentId),
          language);
    }
    if (document == null && StringUtil.isDefined(attachmentId)) {
      document = attachmentService.searchDocumentById(
          new SimpleDocumentPK(attachmentId, componentId), language);
    }
    return document;
  }

  /**
   * Is the specified user allowed to access the files of the given resource? The resource is
   * either a component instance or, for the look files, a space whose identifier is then prefixed
   * by {@link #SPACE_RESOURCE_PREFIX}.
   */
  private boolean isUserAllowed(final String userId, final String resourceId) {
    if (StringUtil.isNotDefined(resourceId)) {
      return false;
    }
    if (resourceId.startsWith(SPACE_RESOURCE_PREFIX)) {
      String spaceId = resourceId.substring(SPACE_RESOURCE_PREFIX.length());
      if (!spaceId.startsWith(SPACE_KEY_PREFIX)) {
        spaceId = SPACE_KEY_PREFIX + spaceId;
      }
      return spaceAccessControl.isUserAuthorized(userId, spaceId) ||
          spaceAccessControl.hasUserSpaceManagementAuthorization(userId, spaceId);
    }
    return componentAccessControl.isUserAuthorized(userId, resourceId);
  }

  @Override
  protected SettingBundle getSettingBunde() {
    return ResourceLocator.getSettingBundle(
        "org.silverpeas.util.peasUtil.multiLang.fileServerBundle");
  }
}
