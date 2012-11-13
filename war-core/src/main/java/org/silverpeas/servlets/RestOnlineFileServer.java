/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;

import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.attachment.web.OnlineAttachment;

import com.silverpeas.accesscontrol.ComponentAccessController;
import com.silverpeas.accesscontrol.SimpleDocumentAccessController;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.RestRequest;

import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.SilverpeasWebUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 * Class declaration
 *
 * @author
 */
public class RestOnlineFileServer extends HttpServlet {

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
      OnlineFile file = getWantedFile(restRequest);
      if (file != null) {
        display(res, file);
        return;
      }
    } catch (IllegalAccessException ex) {
      res.setStatus(HttpServletResponse.SC_FORBIDDEN);
      return;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    displayWarningHtmlCode(res);
  }

  protected OnlineFile getWantedFile(RestRequest restRequest) throws Exception {
    OnlineFile file = getWantedAttachment(restRequest);
    if (file == null) {
      file = getWantedVersionnedDocument(restRequest);
    }
    return file;
  }

  protected OnlineFile getWantedAttachment(RestRequest restRequest) throws Exception {
    String componentId = restRequest.getElementValue("componentId");
    OnlineFile file = null;
    String attachmentId = restRequest.getElementValue("attachmentId");
    String language = restRequest.getElementValue("lang");
    if (StringUtil.isDefined(attachmentId)) {
      SimpleDocument attachment = AttachmentServiceFactory.getAttachmentService().
          searchDocumentById(new SimpleDocumentPK(attachmentId), language);
      if (attachment != null) {
        if (isUserAuthorized(restRequest, componentId, attachment)) {
          file = new OnlineAttachment(attachment);
        } else {
          throw new IllegalAccessException("You can't access this file " + attachment.getFilename());
        }
      }
    }
    return file;
  }

  protected OnlineFile getWantedVersionnedDocument(RestRequest restRequest) throws Exception {
    String componentId = restRequest.getElementValue("componentId");
    OnlineFile file = null;
    String documentId = restRequest.getElementValue("documentId");
    if (StringUtil.isDefined(documentId)) {
      String versionId = restRequest.getElementValue("versionId");
      SimpleDocument version = AttachmentServiceFactory.getAttachmentService().
          searchDocumentById(new SimpleDocumentPK(versionId), null);
      if (version != null) {
        if (isUserAuthorized(restRequest, componentId, version)) {
          String[] path = new String[1];
          path[0] = "Versioning";
          file = new OnlineFile(version.getContentType(), version.getFilename(),
              FileRepositoryManager.getRelativePath(path), componentId);
        } else {
          throw new IllegalAccessException("You can't access this file " + version.getFilename());
        }
      }
    }
    return file;
  }

  /**
   * This method writes the result of the preview action.
   *
   * @param res - The HttpServletResponse where the html code is write
   * @param htmlFilePath - the canonical path of the html document generated by the parser tools. if
   * this String is null that an exception had been catched the html document generated is empty !!
   * also, we display a warning html page
   */
  private void display(HttpServletResponse res, OnlineFile onlineFile) throws IOException {
    res.setContentType(onlineFile.getMimeType());
    res.setHeader("Content-Length", String.valueOf(onlineFile.getContentLength()));
    OutputStream output = res.getOutputStream();
    SilverTrace.info("peasUtil", "OnlineFileServer.display()", "root.MSG_GEN_ENTER_METHOD",
        " htmlFilePath " + onlineFile.getSourceFile());
    try {
      onlineFile.write(output);
    } catch (IOException ioex) {
      SilverTrace.warn("peasUtil", "OnlineFileServer.doPost", "root.EX_CANT_READ_FILE", "file name="
          + onlineFile.getSourceFile(), ioex);
      displayWarningHtmlCode(res);
    } finally {
      IOUtils.closeQuietly(output);
    }
  }

  private void displayWarningHtmlCode(HttpServletResponse res) throws IOException {
    OutputStream output = res.getOutputStream();
    ResourceLocator resourceLocator = new ResourceLocator(
        "org.silverpeas.util.peasUtil.multiLang.fileServerBundle", "");
    StringReader message = new StringReader(resourceLocator.getString("warning"));
    try {
      IOUtils.copy(message, output);
    } catch (Exception e) {
      SilverTrace.warn("peasUtil", "OnlineFileServer.displayWarningHtmlCode",
          "root.EX_CANT_READ_FILE", "warning properties");
    } finally {
      IOUtils.closeQuietly(output);
      IOUtils.closeQuietly(message);
    }
  }

  private boolean isUserAuthorized(RestRequest request, String componentId, Object object)
      throws Exception {
    SilverpeasWebUtil util = new SilverpeasWebUtil();
    MainSessionController controller = util.getMainSessionController(request.getWebRequest());
    ComponentAccessController componentAccessController = new ComponentAccessController();
    if (controller != null && componentAccessController.isUserAuthorized(controller.getUserId(),
        componentId)) {
      if (componentAccessController.isRightOnTopicsEnabled(controller.getUserId(), componentId)) {
        if (object instanceof SimpleDocument) {
          return isSimpleDocumentAuthorized(controller.getUserId(), (SimpleDocument) object);
        }
        return false;
      }
      return true;
    }
    return false;
  }

  private boolean isSimpleDocumentAuthorized(String userId, SimpleDocument attachment) throws
      Exception {
    SimpleDocumentAccessController accessController = new SimpleDocumentAccessController();
    return accessController.isUserAuthorized(userId, attachment);
  }
}
