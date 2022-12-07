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

package org.silverpeas.core.webapi.attachment;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.attachment.AttachmentService;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.viewer.service.ViewerContext;
import org.silverpeas.core.webapi.viewer.ResourceView;
import org.silverpeas.core.webapi.viewer.ResourceViewProvider;
import org.silverpeas.core.webapi.viewer.ResourceViewProviderRegistry;

import javax.inject.Inject;
import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * Implementation of {@link ResourceViewProvider} dedicated to provide {@link SimpleDocument}
 * instances for viewer APIs.
 * @author silveryocha
 */
public class SimpleDocumentEmbedMediaViewProvider implements ResourceViewProvider, Initialization {

  @Inject
  private AttachmentService service;

  @Override
  public void init() throws Exception {
    ResourceViewProviderRegistry.get().addNewEmbedMediaProvider(this);
  }

  @Override
  public Optional<ResourceView> getByIdAndLanguage(final String documentId, final String language) {
    return ofNullable(service.searchDocumentById(new SimpleDocumentPK(documentId), language))
        .map(SimpleDocumentResourceView::new);
  }

  @Override
  public String relatedToService() {
    return "attachment";
  }

  private static class SimpleDocumentResourceView implements ResourceView {
    private final SimpleDocument document;

    private SimpleDocumentResourceView(final SimpleDocument document) {
      this.document = document;
    }

    @Override
    public String getId() {
      return document.getId();
    }

    @Override
    public String getName() {
      return document.getFilename();
    }

    @Override
    public String getContentType() {
      return document.getContentType();
    }

    @Override
    public ViewerContext getViewerContext() {
      return ViewerContext.from(document);
    }

    @Override
    public boolean canBeAccessedBy(final User user) {
      return document.canBeAccessedBy(user);
    }

    @Override
    public boolean isDownloadableBy(final User user) {
      return document.isDownloadAllowedForRolesFrom(user);
    }
  }
}
