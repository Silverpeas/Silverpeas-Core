/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.web.servlets;

import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.io.file.SilverpeasFile;
import org.silverpeas.core.io.file.SilverpeasFileDescriptor;
import org.silverpeas.core.io.file.SilverpeasFileProvider;
import org.silverpeas.core.web.mvc.AbstractFileSender;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Class declaration
 *
 * @author
 */
public class OnlineFileServer extends AbstractFileSender {

  private static final long serialVersionUID = -6153872618631360113L;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    doPost(req, res);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse response)
      throws ServletException, IOException {


    String mimeType = req.getParameter("MimeType");
    String sourceFile = req.getParameter("SourceFile");
    String directory = req.getParameter("Directory");
    String componentId = req.getParameter("ComponentId");
    String attachmentId = req.getParameter("attachmentId");
    String language = req.getParameter("lang");
    String documentId = req.getParameter("DocumentId");
    SilverpeasFileDescriptor ref = null;

    if (StringUtil.isDefined(documentId)) {
      String versionId = req.getParameter("VersionId");
      SimpleDocumentPK versionPK = new SimpleDocumentPK(versionId, componentId);
      SimpleDocument version =
          AttachmentServiceProvider.getAttachmentService().searchDocumentById(versionPK, language);

      if (version != null) {
        ref =
            new SilverpeasFileDescriptor(version.getInstanceId()).mimeType(version.getContentType())
                .fileName(version.getAttachmentPath()).absolutePath();
      }
    }
    if (ref == null && StringUtil.isDefined(attachmentId)) {
      // Check first if attachment exists
      SimpleDocument attachment = AttachmentServiceProvider.getAttachmentService()
          .searchDocumentById(new SimpleDocumentPK(attachmentId), language);
      if (attachment != null) {
        ref = new SilverpeasFileDescriptor(attachment.getInstanceId())
            .mimeType(attachment.getContentType()).fileName(attachment.getAttachmentPath())
            .absolutePath();
      }
    }
    if (ref == null) {
      ref = new SilverpeasFileDescriptor(componentId).mimeType(mimeType).fileName(sourceFile)
          .parentDirectory(directory);
    }

    SilverpeasFile onlineFile = SilverpeasFileProvider.getFile(ref);
    sendFile(response, onlineFile);
  }


  @Override
  protected SettingBundle getSettingBunde() {
    return ResourceLocator.getSettingBundle(
        "org.silverpeas.util.peasUtil.multiLang.fileServerBundle");
  }
}
