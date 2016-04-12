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
package org.silverpeas.core.webapi.calendar;

import org.silverpeas.core.date.Datable;
import org.silverpeas.core.webapi.base.WebEntity;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

/**
 * Web entity abstraction which provides common event informations of a web entity
 * @author Yohann Chastagnier
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractEventEntity<T extends AbstractEventEntity<T>> implements WebEntity {
  private static final long serialVersionUID = -7592985250664860865L;

  @XmlElement(required = true, defaultValue = "")
  private final String type;

  @XmlElement(defaultValue = "")
  private URI uri;

  @XmlElement(defaultValue = "")
  private final String id;

  @XmlElement(defaultValue = "")
  private final String instanceId;

  @XmlElement(required = true)
  @NotNull
  @Size(min = 1)
  private final String title;

  @XmlElement(defaultValue = "")
  private final String description;

  @XmlElement
  private boolean allDay = false;

  @XmlElement(defaultValue = "")
  @NotNull
  @Size(min = 1)
  private final String start;

  @XmlElement(defaultValue = "")
  private final String end;

  @XmlElement(defaultValue = "")
  private final String url;

  /**
   * Default constructor.
   * @param type
   * @param instanceId
   * @param id
   * @param title
   * @param description
   * @param start
   * @param end
   * @param url
   */
  protected AbstractEventEntity(final String type, final String instanceId, final String id,
      final String title, final String description, final Datable start, final Datable end,
      final String url) {
    this.type = type;
    this.instanceId = instanceId;
    this.id = id;
    this.title = title;
    this.description = description;
    this.start = (start != null ? start.toShortISO8601() : null);
    this.end = (end != null ? end.toShortISO8601() : null);
    allDay = (start == null && end == null);
    this.url = url;
  }

  protected AbstractEventEntity() {
    this("", "", "", "", "", null, null, "");
  }

  /**
   * Sets a URI to this entity. With this URI, it can then be accessed through the Web.
   * @param uri the web entity URI.
   * @return itself.
   */
  @SuppressWarnings("unchecked")
  public T withURI(final URI uri) {
    this.uri = uri;
    return (T) this;
  }

  public String getType() {
    return type;
  }

  public String getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public boolean isAllDay() {
    return allDay;
  }

  public String getStart() {
    return start;
  }

  public String getEnd() {
    return end;
  }

  public String getUrl() {
    return url;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.web.WebEntity#getURI()
   */
  @Override
  public URI getURI() {
    return uri;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(getType()).append(id).append(title).append(start)
        .append(end).toHashCode();
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
    final AbstractEventEntity<?> other = (AbstractEventEntity<?>) obj;
    return new EqualsBuilder().append(getType(), other.getType()).append(id, other.getId())
        .append(title, other.title).append(start, other.start).append(end, other.end).isEquals();
  }
}
