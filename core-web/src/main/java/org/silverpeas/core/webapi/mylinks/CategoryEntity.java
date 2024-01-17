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
package org.silverpeas.core.webapi.mylinks;

import org.silverpeas.core.mylinks.model.CategoryDetail;
import org.silverpeas.core.web.rs.WebEntity;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

/**
 * The {@link CategoryEntity} entity is a {@link CategoryDetail} that is exposed in the web as an entity (web entity). As
 * such, it publishes only some of its attributes. It represents a category of user favorite link in Silverpeas
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class CategoryEntity implements WebEntity {

  private static final long serialVersionUID = -4596241423551371699L;

  @XmlElement(defaultValue = "")
  private URI uri;

  @XmlElement
  @NotNull
  private int catId;

  @XmlElement(defaultValue = "-1")
  private int position = -1;

  @XmlElement
  @NotNull
  private String name;

  @XmlElement
  private String description;

  @XmlElement
  private String userId;

  public static CategoryEntity fromCategoryDetail(final CategoryDetail category, URI uri) {
    return new CategoryEntity(category, uri);
  }

  /**
   * Default constructor
   */
  protected CategoryEntity() {
  }

  /**
   * Constructor using {@link CategoryDetail} and uri
   * @param category the category detail
   * @param uri an URI
   */
  public CategoryEntity(CategoryDetail category, URI uri) {
    this.uri = uri;
    this.name = category.getName();
    this.description = category.getDescription();
    this.catId = category.getId();
    if (category.hasPosition()) {
      this.position = category.getPosition();
    } else {
      this.position = -1;
    }
    this.userId = category.getUserId();
  }

  @Override
  public URI getURI() {
    return uri;
  }

  public CategoryDetail toCategoryDetail() {
    CategoryDetail categoryDetail = new CategoryDetail();
    categoryDetail.setId(this.catId);
    categoryDetail.setName(this.name);
    categoryDetail.setDescription(this.description);
    categoryDetail.setUserId(this.userId);
    if (this.position != -1) {
      categoryDetail.setHasPosition(true);
      categoryDetail.setPosition(this.position);
    } else {
      categoryDetail.setHasPosition(false);
    }
    return categoryDetail;
  }

  /**
   * @return the category identifier
   */
  public int getCategoryId() {
    return catId;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @return the userId
   */
  public String getUserId() {
    return userId;
  }

  /**
   * @return the position of the object in the list
   */
  public int getPosition() {
    return position;
  }

}
