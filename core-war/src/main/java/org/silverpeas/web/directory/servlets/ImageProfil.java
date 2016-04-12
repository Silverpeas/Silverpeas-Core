/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.web.directory.servlets;

import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.io.file.SilverpeasFile;
import org.silverpeas.core.io.file.SilverpeasFileProvider;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ImageProfil {

  private String photoFileName;

  public ImageProfil(String photo) {
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
   * In case of unit upload
   * @param data
   * @throws IOException
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
      image.delete();
      image.getParentFile().delete(); // remove the directory in the case of a last avatar
    }
  }

  public InputStream getImage() throws IOException {
    SilverpeasFile image = SilverpeasFileProvider.getFile(getImagePath());
    return image.inputStream();
  }

  private String getImagePath() {
    return FileRepositoryManager.getAvatarPath() + File.separatorChar + photoFileName;
  }
}
