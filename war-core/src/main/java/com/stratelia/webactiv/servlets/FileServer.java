/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.servlets;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.stratelia.silverpeas.peasCore.URLManager;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.silverpeas.versioning.model.DocumentVersionPK;
import com.stratelia.silverpeas.versioning.util.VersioningUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.statistic.control.StatisticBm;
import com.stratelia.webactiv.util.statistic.control.StatisticBmHome;

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
    String mimeType = req.getParameter("MimeType");
    String sourceFile = req.getParameter("SourceFile");
    String directory = req.getParameter("Directory");
    String archiveIt = req.getParameter("ArchiveIt");
    String dirType = req.getParameter("DirType");
    String userId = req.getParameter("UserId");
    String componentId = req.getParameter("ComponentId");
    String typeUpload = req.getParameter("TypeUpload");
    String zip = req.getParameter("zip");
    String fileName = req.getParameter("fileName");
    String tempDirectory = FileRepositoryManager.getTemporaryPath("useless",
        componentId);
    File tempFile = null;

    String attachmentId = req.getParameter("attachmentId");
    String language = req.getParameter("lang");
    AttachmentDetail attachment = null;
    long fileSize = 0;
    if (StringUtil.isDefined(attachmentId)) {
      // Check first if attachment exists
      attachment = AttachmentController.searchAttachmentByPK(new AttachmentPK(
          attachmentId));
      if (attachment != null) {
        mimeType = attachment.getType(language);
        sourceFile = attachment.getPhysicalName(language);
        directory = FileRepositoryManager.getRelativePath(
            FileRepositoryManager.getAttachmentContext(attachment.
                getContext()));
        fileSize = attachment.getSize(language);
      }
    }

    String documentId = req.getParameter("DocumentId");
    if (StringUtil.isDefined(documentId)) {
      String versionId = req.getParameter("VersionId");
      VersioningUtil versioning = new VersioningUtil();
      DocumentVersionPK versionPK = new DocumentVersionPK(Integer.parseInt(versionId), "useless",
          componentId);
      DocumentVersion version = versioning.getDocumentVersion(versionPK);

      if (version != null) {
        mimeType = version.getMimeType();
        sourceFile = version.getPhysicalName();
        fileSize = version.getSize();

        String[] path = new String[1];
        path[0] = "Versioning";
        directory = FileRepositoryManager.getRelativePath(path);
      }
    }

    String filePath = null;

    HttpSession session = req.getSession(true);
    MainSessionController mainSessionCtrl = (MainSessionController) session.getAttribute(
        MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
    if ((mainSessionCtrl == null)
        || (!isUserAllowed(mainSessionCtrl, componentId))) {
      SilverTrace.warn("peasUtil", "FileServer.doPost", "root.MSG_GEN_SESSION_TIMEOUT",
          "NewSessionId=" + session.getId() + URLManager.getApplicationURL()
              + GeneralPropertiesManager.getGeneralResourceLocator().getString("sessionTimeout"));
      res.sendRedirect(URLManager.getApplicationURL() +
          GeneralPropertiesManager.getGeneralResourceLocator().getString("sessionTimeout"));
    }
    if (typeUpload != null) {
      filePath = sourceFile;
    } else {
      if (dirType != null) {
        if (dirType.equals(GeneralPropertiesManager.getGeneralResourceLocator().getString(
            "RepositoryTypeTemp"))) {
          filePath = FileRepositoryManager.getTemporaryPath("useless", componentId) + sourceFile;
        }
      } else {
        // the file to download is not in a temporary directory
        filePath = FileRepositoryManager.getAbsolutePath(componentId) + directory + File.separator
            + sourceFile;
      }
    }
    res.setContentType(mimeType);
    SilverTrace.debug("peasUtil", "FileServer.doPost()", "root.MSG_GEN_PARAM_VALUE", " zip=" + zip);
    if (zip != null) {
      res.setContentType("application/x-zip-compressed");
      tempFile = File.createTempFile("zipfile", ".zip", new File(tempDirectory));
      SilverTrace.debug("peasUtil", "FileServer.doPost()",
          "root.MSG_GEN_PARAM_VALUE", " filePath =" + filePath
              + " tempFile.getCanonicalPath()=" + tempFile.getCanonicalPath()
              + " fileName=" + fileName);
      zipFile(filePath, tempFile.getCanonicalPath(), fileName);
      filePath = tempFile.getCanonicalPath();
    }

    // display the preview code generated by the production tools
    if (zip == null) {
      if (tempFile != null) {
        displayHtmlCode(res, tempFile.getCanonicalPath());
      } else {
        if (fileSize > 0) {
          res.setContentLength(new Long(fileSize).intValue());
        }
        displayHtmlCode(res, filePath);
      }
    }

    if (tempFile != null) {
      SilverTrace.info("peasUtil", "FileServer.doPost()",
          "root.MSG_GEN_ENTER_METHOD", " tempFile != null " + tempFile);
      tempFile.delete();
    }

    if ((archiveIt != null) && (archiveIt.length() > 0)
        && (!archiveIt.equals("null"))) {
      String nodeId = req.getParameter("NodeId");
      String pubId = req.getParameter("PubId");
      ForeignPK pubPK = null;

      try {
        StatisticBmHome statisticBmHome = (StatisticBmHome) EJBUtilitaire.getEJBObjectRef(
            JNDINames.STATISTICBM_EJBHOME,
            StatisticBmHome.class);
        StatisticBm statisticBm = statisticBmHome.create();

        pubPK = new ForeignPK(pubId, componentId);
        statisticBm.addStat(userId, pubPK, 1, "Publication");
      } catch (Exception e) {
        SilverTrace.warn("peasUtil", "FileServer.doPost",
            "peasUtil.CANNOT_WRITE_STATISTICS", "pubPK = " + pubPK
                + " and nodeId = " + nodeId, e);
      }
    }
  }

  // check if the user is allowed to access the required component
  private boolean isUserAllowed(MainSessionController controller,
      String componentId) {
    boolean isAllowed = false;

    if (componentId == null) { // Personal space
      isAllowed = true;
    } else {
      if ("yes".equalsIgnoreCase(
          controller.getComponentParameterValue(componentId, "publicFiles"))) {
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
   * Methode declaration
   *
   * @param filePath
   * @param zipFilePath
   * @zip a given file
   */
  private void zipFile(String filePath, String zipFilePath, String fileName) {
    int compressionLevel = Deflater.DEFAULT_COMPRESSION;
    SilverTrace.debug("peasUtil", "FileServer.zipFile()",
        "root.MSG_GEN_PARAM_VALUE", " fileName =" + fileName);

    try {
      // create and initialize a stream to write it
      ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(
          zipFilePath));

      // zip.setComment("created by Mohammed Hguig - http://www.silverpeas.com");
      zip.setMethod(ZipOutputStream.DEFLATED);
      zip.setLevel(compressionLevel);

      // read a file into memory
      File file = new File(filePath);
      FileInputStream in = new FileInputStream(file);
      byte[] bytes = new byte[in.available()];

      in.read(bytes);
      in.close();

      // create and initialize a zipentry for it
      // ZipEntry entry = new ZipEntry(file.getName());
      ZipEntry entry = new ZipEntry(fileName);

      entry.setTime(file.lastModified());

      // write the entry header, and the data, to the zip
      zip.putNextEntry(entry);
      zip.write(bytes);

      // write the end-of-entry marker to the zip
      zip.closeEntry();

      // no more files, close the zip. This writes the zip
      // directory, so don't forget it.
      zip.close();
    } catch (Exception e) {
      SilverTrace.warn("peasUtil", "FileServer", "peasUtil.MSG_ZIP_FILE_FAIL",
          "file to zip = " + filePath, e);
    }
  }

  // End Add By Mohammed Hguig

  /**
   * This method writes the result of the preview action.
   *
   * @param res          - The HttpServletResponse where the html code is write
   * @param htmlFilePath - the canonical path of the html document generated by the parser tools. if
   *                     this String is null that an exception had been catched the html document
   *                     generated is empty !! also, we display a warning html page
   */
  private void displayHtmlCode(HttpServletResponse res, String htmlFilePath)
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
