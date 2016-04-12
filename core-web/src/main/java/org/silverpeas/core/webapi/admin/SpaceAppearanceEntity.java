/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.core.webapi.admin;

import static org.silverpeas.core.webapi.admin.AdminResourceURIs.SPACES_BASE_URI;
import static org.silverpeas.core.webapi.admin.AdminResourceURIs.buildURI;

import java.net.URI;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import org.silverpeas.core.admin.space.SpaceInstLight;

/**
 * The space instance light entity is a SpaceInstLight object that is exposed in the web as
 * an entity (web entity). As such, it publishes only some of its attributes. It represents a
 * SpaceInstLight in Silverpeas plus some additional information such as the URI for accessing
 * it.
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SpaceAppearanceEntity extends AbstractTypeEntity {

  public static final String TYPE = "space-appearance";

  private static final long serialVersionUID = -6830353601965146743L;

  @XmlElement(defaultValue = "")
  private URI uri;

  @XmlElement(defaultValue = "")
  private URI spaceURI;

  @XmlElement(defaultValue = "")
  private String look;

  @XmlElement(defaultValue = "")
  private String wallpaper;

  @XmlElement(defaultValue = "")
  private String css;

  @XmlTransient
  private String spaceId;

  /**
   * Creates a new space appearance entity from the specified data.
   * @param space
   * @param look
   * @param wallpaper
   * @return new space appearance entity
   */
  public static SpaceAppearanceEntity createFrom(final SpaceInstLight space, final String look,
      final String wallpaper, final String css) {
    return new SpaceAppearanceEntity(space, look, wallpaper, css);
  }

  /**
   * Sets a URI to this entity. With this URI, it can then be accessed through the Web.
   * @param uri the web entity URI.
   * @return itself.
   */
  public SpaceAppearanceEntity withURI(final URI uri) {
    this.uri = uri;
    spaceURI = buildURI(getStringBaseURI(), spaceId);
    return this;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.web.WebEntity#getURI()
   */
  @Override
  public URI getURI() {
    return uri;
  }

  /**
   * @return the spaceURI
   */
  protected final URI getSpaceURI() {
    return spaceURI;
  }

  /**
   * @return the look
   */
  protected final String getLook() {
    return look;
  }

  /**
   * @return the wallpaper
   */
  protected final String getWallpaper() {
    return wallpaper;
  }

  /**
   * @return the wallpaper
   */
  protected final String getCSS() {
    return css;
  }

  /**
   * Instantiating a new web entity from the corresponding data
   * @param space
   */
  private SpaceAppearanceEntity(final SpaceInstLight space, final String look,
      final String wallpaper, final String css) {
    this();
    spaceId = String.valueOf(space.getLocalId());
    this.look = look;
    this.wallpaper = wallpaper;
    this.css = css;
  }

  protected SpaceAppearanceEntity() {
    super(TYPE);
  }

  /**
   * Gets the URI base of the current entity
   * @return
   */
  protected String getStringBaseURI() {
    return getURI().toString().replaceFirst(
        SPACES_BASE_URI + "/[0-9]+/" + AdminResourceURIs.SPACES_APPEARANCE_URI_PART,
        SPACES_BASE_URI);
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(getType()).append(spaceId).toHashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (obj.getClass() != getClass()) {
      return false;
    }
    final SpaceAppearanceEntity other = (SpaceAppearanceEntity) obj;
    return new EqualsBuilder().append(getType(), other.getType()).append(spaceId, other.spaceId)
        .isEquals();
  }
}
