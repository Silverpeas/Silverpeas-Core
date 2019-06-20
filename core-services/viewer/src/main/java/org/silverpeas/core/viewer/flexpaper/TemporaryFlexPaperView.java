/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.viewer.flexpaper;

import org.silverpeas.core.util.DocumentInfo;
import org.silverpeas.core.viewer.model.AbstractView;

import java.io.File;

/**
 * @author Yohann Chastagnier
 */
public class TemporaryFlexPaperView extends AbstractView {
  private static final long serialVersionUID = 2467847236159432664L;

  private final int width;
  private final int height;

  /**
   * Default constructor
   */
  public TemporaryFlexPaperView(final String documentId, final String language,
      final String originalFileName, final File physicalFile, final DocumentInfo info) {
    super(documentId, language, originalFileName, physicalFile, info.getNbPages());
    width = info.getMaxWidth();
    height = info.getMaxHeight();
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.viewer.model.AbstractPreview#getWidth()
   */
  @Override
  public String getWidth() {
    return String.valueOf(width);
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.viewer.model.AbstractPreview#getHeight()
   */
  @Override
  public String getHeight() {
    return String.valueOf(height);
  }
}
