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
 * FLOSS exception.  You should have recieved a copy of the text describing
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

import com.silverpeas.util.FileUtil;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.DiskFileUpload;
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
  HttpSession session;
  PrintWriter out;

  /**
   * Method declaration
   * @param config
   * @see
   */
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
    SilverTrace.info("attachment", "DragAndDrop.doPost",
        "root.MSG_GEN_ENTER_METHOD");

    ResourceLocator settings = new ResourceLocator(
        "com.stratelia.webactiv.util.attachment.Attachment", "");
    boolean runOnUnix = !FileUtil.isWindows();
    boolean actifyPublisherEnable = settings.getBoolean(
        "ActifyPublisherEnable", false);

    SilverTrace.info("importExportPeas", "DragAndDrop",
        "root.MSG_GEN_PARAM_VALUE", "runOnUnix = " + runOnUnix);

    try {
      String componentId = req.getParameter("ComponentId");
      SilverTrace.info("attachment", "DragAndDrop.doPost",
          "root.MSG_GEN_PARAM_VALUE", "componentId = " + componentId);
      String id = req.getParameter("PubId");
      SilverTrace.info("attachment", "DragAndDrop.doPost",
          "root.MSG_GEN_PARAM_VALUE", "id = " + id);
      String userId = req.getParameter("UserId");
      SilverTrace.info("attachment", "DragAndDrop.doPost",
          "root.MSG_GEN_PARAM_VALUE", "userId = " + userId);
      String context = req.getParameter("Context");
      String indexIt = req.getParameter("IndexIt");
      boolean bIndexIt = false;
      if ("1".equals(indexIt))
        bIndexIt = true;

      DiskFileUpload dfu = new DiskFileUpload();
      List items = dfu.parseRequest(req);

      String fullFileName = null;
      for (int i = 0; i < items.size(); i++) {
        FileItem item = (FileItem) items.get(i);
        SilverTrace.info("attachment", "DragAndDrop.doPost",
            "root.MSG_GEN_PARAM_VALUE", "item #" + i + " = "
            + item.getFieldName());
        SilverTrace.info("attachment", "DragAndDrop.doPost",
            "root.MSG_GEN_PARAM_VALUE", "item #" + i + " = " + item.getName());

        if (!item.isFormField()) {
          // create AttachmentPK with spaceId and componentId
          AttachmentPK atPK = new AttachmentPK(null, "useless", componentId);

          // create foreignKey with spaceId, componentId and id
          // use AttachmentPK to build the foreign key of customer object.
          AttachmentPK foreignKey = new AttachmentPK(id, "useless", componentId);

          fullFileName = item.getName();
          if (fullFileName != null && runOnUnix) {
            fullFileName = fullFileName.replace('\\', File.separatorChar);
            SilverTrace.info("attachment", "DragAndDrop.doPost",
                "root.MSG_GEN_PARAM_VALUE", "fullFileName on Unix = "
                + fullFileName);
          }

          String fileName = fullFileName.substring(fullFileName
              .lastIndexOf(File.separator) + 1, fullFileName.length());
          SilverTrace.info("attachment", "DragAndDrop.doPost",
              "root.MSG_GEN_PARAM_VALUE", "file = " + fileName);

          long size = item.getSize();
          SilverTrace.info("attachment", "DragAndDrop.doPost",
              "root.MSG_GEN_PARAM_VALUE", "item #" + i + " size = " + size);

          String type = fileName.substring(fileName.lastIndexOf(".") + 1,
              fileName.length());
          String physicalName = new Long(new Date().getTime()).toString() + "."
              + type;

          item.write(new File(AttachmentController.createPath(componentId,
              context)
              + physicalName));
          String mimeType = AttachmentController.getMimeType(fileName);

          // create AttachmentDetail Object
          AttachmentDetail ad = new AttachmentDetail(atPK, physicalName,
              fileName, null, mimeType, size, context, new Date(), foreignKey);
          ad.setAuthor(userId);

          AttachmentController.createAttachment(ad, bIndexIt);

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
              if (type.equalsIgnoreCase(extension))
                fileForActify = true;
            }
            if (fileForActify) {
              String dirDestName = "a_" + componentId + "_" + id;
              String actifyWorkingPath = settings.getString("ActifyPathSource")
                  + File.separator + dirDestName;

              String destPath = FileRepositoryManager.getTemporaryPath()
                  + actifyWorkingPath;
              if (!new File(destPath).exists())
                FileRepositoryManager.createGlobalTempPath(actifyWorkingPath);

              String destFile = FileRepositoryManager.getTemporaryPath()
                  + actifyWorkingPath + File.separator + fileName;
              FileRepositoryManager.copyFile(AttachmentController.createPath(
                  componentId, "Images")
                  + File.separator + physicalName, destFile);
            }
          }
        }
      }
    } catch (Exception e) {
      SilverTrace.error("attachment", "DragAndDrop.doPost", "ERREUR", e);
    }
  }
}