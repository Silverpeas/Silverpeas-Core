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

import java.io.File;

import static org.silverpeas.core.viewer.model.ViewerSettings.getLicenceKey;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractView extends AbstractPreview implements DocumentView {
  private static final long serialVersionUID = 7925552492746846823L;

  private int nbPages = 0;
  private boolean documentSplit = false;
  private boolean searchDataComputed = false;

  /**
   * Default constructor
   * @param originalFileName
   * @param physicalFile
   */
  protected AbstractView(final String originalFileName, final File physicalFile, final int nbPages) {
    super(originalFileName, physicalFile);
    this.nbPages = nbPages;
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.viewer.model.Preview#getDisplayLicenseKey()
   */
  @Override
  public String getDisplayLicenseKey() {
    return getLicenceKey();
  }

  /**
   * @return the nbPages
   */
  @Override
  public int getNbPages() {
    return nbPages;
  }

  @Override
  public boolean isDocumentSplit() {
    return documentSplit;
  }

  public void markDocumentSplit(final boolean documentSplit) {
    this.documentSplit = documentSplit;
  }

  @Override
  public boolean areSearchDataComputed() {
    return searchDataComputed;
  }

  public void markSearchDataComputed(final boolean searchDataComputed) {
    this.searchDataComputed = searchDataComputed;
  }
}
