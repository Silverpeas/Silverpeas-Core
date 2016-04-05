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

package org.silverpeas.core.pdc.pdc.model;

import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.contribution.contentcontainer.content.GlobalSilverContent;
import org.silverpeas.core.index.search.model.MatchingIndexEntry;
import org.silverpeas.core.util.file.FileServerUtils;

import java.util.List;
import java.util.Map;

/**
 * This class allows the result jsp page of the global search to show all features (name,
 * description, location)
 */
public class GlobalSilverResult extends GlobalSilverContent implements java.io.Serializable {

  private static final long serialVersionUID = 1L;
  private String titleLink = null;
  private String downloadLink = null;
  private String creatorName = null;
  private boolean exportable = false;
  private boolean viewable = false;
  private boolean previewable = false;
  private String attachmentFilename = null;
  private boolean versioned = false;
  private boolean selected = false;
  private MatchingIndexEntry indexEntry = null;
  private boolean hasRead = false; // marks a result as redden
  private int resultId = 0;
  private int hits = -1;
  private String externalUrl = null;
  private boolean isDownloadAllowedForReaders = true;
  private boolean userAllowedToDownloadFile = true;

  /**
   * List of all linked attachment in wysiwyg content
   */
  private List<String> embeddedFileIds;

  private Map<String, String> formFieldsForFacets;

  public GlobalSilverResult(GlobalSilverContent gsc) {
    super(gsc.getName(), gsc.getDescription(), gsc.getId(), gsc.getSpaceId(),
        gsc.getInstanceId(), gsc.getDate(), gsc.getUserId());
    super.setLocation(gsc.getLocation());
    super.setURL(gsc.getURL());
    super.setScore(1);

    super.setThumbnailURL(gsc.getThumbnailURL());
  }

  public GlobalSilverResult(MatchingIndexEntry mie) {
    super(mie);
    indexEntry = mie;
    super.setType(mie.getObjectType());
    super.setScore(mie.getScore());
    this.embeddedFileIds = mie.getEmbeddedFileIds();
    this.formFieldsForFacets = mie.getXMLFormFieldsForFacets();
    this.attachmentFilename = mie.getFilename();

    if (mie.getThumbnail() != null) {
      if (mie.getThumbnail().startsWith("/")) {
        // case of a thumbnail picked up in a gallery
        super.setThumbnailURL(mie.getThumbnail());
      } else {
        // case of an uploaded image
        super.setThumbnailURL(FileServerUtils.getUrl(mie.getComponent(),
            mie.getThumbnail(), mie.getThumbnailMimeType(), mie.getThumbnailDirectory()));
      }
    }
  }

  /**
   * @return the embeddedFileIds
   */
  public List<String> getEmbeddedFileIds() {
    return embeddedFileIds;
  }

  public MatchingIndexEntry getIndexEntry() {
    return indexEntry;
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

  public void setCreatorName(String creatorName) {
    this.creatorName = creatorName;
  }

  public String getCreatorName() {
    return this.creatorName;
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

  public void setFormFieldsForFacets(Map<String, String> formFieldsForFacets) {
    this.formFieldsForFacets = formFieldsForFacets;
  }

  public Map<String, String> getFormFieldsForFacets() {
    return formFieldsForFacets;
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
    String id = getIndexEntry().getObjectType().substring(10); // object type is Attachment1245 or
    // Attachment1245_en
    if (id != null && id.indexOf('_') != -1) {
      id = id.substring(0, id.indexOf('_'));
    }
    return id;
  }

  public String getAttachmentLanguage() {
    String id = getIndexEntry().getObjectType().substring(10); // object type is Attachment1245 or
    // Attachment1245_en
    String language = I18NHelper.defaultLanguage;
    if (id != null && id.indexOf('_') != -1) {
      language = id.substring(id.indexOf('_') + 1, id.length());
    }
    return language;
  }

  public String getAttachmentFilename() {
    return attachmentFilename;
  }

  public void setVersioned(boolean versioned) {
    this.versioned = versioned;
  }

  public boolean isVersioned() {
    return versioned;
  }

  public boolean isAttachment() {
    return getIndexEntry().getObjectType().startsWith("Attachment") ||
        getIndexEntry().getObjectType().startsWith("Versioning");
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