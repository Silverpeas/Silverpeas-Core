/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contribution.attachment.webdav.impl;

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.attachment.AttachmentException;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.webdav.WebdavRepository;
import org.silverpeas.core.contribution.attachment.webdav.WebdavService;
import org.silverpeas.core.persistence.jcr.JcrSession;

import javax.inject.Inject;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

import static org.silverpeas.core.persistence.jcr.JcrRepositoryConnector.openSystemSession;

@Service
public class WebDavDocumentService implements WebdavService {

  @Inject
  private WebdavRepository webdavRepository;

  @Override
  public void updateDocumentContent(SimpleDocument document) {
    try(JcrSession session = openSystemSession()) {
      webdavRepository.updateAttachmentBinaryContent(session, document);
      session.save();
    } catch (RepositoryException | IOException ex) {
      throw new AttachmentException("Fail to update the document content", ex);
    }
  }

  @Override
  public String getContentEditionLanguage(final SimpleDocument document) {
    try(JcrSession session = openSystemSession()) {
      return webdavRepository.getContentEditionLanguage(session, document);
    } catch (RepositoryException ex) {
      throw new AttachmentException("Document not found", ex);
    }
  }

  @Override
  public long getContentEditionSize(final SimpleDocument document) {
    try(JcrSession session = openSystemSession()) {
      return webdavRepository.getContentEditionSize(session, document);
    } catch (RepositoryException ex) {
      throw new AttachmentException("Document not found", ex);
    }
  }

  @Override
  public Optional<WebdavContentDescriptor> getDescriptor(final SimpleDocument document) {
    try(JcrSession session = openSystemSession()) {
      return webdavRepository.getDescriptor(session, document);
    } catch (RepositoryException ex) {
      throw new AttachmentException("Document not found", ex);
    }
  }

  @Override
  public void updateContentFrom(final SimpleDocument document, final InputStream input)
      throws IOException {
    try(JcrSession session = openSystemSession()) {
      webdavRepository.updateContentFrom(session, document, input);
    } catch (RepositoryException ex) {
      throw new AttachmentException("Document not found", ex);
    }
  }

  @Override
  public void loadContentInto(final SimpleDocument document, final OutputStream output)
      throws IOException {
    try(JcrSession session = openSystemSession()) {
      webdavRepository.loadContentInto(session, document, output);
    } catch (RepositoryException ex) {
      throw new AttachmentException("Document not found", ex);
    }
  }
}
