package com.silverpeas.importExport.control;

import org.apache.commons.io.FilenameUtils;

import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.ResourceLocator;

public class ImportSettings {
  
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
      return FilenameUtils.getBaseName(filename);
    }
    return filename;
  }
  
}