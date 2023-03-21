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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.servlets;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.io.file.SilverpeasFile;
import org.silverpeas.core.io.file.SilverpeasFileDescriptor;
import org.silverpeas.core.io.file.SilverpeasFileProvider;
import org.silverpeas.core.security.authorization.AccessControlContext;
import org.silverpeas.core.security.authorization.AccessControlOperation;
import org.silverpeas.core.security.authorization.ComponentAccessController;
import org.silverpeas.core.security.authorization.SimpleDocumentAccessControl;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.mvc.AbstractFileSender;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.controller.SilverpeasWebUtil;

import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Optional;

/**
 * This servlet serves the content of attachments whatever their types.
 */
public class RestOnlineFileServer extends AbstractFileSender {
  private static final long serialVersionUID = 4039504051749955604L;

  @Inject
  private ComponentAccessController componentAccessController;
  @Inject
  private SilverpeasWebUtil silverpeasWebUtil;

  @Override
  public void init(ServletConfig config) {
    try {
      super.init(config);
    } catch (ServletException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
  }

  @Override
  public void service(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    RestRequest restRequest = new RestRequest(req, "");
    try {
      Optional<SilverpeasFile> file = getWantedFile(restRequest);
      if (file.isPresent()) {
        sendFile(req, res, file.get());
      } else {
        displayWarningHtmlCode(res);
      }
    } catch (FileNotFoundException ex) {
      SilverLogger.getLogger(this).error("Requested file not found!", ex);
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.sendError(res.getStatus());
    } catch (IllegalAccessException ex) {
      User currentUser = User.getCurrentRequester();
      String userId = currentUser != null ? currentUser.getId() : "N/A";
      SilverLogger.getLogger(this)
          .warn("Forbidden file access from ''{0}'' by user ''{1}''", req.getRequestURI(), userId,
              ex);
      res.setStatus(HttpServletResponse.SC_FORBIDDEN);
      res.sendError(res.getStatus());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
  }

  private Optional<SilverpeasFile> getWantedFile(RestRequest restRequest)
      throws IllegalAccessException, FileNotFoundException {
    SilverpeasFile file = getWantedAttachment(restRequest);
    if (file == SilverpeasFile.NO_FILE) {
      file = getWantedVersionedDocument(restRequest);
    }
    if (file == SilverpeasFile.NO_FILE) {
      throw new FileNotFoundException();
    }
    return Optional.ofNullable(file);
  }

  private SilverpeasFile getWantedAttachment(RestRequest restRequest)
      throws IllegalAccessException {
    String componentId = restRequest.getElementValue("componentId");
    String attachmentId =
        URLDecoder.decode(restRequest.getElementValue("attachmentId"), Charsets.UTF_8);
    String language = restRequest.getElementValue("lang");
    String size = restRequest.getElementValue("size");
    SilverpeasFile file = SilverpeasFile.NO_FILE;
    if (StringUtil.isDefined(attachmentId)) {
      SimpleDocument attachment = AttachmentServiceProvider.getAttachmentService().
          searchDocumentById(new SimpleDocumentPK(attachmentId, componentId), language);
      if (attachment != null) {
        if (isUserAuthorized(restRequest, componentId, attachment)) {
          // an image of a given size is asked for.
          if (StringUtil.isDefined(size)) {
            attachment.setFilename(size + File.separatorChar + attachment.getFilename());
          }
          file = getSilverpeasFile(attachment);
        } else {
          throw new IllegalAccessException("Forbidden access to file " + attachment);
        }
      }
    }
    return file;
  }

  private SilverpeasFile getWantedVersionedDocument(RestRequest restRequest)
      throws IllegalAccessException {
    String componentId = restRequest.getElementValue("componentId");
    String documentId = restRequest.getElementValue("documentId");
    String fileName = restRequest.getElementValue("name");
    String size = restRequest.getElementValue("size");
    SilverpeasFile file = SilverpeasFile.NO_FILE;
    if (StringUtil.isDefined(documentId)) {
      String versionId = restRequest.getElementValue("versionId");
      SimpleDocument version = AttachmentServiceProvider.getAttachmentService().
          searchDocumentById(new SimpleDocumentPK(versionId), null);
      if (version != null) {
        if (isUserAuthorized(restRequest, componentId, version)) {
          // an image of a given size is asked for.
          if (StringUtil.isDefined(size)) {
            version.setFilename(size + File.separatorChar + fileName);
          }
          file = getSilverpeasFile(version);
        } else {
          throw new IllegalAccessException("Forbidden access to file " + version);
        }
      }
    }
    return file;
  }

  private SilverpeasFile getSilverpeasFile(final SimpleDocument document) {
    SilverpeasFileDescriptor descriptor =
        new SilverpeasFileDescriptor(document.getInstanceId())
            .mimeType(document.getContentType())
            .fileName(document.getAttachmentPath())
            .absolutePath();
    return SilverpeasFileProvider.getFile(descriptor);
  }

  private boolean isUserAuthorized(RestRequest request, String componentId, Object object) {
    MainSessionController controller =
        silverpeasWebUtil.getMainSessionController(request.getWebRequest());
    if (controller != null) {

      if (object instanceof SimpleDocument) {
        return isSimpleDocumentAuthorized(controller.getUserId(), (SimpleDocument) object);
      }

      // If rights on directories are enabled, there is a problem
      if (componentAccessController.isRightOnTopicsEnabled(componentId)) {
        return false;
      }

      // Verification on the component instance
      return componentAccessController.isUserAuthorized(controller.getUserId(), componentId);
    }
    return false;
  }

  private boolean isSimpleDocumentAuthorized(String userId, SimpleDocument attachment) {
    return SimpleDocumentAccessControl.get().isUserAuthorized(userId, attachment,
        AccessControlContext.init().onOperationsOf(AccessControlOperation.DOWNLOAD));
  }

  @Override
  protected SettingBundle getSettingBunde() {
    return ResourceLocator.getSettingBundle(
        "org.silverpeas.util.peasUtil.multiLang.fileServerBundle");
  }

}
