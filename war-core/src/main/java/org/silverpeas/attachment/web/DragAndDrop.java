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
package org.silverpeas.attachment.web;

import com.silverpeas.util.FileUtil;
import com.silverpeas.util.MetaData;
import com.silverpeas.util.MetadataExtractor;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.CharEncoding;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.DocumentType;
import org.silverpeas.attachment.model.SimpleAttachment;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Servlet used whith the drag and drop applet to import documents.
 */
public class DragAndDrop extends HttpServlet {

  private static final long serialVersionUID = 1L;

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
      req.setCharacterEncoding(CharEncoding.UTF_8);
      String componentId = req.getParameter("ComponentId");
      String id = req.getParameter("PubId");
      String lang = I18NHelper.checkLanguage(req.getParameter("lang"));
      String userId = req.getParameter("UserId");
      SilverTrace.info("attachment", "DragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE",
          "componentId = " + componentId + ", id = " + id + ", userId = " + userId);
      boolean bIndexIt = StringUtil.getBooleanValue(req.getParameter("IndexIt"));

      List<FileItem> items = FileUploadUtil.parseRequest(req);
      for (FileItem item : items) {
        SilverTrace.info("attachment", "DragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE", "item = "
            + item.getFieldName());
        SilverTrace.info("attachment", "DragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE", "item = "
            + item.getName() + "; " + item.getString(CharEncoding.UTF_8));

        if (!item.isFormField()) {
          String fileName = item.getName();
          if (fileName != null) {
            String mimeType = FileUtil.getMimeType(fileName);
            SilverTrace.info("attachment", "DragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE",
                "item size = " + item.getSize());
            // create AttachmentDetail Object
            SimpleDocument document = new SimpleDocument(new SimpleDocumentPK(null, componentId),
                id, 0, false, new SimpleAttachment(fileName, lang, null, null, item.getSize(),
                mimeType, userId, new Date(), null));
            document.setDocumentType(determineDocumentType(req));
            InputStream uploadedInputStream = item.getInputStream();
            File tempFile = File.createTempFile("silverpeas_", fileName);
            try {
              FileUtils.copyInputStreamToFile(uploadedInputStream, tempFile);
              MetadataExtractor extractor = new MetadataExtractor();
              MetaData metadata = extractor.extractMetadata(tempFile);
              document.setSize(tempFile.length());
              document.setTitle(metadata.getTitle());
              document.setDescription(metadata.getSubject());
              document = AttachmentServiceFactory.getAttachmentService().createAttachment(document,
                  tempFile, bIndexIt);
            } finally {
              FileUtils.deleteQuietly(tempFile);
            }
            // Specific case: 3d file to convert by Actify Publisher
            if (actifyPublisherEnable) {
              String extensions = settings.getString("Actify3dFiles");
              StringTokenizer tokenizer = new StringTokenizer(extensions, ",");
              // 3d native file ?
              boolean fileForActify = false;
              SilverTrace.info("attachment", "DragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE",
                  "nb tokenizer =" + tokenizer.countTokens());
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

  private DocumentType determineDocumentType(HttpServletRequest req) {
    String context = req.getParameter("Context");
    DocumentType type = DocumentType.attachment;
    if (StringUtil.isDefined(context)) {
      try {
        type = DocumentType.valueOf(context);
      } catch (IllegalArgumentException ex) {
        //wrong parameter value, we keep with the default context.
      }
    }
    return type;
  }
}
