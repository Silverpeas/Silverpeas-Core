/**
 * Copyright (C) 2000 - 2013 Silverpeas
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

import org.silverpeas.attachment.AttachmentService;
import org.silverpeas.util.FileUtil;
import org.silverpeas.util.MetaData;
import org.silverpeas.util.MetadataExtractor;
import org.silverpeas.util.StringUtil;
import org.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.CharEncoding;
import org.silverpeas.attachment.ActifyDocumentProcessor;
import org.silverpeas.attachment.model.DocumentType;
import org.silverpeas.attachment.model.SimpleAttachment;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.servlet.FileUploadUtil;
import org.silverpeas.servlet.HttpRequest;
import org.silverpeas.web.util.SilverpeasTransverseWebErrorUtil;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Servlet used whith the drag and drop applet to import non-versioned documents.
 */
public class DragAndDrop extends HttpServlet {

  private static final long serialVersionUID = 1L;

  @Inject
  private MetadataExtractor metadataExtractor;
  @Inject
  private ActifyDocumentProcessor actifyDocumentProcessor;
  @Inject
  private AttachmentService attachmentService;

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
    HttpRequest request = HttpRequest.decorate(req);
    if (!request.isContentInMultipart()) {
      res.getOutputStream().println("SUCCESS");
      return;
    }

    String userId = req.getParameter("UserId");
    try {
      request.setCharacterEncoding(CharEncoding.UTF_8);
      String componentId = request.getParameter("ComponentId");
      String id = request.getParameter("PubId");
      String lang = I18NHelper.checkLanguage(request.getParameter("lang"));
      SilverTrace.info("attachment", "DragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE",
          "componentId = " + componentId + ", id = " + id + ", userId = " + userId);
      boolean bIndexIt = StringUtil.getBooleanValue(request.getParameter("IndexIt"));

      List<FileItem> items = request.getFileItems();
      for (FileItem item : items) {
        SilverTrace.info("attachment", "DragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE",
            "item = " + item.getFieldName());
        SilverTrace.info("attachment", "DragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE",
            "item = " + item.getName());

        if (!item.isFormField()) {
          String fileName = item.getName();
          if (fileName != null) {
            String mimeType = FileUtil.getMimeType(fileName);
            SilverTrace.info("attachment", "DragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE",
                "item size = " + item.getSize());
            // create AttachmentDetail Object
            SimpleDocument document =
                new SimpleDocument(new SimpleDocumentPK(null, componentId), id, 0, false,
                    new SimpleAttachment(fileName, lang, null, null, item.getSize(), mimeType,
                        userId, new Date(), null));
            document.setDocumentType(determineDocumentType(request));
            File tempFile = File.createTempFile("silverpeas_", fileName);
            try {
              FileUploadUtil.saveToFile(tempFile, item);
              MetaData metadata = metadataExtractor.extractMetadata(tempFile);
              document.setSize(tempFile.length());
              document.setTitle(metadata.getTitle());
              document.setDescription(metadata.getSubject());
              document = attachmentService.createAttachment(document, tempFile, bIndexIt);
            } finally {
              FileUtils.deleteQuietly(tempFile);
            }
            // Specific case: 3d file to convert by Actify Publisher
            actifyDocumentProcessor.process(document);
          }
        }
      }
    } catch (Exception e) {
      SilverTrace.error("attachment", "DragAndDrop.doPost", "ERREUR", e);
      final StringBuilder sb = new StringBuilder("ERROR: ");
      final String errorMessage = SilverpeasTransverseWebErrorUtil
          .performAppletAlertExceptionMessage(e,
              UserDetail.getById(userId).getUserPreferences().getLanguage());
      if (StringUtil.isDefined(errorMessage)) {
        sb.append(errorMessage);
      } else {
        sb.append(e.getMessage());
      }
      res.getOutputStream().println(sb.toString());
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
