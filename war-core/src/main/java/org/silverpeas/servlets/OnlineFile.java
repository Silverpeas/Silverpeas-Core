/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.servlets;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class OnlineFile {

  private String mimeType;
  private String sourceFile;
  private String directory;
  private String componentId;

  public OnlineFile(String mimeType, String sourceFile, String directory, String componentId) {
    this.mimeType = mimeType;
    this.sourceFile = sourceFile;
    this.directory = directory;
    this.componentId = componentId;
  }

  public String getMimeType() {
    return mimeType;
  }

  public String getSourceFile() {
    return sourceFile;
  }

  public String getComponentId() {
    return componentId;
  }
  public File getContentFile() throws IOException {
    String filePath = FileRepositoryManager.getAbsolutePath(componentId) + directory
        + File.separator + sourceFile;
    File realFile = new File(filePath);
    if (!realFile.exists() && !realFile.isFile()) {
      throw new FileNotFoundException("File " + filePath + "not found!");
    }
    SilverTrace.info("peasUtil", "OnlineFile.write()", "root.MSG_GEN_ENTER_METHOD", " file "
        + filePath);
   return realFile;
  }
}
