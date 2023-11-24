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
package org.silverpeas.web.directory.servlets;

import org.silverpeas.core.io.file.SilverpeasFile;
import org.silverpeas.core.io.file.SilverpeasFileProvider;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class ImageProfile {

  private final String photoFileName;

  public ImageProfile(String photo) {
    this.photoFileName = photo;
  }

  public boolean isImage() {
    try {
      MimeType type = new MimeType(FileUtil.getMimeType(photoFileName));
      return "image".equalsIgnoreCase(type.getPrimaryType());
    } catch (MimeTypeParseException e) {
      return false;
    }
  }

  /**
   * Gets the MIME type of the image.
   * @return the MIME type of the image.
   */
  public String getMimeType() {
    try {
      final MimeType type = new MimeType(FileUtil.getMimeType(photoFileName));
      return type.toString();
    } catch (MimeTypeParseException e) {
      return MediaType.APPLICATION_OCTET_STREAM;
    }
  }

  /**
   * Saves the image in the specified input stream.
   * @param data an input stream on an image data.
   * @throws IOException if an error occurs while reading the input stream and saving the image.
   */
  public void saveImage(InputStream data) throws IOException {
    SilverpeasFile image = SilverpeasFileProvider.newFile(getImagePath());
    image.writeFrom(data);
  }

  /**
   * remove existing image
   */
  public void removeImage() {
    SilverpeasFile image = SilverpeasFileProvider.getFile(getImagePath());
    if (image.exists()) {
      try {
        Files.delete(image.toPath());
        // remove the directory in the case of a last avatar
        try(final Stream<Path> children = Files.list(image.getParentFile().toPath())) {
          boolean isParentEmpty = children.findAny().isPresent();
          if (isParentEmpty) {
            Files.delete(image.getParentFile().toPath());
          }
        }
      } catch (IOException e) {
        SilverLogger.getLogger(e).error(e);
      }

    }
  }

  public InputStream getImage() throws IOException {
    SilverpeasFile image = SilverpeasFileProvider.getFile(getImagePath());
    return image.inputStream();
  }

  public boolean exist() {
    return SilverpeasFileProvider.getFile(getImagePath()).exists();
  }

  private String getImagePath() {
    return FileRepositoryManager.getAvatarPath() + File.separatorChar + photoFileName;
  }
}
