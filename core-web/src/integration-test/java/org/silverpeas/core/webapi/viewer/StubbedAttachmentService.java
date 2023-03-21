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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.webapi.viewer;

import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.attachment.SimpleDocumentService;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.content.form.filter.FilterManager;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.web.environment.SilverpeasEnvironmentTest;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.inject.Singleton;

import java.io.File;

import static javax.interceptor.Interceptor.Priority.APPLICATION;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.silverpeas.core.util.file.FileRepositoryManager.getUploadPath;

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
  public SimpleDocument searchDocumentById(final SimpleDocumentPK docPk,
      final String lang) {
    SimpleDocument attachmentDetail = null;
    if (!ATTACHMENT_ID_DOESNT_EXISTS.equals(docPk.getId())) {
      attachmentDetail = mock(SimpleDocument.class);
      when(attachmentDetail.getPk()).thenReturn(docPk);
      when(attachmentDetail.getId()).thenReturn(docPk.getId());
      when(attachmentDetail.getOldSilverpeasId()).thenReturn(Long.parseLong(docPk.getId()));
      when(attachmentDetail.getAttachment()).thenReturn(SimpleAttachment.builder().build());
      final String language = attachmentDetail.getAttachment().getLanguage();
      when(attachmentDetail.getLanguage()).thenReturn(language);
      final String filename = "originalFileName" + docPk.getId();
      when(attachmentDetail.getFilename()).thenReturn(filename);
      when(attachmentDetail.getAttachmentPath()).thenReturn(new File(getUploadPath(), filename).getPath());
      final ComponentInst linkedComponentInst = SilverpeasEnvironmentTest.get()
          .getDummyPublicComponent();
      final boolean canAccess = ComponentAccessControl.get().isUserAuthorized(
          User.getCurrentRequester().getId(), linkedComponentInst.getId());
      when(attachmentDetail.canBeAccessedBy(any(User.class))).thenReturn(canAccess);
      when(attachmentDetail.canBeModifiedBy(any(User.class))).thenReturn(canAccess);
    }
    return attachmentDetail;
  }
}
  