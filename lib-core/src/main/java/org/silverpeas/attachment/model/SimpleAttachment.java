/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
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
package org.silverpeas.attachment.model;

import com.silverpeas.util.i18n.I18NHelper;
import java.util.Date;

/**
 *
 * @author ehugonnet
 */
public class SimpleAttachment {

  private String filename;
  private String language = I18NHelper.defaultLanguage;
  private String title;
  private String description;
  private long size;
  private String contentType;
  private String createdBy;
  private Date created;
  private String updatedBy;
  private Date updated;
  private int minorVersion = 0;
  private int majorVersion = 1;
  private String xmlFormId;

  public SimpleAttachment(String filename, String language, String title, String description,
      long size, String contentType, String createdBy, Date created, String xmlFormId) {
    this.filename = filename;
    this.language = language;
    this.title = title;
    this.description = description;
    this.size = size;
    this.contentType = contentType;
    this.createdBy = createdBy;
    this.created = created;
    this.xmlFormId = xmlFormId;
  }

  public SimpleAttachment() {
  }

  public String getNodeName() {
    return SimpleDocument.FILE_PREFIX + getLanguage();
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public Date getCreated() {
    return created;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public String getUpdatedBy() {
    return updatedBy;
  }

  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }

  public Date getUpdated() {
    return updated;
  }

  public void setUpdated(Date updated) {
    this.updated = updated;
  }

  public int getMinorVersion() {
    return minorVersion;
  }

  public void setMinorVersion(int minorVersion) {
    this.minorVersion = minorVersion;
  }

  public int getMajorVersion() {
    return majorVersion;
  }

  public void setMajorVersion(int majorVersion) {
    this.majorVersion = majorVersion;
  }

  public String getXmlFormId() {
    return xmlFormId;
  }

  public void setXmlFormId(String xmlFormId) {
    this.xmlFormId = xmlFormId;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 61 * hash + (this.filename != null ? this.filename.hashCode() : 0);
    hash = 61 * hash + (this.language != null ? this.language.hashCode() : 0);
    hash = 61 * hash + (this.title != null ? this.title.hashCode() : 0);
    hash = 61 * hash + (this.description != null ? this.description.hashCode() : 0);
    hash = 61 * hash + (int) (this.size ^ (this.size >>> 32));
    hash = 61 * hash + (this.contentType != null ? this.contentType.hashCode() : 0);
    hash = 61 * hash + (this.createdBy != null ? this.createdBy.hashCode() : 0);
    hash = 61 * hash + (this.created != null ? this.created.hashCode() : 0);
    hash = 61 * hash + (this.updatedBy != null ? this.updatedBy.hashCode() : 0);
    hash = 61 * hash + (this.updated != null ? this.updated.hashCode() : 0);
    hash = 61 * hash + this.minorVersion;
    hash = 61 * hash + this.majorVersion;
    hash = 61 * hash + (this.xmlFormId != null ? this.xmlFormId.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final SimpleAttachment other = (SimpleAttachment) obj;
    if ((this.filename == null) ? (other.filename != null) : !this.filename.equals(other.filename)) {
      return false;
    }
    if ((this.language == null) ? (other.language != null) : !this.language.equals(other.language)) {
      return false;
    }
    if ((this.title == null) ? (other.title != null) : !this.title.equals(other.title)) {
      return false;
    }
    if ((this.description == null) ? (other.description != null)
        : !this.description.equals(other.description)) {
      return false;
    }
    if (this.size != other.size) {
      return false;
    }
    if ((this.contentType == null) ? (other.contentType != null)
        : !this.contentType.equals(other.contentType)) {
      return false;
    }
    if ((this.createdBy == null) ? (other.createdBy != null)
        : !this.createdBy.equals(other.createdBy)) {
      return false;
    }
    if (this.created != other.created && (this.created == null || !this.created.
        equals(other.created))) {
      return false;
    }
    if ((this.updatedBy == null) ? (other.updatedBy != null)
        : !this.updatedBy.equals(other.updatedBy)) {
      return false;
    }
    if (this.updated != other.updated && (this.updated == null || !this.updated.
        equals(other.updated))) {
      return false;
    }
    if (this.minorVersion != other.minorVersion) {
      return false;
    }
    if (this.majorVersion != other.majorVersion) {
      return false;
    }

    if ((this.xmlFormId == null) ? (other.xmlFormId != null)
        : !this.xmlFormId.equals(other.xmlFormId)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "SimpleAttachment{" + "filename=" + filename + ", language=" + language + ", title="
        + title + ", description=" + description + ", size=" + size + ", contentType=" + contentType
        + ", createdBy=" + createdBy + ", created=" + created + ", updatedBy=" + updatedBy
        + ", updated=" + updated + ", minorVersion=" + minorVersion + ", majorVersion="
        + majorVersion + ", xmlFormId=" + xmlFormId + '}';
  }
}
