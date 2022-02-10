/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

package org.silverpeas.core.webapi.viewer;

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.attachment.SimpleDocumentService;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.inject.Singleton;

import static javax.interceptor.Interceptor.Priority.APPLICATION;

/**
 * @author mmoquillon
 */
@Service
@Singleton
@Alternative
@Priority(APPLICATION + 10)
public class StubbedAttachmentService extends SimpleDocumentService {

  static String ATTACHMENT_ID_DOESNT_EXISTS = "8";

  @Override
  public SimpleDocument searchDocumentById(final SimpleDocumentPK attachmentPK,
      final String lang) {
    SimpleDocument attachmentDetail = null;
    if (!ATTACHMENT_ID_DOESNT_EXISTS.equals(attachmentPK.getId())) {
      attachmentDetail = new SimpleDocument();
      attachmentDetail.setPK(attachmentPK);
      attachmentDetail.setOldSilverpeasId(Long.parseLong(attachmentPK.getId()));
      attachmentDetail.setAttachment(SimpleAttachment.builder().build());
      attachmentDetail.setFilename("originalFileName" + attachmentPK.getId());
    }
    return attachmentDetail;
  }
}
  