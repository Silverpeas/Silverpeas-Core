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
package org.silverpeas.core.viewer.service;

import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.viewer.model.Preview;

import java.io.File;

/**
 * The preview service. Its goal is to generate a preview of a document.
 * @author Yohann Chastagnier
 */
public interface PreviewService {

  static PreviewService get() {
    return ServiceProvider.getService(PreviewService.class);
  }

  /**
   * Verifying if it is possible to obtain a preview of the given file.
   * @param file the file to verify.
   * @return true preview is possible, false otherwise.
   */
  boolean isPreviewable(File file);

  /**
   * Getting a Preview instance of the given file
   * @param viewerContext the context of the preview.
   * @return a {@link Preview} instance.
   */
  Preview getPreview(ViewerContext viewerContext);

  /**
   * Removes data about a preview from given context.
   * @param viewerContext the context of the preview.
   */
  void removePreview(ViewerContext viewerContext);
}
