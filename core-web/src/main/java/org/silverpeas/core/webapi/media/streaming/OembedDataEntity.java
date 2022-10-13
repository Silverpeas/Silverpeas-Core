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
package org.silverpeas.core.webapi.media.streaming;

import org.silverpeas.core.io.media.Definition;
import org.silverpeas.core.webapi.media.MediaDefinitionEntity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author silveryocha
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class OembedDataEntity {

  @XmlElement(name = "provider_name")
  private String providerName;

  @XmlElement
  private String title;

  @XmlElement(name = "author_name")
  private String author;

  @XmlElement
  private String html;

  @XmlElement
  private String width;

  @XmlElement
  private String height;

  @XmlElement
  private String duration;

  @XmlElement
  private String version;

  @XmlElement(name="thumbnail_url")
  private String thumbnailUrl;

  @XmlElement(name="thumbnail_width")
  private String thumbnailWidth;

  @XmlElement(name="thumbnail_height")
  private String thumbnailHeight;

  protected OembedDataEntity() {
  }

  public String getProviderName() {
    return providerName;
  }

  public void setProviderName(final String providerName) {
    this.providerName = providerName;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(final String author) {
    this.author = author;
  }

  public MediaDefinitionEntity getDefinition() {
    return MediaDefinitionEntity
        .createFrom(Definition.of(Integer.parseInt(getWidth()), Integer.parseInt(getHeight())));
  }

  public String getHtml() {
    return html;
  }

  public void setHtml(final String html) {
    this.html = html;
  }

  public String getWidth() {
    return width;
  }

  public void setWidth(final String width) {
    this.width = width;
  }

  public String getHeight() {
    return height;
  }

  public void setHeight(final String height) {
    this.height = height;
  }

  public String getDuration() {
    return duration;
  }

  public void setDuration(final String duration) {
    this.duration = duration;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(final String version) {
    this.version = version;
  }

  public String getThumbnailUrl() {
    return thumbnailUrl;
  }

  public void setThumbnailUrl(final String thumbnailUrl) {
    this.thumbnailUrl = thumbnailUrl;
  }

  public String getThumbnailWidth() {
    return thumbnailWidth;
  }

  public void setThumbnailWidth(final String thumbnailWidth) {
    this.thumbnailWidth = thumbnailWidth;
  }

  public String getThumbnailHeight() {
    return thumbnailHeight;
  }

  public void setThumbnailHeight(final String thumbnailHeight) {
    this.thumbnailHeight = thumbnailHeight;
  }
}
