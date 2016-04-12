/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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

import org.silverpeas.core.viewer.model.AbstractView;
import org.silverpeas.core.viewer.util.DocumentInfo;

import java.io.File;

/**
 * @author Yohann Chastagnier
 */
public class TemporaryFlexPaperView extends AbstractView {
  private static final long serialVersionUID = 2467847236159432664L;

  private int width = 0;
  private int height = 0;

  /**
   * Default constructor
   * @param physicalFile
   */
  public TemporaryFlexPaperView(final String originalFileName, final File physicalFile,
      final DocumentInfo info) {
    super(originalFileName, physicalFile, info.getNbPages());
    width = info.getMaxWidh();
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
