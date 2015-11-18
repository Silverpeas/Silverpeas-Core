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
package org.silverpeas.attachment.webdav.impl;

import com.silverpeas.jcrutil.BasicDaoFactory;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import org.silverpeas.attachment.AttachmentException;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.webdav.WebdavRepository;
import org.silverpeas.attachment.webdav.WebdavService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;

@Named("webdavService")
public class WebDavDocumentService implements WebdavService {

  @Inject
  @Named("webdavRepository")
  private WebdavRepository webdavRepository;

  @Override
  public void updateDocumentContent(SimpleDocument document) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      webdavRepository.updateAttachmentBinaryContent(session, document);
      session.save();
    } catch (RepositoryException ex) {
      SilverTrace
          .error("attachment", "WebDavDocumentService", "attachment.jcr.create.exception", ex);
      throw new AttachmentException("WebDavDocumentService", SilverpeasRuntimeException.ERROR,
          "attachment.jcr.create.exception", ex);
    } catch (IOException ex) {
      SilverTrace
          .error("attachment", "WebDavDocumentService", "attachment.jcr.create.exception", ex);
      throw new AttachmentException("WebDavDocumentService", SilverpeasRuntimeException.ERROR,
          "attachment.jcr.create.exception", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  @Override
  public String getContentEditionLanguage(final SimpleDocument document) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      return webdavRepository.getContentEditionLanguage(session, document);
    } catch (RepositoryException ex) {
      SilverTrace
          .error("attachment", "WebDavDocumentService", "attachment.jcr.node.notFound.exception",
              ex);
      throw new AttachmentException("WebDavDocumentService", SilverpeasRuntimeException.ERROR,
          "attachment.jcr.node.notFound.exception", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  @Override
  public long getContentEditionSize(final SimpleDocument document) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      return webdavRepository.getContentEditionSize(session, document);
    } catch (RepositoryException ex) {
      SilverTrace
          .error("attachment", "WebDavDocumentService", "attachment.jcr.node.notFound.exception",
              ex);
      throw new AttachmentException("WebDavDocumentService", SilverpeasRuntimeException.ERROR,
          "attachment.jcr.node.notFound.exception", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }
}
