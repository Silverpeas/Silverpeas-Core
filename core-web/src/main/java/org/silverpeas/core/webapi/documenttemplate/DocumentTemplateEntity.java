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

package org.silverpeas.core.webapi.documenttemplate;

import org.silverpeas.core.documenttemplate.DocumentTemplate;
import org.silverpeas.core.viewer.service.PreviewService;
import org.silverpeas.core.web.rs.WebEntity;
import org.silverpeas.core.webapi.viewer.PreviewEntity;

import javax.validation.constraints.NotNull;
import java.net.URI;

/**
 * The {@link DocumentTemplateEntity} entity is a {@link DocumentTemplate} that is exposed in the
 * web as an entity (web entity). As such, it publishes only some of its attributes. It
 * represents a document template in Silverpeas.
 * @author silveryocha
 */
public class DocumentTemplateEntity implements WebEntity {
  private static final long serialVersionUID = 5543809634585798730L;

  private URI uri;

  @NotNull
  private String id;

  @NotNull
  private String name;

  @NotNull
  private String description;

  private PreviewEntity preview;

  public static DocumentTemplateEntity from(final DocumentTemplate documentTemplate, URI uri,
      String language) {
    return new DocumentTemplateEntity(documentTemplate, uri, language);
  }

  /**
   * Default constructor
   */
  protected DocumentTemplateEntity() {
  }

  /**
   * Constructor using {@link DocumentTemplate} and uri.
   * @param documentTemplate the document template data.
   * @param uri an URI.
   */
  public DocumentTemplateEntity(DocumentTemplate documentTemplate, URI uri, String language) {
    this.uri = uri;
    this.id = documentTemplate.getId();
    this.name = documentTemplate.getName(language);
    this.description = documentTemplate.getDescription(language);
    this.preview = PreviewEntity.createFrom(
        PreviewService.get().getPreview(documentTemplate.getViewerContext(language)));
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public PreviewEntity getPreview() {
    return preview;
  }

  @Override
  public URI getURI() {
    return uri;
  }
}
