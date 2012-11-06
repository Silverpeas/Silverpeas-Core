/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.util.attachment.model.jcr;

import java.io.IOException;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;

public interface JcrAttachmentDao {

  /**
   * Create a new node for the specified attachment so that the file may be accessed through webdav.
   * @param session the JCR session.
   * @param attachment the attachment.
   * @param language the language to obtain the file.
   */
  public void createAttachmentNode(Session session,
      AttachmentDetail attachment, String language) throws RepositoryException,
      IOException;

  /**
   * Delete the node associated to the specified attachment.
   * @param session the JCR session.
   * @param attachment the attachment.
   * @param language the language to obtain the file.
   */
  public void deleteAttachmentNode(Session session,
      AttachmentDetail attachment, String language) throws RepositoryException,
      IOException;

  /**
   * Update the AttachmentDetail using the node
   * @param session the JCR session.
   * @param attachment
   * @param language
   * @throws RepositoryException
   * @throws IOException
   */
  public void updateAttachment(Session session, AttachmentDetail attachment,
      String language) throws RepositoryException, IOException;

  /**
   * Update the node using the AttachmentDetail
   * @param session the JCR session.
   * @param attachment
   * @param language
   * @throws RepositoryException
   * @throws IOException
   */
  public void updateNodeAttachment(Session session,
      AttachmentDetail attachment, String language) throws RepositoryException,
      IOException;

  /**
   * Indicate if the node for the specified attachment is currently locked (for example by Office in
   * the case of a webdav online edition).
   * @param session the JCR session.
   * @param attachment the attachment.
   * @param language the language to obtain the file.
   * @return true if the node is locked - false otherwise.
   */
  public boolean isNodeLocked(Session session, AttachmentDetail attachment,
      String language) throws RepositoryException;

}