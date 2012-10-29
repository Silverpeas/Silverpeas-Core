/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.form.displayers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;

import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleAttachment;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;

import com.silverpeas.form.PagesContext;
import com.silverpeas.form.fieldType.FileField;
import com.silverpeas.util.FileUtil;

import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 *
 * @author ehugonnet
 */
public abstract class AbstractFileFieldDisplayer extends AbstractFieldDisplayer<FileField> {

  protected SimpleDocument createSimpleDocument(String objectId, String componentId, FileItem item,
      String fileName, String userId) throws IOException {
    SimpleDocumentPK documentPk = new SimpleDocumentPK(null, componentId);
    SimpleDocument document = new SimpleDocument(documentPk, objectId, 0, false, userId,
        new SimpleAttachment(fileName, null, null, null, item.getSize(),
        FileUtil.getMimeType(fileName), userId, new Date(), null));
    InputStream in = item.getInputStream();
    try {
      return AttachmentServiceFactory.getAttachmentService().createAttachment(document, in, false);
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

  /**
   * Deletes the specified attachment, identified by its unique identifier.?
   *
   * @param attachmentId the unique identifier of the attachment to delete.
   * @param pageContext the context of the page.
   */
  protected void deleteAttachment(String attachmentId, PagesContext pageContext) {
    SilverTrace.info("form", "AbstractFileFieldDisplayer.deleteAttachment",
        "root.MSG_GEN_ENTER_METHOD", "attachmentId = " + attachmentId + ", componentId = "
        + pageContext.getComponentId());
    SimpleDocumentPK pk = new SimpleDocumentPK(attachmentId, pageContext.getComponentId());
    AttachmentServiceFactory.getAttachmentService().deleteAttachment(AttachmentServiceFactory.
        getAttachmentService().searchDocumentById(pk, null));
  }

  /**
   * Returns the name of the managed types.
   *
   * @return
   */
  public String[] getManagedTypes() {
    return new String[]{FileField.TYPE};
  }
}
