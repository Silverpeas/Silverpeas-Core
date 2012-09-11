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
package org.silverpeas.process.io.file;

import java.io.File;

import com.stratelia.webactiv.util.GeneralPropertiesManager;

/**
 * This enumeration represents all pathes that are handled by the transactional mechanism.
 * For each definition is associated the real handled path on the file system and the node path used
 * for internal mechanism file manipulations.
 * @author Yohann Chastagnier
 */
public enum FileBasePath {
  UPLOAD_PATH(GeneralPropertiesManager.getString("uploadsPath"), "~uploads~");

  private String path;
  private String ioNodeName;

  private FileBasePath(final String path, final String ioNodeName) {
    this.path = new File(path + "/").getPath();
    this.ioNodeName = ioNodeName;
  }

  /**
   * @return the path
   */
  public String getPath() {
    return path;
  }

  /**
   * @return the ioNodeName
   */
  public String getIoNodeName() {
    return ioNodeName;
  }
}
