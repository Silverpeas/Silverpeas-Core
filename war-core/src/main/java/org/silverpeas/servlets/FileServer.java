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
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.servlets;

import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.MimeTypes;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.ZipManager;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.statistic.control.StatisticBm;
import com.stratelia.webactiv.util.statistic.control.StatisticBmHome;
import org.apache.commons.io.FileUtils;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;

import javax.ejb.CreateException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;

import static com.stratelia.webactiv.util.FileServerUtils.*;

public class FileServer extends AbstractFileSender {

  private static final long serialVersionUID = 6377810839728682983L;

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
    SilverTrace.info("peasUtil", "FileServer.doPost", "root.MSG_GEN_ENTER_METHOD");
    String mimeType = req.getParameter(MIME_TYPE_PARAMETER);
    String sourceFile = req.getParameter(SOURCE_FILE_PARAMETER);
    String archiveIt = req.getParameter(ARCHIVE_IT_PARAMETER);
    String dirType = req.getParameter(DIR_TYPE_PARAMETER);
    String userId = req.getParameter(USER_ID_PARAMETER);
    String componentId = req.getParameter(COMPONENT_ID_PARAMETER);
    String typeUpload = req.getParameter(TYPE_UPLOAD_PARAMETER);
    String zip = req.getParameter(ZIP_PARAMETER);
    String fileName = req.getParameter(FILE_NAME_PARAMETER);
    String tempDirectory = FileRepositoryManager.getTemporaryPath("useless", componentId);
    File tempFile = null;
    String attachmentId = req.getParameter(ATTACHMENT_ID_PARAMETER);
    String language = req.getParameter(LANGUAGE_PARAMETER);
    if (!StringUtil.isDefined(attachmentId)) {
      attachmentId = req.getParameter(VERSION_ID_PARAMETER);
    }
    SimpleDocument attachment = null;
    if (StringUtil.isDefined(attachmentId)) {
      attachment = AttachmentServiceFactory.getAttachmentService().
          searchDocumentById(new SimpleDocumentPK(attachmentId, componentId), language);
      if (attachment != null) {
        mimeType = attachment.getContentType();
        sourceFile = attachment.getFilename();
      }
    }
    HttpSession session = req.getSession(true);
    MainSessionController mainSessionCtrl = (MainSessionController) session
        .getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
    if ((mainSessionCtrl == null) || (!isUserAllowed(mainSessionCtrl, componentId))) {
      SilverTrace.warn("peasUtil", "FileServer.doPost", "root.MSG_GEN_SESSION_TIMEOUT",
          "NewSessionId=" + session.getId() + URLManager.getApplicationURL() +
              GeneralPropertiesManager.getString("sessionTimeout"));
      res.sendRedirect(
          URLManager.getApplicationURL() + GeneralPropertiesManager.getString("sessionTimeout"));
      return;
    }

    String filePath = null;
    if (typeUpload != null) {
      filePath = sourceFile;
    } else {
      if (dirType != null) {
        if (dirType.equals(GeneralPropertiesManager.getString("RepositoryTypeTemp"))) {
          filePath = FileRepositoryManager.getTemporaryPath("useless", componentId) + sourceFile;
        }
      } else if (attachment != null) {
        // the file to download is not in a temporary directory
        filePath = attachment.getAttachmentPath();
      }
    }
    res.setContentType(mimeType);
    SilverTrace.debug("peasUtil", "FileServer.doPost()", "root.MSG_GEN_PARAM_VALUE", " zip=" + zip);
    if (zip != null) {
      res.setContentType(MimeTypes.ARCHIVE_MIME_TYPE);
      tempFile = File.createTempFile("zipfile", ".zip", new File(tempDirectory));
      SilverTrace.debug("peasUtil", "FileServer.doPost()", "root.MSG_GEN_PARAM_VALUE",
          " filePath =" + filePath + " tempFile.getCanonicalPath()=" + tempFile.getCanonicalPath() +
              " fileName=" + fileName);
      ZipManager.compressFile(filePath, tempFile.getCanonicalPath());
      filePath = tempFile.getCanonicalPath();
    }

    // display the preview code generated by the production tools
    if (zip == null) {
      if (tempFile != null) {
        sendFile(res, tempFile.getCanonicalPath());
      } else {
        sendFile(res, filePath);
      }
    }

    if (tempFile != null) {
      SilverTrace.info("peasUtil", "FileServer.doPost()", "root.MSG_GEN_ENTER_METHOD",
          " tempFile != null " + tempFile);
      FileUtils.deleteQuietly(tempFile);
    }

    if (StringUtil.isDefined(archiveIt)) {
      String nodeId = req.getParameter(NODE_ID_PARAMETER);
      String pubId = req.getParameter(PUBLICATION_ID_PARAMETER);
      ForeignPK pubPK = new ForeignPK(pubId, componentId);
      try {
        StatisticBmHome statisticBmHome =
            EJBUtilitaire.getEJBObjectRef(JNDINames.STATISTICBM_EJBHOME, StatisticBmHome.class);
        StatisticBm statisticBm = statisticBmHome.create();
        statisticBm.addStat(userId, pubPK, 1, "Publication");
      } catch (CreateException ex) {
        SilverTrace.warn("peasUtil", "FileServer.doPost", "peasUtil.CANNOT_WRITE_STATISTICS",
            "pubPK = " + pubPK + " and nodeId = " + nodeId, ex);
      } catch (RemoteException ex) {
        SilverTrace.warn("peasUtil", "FileServer.doPost", "peasUtil.CANNOT_WRITE_STATISTICS",
            "pubPK = " + pubPK + " and nodeId = " + nodeId, ex);
      }
    }
  }

  // check if the user is allowed to access the required component
  private boolean isUserAllowed(MainSessionController controller, String componentId) {
    boolean isAllowed;
    if (componentId == null) { // Personal space
      isAllowed = true;
    } else {
      if ("yes"
          .equalsIgnoreCase(controller.getComponentParameterValue(componentId, "publicFiles"))) {
        // Case of file contained in a component used as a file storage
        isAllowed = true;
      } else {
        isAllowed = controller.getOrganizationController()
            .isComponentAvailable(componentId, controller.getUserId());
      }
    }
    return isAllowed;
  }


  @Override
  protected ResourceLocator getResources() {
    return new ResourceLocator("org.silverpeas.util.peasUtil.multiLang.fileServerBundle", "");
  }
}
