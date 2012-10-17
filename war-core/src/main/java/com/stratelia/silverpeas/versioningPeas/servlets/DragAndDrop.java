/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.stratelia.silverpeas.versioningPeas.servlets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;

import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.silverpeas.versioning.importExport.VersioningImportExport;
import com.silverpeas.versioning.importExport.VersionsType;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioning.model.DocumentPK;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.silverpeas.versioning.model.DocumentVersionPK;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;

/**
 * Class declaration
 * @author
 */
public class DragAndDrop extends HttpServlet {
  private static final long serialVersionUID = -4994428375938427492L;

  @Override
  public void init(ServletConfig config) {
    try {
      super.init(config);
    } catch (ServletException se) {
      SilverTrace.fatal("versioningPeas", "DragAndDrop.init",
          "attachment.CANNOT_ACCESS_SUPERCLASS");
    }
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    doPost(req, res);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException,
      IOException {
    SilverTrace.info("versioningPeas", "DragAndDrop.doPost", "root.MSG_GEN_ENTER_METHOD");
    if (!FileUploadUtil.isRequestMultipart(req)) {
      res.getOutputStream().println("SUCCESS");
      return;
    }
    try {
      req.setCharacterEncoding("UTF-8");
      String componentId = req.getParameter("ComponentId");
      SilverTrace.info("versioningPeas", "DragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE",
          "componentId = " + componentId);
      String id = req.getParameter("Id");
      SilverTrace.info("versioningPeas", "DragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE", "id = "
          + id);
      int userId = Integer.parseInt(req.getParameter("UserId"));
      SilverTrace.info("versioningPeas", "DragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE",
          "userId = " + userId);
      int versionType = Integer.parseInt(req.getParameter("Type"));
      boolean bIndexIt = StringUtil.getBooleanValue(req.getParameter("IndexIt"));

      String documentId = req.getParameter("DocumentId");

      List<FileItem> items = FileUploadUtil.parseRequest(req);

      VersioningImportExport vie = new VersioningImportExport();
      int majorNumber = 0;
      int minorNumber = 0;

      String fullFileName = null;
      for (FileItem item : items) {
        if (!item.isFormField()) {
          String fileName = item.getName();
          if (fileName != null) {
            fileName = fileName.replace('\\', File.separatorChar);
            fileName = fileName.replace('/', File.separatorChar);
            SilverTrace.info("versioningPeas", "DragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE",
                "file = " + fileName);
            long size = item.getSize();
            SilverTrace.info("versioningPeas", "DragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE",
                "item #" + fullFileName + " size = " + size);
            String mimeType = AttachmentController.getMimeType(fileName);
            String physicalName = saveFileOnDisk(item, componentId, vie);
            DocumentPK documentPK = new DocumentPK(-1, componentId);
            if (StringUtil.isDefined(documentId)) {
              documentPK.setId(documentId);
            }
            DocumentVersionPK versionPK = new DocumentVersionPK(-1, documentPK);
            ForeignPK foreignPK = new ForeignPK(id, componentId);
            Document document = new Document(documentPK, foreignPK, fileName, null,
                Document.STATUS_CHECKINED, userId, null, null, componentId, null, null, 0, 0);

            DocumentVersion version = new DocumentVersion(versionPK, documentPK, majorNumber,
                minorNumber, userId, new Date(), null, versionType,
                DocumentVersion.STATUS_VALIDATION_NOT_REQ, physicalName, fileName, mimeType,
                new Long(size).intValue(), componentId);

            List<DocumentVersion> versions = new ArrayList<DocumentVersion>();
            versions.add(version);
            VersionsType versionsType = new VersionsType();
            versionsType.setListVersions(versions);
            document.setVersionsType(versionsType);
            List<Document> documents = new ArrayList<Document>();
            documents.add(document);

            try {
              vie.importDocuments(foreignPK, documents, userId, bIndexIt);
            } catch (Exception e) {
              // storing data into DB failed, delete file just added on disk
              deleteFileOnDisk(physicalName, componentId, vie);
              throw e;
            }
          } else {
            SilverTrace.info("versioningPeas", "DragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE",
                "item " + item.getFieldName() + "=" + item.getString());
          }
        }
      }
    } catch (Exception e) {
      SilverTrace.error("versioningPeas", "DragAndDrop.doPost", "ERREUR", e);
      res.getOutputStream().println("ERROR");
      return;
    }
    res.getOutputStream().println("SUCCESS");
  }

  private String saveFileOnDisk(FileItem item, String componentId, VersioningImportExport vie)
      throws Exception {
    String fileName = item.getName();
    if (fileName != null) {
      fileName = fileName.replace('\\', File.separatorChar);
      fileName = fileName.replace('/', File.separatorChar);
      SilverTrace.info("versioningPeas", "DragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE",
          "file = " + fileName);

      String type = FileRepositoryManager.getFileExtension(fileName);
      String physicalName = new Date().getTime() + "." + type;
      File savedFile = new File(vie.getVersioningPath(componentId) + physicalName);
      File parent = savedFile.getParentFile();
      if (!parent.exists()) {
        parent.mkdirs();
      }
      item.write(savedFile);
      return physicalName;
    }
    return null;
  }

  private void deleteFileOnDisk(String physicalName, String componentId, VersioningImportExport vie) {
    String path = vie.getVersioningPath(componentId) + physicalName;
    try {
      FileFolderManager.deleteFile(path);
    } catch (UtilException e) {
      SilverTrace.error("versioningPeas", "DragAndDrop.deleteFileOnDisk", "ERREUR",
          "Can't delete file : " + path);
    }
  }
}
