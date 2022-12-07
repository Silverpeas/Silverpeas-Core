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

import org.silverpeas.core.annotation.Service;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

/**
 * Default implementation of document template service interface.
 * <p>
 *   This implementation uses {@link DocumentTemplateRepository} for repository management.
 * </p>
 * @author silveryocha
 */
@Service
public class DefaultDocumentTemplateService implements DocumentTemplateService {

  @Inject
  private DocumentTemplateRepository repository;

  @Override
  public Optional<DocumentTemplate> getById(final String id) {
    return repository.getById(id);
  }

  @Override
  public DocumentTemplate put(final DocumentTemplate documentTemplate, final InputStream content) {
    if (documentTemplate.isPersisted()) {
      return repository.update(documentTemplate, content);
    } else {
      documentTemplate.setId(UUID.randomUUID().toString());
      if (documentTemplate.getPosition() < 0) {
        documentTemplate.setPosition(repository.streamAll()
            .map(DocumentTemplate::getPosition)
            .max(Comparator.comparing(i -> i))
            .orElse(-1) + 1);
      }
      return repository.create(documentTemplate, content);
    }
  }

  @Override
  public void remove(final DocumentTemplate documentTemplate) {
    repository.delete(documentTemplate);
  }

  @Override
  public List<DocumentTemplate> listAll() {
    return repository.streamAll()
        .sorted(comparing(DocumentTemplate::getPosition).thenComparing(DocumentTemplate::getId))
        .collect(Collectors.toList());
  }
}
