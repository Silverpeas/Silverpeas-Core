/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
package org.silverpeas.attachment.webdav;

import java.io.IOException;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.silverpeas.attachment.model.SimpleDocument;

public interface WebdavRepository {
  /**
   * Create a new node for the specified attachment so that the file may be accessed through webdav.
   *
   * @param session the JCR session.
   * @param attachment the attachment.
   * @throws RepositoryException
   * @throws IOException  
   */
  public void createAttachmentNode(Session session, SimpleDocument attachment)
      throws RepositoryException, IOException;

  /**
   * Delete the node associated to the specified attachment.
   *
   * @param session the JCR session.
   * @param attachment the attachment.
   * @throws RepositoryException  
   */
  public void deleteAttachmentNode(Session session, SimpleDocument attachment)
      throws RepositoryException;

  /**
   * Update the AttachmentDetail using the node
   *
   *
   * @param session the JCR session.
   * @param attachment
   * @throws RepositoryException
   * @throws IOException
   */
  public void updateAttachment(Session session, SimpleDocument attachment) throws
      RepositoryException, IOException;

  /**
   * Update the node using the AttachmentDetail
   *
   *
   * @param session the JCR session.
   * @param attachment
   * @throws RepositoryException
   * @throws IOException
   */
  public void updateNodeAttachment(Session session, SimpleDocument attachment)
      throws RepositoryException, IOException;

  /**
   * Indicate if the node for the specified attachment is currently locked (for example by Office in
   * the case of a webdav online edition).
   *
   *
   * @param session the JCR session.
   * @param attachment the attachment.
   * @return true if the node is locked - false otherwise.
   */
  public boolean isNodeLocked(Session session, SimpleDocument attachment) throws
      RepositoryException;
}