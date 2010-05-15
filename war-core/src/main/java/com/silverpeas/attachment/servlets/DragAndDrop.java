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
package com.silverpeas.attachment.servlets;

import com.silverpeas.util.web.servlet.FileUploadUtil;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;

/**
 * Class declaration
 * @author
 */
public class DragAndDrop extends HttpServlet {

  /**
   * Method declaration
   * @param config
   * @see
   */
  @Override
  public void init(ServletConfig config) {
    try {
      super.init(config);
    } catch (ServletException se) {
      SilverTrace.fatal("attachment", "DragAndDrop.init",
          "attachment.CANNOT_ACCESS_SUPERCLASS");
    }
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
    SilverTrace.info("attachment", "DragAndDrop.doPost", "root.MSG_GEN_ENTER_METHOD");

    ResourceLocator settings = new ResourceLocator(
        "com.stratelia.webactiv.util.attachment.Attachment", "");
    boolean actifyPublisherEnable = settings.getBoolean("ActifyPublisherEnable", false);
    try {
      req.setCharacterEncoding("UTF-8");
      String componentId = req.getParameter("ComponentId");
      SilverTrace.info("attachment", "DragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE",
          "componentId = "
              + componentId);
      String id = req.getParameter("PubId");
      SilverTrace
          .info("attachment", "DragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE", "id = " + id);
      String userId = req.getParameter("UserId");
      SilverTrace.info("attachment", "DragAndDrop.doPost",
          "root.MSG_GEN_PARAM_VALUE", "userId = " + userId);
      String context = req.getParameter("Context");
      boolean bIndexIt = "1".equals(req.getParameter("IndexIt"));

      List<FileItem> items = FileUploadUtil.parseRequest(req);
      for (FileItem item : items) {
        SilverTrace.info("attachment", "DragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE", "item = "
            + item.getFieldName());
        SilverTrace
            .info("attachment", "DragAndDrop.doPost",
                "root.MSG_GEN_PARAM_VALUE", "item = " + item.getName() + "; " +
                item.getString("UTF-8"));

        if (!item.isFormField()) {
          String fileName = item.getName();
          if (fileName != null) {
            fileName = fileName.replace('\\', File.separatorChar);
            fileName = fileName.replace('/', File.separatorChar);
            SilverTrace.info("attachment", "DragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE",
                "file = " + fileName);

            long size = item.getSize();
            SilverTrace.info("attachment", "DragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE",
                "item size = " + size);

            String type = FileRepositoryManager.getFileExtension(fileName);
            String physicalName = new Date().getTime() + "." + type;
            item.write(
                new File(AttachmentController.createPath(componentId, context) + physicalName));
            String mimeType = AttachmentController.getMimeType(fileName);
            // create AttachmentDetail Object
            AttachmentDetail attachment = new AttachmentDetail(new AttachmentPK(null, "useless",
                componentId), physicalName, fileName, null, mimeType, size, context, new Date(),
                new AttachmentPK(id, "useless", componentId));
            attachment.setAuthor(userId);
            AttachmentController.createAttachment(attachment, bIndexIt);
            // Specific case: 3d file to convert by Actify Publisher
            if (actifyPublisherEnable) {
              String extensions = settings.getString("Actify3dFiles");
              StringTokenizer tokenizer = new StringTokenizer(extensions, ",");
              // 3d native file ?
              boolean fileForActify = false;
              SilverTrace.info("attachment", "DragAndDrop.doPost",
                  "root.MSG_GEN_PARAM_VALUE", "nb tokenizer ="
                  + tokenizer.countTokens());
              while (tokenizer.hasMoreTokens() && !fileForActify) {
                String extension = tokenizer.nextToken();
                fileForActify = type.equalsIgnoreCase(extension);
              }
              if (fileForActify) {
                String dirDestName = "a_" + componentId + "_" + id;
                String actifyWorkingPath = settings.getString("ActifyPathSource")
                    + File.separatorChar + dirDestName;

                String destPath = FileRepositoryManager.getTemporaryPath() + actifyWorkingPath;
                if (!new File(destPath).exists()) {
                  FileRepositoryManager.createGlobalTempPath(actifyWorkingPath);
                }
                String destFile = FileRepositoryManager.getTemporaryPath() + actifyWorkingPath
                    + File.separatorChar + fileName;
                FileRepositoryManager.copyFile(AttachmentController.createPath(componentId,
                    "Images")
                    +
                    File.separatorChar + physicalName, destFile);
              }
            }
          }
        }
      }
    } catch (Exception e) {
      SilverTrace.error("attachment", "DragAndDrop.doPost", "ERREUR", e);
      res.getOutputStream().println("ERROR");
      return;
    }
    res.getOutputStream().println("SUCCESS");
  }
}
