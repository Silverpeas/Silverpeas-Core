/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.importexport.control;

import org.apache.commons.io.FilenameUtils;
import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;
import org.silverpeas.kernel.util.StringUtil;

import static org.silverpeas.core.contribution.attachment.util.AttachmentSettings.isUseFileMetadataForAttachmentDataEnabled;

public class ImportSettings implements Cloneable {

  private static final SettingBundle settings =
      ResourceLocator.getSettingBundle("org.silverpeas.importExport.settings.importSettings");
  public static final int FROM_XML = 0;
  public static final int FROM_DRAGNDROP = 1;
  public static final int FROM_MANUAL = 2;

  private String pathToImport;
  private final UserDetail user;
  private final String componentId;
  private String folderId;
  private final boolean draftUsed;
  private final boolean poiUsed;
  private boolean versioningUsed;
  private int versionType;
  private String versionComment;
  private int method;
  private String contentLanguage;
  private String targetValidatorIds;
  private final PublicationDetail publicationForAllFiles = PublicationDetail.builder().build();
  private String singleFileTitle;
  private String singleFileDescription;
  private final boolean useFileMetadata = isUseFileMetadataForAttachmentDataEnabled();
  private boolean fromDocumentTemplate;

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
    if (isFromDocumentTemplate()) {
      return false;
    } else if (getMethod() == FROM_XML) {
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
    if (isFromDocumentTemplate()) {
      return false;
    } else if (getMethod() == FROM_XML) {
      return settings.getBoolean("xml.publication.merge", true);
    } else if (getMethod() == FROM_DRAGNDROP) {
      return settings.getBoolean("dnd.publication.merge", false);
    } else {
      return settings.getBoolean("manual.publication.merge", false);
    }
  }

  /**
   * Indicates that it must be created one publication for all files.
   * <p>PLEASE notice that the creation of one publication for all files is compatible only with
   * {@link #getMethod()} returning {@link #FROM_DRAGNDROP}. The code has to be adjusted for other
   * processes.</p>
   * @return true if one publication only must be created.
   */
  public boolean mustCreateOnePublicationForAllFiles() {
    return StringUtil.isDefined(publicationForAllFiles.getName());
  }

  /**
   * Gets the instance of {@link PublicationDetail} that stores the following data in case of
   * creation of one publication for all imported files:
   * <ul>
   *   <li>the name of the publication. If it exists then the method {@link
   *   #mustCreateOnePublicationForAllFiles()} will indicate that all files will be attached to
   *   a same publication</li>
   *   <li>the description of a publication</li>
   *   <li>the keywords of a publication</li>
   * </ul>
   * @return a string that represents the publication description.
   */
  public PublicationDetail getPublicationForAllFiles() {
    return publicationForAllFiles;
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

  public void setVersionAndComment(int versionType, String comment) {
    this.versionType = versionType;
    this.versionComment = comment;
  }

  public int getVersionType() {
    return versionType;
  }

  public String getVersionComment() {
    return versionComment;
  }

  public String getSingleFileTitle() {
    return singleFileTitle;
  }

  public void setSingleFileTitle(final String singleFileTitle) {
    this.singleFileTitle = singleFileTitle;
  }

  public String getSingleFileDescription() {
    return singleFileDescription;
  }

  public void setSingleFileDescription(final String singleFileDescription) {
    this.singleFileDescription = singleFileDescription;
  }

  public boolean isUseFileMetadata() {
    return !isFromDocumentTemplate() && useFileMetadata;
  }

  public String getContentLanguage() {
    return contentLanguage;
  }

  public void setContentLanguage(final String contentLanguage) {
    this.contentLanguage = contentLanguage;
  }

  public String getTargetValidatorIds() {
    return targetValidatorIds;
  }

  public void setTargetValidatorIds(final String targetValidatorIds) {
    this.targetValidatorIds = targetValidatorIds;
  }

  public boolean isFromDocumentTemplate() {
    return fromDocumentTemplate;
  }

  public void setFromDocumentTemplate(final boolean fromDocumentTemplate) {
    this.fromDocumentTemplate = fromDocumentTemplate;
  }

  public boolean useFileDates() {
    if (isFromDocumentTemplate()) {
      return false;
    } else if (getMethod() == FROM_XML) {
      return settings.getBoolean("xml.publication.useFileDates", false);
    } else if (getMethod() == FROM_DRAGNDROP) {
      return settings.getBoolean("dnd.publication.useFileDates", false);
    } else {
      return settings.getBoolean("manual.publication.useFileDates", false);
    }
  }

  @Override
  public ImportSettings clone() {
    try {
      return (ImportSettings) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }
}