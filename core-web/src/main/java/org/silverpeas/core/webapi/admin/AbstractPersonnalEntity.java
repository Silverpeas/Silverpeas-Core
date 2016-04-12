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

import java.net.URI;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * The space instance light entity is a SpaceInstLight object that is exposed in the web as
 * an entity (web entity). As such, it publishes only some of its attributes. It represents a
 * SpaceInstLight in Silverpeas plus some additional information such as the URI for accessing
 * it.
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractPersonnalEntity extends AbstractTypeEntity {

  private static final long serialVersionUID = 1025414384658732933L;

  @XmlElement(defaultValue = "")
  private URI uri;

  @XmlElement(required = true)
  private final String id;

  @XmlElement
  private final int nb;

  @XmlElement(defaultValue = "")
  private final String name;

  @XmlElement(required = true)
  @NotNull
  @Size(min = 1)
  private final String label;

  @XmlElement(defaultValue = "")
  private final String description;

  @XmlElement(required = true)
  @NotNull
  private String url = "";

  @SuppressWarnings("unchecked")
  public <T extends AbstractPersonnalEntity> T withUriBase(final URI uriBase) {
    uri =
        AdminResourceURIs
            .buildURI(uriBase.toString(), AdminResourceURIs.SPACES_BASE_URI, AdminResourceURIs.SPACES_PERSONAL_URI_PART, getUriIdentifier());
    return (T) this;
  }

  protected abstract String getUriIdentifier();

  /*
   * (non-Javadoc)
   * @see com.silverpeas.web.WebEntity#getURI()
   */
  @Override
  public URI getURI() {
    return uri;
  }

  /**
   * @return the id
   */
  protected final String getId() {
    return id;
  }

  /**
   * @return the nb
   */
  protected final int getNb() {
    return nb;
  }

  /**
   * @return the name
   */
  protected final String getName() {
    return name;
  }

  /**
   * @return the label
   */
  protected final String getLabel() {
    return label;
  }

  /**
   * @return the description
   */
  protected final String getDescription() {
    return description;
  }

  /**
   * @return the url
   */
  protected final String getUrl() {
    return url;
  }

  public AbstractPersonnalEntity(final String type, final String id, final int nb,
      final String name, final String label, final String description, final String url) {
    super(type);
    this.id = id;
    this.nb = nb;
    this.name = name;
    this.label = label;
    this.description = description;
    this.url = url;
  }

  protected AbstractPersonnalEntity() {
    this("", "", 0, "", "", "", "");
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(getType()).append(id).append(name).toHashCode();
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
    final AbstractPersonnalEntity other = (AbstractPersonnalEntity) obj;
    return new EqualsBuilder().append(getType(), other.getType()).append(id, other.id)
        .append(name, other.name).isEquals();
  }
}
