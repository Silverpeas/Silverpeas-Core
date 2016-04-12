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
 * FLOSS exception. You should have recieved a copy of the text describing
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

import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * This entity provides the users and groups that are in the role for the parent URI that
 * represents, for example, a space or a component. Users and groups are represented by their URI,
 * user and group information are retrieved by user profile and group profile Web-Services.
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class UsersAndGroupsRoleEntity extends AbstractTypeEntity {
  private static final long serialVersionUID = 3331973174319718556L;

  public static final String TYPE = "users-groups-role";

  @XmlElement(defaultValue = "")
  private URI uri;

  @XmlElement(defaultValue = "")
  private URI parentURI;

  @XmlElement(defaultValue = "")
  private SilverpeasRole role;

  @XmlElement(defaultValue = "")
  private String label;

  @XmlElement(defaultValue = "")
  private Collection<URI> users = new LinkedHashSet<URI>();

  @XmlElement(defaultValue = "")
  private Collection<URI> groups = new LinkedHashSet<URI>();

  /**
   * Creates a new entity.
   * @param role
   * @param label
   * @return
   */
  public static UsersAndGroupsRoleEntity createFrom(SilverpeasRole role, String label) {
    return new UsersAndGroupsRoleEntity(role, label);
  }

  /**
   * Sets a URI to this entity. With this URI, it can then be accessed through the Web.
   * @param uri the web entity URI.
   * @return itself.
   */
  public UsersAndGroupsRoleEntity withURI(final URI uri) {
    this.uri = uri;
    return this;
  }

  /**
   * Sets a parent URI to this entity. It could be a space URI or a component URI for example.
   * @param parentURI the parent web entity URI.
   * @return itself.
   */
  public UsersAndGroupsRoleEntity withParentURI(final URI parentURI) {
    this.parentURI = parentURI;
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

  protected final URI getParentURI() {
    return parentURI;
  }

  public SilverpeasRole getRole() {
    return role;
  }

  public String getLabel() {
    return label;
  }

  public Collection<URI> getUsers() {
    return users;
  }

  protected UsersAndGroupsRoleEntity addUser(URI userProfileURI) {
    users.add(userProfileURI);
    return this;
  }

  public Collection<URI> getGroups() {
    return groups;
  }

  protected UsersAndGroupsRoleEntity addGroup(URI groupProfileURI) {
    groups.add(groupProfileURI);
    return this;
  }

  /**
   * Instantiating a new web entity
   * @param role
   * @param label
   */
  private UsersAndGroupsRoleEntity(SilverpeasRole role, String label) {
    this();
    this.role = role;
    this.label = label;
  }

  protected UsersAndGroupsRoleEntity() {
    super(TYPE);
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(getType()).append(role).toHashCode();
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
    final UsersAndGroupsRoleEntity other = (UsersAndGroupsRoleEntity) obj;
    return new EqualsBuilder().append(getType(), other.getType()).append(role, other.role)
        .isEquals();
  }
}
