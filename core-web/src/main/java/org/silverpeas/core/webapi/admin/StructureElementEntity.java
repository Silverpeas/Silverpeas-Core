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

import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.silverpeas.core.util.StringUtil;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.net.URI;

import static org.silverpeas.core.webapi.admin.AdminResourceURIs.USERS_AND_GROUPS_ROLES_URI_PART;
import static org.silverpeas.core.webapi.admin.AdminResourceURIs.buildURI;

/**
 * The component instance light entity is a ComponentInstLight object that is exposed in the web as
 * an entity (web entity). As such, it publishes only some of its attributes. It represents a
 * ComponentInstLight in Silverpeas plus some additional information such as the URI for accessing
 * it.
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class StructureElementEntity<T extends StructureElementEntity<T>>
    extends AbstractTypeEntity {

  private static final long serialVersionUID = -2205892819135663318L;

  @XmlElement(defaultValue = "")
  private URI uri;

  @XmlElement(defaultValue = "")
  private URI parentURI;

  @XmlElement(defaultValue = "")
  private URI usersAndGroupsRolesURI;

  @XmlElement(required = true)
  @NotNull
  @Pattern(regexp = "^[0-9]+$")
  private final String id;

  @XmlElement(required = true)
  @NotNull
  @Size(min = 1)
  private final String label;

  @XmlElement(defaultValue = "")
  private final String description;

  @XmlElement(defaultValue = "")
  private final String status;

  @XmlElement
  private final int rank;

  @XmlElement
  private final boolean isInheritanceBlocked;

  @XmlTransient
  private final String parentId;

  /**
   * Sets a URI to this entity. With this URI, it can then be accessed through the Web.
   * @param uri the web entity URI.
   * @return itself.
   */
  @SuppressWarnings("unchecked")
  public T withURI(final URI uri) {
    this.uri = uri;
    this.parentURI = buildURI(StringUtil.isDefined(parentId) && !SpaceInstLight.isRoot(parentId) ?
        getStringParentBaseURI() : null, parentId);
    this.usersAndGroupsRolesURI = buildURI(uri.toString(), USERS_AND_GROUPS_ROLES_URI_PART);
    return (T) this;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.web.WebEntity#getURI()
   */
  @Override
  public URI getURI() {
    return uri;
  }

  protected URI getParentURI() {
    return parentURI;
  }

  public URI getUsersAndGroupsRolesURI() {
    return usersAndGroupsRolesURI;
  }

  protected String getId() {
    return id;
  }

  protected String getLabel() {
    return label;
  }

  protected String getDescription() {
    return description;
  }

  protected String getStatus() {
    return status;
  }

  protected int getRank() {
    return rank;
  }

  protected boolean isInheritanceBlocked() {
    return isInheritanceBlocked;
  }

  /**
   * @return the URI base of the current entity
   */
  protected abstract String getStringBaseURI();

  /**
   * @return the parent URI base of the current entity
   */
  protected abstract String getStringParentBaseURI();

  /**
   * Instantiating a new web entity from the corresponding data
   */
  protected StructureElementEntity(final String type, final String id, final String parentId,
      final String label, final String description, final String status, final int rank,
      final boolean isInheritanceBlocked) {
    super(type);
    this.id = id == null ? "" : id;
    this.parentId = parentId == null ? "" : parentId.replaceFirst(SpaceInst.SPACE_KEY_PREFIX, "");
    this.label = label == null ? "" : label;
    this.description = description == null ? "" : description;
    this.status = status == null ? "" : status;
    this.rank = rank;
    this.isInheritanceBlocked = isInheritanceBlocked;
  }

  protected StructureElementEntity() {
    this("", "", "", "", "", "", 0, false);
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(getType()).append(id).toHashCode();
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
    final StructureElementEntity<?> other = (StructureElementEntity<?>) obj;
    return new EqualsBuilder().append(getType(), other.getType()).append(id, other.getId())
        .isEquals();
  }
}
