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
package org.silverpeas.core.webapi.viewer;

import org.silverpeas.core.viewer.model.DocumentView;
import org.silverpeas.core.viewer.service.ViewerException;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.silverpeas.core.webapi.viewer.DocumentViewGettingIT.ATTACHMENT_ID;

/**
 * @author Yohann Chastagnier
 */
public class DocumentViewBuilder {

  public static DocumentViewBuilder getDocumentViewBuilder() {
    return new DocumentViewBuilder();
  }

  public DocumentView buildFileName(final String uriId, final String fileName) {
    return new DocumentViewMock(uriId, fileName);
  }

  private DocumentViewBuilder() {
    // Nothing to do
  }

  protected class DocumentViewMock implements DocumentView {

    private final String uriId;
    private final String fileName;

    public DocumentViewMock(final String uriId, final String fileName) {
      this.uriId = uriId;
      this.fileName = fileName;
    }

    @Override
    public String getDocumentId() {
      return ATTACHMENT_ID;
    }

    @Override
    public String getDocumentType() {
      return "attachment";
    }

    @Override
    public String getLanguage() {
      return "fr";
    }

    @Override
    public Path getServerFilePath() {
      return Paths.get(fileName);
    }

    /*
     * (non-Javadoc)
     * @see org.silverpeas.core.viewer.model.DocumentView#getDisplayLicenseKey()
     */
    @Override
    public String getDisplayLicenseKey() {
      return "licenseKey";
    }

    /*
     * (non-Javadoc)
     * @see org.silverpeas.core.viewer.model.DocumentView#getURLAsString()
     */
    @Override
    public String getURLAsString() throws ViewerException {
      return "/URL/" + fileName;
    }

    /*
     * (non-Javadoc)
     * @see org.silverpeas.core.viewer.model.DocumentView#getAttachment()
     */
    @Override
    public File getPhysicalFile() {
      return new File("URI/" + uriId);
    }

    /*
     * (non-Javadoc)
     * @see org.silverpeas.core.viewer.model.DocumentView#getOriginalFileName()
     */
    @Override
    public String getOriginalFileName() {
      return fileName;
    }

    /*
     * (non-Javadoc)
     * @see org.silverpeas.core.viewer.model.DocumentView#getWidth()
     */
    @Override
    public String getWidth() {
      return null;
    }

    /*
     * (non-Javadoc)
     * @see org.silverpeas.core.viewer.model.DocumentView#getHeight()
     */
    @Override
    public String getHeight() {
      return null;
    }

    @Override
    public int getNbPages() {
      return 10;
    }

    @Override
    public boolean isDocumentSplit() {
      return true;
    }

    @Override
    public boolean areSearchDataComputed() {
      return true;
    }
  }
}
