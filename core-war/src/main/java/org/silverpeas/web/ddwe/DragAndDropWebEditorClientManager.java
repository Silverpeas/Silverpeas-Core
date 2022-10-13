/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

package org.silverpeas.web.ddwe;

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.wbe.WbeClientManager;
import org.silverpeas.core.wbe.WbeFile;
import org.silverpeas.core.wbe.WbeUser;

import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.silverpeas.core.contribution.content.ddwe.model.DragAndDropWebEditorStore.File.MIME_TYPE;

/**
 * Implementation of a client able to take in charge the edition into the Web Browser of HTML
 * content by Drag & Drop.
 * @author silveryocha
 */
@Service
public class DragAndDropWebEditorClientManager implements WbeClientManager {

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public boolean isHandled(final WbeFile file) {
    return MIME_TYPE.equals(file.mimeType());
  }

  @SuppressWarnings("unchecked")
  @Override
  public Optional<DragAndDropWebEditorEdition> prepareEditionWith(final WbeUser user,
      final WbeFile file) {
    return of(new DragAndDropWebEditorEdition(file, user));
  }

  @Override
  public Optional<String> getAdministrationUrl() {
    return empty();
  }

  @Override
  public void clear() {
    // nothing to clear
  }

  @Override
  public String getName(final String language) {
    return ResourceLocator
        .getLocalizationBundle("org.silverpeas.ddwe.multilang.ddwe", language)
        .getString("ddwe.client.name");
  }
}
