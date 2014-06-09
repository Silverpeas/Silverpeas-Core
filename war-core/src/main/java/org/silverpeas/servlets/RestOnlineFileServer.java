/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.servlets;

import com.silverpeas.accesscontrol.AccessControlContext;
import com.silverpeas.accesscontrol.AccessControlOperation;
import com.silverpeas.accesscontrol.AccessController;
import com.silverpeas.accesscontrol.AccessControllerProvider;
import com.silverpeas.accesscontrol.ComponentAccessController;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.RestRequest;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.SilverpeasWebUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.file.SilverpeasFile;
import org.silverpeas.file.SilverpeasFileFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Class declaration
 * @author
 */
public class RestOnlineFileServer extends AbstractFileSender {

  private static final long serialVersionUID = 4039504051749955604L;

  @Override
  public void init(ServletConfig config) {
    try {
      super.init(config);
    } catch (ServletException se) {
      SilverTrace.fatal("peasUtil", "FileServer.init", "peasUtil.CANNOT_ACCESS_SUPERCLASS");
    }
  }

  @Override
  public void service(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    RestRequest restRequest = new RestRequest(req, "");
    SilverTrace.info("peasUtil", "RestOnlineFileServer.doPost", "root.MSG_GEN_ENTER_METHOD");
    try {
      SilverpeasFile file = getWantedFile(restRequest);
      if (file != null) {
        sendFile(res, file);
        return;
      }
    } catch (IllegalAccessException ex) {
      res.setStatus(HttpServletResponse.SC_FORBIDDEN);
      res.sendError(res.getStatus());
      return;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    displayWarningHtmlCode(res);
  }

  protected SilverpeasFile getWantedFile(RestRequest restRequest) throws Exception {
    SilverpeasFile file = getWantedAttachment(restRequest);
    if (file == SilverpeasFile.NO_FILE) {
      file = getWantedVersionnedDocument(restRequest);
    }
    return file;
  }

  protected SilverpeasFile getWantedAttachment(RestRequest restRequest) throws Exception {
    String componentId = restRequest.getElementValue("componentId");
    SilverpeasFile file = null;
    String attachmentId = restRequest.getElementValue("attachmentId");
    String language = restRequest.getElementValue("lang");
    if (StringUtil.isDefined(attachmentId)) {
      SimpleDocument attachment = AttachmentServiceFactory.getAttachmentService().
          searchDocumentById(new SimpleDocumentPK(attachmentId, componentId), language);
      if (null != attachment) {
        if (isUserAuthorized(restRequest, componentId, attachment)) {
          file = getSilverpeasFileFactory().getSilverpeasFile(attachment);
        } else {
          throw new IllegalAccessException("You can't access this file " + attachment.getFilename());
        }
      }
    }
    return file;
  }

  protected SilverpeasFile getWantedVersionnedDocument(RestRequest restRequest) throws Exception {
    String componentId = restRequest.getElementValue("componentId");
    SilverpeasFile file = null;
    String documentId = restRequest.getElementValue("documentId");
    if (StringUtil.isDefined(documentId)) {
      String versionId = restRequest.getElementValue("versionId");
      SimpleDocument version = AttachmentServiceFactory.getAttachmentService().
          searchDocumentById(new SimpleDocumentPK(versionId), null);
      if (version != null) {
        if (isUserAuthorized(restRequest, componentId, version)) {
          file = getSilverpeasFileFactory().getSilverpeasFile(version);
        } else {
          throw new IllegalAccessException("You can't access this file " + version.getFilename());
        }
      }
    }
    return file;
  }

  private boolean isUserAuthorized(RestRequest request, String componentId, Object object)
      throws Exception {
    SilverpeasWebUtil util = new SilverpeasWebUtil();
    MainSessionController controller = util.getMainSessionController(request.getWebRequest());
    ComponentAccessController componentAccessController = new ComponentAccessController();
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

  private boolean isSimpleDocumentAuthorized(String userId, SimpleDocument attachment)
      throws Exception {
    AccessController<SimpleDocument> accessController =
        AccessControllerProvider.getAccessController("simpleDocumentAccessController");
    return accessController.isUserAuthorized(userId, attachment,
        AccessControlContext.init().onOperationsOf(AccessControlOperation.download));
  }

  @Override
  protected ResourceLocator getResources() {
    return new ResourceLocator("org.silverpeas.util.peasUtil.multiLang.fileServerBundle", "");
  }

  private SilverpeasFileFactory getSilverpeasFileFactory() {
    return SilverpeasFileFactory.getFactory();
  }
}
