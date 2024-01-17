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

import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.documenttemplate.DocumentTemplate;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.web.SilverpeasWebResource;

import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * Base URIs from which the REST-based ressources representing MyLinks entities are defined.
 * @author silveryocha
 */
@Bean
@Singleton
public class DocumentTemplateResourceURIs {

  public static final String DOC_TEMPLATE_BASE_URI = "documentTemplates";

  public static DocumentTemplateResourceURIs get() {
    return ServiceProvider.getSingleton(DocumentTemplateResourceURIs.class);
  }

  /**
   * Centralizes the build of a document template URI.
   * @param documentTemplate a document template.
   * @return the computed URI.
   */
  public URI ofDocumentTemplate(final DocumentTemplate documentTemplate) {
    if (documentTemplate == null || !documentTemplate.isPersisted()) {
      return null;
    }
    return getBase().path(documentTemplate.getId()).build();
  }

  private UriBuilder getBase() {
    return SilverpeasWebResource.getBasePathBuilder().path(DOC_TEMPLATE_BASE_URI);
  }

  private DocumentTemplateResourceURIs() {
  }
}
