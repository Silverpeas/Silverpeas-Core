/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.webapi.attachment;

import org.silverpeas.core.contribution.attachment.AttachmentException;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.rs.WebEntity;

import javax.xml.bind.annotation.XmlElement;
import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 * @author ehugonnet
 */
public class SimpleDocumentEntity implements WebEntity {

  private static final long serialVersionUID = 6578990825699318566L;
  @XmlElement(required = true)
  private String id;
  @XmlElement(required = true)
  private Long spId;
  @XmlElement(required = true)
  private String instanceId;
  @XmlElement(required = true)
  private String fileName;
  @XmlElement(defaultValue = "")
  private String description;
  @XmlElement(defaultValue = "")
  private String contentType;
  @XmlElement(defaultValue = "0")
  private long creationDate;
  @XmlElement(defaultValue = "")
  private String createdBy;
  @XmlElement(defaultValue = "0")
  private long updateDate;
  @XmlElement(defaultValue = "")
  private String updatedBy;
  @XmlElement(defaultValue = "0")
  private long size;
  @XmlElement(defaultValue = "")
  private String title;
  @XmlElement(defaultValue = "")
  private String lang;
  @XmlElement(required = true)
  private URI uri;
  @XmlElement(required = true)
  private String icon;
  @XmlElement(required = true)
  private String permalink;
  @XmlElement(defaultValue = "")
  private String downloadUrl;
  @XmlElement(defaultValue = "")
  private String comment;
  @XmlElement(defaultValue = "false")
  private String versioned;
  @XmlElement
  private Boolean prewiewable;
  @XmlElement
  private Boolean viewable;
  @XmlElement
  private Boolean displayAsContent;
  @XmlElement
  private Integer widthInPixel;
  @XmlElement
  private Integer heightInPixel;

  public static SimpleDocumentEntity fromAttachment(SimpleDocument document) {
    SimpleDocumentEntity entity = new SimpleDocumentEntity();
    try {
      entity.uri = new URI(URLUtil.getSimpleURL(URLUtil.URL_FILE, document.getId()));
    } catch (URISyntaxException e) {
      throw new AttachmentException("Couldn't build the URI to the attachment", e);
    }
    entity.id = document.getId();
    entity.spId = document.getOldSilverpeasId();
    entity.instanceId = document.getInstanceId();
    entity.fileName = document.getFilename();
    entity.description = document.getDescription();
    entity.size = document.getSize();
    entity.creationDate = document.getCreationDate().getTime();
    entity.createdBy = document.getCreatedBy();
    if (document.getLastUpdateDate() != null) {
      entity.updateDate = document.getLastUpdateDate().getTime();
    }
    entity.updatedBy = document.getUpdatedBy();
    entity.title = document.getTitle();
    entity.contentType = document.getContentType();
    entity.icon = document.getDisplayIcon();
    entity.permalink = URLUtil.getSimpleURL(URLUtil.URL_FILE, document.getId());
    entity.downloadUrl = document.getAttachmentURL();
    entity.lang = document.getLanguage();
    entity.comment = document.getComment();
    entity.versioned = String.valueOf(document.isVersioned());
    return entity;
  }

  /**
   * Sets a URI to this entity. With this URI, it can then be accessed through the Web.
   *
   * @param uri the web entity URI.
   * @return itself.
   */
  public SimpleDocumentEntity withURI(final URI uri) {
    this.uri = uri;
    return this;
  }

  @Override
  public URI getURI() {
    return this.uri;
  }

  public String getId() {
    return id;
  }

  public Long getSpId() {
    return spId;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public String getFileName() {
    return fileName;
  }

  public String getDescription() {
    return description;
  }

  public String getContentType() {
    return contentType;
  }

  public long getCreationDate() {
    return creationDate;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public long getUpdateDate() {
    return updateDate;
  }

  public String getUpdatedBy() {
    return updatedBy;
  }

  public long getSize() {
    return size;
  }

  public String getTitle() {
    return title;
  }

  public String getLang() {
    return lang;
  }

  public String getIcon() {
    return icon;
  }

  public String getPermalink() {
    return permalink;
  }

  public String getDownloadUrl() {
    return downloadUrl;
  }

  public String getComment() {
    return comment;
  }

  public String getVersioned() {
    return versioned;
  }

  public Boolean getPrewiewable() {
    return prewiewable;
  }

  public SimpleDocumentEntity prewiewable(final boolean prewiewable) {
    this.prewiewable = prewiewable;
    return this;
  }

  public Boolean getViewable() {
    return viewable;
  }

  public SimpleDocumentEntity viewable(final boolean viewable) {
    this.viewable = viewable;
    return this;
  }

  public Boolean getDisplayAsContent() {
    return displayAsContent;
  }

  public SimpleDocumentEntity displayAsContent(final boolean displayAsContent) {
    this.displayAsContent = displayAsContent;
    return this;
  }

  public Integer getWidthInPixel() {
    return widthInPixel;
  }

  public SimpleDocumentEntity widthInPixelOf(final Integer width) {
    this.widthInPixel = width;
    return this;
  }

  public Integer getHeightInPixel() {
    return heightInPixel;
  }

  public SimpleDocumentEntity heightInPixelOf(final Integer height) {
    this.heightInPixel = height;
    return this;
  }
}
