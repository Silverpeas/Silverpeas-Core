/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.viewer.web;

import java.io.File;

import org.silverpeas.viewer.Preview;
import org.silverpeas.viewer.exception.PreviewException;

/**
 * @author Yohann Chastagnier
 */
public class PreviewBuilder {

  public static PreviewBuilder getPreviewBuilder() {
    return new PreviewBuilder();
  }

  public Preview buildFileName(final String uriId, final String fileName) {
    return new PreviewMock(uriId, fileName);
  }

  private PreviewBuilder() {
    // Nothing to do
  }

  protected class PreviewMock implements Preview {

    private final String uriId;
    private final String fileName;

    public PreviewMock(final String uriId, final String fileName) {
      this.uriId = uriId;
      this.fileName = fileName;
    }

    /*
     * (non-Javadoc)
     * @see org.silverpeas.viewer.Preview#getDisplayLicenseKey()
     */
    @Override
    public String getDisplayLicenseKey() {
      return null;
    }

    /*
     * (non-Javadoc)
     * @see org.silverpeas.viewer.Preview#getURLAsString()
     */
    @Override
    public String getURLAsString() throws PreviewException {
      return "/URL/" + fileName;
    }

    /*
     * (non-Javadoc)
     * @see org.silverpeas.viewer.Preview#getFile()
     */
    @Override
    public File getPhysicalFile() {
      return new File("URI/" + uriId);
    }

    /*
     * (non-Javadoc)
     * @see org.silverpeas.viewer.Preview#getOriginalFileName()
     */
    @Override
    public String getOriginalFileName() {
      return null;
    }

    /*
     * (non-Javadoc)
     * @see org.silverpeas.viewer.Preview#getWidth()
     */
    @Override
    public String getWidth() {
      return null;
    }

    /*
     * (non-Javadoc)
     * @see org.silverpeas.viewer.Preview#getHeight()
     */
    @Override
    public String getHeight() {
      return null;
    }
  }
}
