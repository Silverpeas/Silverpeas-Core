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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.servlets;

import com.silverpeas.util.ArrayUtil;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.ZipManager;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.silverpeas.versioning.model.DocumentVersionPK;
import com.stratelia.silverpeas.versioning.util.VersioningUtil;
import com.stratelia.webactiv.util.*;
import com.stratelia.webactiv.util.statistic.control.StatisticBm;
import com.stratelia.webactiv.util.statistic.control.StatisticBmHome;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;

import com.silverpeas.util.MimeTypes;
import static com.stratelia.webactiv.util.FileServerUtils.*;

/**
 * Class declaration
 *
 * @author
 */
public class FileServer extends HttpServlet {

  private static final long serialVersionUID = 6377810839728682983L;

  /**
   * @param spaceId
   * @param componentId
   * @param logicalName
   * @param physicalName
   * @param mimeType
   * @param subDirectory
   * @return
   * @deprecated Use com.stratelia.webactiv.util.FileServerUtils instead.
   */
  public static String getUrl(String spaceId, String componentId,
      String logicalName, String physicalName, String mimeType,
      String subDirectory) {
    return FileServerUtils.getUrl(spaceId, componentId, logicalName,
        physicalName, mimeType, subDirectory);
  }

  /**
   * @param logicalName
   * @param physicalName
   * @param mimeType
   * @return
   * @deprecated Use com.stratelia.webactiv.util.FileServerUtils instead.
   */
  public static String getUrl(String logicalName, String physicalName,
      String mimeType) {
    return FileServerUtils.getUrl(logicalName, physicalName, mimeType);
  }

  /**
   * @param spaceId
   * @param componentId
   * @param name
   * @param mimeType
   * @param subDirectory
   * @return
   * @deprecated Use com.stratelia.webactiv.util.FileServerUtils instead.
   */
  public static String getUrl(String spaceId, String componentId, String name,
      String mimeType, String subDirectory) {
    return FileServerUtils.getUrl(spaceId, componentId, name, name, mimeType,
        subDirectory);
  }

  /**
   * @param spaceId
   * @param componentId
   * @param userId
   * @param logicalName
   * @param physicalName
   * @param mimeType
   * @param archiveIt
   * @param pubId
   * @param nodeId
   * @param subDirectory
   * @return
   * @deprecated Use com.stratelia.webactiv.util.FileServerUtils instead.
   */
  public static String getUrl(String spaceId, String componentId,
      String userId, String logicalName, String physicalName, String mimeType,
      boolean archiveIt, int pubId, int nodeId, String subDirectory) {
    return FileServerUtils.getUrl(spaceId, componentId, userId, logicalName,
        physicalName, mimeType, archiveIt, pubId, nodeId, subDirectory);
  }

  /**
   * @param logicalName
   * @return
   * @deprecated Use com.stratelia.webactiv.util.FileServerUtils instead.
   */
  public static String getUrlToTempDir(String logicalName) {
    return FileServerUtils.getUrlToTempDir(logicalName);
  }

  @Override
  public void init(ServletConfig config) {
    try {
      super.init(config);
    } catch (ServletException se) {
      SilverTrace.fatal("peasUtil", "FileServer.init", "peasUtil.CANNOT_ACCESS_SUPERCLASS");
    }
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    doPost(req, res);
  }

  /**
   * Method declaration
   *
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
    String directory = req.getParameter(DIRECTORY_PARAMETER);
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
    long fileSize = 0;
    if (!StringUtil.isDefined(attachmentId)) {
      attachmentId = req.getParameter(VERSION_ID_PARAMETER);
    }
    SimpleDocument attachment = null;
    if (StringUtil.isDefined(attachmentId)) {
      attachment = AttachmentServiceFactory.getAttachmentService().
          searchAttachmentById(new SimpleDocumentPK(attachmentId, componentId), language);
      if (attachment != null) {
        mimeType = attachment.getContentType();
        sourceFile = attachment.getFilename();
        directory = attachment.getAttachmentPath();
        fileSize = attachment.getSize();
      }
    }
    HttpSession session = req.getSession(true);
    MainSessionController mainSessionCtrl = (MainSessionController) session.getAttribute(
        MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
    if ((mainSessionCtrl == null) || (!isUserAllowed(mainSessionCtrl, componentId))) {
      SilverTrace.warn("peasUtil", "FileServer.doPost", "root.MSG_GEN_SESSION_TIMEOUT",
          "NewSessionId=" + session.getId() + URLManager.getApplicationURL()
          + GeneralPropertiesManager.getGeneralResourceLocator().getString("sessionTimeout"));
      res.sendRedirect(URLManager.getApplicationURL() + GeneralPropertiesManager.
          getGeneralResourceLocator().getString("sessionTimeout"));
    }

    String filePath = null;
    if (typeUpload != null) {
      filePath = sourceFile;
    } else {
      if (dirType != null) {
        if (dirType.equals(GeneralPropertiesManager.getGeneralResourceLocator().getString(
            "RepositoryTypeTemp"))) {
          filePath = FileRepositoryManager.getTemporaryPath("useless", componentId) + sourceFile;
        }
      } else if(attachment != null) {
        // the file to download is not in a temporary directory
        filePath = attachment.getAttachmentPath();
      }
    }
    res.setContentType(mimeType);
    SilverTrace.debug("peasUtil", "FileServer.doPost()", "root.MSG_GEN_PARAM_VALUE", " zip=" + zip);
    if (zip != null) {
      res.setContentType(MimeTypes.ARCHIVE_MIME_TYPE);
      tempFile = File.createTempFile("zipfile", ".zip", new File(tempDirectory));
      SilverTrace.debug("peasUtil", "FileServer.doPost()", "root.MSG_GEN_PARAM_VALUE", " filePath ="
          + filePath + " tempFile.getCanonicalPath()=" + tempFile.getCanonicalPath()
          + " fileName=" + fileName);
      ZipManager.compressFile(filePath, tempFile.getCanonicalPath());
      filePath = tempFile.getCanonicalPath();
    }

    // display the preview code generated by the production tools
    if (zip == null) {
      if (tempFile != null) {
        downloadFile(res, tempFile.getCanonicalPath());
      } else {
        if (fileSize > 0) {
          res.setContentLength(new Long(fileSize).intValue());
        }
        downloadFile(res, filePath);
      }
    }

    if (tempFile != null) {
      SilverTrace.info("peasUtil", "FileServer.doPost()", "root.MSG_GEN_ENTER_METHOD",
          " tempFile != null " + tempFile);
      tempFile.delete();
    }

    if (StringUtil.isDefined(archiveIt)) {
      String nodeId = req.getParameter(NODE_ID_PARAMETER);
      String pubId = req.getParameter(PUBLICATION_ID_PARAMETER);
      ForeignPK pubPK = new ForeignPK(pubId, componentId);
      try {
        StatisticBmHome statisticBmHome = EJBUtilitaire.getEJBObjectRef(
            JNDINames.STATISTICBM_EJBHOME, StatisticBmHome.class);
        StatisticBm statisticBm = statisticBmHome.create();
        statisticBm.addStat(userId, pubPK, 1, "Publication");
      } catch (Exception e) {
        SilverTrace.warn("peasUtil", "FileServer.doPost", "peasUtil.CANNOT_WRITE_STATISTICS",
            "pubPK = " + pubPK + " and nodeId = " + nodeId, e);
      }
    }
  }

  // check if the user is allowed to access the required component
  private boolean isUserAllowed(MainSessionController controller, String componentId) {
    boolean isAllowed;
    if (componentId == null) { // Personal space
      isAllowed = true;
    } else {
      if ("yes".equalsIgnoreCase(controller.getComponentParameterValue(componentId, "publicFiles"))) {
        // Case of file contained in a component used as a file storage
        isAllowed = true;
      } else {
        isAllowed = controller.getOrganizationController().isComponentAvailable(
            componentId, controller.getUserId());
      }
    }
    return isAllowed;
  }

  /**
   * This method writes the result of the preview action.
   *
   * @param res - The HttpServletResponse where the html code is write
   * @param htmlFilePath - the canonical path of the html document generated by the parser tools. if
   * this String is null that an exception had been catched the html document generated is empty !!
   * also, we display a warning html page
   */
  private void downloadFile(HttpServletResponse res, String htmlFilePath)
      throws IOException {
    OutputStream out2 = res.getOutputStream();
    int read;
    BufferedInputStream input = null; // for the html document generated
    SilverTrace.info("peasUtil", "FileServer.displayHtmlCode()",
        "root.MSG_GEN_ENTER_METHOD", " htmlFilePath " + htmlFilePath);
    try {
      input = new BufferedInputStream(new FileInputStream(htmlFilePath));
      read = input.read();
      SilverTrace.info("peasUtil", "FileServer.displayHtmlCode()",
          "root.MSG_GEN_ENTER_METHOD", " BufferedInputStream read " + read);
      if (read == -1) {
        displayWarningHtmlCode(res);
      } else {
        while (read != -1) {
          out2.write(read); // writes bytes into the response
          read = input.read();
        }
      }
    } catch (Exception e) {
      SilverTrace.warn("peasUtil", "FileServer.doPost",
          "root.EX_CANT_READ_FILE", "file name=" + htmlFilePath);
      displayWarningHtmlCode(res);
    } finally {
      SilverTrace.info("peasUtil", "FileServer.displayHtmlCode()", "",
          " finally ");
      // we must close the in and out streams
      try {
        if (input != null) {
          input.close();
        }
        out2.close();
      } catch (Exception e) {
        SilverTrace.warn("peasUtil", "FileServer.displayHtmlCode",
            "root.EX_CANT_READ_FILE", "close failed");
      }
    }
  }

  private void displayWarningHtmlCode(HttpServletResponse res)
      throws IOException {
    StringReader sr = null;
    OutputStream out2 = res.getOutputStream();
    int read;
    ResourceLocator resourceLocator = new ResourceLocator(
        "com.stratelia.webactiv.util.peasUtil.multiLang.fileServerBundle", "");

    sr = new StringReader(resourceLocator.getString("warning"));
    try {
      read = sr.read();
      while (read != -1) {
        SilverTrace.info("peasUtil", "FilServer.displayHtmlCode()",
            "root.MSG_GEN_ENTER_METHOD", " StringReader read " + read);
        out2.write(read); // writes bytes into the response
        read = sr.read();
      }
    } catch (Exception e) {
      SilverTrace.warn("peasUtil", "FileServer.displayWarningHtmlCode",
          "root.EX_CANT_READ_FILE", "warning properties");
    } finally {
      try {
        if (sr != null) {
          sr.close();
        }
        out2.close();
      } catch (Exception e) {
        SilverTrace.warn("peasUtil", "FileServer.displayHtmlCode",
            "root.EX_CANT_READ_FILE", "close failed");
      }
    }
  }
}
