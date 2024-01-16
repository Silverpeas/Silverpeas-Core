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
package org.silverpeas.core.webapi.search;

import org.silverpeas.core.index.search.model.SearchResult;
import org.silverpeas.core.util.StringUtil;

import javax.xml.bind.annotation.XmlElement;
import java.util.Map;

/**
 * @author Nicolas Eysseric
 */
public class ResultEntity {

  @XmlElement(defaultValue = "")
  private String name;

  @XmlElement(defaultValue = "")
  private String description;

  @XmlElement
  private String creationDate;

  @XmlElement
  private String updateDate;

  @XmlElement
  private String type;

  @XmlElement
  private String id;

  @XmlElement
  private String componentId;

  @XmlElement
  private String thumbnailURL;

  @XmlElement
  private Map<String, String> fieldsForFacets;

  private ResultEntity(SearchResult gsr) {
    this.name = gsr.getName();
    this.description = gsr.getDescription();
    if (gsr.getCreationDate() != null) {
      this.creationDate = gsr.getCreationDate().toString();
    }
    if (gsr.getLastUpdateDate() != null) {
      this.updateDate = gsr.getLastUpdateDate().toString();
    }
    this.id = gsr.getId();
    this.type = gsr.getType();
    this.componentId = gsr.getInstanceId();
    if (StringUtil.isDefined(gsr.getThumbnailURL())) {
      this.thumbnailURL = gsr.getThumbnailURL().replaceFirst("/FileServer/", "/OnlineFileServer/");
    }

    this.fieldsForFacets = gsr.getFormFieldsForFacets();
  }

  public static ResultEntity fromSearchResult(SearchResult gsr) {
    return new ResultEntity(gsr);
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  public String getUpdateDate() {
    return updateDate;
  }

  public String getCreationDate() {
    return creationDate;
  }

  public String getType() {
    return type;
  }

  public void setType(final String type) {
    this.type = type;
  }

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public String getComponentId() {
    return componentId;
  }

  public void setComponentId(final String componentId) {
    this.componentId = componentId;
  }

  public String getThumbnailURL() {
    return thumbnailURL;
  }

  public void setThumbnailURL(final String thumbnailURL) {
    this.thumbnailURL = thumbnailURL;
  }

  public Map<String, String> getFieldsForFacets() {
    return fieldsForFacets;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final ResultEntity that = (ResultEntity) o;

    if (id != null ? !id.equals(that.id) : that.id != null) {
      return false;
    }
    return componentId != null ? componentId.equals(that.componentId) : that.componentId == null;

  }

  @Override
  public int hashCode() {
    int result = type != null ? type.hashCode() : 0;
    result = 31 * result + (id != null ? id.hashCode() : 0);
    result = 31 * result + (componentId != null ? componentId.hashCode() : 0);
    return result;
  }
}
