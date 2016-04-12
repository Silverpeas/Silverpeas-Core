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

import org.silverpeas.core.admin.space.SpaceInstLight;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

import static org.silverpeas.core.webapi.admin.AdminResourceURIs.*;

/**
 * The space instance light entity is a SpaceInstLight object that is exposed in the web as
 * an entity (web entity). As such, it publishes only some of its attributes. It represents a
 * SpaceInstLight in Silverpeas plus some additional information such as the URI for accessing
 * it.
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SpaceEntity extends StructureElementEntity<SpaceEntity> {

  public static final String TYPE = "space";

  private static final long serialVersionUID = 2872199842421965243L;

  @XmlElement(defaultValue = "")
  private URI spacesURI;

  @XmlElement(defaultValue = "")
  private URI componentsURI;

  @XmlElement(defaultValue = "")
  private URI contentURI;

  @XmlElement(defaultValue = "")
  private URI appearanceURI;

  @XmlElement
  private int level;

  @XmlElement
  private boolean isSpaceDisplayedAtFirst;

  @XmlElement(defaultValue = "")
  private String favorite = "";

  /**
   * Creates a new SpaceInstLight entity from the specified SpaceInstLight.
   * @param space the SpaceInstLight to entitify.
   * @param language the current language.
   * @return the entity representing the specified SpaceInstLight.
   */
  public static SpaceEntity createFrom(final SpaceInstLight space, final String language) {
    return new SpaceEntity(space, language);
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.webapi.admin.StructureElementEntity#withURI(java.net.URI)
   */
  @Override
  public SpaceEntity withURI(final URI uri) {
    super.withURI(uri);
    spacesURI = buildURI(getStringBaseURI(), getId(), SPACES_SPACES_URI_PART);
    componentsURI = buildURI(getStringBaseURI(), getId(), SPACES_COMPONENTS_URI_PART);
    contentURI = buildURI(getStringBaseURI(), getId(), SPACES_CONTENT_URI_PART);
    appearanceURI = buildURI(getStringBaseURI(), getId(), SPACES_APPEARANCE_URI_PART);
    return this;
  }

  public SpaceEntity addUserFavorites(final String favorite) {
    this.favorite = favorite;
    return this;
  }

  /**
   * Gets the level of the space.
   * @return the level of the space
   */
  public int getLevel() {
    return level;
  }

  /**
   * Gets the URI of spaces included in the space.
   * @return the URI of spaces included in the space
   */
  public URI getSpacesURI() {
    return spacesURI;
  }

  /**
   * Gets the URI of components included in the space.
   * @return the URI of component included in the space
   */
  public URI getComponentsURI() {
    return componentsURI;
  }

  /**
   * Gets the URI of the content included in the space.
   * @return the URI of the content included in the space
   */
  public URI getContentURI() {
    return contentURI;
  }

  /**
   * Gets the URI of the appearance of the space.
   * @return the URI of the appearance of the space
   */
  public URI getAppearanceURI() {
    return appearanceURI;
  }

  /**
   * Indicates if spaces have to be displayed before or not the applications.
   * @return true or false
   */
  public boolean isSpaceDisplayedAtFirst() {
    return isSpaceDisplayedAtFirst;
  }

  /**
   * Gets the favorite data of the space
   * @return the favorite data
   */
  protected final String getFavorite() {
    return favorite;
  }

  /**
   * Instantiating a new web entity from the corresponding data
   * @param space
   * @param language
   */
  private SpaceEntity(final SpaceInstLight space, final String language) {
    super(TYPE, String.valueOf(space.getLocalId()), space.getFatherId(), space.getName(language),
        space.getDescription(), space.getStatus(), space.getOrderNum(), space.isInheritanceBlocked());
    level = space.getLevel();
    isSpaceDisplayedAtFirst = space.isDisplaySpaceFirst();
  }

  protected SpaceEntity() {
    super();
  }

  /**
   * @return the URI base of the current entity
   */
  @Override
  protected String getStringBaseURI() {
    return getURI().toString().replaceFirst(SPACES_BASE_URI + "/[0-9]+", SPACES_BASE_URI);
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.webapi.admin.StructureElementEntity#getStringParentBaseURI()
   */
  @Override
  protected String getStringParentBaseURI() {
    return getStringBaseURI();
  }
}
