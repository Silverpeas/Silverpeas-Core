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

package com.stratelia.silverpeas.pdcPeas.model;

import com.silverpeas.util.ImageUtil;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.contentManager.GlobalSilverContent;
import org.silverpeas.search.searchEngine.model.MatchingIndexEntry;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.FileServerUtils;
import java.io.File;
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
  private String attachmentId = null;
  private boolean versioned = false;
  private boolean selected = false;
  private MatchingIndexEntry indexEntry = null;
  private boolean hasRead = false; // marks a result as redden
  private int resultId = 0;
  private int hits = -1;
  private String externalUrl = null;

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

    super.setThumbnailHeight(gsc.getThumbnailHeight());
    super.setThumbnailMimeType(gsc.getThumbnailMimeType());
    super.setThumbnailWidth(gsc.getThumbnailWidth());
    super.setThumbnailURL(gsc.getThumbnailURL());
  }

  public GlobalSilverResult(MatchingIndexEntry mie) {
    super(mie);
    indexEntry = mie;
    super.setType(mie.getObjectType());
    super.setScore(mie.getScore());
    this.embeddedFileIds = mie.getEmbeddedFileIds();
    this.formFieldsForFacets = mie.getXMLFormFieldsForFacets();

    if (mie.getThumbnail() != null) {
      File image;
      if (mie.getThumbnail().startsWith("/")) {
        // case of a thumbnail picked up in a gallery
        super.setThumbnailURL(mie.getThumbnail());

        // thumbnail URL is like
        // /silverpeas/GalleryInWysiwyg/dummy?ImageId=31&ComponentId=gallery6974&UseOriginal=true
        String url = mie.getThumbnail();
        url = url.substring(url.indexOf("?"));
        String[] parameters = url.split("&");

        String imageId = parameters[0].substring(parameters[0].indexOf("=") + 1);
        String componentId = parameters[1].substring(parameters[1].indexOf("=") + 1);

        String filePath =
            FileRepositoryManager.getAbsolutePath(componentId) + "image" + imageId +
            File.separator + imageId + "_preview.jpg";

        image = new File(filePath);
      } else {
        // case of an uploaded image
        super.setThumbnailURL(FileServerUtils.getUrl(mie.getComponent(),
            mie.getThumbnail(), mie.getThumbnailMimeType(), mie.getThumbnailDirectory()));

        String[] directory = new String[1];
        directory[0] = mie.getThumbnailDirectory();

        image = new File(FileRepositoryManager.getAbsolutePath(mie.getComponent(), directory)
            + mie.getThumbnail());
      }
      String[] dimensions = ImageUtil.getWidthAndHeightByWidth(image, 60);
      if (!StringUtil.isDefined(dimensions[0])) {
        dimensions[0] = "60";
        dimensions[1] = "45";
      }
      setThumbnailWidth(dimensions[0]);
      setThumbnailHeight(dimensions[1]);
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

  public void setAttachmentId(String attachmentId) {
    this.attachmentId = attachmentId;
  }

  public String getAttachmentId() {
    return attachmentId;
  }

  public void setVersioned(boolean versioned) {
    this.versioned = versioned;
  }

  public boolean isVersioned() {
    return versioned;
  }

  public boolean isAttachment() {
    return StringUtil.isDefined(getAttachmentId());
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