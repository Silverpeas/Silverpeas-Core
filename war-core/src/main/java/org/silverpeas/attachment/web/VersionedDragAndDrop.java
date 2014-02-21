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

import com.silverpeas.util.FileUtil;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang3.CharEncoding;
import org.silverpeas.attachment.ActifyDocumentProcessor;
import org.silverpeas.attachment.AttachmentException;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.HistorisedDocument;
import org.silverpeas.attachment.model.SimpleAttachment;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.attachment.model.UnlockContext;
import org.silverpeas.attachment.model.UnlockOption;

/**
 * Servlet used whith the drag and drop applet to import versioned documents.
 */
public class VersionedDragAndDrop extends HttpServlet {

  private static final long serialVersionUID = -4994428375938427492L;

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
    req.setCharacterEncoding(CharEncoding.UTF_8);
    String componentId = req.getParameter("ComponentId");
    SilverTrace.info("versioningPeas", "DragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE",
        "componentId = " + componentId);
    String foreignId = req.getParameter("Id");
    SilverTrace.info("versioningPeas", "DragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE", "id = "
        + foreignId);
    int userId = Integer.parseInt(req.getParameter("UserId"));
    SilverTrace.info("versioningPeas", "DragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE",
        "userId = " + userId);
    boolean publicDocument = !StringUtil.getBooleanValue(req.getParameter("Type"));
    boolean bIndexIt = StringUtil.getBooleanValue(req.getParameter("IndexIt"));

    String documentId = req.getParameter("DocumentId");

    String lang = I18NHelper.checkLanguage(req.getParameter("lang"));
    List<FileItem> items = FileUploadUtil.parseRequest(req);
    for (FileItem item : items) {
      if (!item.isFormField()) {
        String fileName = FileUtil.getFilename(item.getName());
        long size = item.getSize();
        SilverTrace.info("versioningPeas", "DragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE",
            "item #" + fileName + " size = " + size);
        String mimeType = FileUtil.getMimeType(fileName);
        SimpleDocumentPK documentPK = new SimpleDocumentPK(null, componentId);
        if (StringUtil.isDefined(documentId)) {
          if (StringUtil.isInteger(documentId)) {
            documentPK.setOldSilverpeasId(Long.parseLong(documentId));
          } else {
            documentPK.setId(documentId);
          }
        }
        SimpleDocument document = AttachmentServiceFactory.getAttachmentService().
            findExistingDocument(documentPK, fileName, new ForeignPK(foreignId, componentId), lang);
        boolean needCreation = document == null;
        if (needCreation) {

          document = new HistorisedDocument(documentPK, foreignId, 0, "" + userId,
              new SimpleAttachment(fileName, lang, fileName, "", item.getSize(), mimeType, ""
                  + userId, new Date(), null));
        }
        document.setPublicDocument(publicDocument);
        try {
          if (needCreation) {
            AttachmentServiceFactory.getAttachmentService().createAttachment(document,
                item.getInputStream(), bIndexIt, publicDocument);
          } else {
            document.edit("" + userId);
            AttachmentServiceFactory.getAttachmentService().updateAttachment(document,
                item.getInputStream(), bIndexIt, publicDocument);
            UnlockContext unlockContext = new UnlockContext(document.getId(), "" + userId, lang, "");
            unlockContext.addOption(UnlockOption.UPLOAD);
            if (!publicDocument) {
              unlockContext.addOption(UnlockOption.PRIVATE_VERSION);
            }
            AttachmentServiceFactory.getAttachmentService().unlock(unlockContext);
          }
          // Specific case: 3d file to convert by Actify Publisher
          ActifyDocumentProcessor.getProcessor().process(document);

        } catch (AttachmentException e) {
          SilverTrace.error("versioningPeas", "DragAndDrop.doPost", "ERREUR", e);
          res.getOutputStream().println("ERROR");
          return;
        }
      } else {
        SilverTrace.info("versioningPeas", "DragAndDrop.doPost", "root.MSG_GEN_PARAM_VALUE",
            "item " + item.getFieldName() + "=" + item.getString());
      }
    }
    res.getOutputStream().println("SUCCESS");
  }
}
