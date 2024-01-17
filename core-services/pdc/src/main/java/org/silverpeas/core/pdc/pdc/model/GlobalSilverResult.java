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
package org.silverpeas.core.pdc.pdc.model;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.index.search.model.SearchResult;
import org.silverpeas.core.util.StringUtil;

import java.time.LocalDate;
import java.util.Map;

/**
 * This class allows the result jsp page of the global search to show all features (name,
 * description, location)
 */
public class GlobalSilverResult implements java.io.Serializable {

  private final SearchResult result;
  private String titleLink = null;
  private String downloadLink = null;
  private String location;
  private boolean exportable = false;
  private boolean viewable = false;
  private boolean previewable = false;
  private boolean versioned = false;
  private boolean selected = false;
  private boolean hasRead = false; // marks a result as read
  private int resultId = 0;
  private int hits = -1;
  private String externalUrl = null;
  private boolean isDownloadAllowedForReaders = true;
  private boolean userAllowedToDownloadFile = true;

  public GlobalSilverResult(SearchResult result) {
    this.result = result;
  }

  public String getId() {
    return result.getId();
  }

  public String getInstanceId() {
    return result.getInstanceId();
  }

  public String getType() {
    return result.getType();
  }

  public String getCreatorId() {
    return result.getCreatorId();
  }

  public boolean isExternalResult() {
    return result.isExternalResult();
  }

  public String getAttachmentFilename() {
    return result.getAttachmentFilename();
  }

  public Map<String, String> getFormFieldsForFacets() {
    return result.getFormFieldsForFacets();
  }

  public String getServerName() {
    return result.getServerName();
  }

  public String getTitleLink() {
    return titleLink;
  }

  public void setTitleLink(String link) {
    this.titleLink = link;
  }

  public String getDownloadLink() {
    return downloadLink;
  }

  public void setDownloadLink(String link) {
    this.downloadLink = link;
  }

  public String getCreatorName() {
    User user = User.getById(getCreatorId());
    if (user == null) {
      return "";
    }
    return user.getDisplayedName();
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(final String location) {
    this.location = location;
  }

  public boolean isExportable() {
    return exportable;
  }

  public void setExportable(boolean exportable) {
    this.exportable = exportable;
  }

  public boolean isSelected() {
    return selected;
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  /**
   * indicates if a entry has been read
   * @return the hasRead
   */
  public boolean isHasRead() {
    return hasRead;
  }

  /**
   * @param hasRead the hasRead to set
   */
  public void setHasRead(boolean hasRead) {
    this.hasRead = hasRead;
  }

  public int getResultId() {
    return resultId;
  }

  public void setResultId(int resultId) {
    this.resultId = resultId;
  }

  public void setHits(int hits) {
    this.hits = hits;
  }

  public int getHits() {
    return hits;
  }

  /**
   * @return the externalUrl
   */
  public String getExternalUrl() {
    return externalUrl;
  }

  /**
   * @param externalUrl the externalUrl to set
   */
  public void setExternalUrl(String externalUrl) {
    this.externalUrl = externalUrl;
  }

  public void setViewable(boolean viewable) {
    this.viewable = viewable;
  }

  public boolean isViewable() {
    return viewable;
  }

  public void setPreviewable(boolean previewable) {
    this.previewable = previewable;
  }

  public boolean isPreviewable() {
    return previewable;
  }

  public String getAttachmentId() {
    String id = getType().substring(10); // object type is Attachment1245 or
    // Attachment1245_en
    if (id.indexOf('_') != -1) {
      id = id.substring(0, id.indexOf('_'));
    }
    return id;
  }

  public String getAttachmentLanguage() {
    String id = getType().substring(10); // object type is Attachment1245 or
    // Attachment1245_en
    String language = I18NHelper.DEFAULT_LANGUAGE;
    if (id.indexOf('_') != -1) {
      language = id.substring(id.indexOf('_') + 1);
    }
    return language;
  }

  public void setVersioned(boolean versioned) {
    this.versioned = versioned;
  }

  public boolean isVersioned() {
    return versioned;
  }

  public boolean isAttachment() {
    return getType().startsWith("Attachment") || getType().startsWith("Versioning");
  }

  public boolean isUserAllowedToDownloadFile() {
    return userAllowedToDownloadFile;
  }

  public void setUserAllowedToDownloadFile(final boolean userAllowedToDownloadFile) {
    this.userAllowedToDownloadFile = userAllowedToDownloadFile;
  }

  public boolean isDownloadAllowedForReaders() {
    return isDownloadAllowedForReaders;
  }

  public void setDownloadAllowedForReaders(final boolean isDownloadAllowedForReaders) {
    this.isDownloadAllowedForReaders = isDownloadAllowedForReaders;
  }

  public void setName(String name) {
    result.setName(name);
  }

  public String getName() {
    return result.getName();
  }

  public String getDescription() {
    return result.getDescription();
  }

  public String getName(String lang) {
    return result.getName(lang);
  }

  public String getDescription(String lang) {
    return result.getDescription(lang);
  }

  public String getThumbnailURL() {
    return result.getThumbnailURL();
  }

  public void setThumbnailURL(String url) {
    result.setThumbnailURL(url);
  }

  public float getScore() {
    return result.getScore();
  }

  public LocalDate getCreationDate() {
    return result.getCreationLocalDate();
  }

  public LocalDate getLastUpdateDate() {
    return result.getLastUpdateLocalDate();
  }

  public String getSpaceId() {
    return StringUtil.EMPTY;
  }

  public boolean isAlias() {
    return result.isAlias();
  }

  public boolean isNew() {
    return result.isNew();
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof GlobalSilverResult)) {
      return false;
    }
    return (getId().equals(((GlobalSilverResult) other).getId()))
        && (getInstanceId().equals(((GlobalSilverResult) other).getInstanceId()));
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 29 * hash + (this.getId() != null ? this.getId().hashCode() : 0);
    hash = 29 * hash + (this.getInstanceId() != null ? this.getInstanceId().hashCode() : 0);
    return hash;
  }

}