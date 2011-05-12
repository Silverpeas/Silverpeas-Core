/**
 * Copyright (C) 2000 - 2011 Silverpeas
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


import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.model.DocumentPK;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.silverpeas.versioning.util.VersioningUtil;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;

/**
 * Class declaration
 * @author
 */
public class FileUploader extends HttpServlet {

  private static final long serialVersionUID = 2484011627517965712L;

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
    SilverTrace.info("peasUtil", "FileUploader.doPost", "root.MSG_GEN_ENTER_METHOD");
    try {
      DiskFileUpload dfu = new DiskFileUpload();
      List<FileItem> items = dfu.parseRequest(req);
      FileItem file = getUploadedFile(items, "FileField");
      String userId = getParameterValue(items, "UserId");
      String attachmentId = getParameterValue(items, "FileId");
      String contentLanguage = getParameterValue(items, "FileLang");
      // in case of versioning
      String privateOrPublic = getParameterValue(items, "Version");
      String comments = getParameterValue(items, "Comment");

      if (StringUtil.isDefined(privateOrPublic)) {
        int versionType = ("publique".equals(privateOrPublic) ? DocumentVersion.TYPE_PUBLIC_VERSION
            : DocumentVersion.TYPE_DEFAULT_VERSION);
        processVersion(attachmentId, versionType, comments, file, userId, contentLanguage);
      } else {
        processAttachment(attachmentId, getUserId(req), file, contentLanguage);
      }
    } catch (Exception e) {
      SilverTrace.debug("peasUtil", "FileUploader.doPost",
          "root.MSG_GEN_PARAM_VALUE", e);
    }
  }

  private void processAttachment(String fileId, String userId, FileItem file, String language)
      throws Exception {
    SilverTrace.debug("peasUtil", "FileUploader.processAttachment()", "root.MSG_GEN_ENTER_METHOD", 
        "fileId = " + fileId);
    AttachmentDetail attachmentDetail = AttachmentController.searchAttachmentByPK(
        new AttachmentPK(fileId));
    String componentId = attachmentDetail.getInstanceId();
    String destFile = FileRepositoryManager.getAbsolutePath(componentId)
        + AttachmentController.CONTEXT_ATTACHMENTS + attachmentDetail.getPhysicalName(language);
    File uploadFile = new File(destFile);
    file.write(uploadFile);    
    AttachmentController.checkinFile(fileId, userId, true, false, true, language);
  }

  private void processVersion(String documentId, int versionType,
      String comment, FileItem file, String userId, String language)
      throws Exception {
    VersioningUtil versioningUtil = new VersioningUtil();
    DocumentVersion documentVersion = versioningUtil.getLastVersion(new DocumentPK(Integer.parseInt(documentId)));
    String componentId = documentVersion.getInstanceId();
    String logicalName = documentVersion.getLogicalName();
    String suffix = FileRepositoryManager.getFileExtension(logicalName);
    String newPhysicalName = new Long(new Date().getTime()).toString() + "." + suffix;
    String newVersionFile = FileRepositoryManager.getAbsolutePath(componentId)
        + DocumentVersion.CONTEXT_VERSIONING + newPhysicalName;
    File uploadFile = new File(newVersionFile);
    file.write(uploadFile);
    versioningUtil.checkinFile(documentId, versionType, comment, userId, newPhysicalName);
  }

  private FileItem getUploadedFile(List<FileItem> items, String parameterName) {
    for (FileItem item : items) {
      if (!item.isFormField() && parameterName.equals(item.getFieldName())) {
        return item;
      }
    }
    return null;
  }

  private String getParameterValue(List<FileItem> items, String parameterName) {
    for (FileItem item : items) {
      if (item.isFormField() && parameterName.equals(item.getFieldName())) {
        return item.getString();
      }
    }
    return null;
  }
  
    private String getUserId(HttpServletRequest request) {
    return ((MainSessionController) request.getSession().getAttribute(
        MainSessionController.MAIN_SESSION_CONTROLLER_ATT)).getUserId();
  }
}