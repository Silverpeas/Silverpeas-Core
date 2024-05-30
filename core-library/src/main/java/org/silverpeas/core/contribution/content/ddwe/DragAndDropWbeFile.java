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

package org.silverpeas.core.contribution.content.ddwe;

import org.apache.commons.io.IOUtils;
import org.silverpeas.core.ComponentResourceIdentifier;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.content.ddwe.model.DragAndDropWebEditorStore;
import org.silverpeas.core.contribution.content.ddwe.model.DragAndDropWebEditorStore.Content;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.wbe.SimpleWbeFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.silverpeas.kernel.util.StringUtil.EMPTY;

/**
 * This class represents a file content dedicated to the registering of Drag&Drop web editor
 * contents.
 * <p>
 * This file representation permits to such content to be taken in charge by WBE services
 * and so to be edited by web browser editors.
 * </p>
 * @author silveryocha
 */
public class DragAndDropWbeFile extends SimpleWbeFile {

  private final DragAndDropWebEditorStore store;

  public DragAndDropWbeFile(final DragAndDropWebEditorStore store) {
    super(store.getFile());
    this.store = store;
  }

  public Optional<ContributionIdentifier> linkedToContribution() {
    return Optional.of(getStore())
        .map(DragAndDropWebEditorStore::getContributionId);
  }

  @Override
  public Optional<ResourceReference> linkedToResource() {
    return linkedToContribution()
        .map(ComponentResourceIdentifier::toReference);
  }

  @Override
  public String id() {
    return StringUtil.asBase64(("ddwe-" + super.id()).getBytes(Charsets.UTF_8)).replace("=", "-");
  }

  @Override
  public String silverpeasId() {
    return getStore().getIdentifier().asString();
  }

  /**
   * Gets the unique identifier of the component instance within which this WBE file is managed.
   * @return the unique identifier of a component instance.
   */
  public String componentInstanceId() {
    return getStore().getContributionId().getComponentInstanceId();
  }

  @Override
  public User owner() {
    return getStore().getFile().getContainer().getTmpContent()
        .map(Content::getMetadata)
        .map(Content.Metadata::getLastUpdatedBy)
        .map(User::getById)
        .orElse(null);
  }

  @Override
  public String name() {
    return getStore().getName();
  }

  @Override
  public String mimeType() {
    return getStore().getFile().getMimeType();
  }

  @Override
  public void updateFrom(final InputStream input) throws IOException {
    synchronized (MUTEX) {
      final String exContentValue = IOUtils.toString(input, UTF_8);
      final Content spContent = getStore().getFile().getContainer().getOrCreateTmpContent();
      spContent.setValue(exContentValue);
      getStore().save();
    }
  }

  @Override
  public void loadInto(final OutputStream output) throws IOException {
    synchronized (MUTEX) {
      final Optional<String> tmpContent = getTemporaryContent();
      if (tmpContent.isPresent()) {
        try (final StringReader in = new StringReader(tmpContent.get())) {
          IOUtils.copy(in, output, UTF_8);
        }
      }
    }
  }

  public Optional<String> getTemporaryContent() {
    return getStore().getFile().getContainer().getTmpContent().map(Content::getValue);
  }

  public Optional<String> getFinalContent() {
    return getStore().getFile().getContainer().getContent().map(Content::getValue);
  }

  /**
   * Resets the temporary content with the final one.
   * <p>
   * If no final content exists, then the temporary content if exists is set to empty value or
   * nothing is performed if no temporary content exists.
   * </p>
   */
  public void resetTemporaryContent() {
    synchronized (MUTEX) {
      final DragAndDropWebEditorStore.Container container = getStore().getFile().getContainer();
      container.getContent()
          .map(Content::getValue)
          .ifPresentOrElse(v -> container.getOrCreateTmpContent().setValue(v),
              () -> container.getTmpContent().ifPresent(t -> t.setValue(EMPTY)));
    }
  }

  @Override
  public boolean canBeModifiedBy(final User user) {
    return true;
  }

  private DragAndDropWebEditorStore getStore() {
    return store;
  }
}
