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
package org.silverpeas.core.contribution.attachment.webdav;

import org.silverpeas.core.contribution.attachment.model.SimpleDocument;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;

public interface WebdavRepository {
  /**
   * Create a new node for the specified attachment so that the file may be accessed through
   * webdav. For an attachment, it can exist in webdav one, and only one,
   * content language of the attachment.
   * @param session the JCR session.
   * @param attachment the attachment.
   * @throws RepositoryException
   * @throws IOException
   */
  void createAttachmentNode(Session session, SimpleDocument attachment)
      throws RepositoryException, IOException;

  /**
   * Delete the node associated to the specified attachment.
   * All contents will be removed.
   * If a specific content (language) must be removed, then use
   * {@link #deleteAttachmentContentNode(Session, SimpleDocument, String)} method.
   * @param session the JCR session.
   * @param attachment the attachment.
   * @throws RepositoryException
   */
  void deleteAttachmentNode(Session session, SimpleDocument attachment) throws RepositoryException;

  /**
   * Delete the node associated to the specified language content attachment.
   * @param session the JCR session.
   * @param attachment the attachment.
   * @param language the aimed language content to delete.
   * @throws RepositoryException
   */
  void deleteAttachmentContentNode(Session session, SimpleDocument attachment, String language)
      throws RepositoryException;

  /**
   * Update the file content of the specified attachment without modifying its metadata.
   * @param session the JCR session.
   * @param attachment the attachment for which the file content will be updated with the ralated
   * webdav content.
   * @throws RepositoryException
   * @throws IOException
   */
  void updateAttachmentBinaryContent(Session session, SimpleDocument attachment)
      throws RepositoryException, IOException;

  /**
   * Update the node using the {@link SimpleDocument}. For an attachment,
   * it can exist in webdav one, and only one, content language of the attachment.
   * @param session the JCR session.
   * @param attachment the attachment.
   * @throws RepositoryException
   * @throws IOException
   */
  void updateNodeAttachment(Session session, SimpleDocument attachment)
      throws RepositoryException, IOException;

  /**
   * Move the specified attachment to the specified component instance id.
   * @param session the JCR session.
   * @param attachment the attachment to move to another component instance identifier.
   * @param targetComponentInstanceId the identifier of the target component instance.
   * @throws RepositoryException
   * @throws IOException
   */
  void moveNodeAttachment(Session session, SimpleDocument attachment,
      String targetComponentInstanceId) throws RepositoryException, IOException;

  /**
   * Indicate if the node for the specified attachment is currently locked (for example by Office
   * in the case of a webdav online edition).
   * @param session the JCR session.
   * @param attachment the attachment.
   * @return true if the node is locked - false otherwise.
   */
  boolean isNodeLocked(Session session, SimpleDocument attachment) throws RepositoryException;

  /**
   * Gets the current content edition language of the specified attachment.
   * If several webdav document exists (several content languages), then the one which has the
   * highest modified date is taken into account.
   * @param session the JCR session.
   * @param attachment the attachment.
   * @return the content edition language if the specified attachment exists in the webdav
   * repository, null otherwise.
   * @throws RepositoryException
   */
  String getContentEditionLanguage(Session session, SimpleDocument attachment)
      throws RepositoryException;

  /**
   * Gets the current content edition size of the specified attachment.
   * If several webdav document exists (several content languages), then the one which has the
   * highest modified date is taken into account.
   * @param session the JCR session.
   * @param attachment the attachment.
   * @return the content edition size if the specified attachment exists in the webdav
   * repository, -1 otherwise.
   * @throws RepositoryException
   */
  long getContentEditionSize(Session session, SimpleDocument attachment)
      throws RepositoryException;
}