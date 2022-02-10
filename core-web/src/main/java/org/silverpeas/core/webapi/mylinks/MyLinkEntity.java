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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.webapi.mylinks;

import org.silverpeas.core.mylinks.model.LinkDetail;
import org.silverpeas.core.web.rs.WebEntity;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

import static java.lang.String.valueOf;

/**
 * The mylink entity is a mylink object that is exposed in the web as an entity (web entity). As
 * such, it publishes only some of its attributes. It represents a user favorite link in Silverpeas
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class MyLinkEntity implements WebEntity {

  private static final long serialVersionUID = -4596241423551371699L;

  @XmlElement(defaultValue = "")
  private URI uri;

  @XmlElement
  @NotNull
  private int linkId;

  @XmlElement(defaultValue = "-1")
  private int position = -1;

  @XmlElement
  @NotNull
  private String name;

  @XmlElement
  private String description;

  @XmlElement(required = true)
  @NotNull
  @Size(min = 1)
  private String url;

  @XmlElement(defaultValue = "false")
  @NotNull
  private boolean visible = false;

  @XmlElement(defaultValue = "false")
  @NotNull
  private boolean popup = false;

  @XmlElement
  private String userId;

  @XmlElement
  private String instanceId;

  @XmlElement
  private String objectId;

  @XmlElement
  private Integer categoryId;

  public static MyLinkEntity fromLinkDetail(final LinkDetail link, URI uri) {
    return new MyLinkEntity(link, uri);
  }

  /**
   * Default constructor
   */
  protected MyLinkEntity() {
  }

  /**
   * Constructor using linkDetail and uri
   * @param link the link detail
   * @param uri an URI
   */
  public MyLinkEntity(LinkDetail link, URI uri) {
    this.uri = uri;
    this.name = link.getName();
    this.description = link.getDescription();
    this.url = link.getUrl();
    this.linkId = link.getLinkId();
    if (link.hasPosition()) {
      this.position = link.getPosition();
    } else {
      this.position = -1;
    }
    this.visible = link.isVisible();
    this.popup = link.isPopup();
    this.userId = link.getUserId();
    this.instanceId = link.getInstanceId();
    this.objectId = link.getObjectId();
    this.categoryId = link.getCategory() != null ? link.getCategory().getId() : null;
  }

  @Override
  public URI getURI() {
    return uri;
  }

  public LinkDetail toLinkDetail() {
    LinkDetail linkDetail =
        new LinkDetail(this.name, this.description, this.url, this.visible, this.popup);
    linkDetail.setInstanceId(this.instanceId);
    linkDetail.setObjectId(this.objectId);
    linkDetail.setUserId(this.userId);
    linkDetail.setLinkId(this.linkId);
    if (this.position != -1) {
      linkDetail.setHasPosition(true);
      linkDetail.setPosition(this.position);
    } else {
      linkDetail.setHasPosition(false);
    }
    if (categoryId != null) {
      linkDetail.setCategory(MyLinksWebManager.get().getAuthorizedCategory(valueOf(categoryId)));
    }
    return linkDetail;
  }

  /**
   * @return the linkId
   */
  public int getLinkId() {
    return linkId;
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
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * @return the visible
   */
  public boolean isVisible() {
    return visible;
  }

  /**
   * @return the popup
   */
  public boolean isPopup() {
    return popup;
  }

  /**
   * @return the userId
   */
  public String getUserId() {
    return userId;
  }

  /**
   * @return the instanceId
   */
  public String getInstanceId() {
    return instanceId;
  }

  /**
   * @return the objectId
   */
  public String getObjectId() {
    return objectId;
  }

  /**
   * @return the position of the object in the list
   */
  public int getPosition() {
    return position;
  }

  /**
   * @return the optional category identifier the link is associated to.
   */
  public Integer getCategoryId() {
    return categoryId;
  }
}
