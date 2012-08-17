/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.attachment.servlets;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleAttachment;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;

import com.silverpeas.util.FileUtil;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.FileUploadUtil;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 * Class declaration
 *
 * @author
 */
public class DragAndDrop extends HttpServlet {

  private static final long serialVersionUID = 1L;

  /**
   * Method declaration
   *
   * @param config
   * @see
   */
  @Override
  public void init(ServletConfig config) {
    try {
      super.init(config);
    } catch (ServletException se) {
      SilverTrace.fatal("attachment", "DragAndDrop.init", "attachment.CANNOT_ACCESS_SUPERCLASS");
    }
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
    SilverTrace.info("attachment", "DragAndDrop.doPost", "root.MSG_GEN_ENTER_METHOD");
    if (!FileUploadUtil.isRequestMultipart(req)) {
      res.getOutputStream().println("SUCCESS");
      return;
    }
    ResourceLocator settings = new ResourceLocator("org.silverpeas.util.attachment.Attachment", "");
    boolean actifyPublisherEnable = settings.getBoolean("ActifyPublisherEnable", false);
    try {
      req.setCharacterEncoding("UTF-8");
      String componentId = req.getParameter("ComponentId");
      SilverTrace.info("attachment", "DragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE",
          "componentId = " + componentId);
      String id = req.getParameter("PubId");
      SilverTrace
          .info("attachment", "DragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE", "id = " + id);
      String userId = req.getParameter("UserId");
      SilverTrace.info("attachment", "DragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE", "userId = "
          + userId);
      boolean bIndexIt = StringUtil.getBooleanValue(req.getParameter("IndexIt"));

      List<FileItem> items = FileUploadUtil.parseRequest(req);
      for (FileItem item : items) {
        SilverTrace.info("attachment", "DragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE", "item = "
            + item.getFieldName());
        SilverTrace.info("attachment", "DragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE", "item = "
            + item.getName() + "; " + item.getString("UTF-8"));

        if (!item.isFormField()) {
          String fileName = item.getName();
          if (fileName != null) {
            String mimeType = FileUtil.getMimeType(fileName);

            SilverTrace.info("attachment", "DragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE",
                "item size = " + item.getSize());
            SimpleDocument document = new SimpleDocument(new SimpleDocumentPK(null, componentId),
                id, 0, false, new SimpleAttachment(fileName, null, fileName, null, item.
                getSize(), mimeType, userId, new Date(), null));
            // create AttachmentDetail Object
            InputStream in = item.getInputStream();
            try {
              document = AttachmentServiceFactory.getAttachmentService().createAttachment(document,
                  in, bIndexIt);
            } catch (Exception e) {
              throw e;
            } finally {
              IOUtils.closeQuietly(in);
            }
            // Specific case: 3d file to convert by Actify Publisher
            if (actifyPublisherEnable) {
              String extensions = settings.getString("Actify3dFiles");
              StringTokenizer tokenizer = new StringTokenizer(extensions, ",");
              // 3d native file ?
              boolean fileForActify = false;
              SilverTrace.info("attachment", "DragAndDrop.doPost",
                  "root.MSG_GEN_PARAM_VALUE", "nb tokenizer =" + tokenizer.countTokens());
              String type = FileRepositoryManager.getFileExtension(fileName);
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
                String normalizedFileName = FilenameUtils.normalize(fileName);
                if (normalizedFileName == null) {
                  normalizedFileName = FilenameUtils.getName(fileName);
                }
                String destFile = FileRepositoryManager.getTemporaryPath() + actifyWorkingPath
                    + File.separatorChar + normalizedFileName;
                FileRepositoryManager.copyFile(document.getAttachmentPath(), destFile);
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
