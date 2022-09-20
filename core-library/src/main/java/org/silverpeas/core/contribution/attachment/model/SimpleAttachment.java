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
package org.silverpeas.core.contribution.attachment.model;

import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.i18n.I18n;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * An attachment of a user contribution. It represents both the file in the Silverpeas filesystem
 * and the node in the JCR about a given document attached to a user contribution.
 * @author ehugonnet
 * @author mmoquillon
 */
public class SimpleAttachment implements Serializable {
  private static final long serialVersionUID = -6153003608158238503L;

  private String filename;
  private String language = I18NHelper.DEFAULT_LANGUAGE;
  private String title;
  private String description;
  private long size;
  private String contentType;
  private String createdBy;
  private Date created;
  private String updatedBy;
  private Date updated;
  private String xmlFormId;

  /**
   * Constructs a new {@link Builder} of {@link SimpleAttachment} instances for the specified
   * language.
   * @param language the ISO 639-1 code of the language.
   * @return a {@link Builder} of {@link SimpleAttachment} instances.
   */
  public static Builder builder(final String language) {
    return new Builder(language);
  }

  /**
   * Creates a new {@link Builder} of {@link SimpleAttachment} instances for the default language
   * as defined in {@link I18n}.
   * @return a {@link Builder} of {@link SimpleAttachment} instances.
   */
  public static Builder builder() {
    return new Builder(I18n.get().getDefaultLanguage());
  }

  private SimpleAttachment() {
  }

  public SimpleAttachment(final SimpleAttachment attachment) {
    this.filename = attachment.filename;
    this.language = attachment.language;
    this.title = attachment.title;
    this.description = attachment.description;
    this.size = attachment.size;
    this.contentType = attachment.contentType;
    this.createdBy = attachment.createdBy;
    this.created = attachment.created;
    this.updatedBy = attachment.updatedBy;
    this.updated = attachment.updated;
    this.xmlFormId = attachment.xmlFormId;
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
    this.language = I18NHelper.checkLanguage(language);
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

  public Date getCreationDate() {
    if (created == null) {
      return null;
    }
    return new Date(created.getTime());
  }

  public final void setCreationDate(Date creationDate) {
    if (creationDate == null) {
      this.created = null;
    } else {
      this.created = new Date(creationDate.getTime());
    }
  }

  public String getUpdatedBy() {
    return updatedBy;
  }

  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }

  public Date getLastUpdateDate() {
    if (updated == null) {
      return null;
    }
    return new Date(updated.getTime());
  }

  public final void setLastUpdateDate(Date updateDate) {
    if (updateDate == null) {
      this.updated = null;
    } else {
      this.updated = new Date(updateDate.getTime());
    }
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
    if (!Objects.equals(this.filename, other.filename)) {
      return false;
    }
    if (!Objects.equals(this.language, other.language)) {
      return false;
    }
    if (!Objects.equals(this.title, other.title)) {
      return false;
    }
    if (!Objects.equals(this.description, other.description)) {
      return false;
    }
    if (this.size != other.size) {
      return false;
    }
    if (!Objects.equals(this.contentType, other.contentType)) {
      return false;
    }
    if (!Objects.equals(this.createdBy, other.createdBy)) {
      return false;
    }
    if (!Objects.equals(this.created, other.created)) {
      return false;
    }
    if (!Objects.equals(this.updatedBy, other.updatedBy)) {
      return false;
    }
    if (!Objects.equals(this.updated, other.updated)) {
      return false;
    }
    return Objects.equals(this.xmlFormId, other.xmlFormId);
  }

  @Override
  public String toString() {
    return "SimpleAttachment{" + "filename=" + filename + ", language=" + language + ", title=" +
        title + ", description=" + description + ", size=" + size + ", contentType=" + contentType +
        ", createdBy=" + createdBy + ", created=" + created + ", updatedBy=" + updatedBy +
        ", updated=" + updated + ", xmlFormId=" + xmlFormId + '}';
  }

  public static class Builder {

    private final SimpleAttachment attachment = new SimpleAttachment();

    /**
     * Creates a new {@link Builder} of {@link SimpleAttachment} instances for the specified
     * language.
     * @param language the ISO 639-1 code of the language.
     */
    private Builder(final String language) {
      attachment.setLanguage(language);
    }

    /**
     * Sets the name of the file that contains the content. The file represents in the physical
     * storage the attachment.
     * @param filename the name of the file to which the attachment is related.
     * @return itself.
     */
    public Builder setFilename(final String filename) {
      attachment.setFilename(filename);
      return this;
    }

    /**
     * Sets the title of the attachment.
     * @param title the attachment title.
     * @return itself.
     */
    public Builder setTitle(final String title) {
      attachment.setTitle(title);
      return this;
    }

    /**
     * Sets a short description about the content of the attachment.
     * @param description a simple textual description.
     * @return itself.
     */
    public Builder setDescription(final String description) {
      attachment.setDescription(description);
      return this;
    }

    /**
     * Sets the size of the content of the attachment.
     * @param size the size of the file in bytes.
     * @return itself.
     */
    public Builder setSize(long size) {
      attachment.setSize(size);
      return this;
    }

    /**
     * Sets the type of the content in the file.
     * @param contentType a MIME type code defining the type of the content of the attachment.
     * @return itself.
     */
    public Builder setContentType(final String contentType) {
      attachment.setContentType(contentType);
      return this;
    }

    /**
     * Sets the data of the creation of the attachment (and hence of the file under the hood).
     * @param creator the unique identifier of a user in Silverpeas.
     * @param creationDate the datetime at which the attachment was created.
     * @return itself.
     */
    public Builder setCreationData(final String creator, final Date creationDate) {
      attachment.setCreatedBy(creator);
      attachment.setCreationDate(creationDate);
      return this;
    }

    /**
     * In the case the content is a form stored in an XML file, sets its unique identifier.
     * @param formId the unique identifier of an XML form instance in Silverpeas.
     * @return itself.
     */
    public Builder setFormId(final String formId) {
      attachment.setXmlFormId(formId);
      return this;
    }

    /**
     * Builds the attachment from the parameters previously set with the builder.
     * @return a {@link SimpleAttachment} instance.
     */
    public SimpleAttachment build() {
      return attachment;
    }
  }
}
