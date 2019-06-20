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
package org.silverpeas.core.viewer.model;

import org.silverpeas.core.util.ImageUtil;
import org.silverpeas.core.util.file.FileServerUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.silverpeas.core.util.file.FileRepositoryManager.getTemporaryPath;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractPreview implements Preview {
  private static final long serialVersionUID = 3597757215012779572L;

  private final String documentId;
  private final String language;
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
   */
  AbstractPreview(final String documentId, final String language, final String originalFileName,
      final File physicalFile) {
    this.documentId = documentId;
    this.language = language;
    this.originalFileName = originalFileName;
    this.physicalFile = physicalFile;
  }

  @Override
  public String getDocumentId() {
    return this.documentId;
  }

  @Override
  public String getLanguage() {
    return this.language;
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

  @Override
  public Path getServerFilePath() {
    return Paths.get(getTemporaryPath(), getPhysicalFile().getParentFile().getName(),
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
