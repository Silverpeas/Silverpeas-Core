/**
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

package com.silverpeas.jobStartPagePeas;

import java.io.File;
import java.io.Serializable;

import com.silverpeas.util.FileUtil;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.FileServerUtils;

public class SpaceLookItem implements Serializable {

  private static final long serialVersionUID = 1L;
  private String name = null;
  private String size = null;
  private String url = null;

  public SpaceLookItem(File file, String spaceId) {
    name = file.getName();
    size = FileRepositoryManager.formatFileSize(file.length());

    url = FileServerUtils.getOnlineURL(spaceId, name, name, FileUtil.getMimeType(name), "look");
  }

  public String getName() {
    return name;
  }

  public String getSize() {
    return size;
  }

  public String getURL() {
    return url;
  }
}
