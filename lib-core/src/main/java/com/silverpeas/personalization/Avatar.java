/**
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

package com.silverpeas.personalization;

import com.silverpeas.util.FileUtil;
import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;
import org.silverpeas.file.SilverpeasFileDescriptor;
import org.silverpeas.file.SilverpeasFileProvider;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Avatar {

  private static final ResourceLocator settings =
      new ResourceLocator("org.silverpeas.lookAndFeel.generalLook", "");

  private String avatarFileName;

  public Avatar(String fileName) {
    this.avatarFileName = fileName;
  }

  public boolean isImage() {
    try {
      MimeType type = new MimeType(FileUtil.getMimeType(avatarFileName));
      return "image".equalsIgnoreCase(type.getPrimaryType());
    } catch (MimeTypeParseException e) {
      return false;
    }
  }

  public String getImageUrl() {
      String size = settings.getString("image.size.avatar");
      return getResizedImageUrl(size);
  }

  public String getResizedImageUrl(String size) {
    String url = null;
    SilverpeasFileProvider provider = SilverpeasFileProvider.getInstance();
    File image = provider.getDirectSilverpeasFile(getImageFileDescriptor());
    if (image.exists()) {
      if (StringUtil.isDefined(size)) {
        url = "/display/avatar/" + size + "/" + avatarFileName;
      }
    }
    if (url == null) {
      url = "/directory/jsp/icons/avatar.png";
    }
    return url;
  }

  protected SilverpeasFileDescriptor getImageFileDescriptor() {
    String path = FileRepositoryManager.getAvatarPath() + File.separatorChar + avatarFileName;
    return new SilverpeasFileDescriptor().fileName(path).absolutePath();
  }

}
