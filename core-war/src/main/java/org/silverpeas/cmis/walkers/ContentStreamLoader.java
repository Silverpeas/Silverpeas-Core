/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Lib
 * Open Source Software ("FLOSS") applications as described in Silverpeas
 * FLOSS exception. You should have received a copy of the text describin
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public Licen
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package org.silverpeas.cmis.walkers;

import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStorageException;
import org.silverpeas.cmis.util.CmisProperties;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.kernel.logging.SilverLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class ContentStreamLoader {

  private final CmisProperties properties;
  private File file = null;

  /**
   * Constructs a new loader of a content stream for the object defined by the specified CMIS
   * properties.
   * @param properties CMIS properties of the object for which content streams will be loaded.
   */
  public ContentStreamLoader(final CmisProperties properties) {
    this.properties = properties;
  }

  /**
   * Loads the specified stream for a content and stores it into a file. The CMIS properties will be
   * updated with file information.
   * @param contentStream a stream on a content.
   * @return the file in which the content has been stored.
   */
  public File loadFile(ContentStream contentStream) {
    if (contentStream == null) {
      throw new CmisConstraintException("Content stream is required!");
    }
    try {
      String fileName = StringUtil.isDefined(contentStream.getFileName()) ?
          contentStream.getFileName() :
          properties.getName();

      file = File.createTempFile("cmis_", fileName);
      try (FileOutputStream output = new FileOutputStream(file);
           InputStream input = contentStream.getStream()) {
        input.transferTo(output);
      }
      properties.setContent(null, contentStream.getMimeType(), file.length(), fileName)
          .setContentPath(file.getAbsolutePath());
      return file;
    } catch (IOException e) {
      throw new CmisStorageException(e.getMessage());
    }
  }

  /**
   * Deletes the file into which a content has been loaded from a stream.
   */
  public void deleteFile() {
    if (file != null) {
      try {
        Files.deleteIfExists(file.toPath());
        file = null;
      } catch (IOException e) {
        SilverLogger.getLogger(this).silent(e);
      }
    }
  }

  /**
   * Gets the CMIS properties of the object for which this loaded has been created.
   * @return the CMIS properties of the object related by this loader.
   */
  public CmisProperties getProperties() {
    return properties;
  }
}
