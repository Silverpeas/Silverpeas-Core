/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.contribution.attachment.webdav;

import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.webdav.impl.WebdavContentDescriptor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

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
   */
  String getContentEditionLanguage(SimpleDocument document);

  /**
   * Gets the current content edition size of the specified attachment.
   * If several webdav document exists (several content languages), then the one which has the
   * highest modified date is taken into account.
   * @param document the attachment.
   * @return the content edition size if the specified attachment exists in the webdav
   * repository, -1 otherwise.
   */
  long getContentEditionSize(SimpleDocument document);

  /**
   * Gets the current webdav descriptor of the specified attachment.
   * If several webdav document exists (several content languages), then the one which has the
   * highest modified date is taken into account.
   * @param document the attachment.
   * @return the optional content edition webdav descriptor if the specified attachment exists in
   * the webdav repository.
   */
  Optional<WebdavContentDescriptor> getDescriptor(SimpleDocument document);

  /**
   * Updates a document content into the WEBDAV repository.
   * <p>
   *  If several webdav document exists (several content languages), then the one which has the
   *  highest modified date is taken into account.
   * </p>
   * @param document the aimed document.
   * @param input the data to write.
   * @throws IOException when it is not possible to write physically the data.
   */
  void updateContentFrom(final SimpleDocument document, final InputStream input) throws IOException;

  /**
   * Loads a document content from the WEBDAV repository and writes it into given output.
   * <p>
   *  If several webdav document exists (several content languages), then the one which has the
   *  highest modified date is taken into account.
   * </p>
   * @param document the aimed document.
   * @param output the stream to write into.
   * @throws IOException when it is not possible to write physically the data.
   */
  void loadContentInto(final SimpleDocument document, final OutputStream output) throws IOException;

  /**
   * Remove the lock on the given document set by the office editor.
   * <p>
   *   Nothing is done if the document is already unlocked.
   * </p>
   * @param document the locked document.
   */
  void unlockOfficeEditor(SimpleDocument document);
}