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
package org.silverpeas.core.process.io.file;

import java.io.File;

import org.silverpeas.core.util.file.FileRepositoryManager;


/**
 * This enumeration represents all root pathes that are handled by Silverpeas.
 * For each definition is associated a real path on the file system and its assiocated node path.
 * The node path is used by <code>FileHandler</code> in case it has to be managed by this one.
 * @author Yohann Chastagnier
 */
public enum FileBasePath {
  UPLOAD_PATH(FileRepositoryManager.getUploadPath(), "~uploads~");


  private String path;
  private String handledNodeName;

  FileBasePath(final String path, final String handledNodeName) {
    this.path = new File(path).getPath();
    this.handledNodeName = handledNodeName;
  }

  /**
   * @return the path
   */
  public String getPath() {
    return path;
  }

  /**
   * @return the handledNodeName
   */
  public String getHandledNodeName() {
    return handledNodeName;
  }
}
