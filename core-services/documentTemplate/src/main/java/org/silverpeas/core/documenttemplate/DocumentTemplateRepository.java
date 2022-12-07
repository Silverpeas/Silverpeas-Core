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
import java.util.Optional;
import java.util.stream.Stream;

/**
 * This interface defines the abilities a document template repository MUST provide.
 * @author silveryocha
 */
public interface DocumentTemplateRepository {

  /**
   * Gets the document template from given identifier.
   * @return an optional {@link DocumentTemplate} instance
   */
  Optional<DocumentTemplate> getById(final String id);

  /**
   * Lists all document template registered into repository.
   * @return a list of {@link DocumentTemplate} instance. The list is not sorted.
   */
  Stream<DocumentTemplate> streamAll();

  /**
   * Creates the given document template into the repository.
   * @param documentTemplate the {@link DocumentTemplate} instance to take into account.
   * @param content the content of the document template.
   * @return the document template created into Silverpeas's context.
   * @throws DocumentTemplateRuntimeException if the document template already exists.
   */
  DocumentTemplate create(DocumentTemplate documentTemplate, final InputStream content);

  /**
   * Updates the given document template into the repository.
   * @param documentTemplate the {@link DocumentTemplate} instance to take into account.
   * @param content the content of the document template.
   * @return the document template updated into Silverpeas's context.
   * @throws DocumentTemplateRuntimeException if the document template is not found against its
   * {@link DocumentTemplate#getId()} value.
   */
  DocumentTemplate update(DocumentTemplate documentTemplate, final InputStream content);

  /**
   * Deletes the given document template from the repository.
   * @param documentTemplate the {@link DocumentTemplate} instance to take into account.
   * @throws DocumentTemplateRuntimeException if the document template is not found against its
   * {@link DocumentTemplate#getId()} value.
   */
  void delete(DocumentTemplate documentTemplate);
}
