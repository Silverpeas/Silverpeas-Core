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

package org.silverpeas.core.documenttemplate;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * Services dedicated to document template manipulations.
 * @author silveryocha
 */
public interface DocumentTemplateService {

  /**
   * Gets the document template from given identifier.
   * @return an optional {@link DocumentTemplate} instance
   */
  Optional<DocumentTemplate> getById(final String id);

  /**
   * Puts the given document template into Silverpeas's context.
   * <p>
   * If document template does not yet exists (no identifier), then the document template is
   * created into repository (a new identifier is automatically generated).
   * </p>
   * <p>
   * If the document template does already exist (id is defined), then the document template is
   * updated into repository.
   * </p>
   * @param documentTemplate a document template representation.
   * @param content an {@link InputStream} instance over the document template content. This
   * stream is optional in case of an update.
   * @return the document template putted into Silverpeas's context.
   * @throws DocumentTemplateRuntimeException in case of data integrity error.
   */
  DocumentTemplate put(DocumentTemplate documentTemplate, final InputStream content);

  /**
   * Removes the given document template from the Silverpeas's context.
   * @param documentTemplate a document template representation.
   * @throws DocumentTemplateRuntimeException in case of data integrity error.
   */
  void remove(DocumentTemplate documentTemplate);

  /**
   * List all the document templates from Silverpeas's context sorted on
   * {@link DocumentTemplate#getPosition()} value.
   * @return a list of {@link DocumentTemplate} instance.
   */
  List<DocumentTemplate> listAll();
}
