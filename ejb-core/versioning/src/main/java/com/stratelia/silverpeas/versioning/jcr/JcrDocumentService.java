/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.silverpeas.versioning.jcr;

import javax.jcr.RepositoryException;

import com.stratelia.silverpeas.versioning.model.DocumentVersion;

public interface JcrDocumentService {

  public void createDocument(DocumentVersion document);

  public void getUpdatedDocument(DocumentVersion document);

  public void deleteDocument(DocumentVersion document);

  public void updateDocument(DocumentVersion document);

  /**
   * Indicate if the node for the specified attachment is currently locked (for example by Office in
   * the case of a webdav online edition).
   * @param attachment the attachment.
   * @param language the language to obtain the file.
   * @return true if the node is locked - false otherwise.
   * @throws RepositoryException
   */
  public boolean isNodeLocked(DocumentVersion document);

}