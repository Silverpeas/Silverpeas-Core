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

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Provider;
import org.silverpeas.core.documenttemplate.DocumentTemplate;
import org.silverpeas.core.documenttemplate.DocumentTemplateService;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.viewer.service.ViewerContext;
import org.silverpeas.core.webapi.viewer.ResourceView;
import org.silverpeas.core.webapi.viewer.ResourceViewProvider;
import org.silverpeas.core.webapi.viewer.ResourceViewProviderRegistry;

import javax.inject.Inject;
import java.util.Optional;

/**
 * Implementation of {@link ResourceViewProvider} dedicated to provide {@link DocumentTemplate}
 * instances for viewer APIs.
 * @author silveryocha
 */
@Provider
public class DocumentTemplateResourceViewProvider
    implements ResourceViewProvider, Initialization {

  @Inject
  private DocumentTemplateService service;

  @Override
  public void init() throws Exception {
    ResourceViewProviderRegistry.get().addNewEmbedMediaProvider(this);
  }

  @Override
  public Optional<ResourceView> getByIdAndLanguage(final String mediaId, final String language) {
    return service.getById(mediaId).map(t -> new DocumentTemplateResourceView(t, language));
  }

  @Override
  public String relatedToService() {
    return "documentTemplate";
  }

  private static class DocumentTemplateResourceView implements ResourceView {

    private final DocumentTemplate template;
    private final String language;

    private DocumentTemplateResourceView(final DocumentTemplate template, final String language) {
      this.template = template;
      this.language = language;
    }

    @Override
    public String getId() {
      return template.getId();
    }

    @Override
    public String getName() {
      return template.getName(language);
    }

    @Override
    public String getContentType() {
      return template.getContentType();
    }

    @Override
    public ViewerContext getViewerContext() {
      return template.getViewerContext(language);
    }

    @Override
    public boolean canBeAccessedBy(final User user) {
      return true;
    }

    @Override
    public boolean isDownloadableBy(final User user) {
      return false;
    }
  }
}
