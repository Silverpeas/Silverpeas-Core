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
package org.silverpeas.core.viewer.model;

import org.silverpeas.core.util.file.FileServerUtils;
import org.silverpeas.core.util.ImageUtil;

import java.io.File;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractPreview implements Preview {
  private static final long serialVersionUID = 3597757215012779572L;

  private final String originalFileName;
  private final File physicalFile;
  private String[] widthAndHeight = null;

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.viewer.model.Preview#getDisplayLicenseKey()
   */
  @Override
  public String getDisplayLicenseKey() {
    return "";
  }

  /**
   * Default constructor
   * @param originalFileName
   * @param physicalFile
   */
  protected AbstractPreview(final String originalFileName, final File physicalFile) {
    this.originalFileName = originalFileName;
    this.physicalFile = physicalFile;
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.viewer.model.Preview#getOriginalFileName()
   */
  @Override
  public String getOriginalFileName() {
    return originalFileName;
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.viewer.model.Preview#getPhysicalFile()
   */
  @Override
  public File getPhysicalFile() {
    return physicalFile;
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.viewer.model.Preview#getWidth()
   */
  @Override
  public String getWidth() {
    return getWidthAndHeight()[0];
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.viewer.model.Preview#getHeight()
   */
  @Override
  public String getHeight() {
    return getWidthAndHeight()[1];
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.viewer.model.Preview#getURLAsString()
   */
  @Override
  public String getURLAsString() {
    return FileServerUtils.getUrlToTempDir(getPhysicalFile().getParentFile().getName() + "/" +
        getPhysicalFile().getName());
  }

  /**
   * Centralized method providing width and height of a preview
   * @return
   */
  private String[] getWidthAndHeight() {
    if (widthAndHeight == null) {
      widthAndHeight = ImageUtil.getWidthAndHeight(physicalFile);
    }
    return widthAndHeight;
  }
}
