/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection withWriter Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
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
package com.silverpeas.importExport.control;

import org.apache.commons.io.FilenameUtils;

import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.ResourceLocator;

public class ImportSettings implements Cloneable {
  
  private static final ResourceLocator settings = new ResourceLocator(
      "org.silverpeas.importExport.settings.importSettings", "");
  public static final int FROM_XML = 0;
  public static final int FROM_DRAGNDROP = 1;
  public static final int FROM_MANUAL = 2;

  private String pathToImport;
  private UserDetail user;
  private String componentId;
  private String folderId;
  private boolean draftUsed;
  private boolean poiUsed;
  private boolean versioningUsed;
  private int versionType;
  private int method;
  
  public ImportSettings(String pathToImport, UserDetail user, String componentId, String folderId,
      boolean draftUsed, boolean poiUsed, int method) {
    super();
    this.pathToImport = pathToImport;
    this.user = user;
    this.componentId = componentId;
    this.folderId = folderId;
    this.draftUsed = draftUsed;
    this.poiUsed = poiUsed;
    this.method = method;
  }

  public String getPathToImport() {
    return pathToImport;
  }
  
  public void setPathToImport(String path) {
    pathToImport = path;
  }

  public UserDetail getUser() {
    return user;
  }

  public String getComponentId() {
    return componentId;
  }

  public String getFolderId() {
    return folderId;
  }
  
  public void setFolderId(String folderId) {
    this.folderId = folderId;
  }

  public boolean isDraftUsed() {
    return draftUsed;
  }

  public boolean isPoiUsed() {
    if (getMethod() == FROM_XML) {
      return poiUsed;
    } else if (getMethod() == FROM_DRAGNDROP) {
      return settings.getBoolean("dnd.publication.usePOI", true);
    } else {
      return settings.getBoolean("manual.publication.usePOI", true);
    }
  }

  public void setVersioningUsed(boolean versioningUsed) {
    this.versioningUsed = versioningUsed;
  }

  public boolean isVersioningUsed() {
    return versioningUsed;
  }

  public void setFrom(int from) {
    this.method = from;
  }

  public int getMethod() {
    return method;
  }
  
  public boolean isPublicationMergeEnabled() {
    if (getMethod() == FROM_XML) {
      return settings.getBoolean("xml.publication.merge", true);
    } else if (getMethod() == FROM_DRAGNDROP) {
      return settings.getBoolean("dnd.publication.merge", false);
    } else {
      return settings.getBoolean("manual.publication.merge", false);
    }
  }
  
  public String getPublicationName(String filename) {
    if (settings.getBoolean("publication.name.with.extension", false)) {
      return filename;
    }
    return FilenameUtils.getBaseName(filename);
  }

  public void setVersionType(int versionType) {
    this.versionType = versionType;
  }

  public int getVersionType() {
    return versionType;
  }
  
  public boolean useFileDates() {
    if (getMethod() == FROM_XML) {
      return settings.getBoolean("xml.publication.useFileDates", false);
    } else if (getMethod() == FROM_DRAGNDROP) {
      return settings.getBoolean("dnd.publication.useFileDates", false);
    } else {
      return settings.getBoolean("manual.publication.useFileDates", false);
    }
  }

  public ImportSettings clone() {
    return new ImportSettings(pathToImport, user, componentId, folderId, draftUsed, poiUsed, method);
  }
  
}