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
package org.silverpeas.core.contribution.attachment.webdav.impl;

import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.contribution.attachment.AttachmentException;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.webdav.WebdavRepository;
import org.silverpeas.core.contribution.attachment.webdav.WebdavService;
import org.silverpeas.core.persistence.jcr.JcrSession;
import org.silverpeas.core.exception.SilverpeasRuntimeException;

import javax.inject.Inject;
import javax.jcr.RepositoryException;
import java.io.IOException;

import static org.silverpeas.core.persistence.jcr.JcrRepositoryConnector.openSystemSession;

public class WebDavDocumentService implements WebdavService {

  @Inject
  private WebdavRepository webdavRepository;

  @Override
  public void updateDocumentContent(SimpleDocument document) {
    try(JcrSession session = openSystemSession()) {
      webdavRepository.updateAttachmentBinaryContent(session, document);
      session.save();
    } catch (RepositoryException | IOException ex) {
      SilverTrace
          .error("attachment", "WebDavDocumentService", "attachment.jcr.create.exception", ex);
      throw new AttachmentException("WebDavDocumentService", SilverpeasRuntimeException.ERROR,
          "attachment.jcr.create.exception", ex);
    }
  }

  @Override
  public String getContentEditionLanguage(final SimpleDocument document) {
    try(JcrSession session = openSystemSession()) {
      return webdavRepository.getContentEditionLanguage(session, document);
    } catch (RepositoryException ex) {
      SilverTrace
          .error("attachment", "WebDavDocumentService", "attachment.jcr.node.notFound.exception",
              ex);
      throw new AttachmentException("WebDavDocumentService", SilverpeasRuntimeException.ERROR,
          "attachment.jcr.node.notFound.exception", ex);
    }
  }

  @Override
  public long getContentEditionSize(final SimpleDocument document) {
    try(JcrSession session = openSystemSession()) {
      return webdavRepository.getContentEditionSize(session, document);
    } catch (RepositoryException ex) {
      SilverTrace
          .error("attachment", "WebDavDocumentService", "attachment.jcr.node.notFound.exception",
              ex);
      throw new AttachmentException("WebDavDocumentService", SilverpeasRuntimeException.ERROR,
          "attachment.jcr.node.notFound.exception", ex);
    }
  }
}
