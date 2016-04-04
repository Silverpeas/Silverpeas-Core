/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.contribution.attachment.webdav;

import org.silverpeas.core.contribution.attachment.model.SimpleDocument;

public interface WebdavService {

  /**
   * Update the document content language with the data from the associated webdav node.
   * @param document the document for which the content language will be updated with data from
   * webdav.
   */
  void updateDocumentContent(SimpleDocument document);

  /**
   * Gets the current content edition language of the specified attachment.
   * If several webdav document exists (several content languages), then the one which has the
   * highest modified date is taken into account.
   * @param document the attachment.
   * @return the content edition language if the specified attachment exists in the webdav
   * repository, null otherwise.
   * @throws javax.jcr.RepositoryException
   */
  String getContentEditionLanguage(SimpleDocument document);

  /**
   * Gets the current content edition size of the specified attachment.
   * If several webdav document exists (several content languages), then the one which has the
   * highest modified date is taken into account.
   * @param document the attachment.
   * @return the content edition size if the specified attachment exists in the webdav
   * repository, -1 otherwise.
   * @throws javax.jcr.RepositoryException
   */
  long getContentEditionSize(SimpleDocument document);
}