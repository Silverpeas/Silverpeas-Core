package org.silverpeas.core.webapi.search;

import org.silverpeas.core.index.search.model.SearchResult;
import org.silverpeas.core.util.StringUtil;

import javax.xml.bind.annotation.XmlElement;

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

  public static ResultEntity fromSearchResult(SearchResult gsr) {
    return new ResultEntity(gsr);
  }

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
    String thumbnailURL = gsr.getThumbnailURL();
    if (StringUtil.isDefined(thumbnailURL)) {
      this.thumbnailURL = thumbnailURL.replaceFirst("/FileServer/", "/OnlineFileServer/");
    }
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
