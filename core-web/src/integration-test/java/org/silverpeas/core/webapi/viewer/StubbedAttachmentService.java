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
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.web.environment.SilverpeasTestEnvironment;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.inject.Singleton;
import java.io.File;

import static javax.interceptor.Interceptor.Priority.APPLICATION;
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
    SimpleDocument doc = null;
    if (!ATTACHMENT_ID_DOESNT_EXISTS.equals(docPk.getId())) {
      doc = new TestSimpleDocument();
      doc.setPK(docPk);

      SimpleAttachment attachment = SimpleAttachment.builder(lang)
          .setFilename("originalFileName" + docPk.getId())
          .build();
      doc.setAttachment(attachment);
    }
    return doc;
  }

  private static class TestSimpleDocument extends SimpleDocument {

    @Override
    public long getOldSilverpeasId() {
      return Long.parseLong(getPk().getId());
    }

    @Override
    public String getAttachmentPath() {
      return new File(getUploadPath(), getFilename()).getPath();
    }

    @Override
    public boolean canBeAccessedBy(final User user) {
      return isAllowed(user);
    }

    @Override
    public boolean canBeModifiedBy(final User user) {
      return isAllowed(user);
    }

    private boolean isAllowed(final User user) {
      if (user == null) {
        return false;
      }
      final ComponentInst linkedComponentInst = SilverpeasTestEnvironment.get()
          .getDummyPublicComponent();
      return ComponentAccessControl.get().isUserAuthorized(
          user.getId(), linkedComponentInst.getId());
    }
  }
}
  