/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.core.webapi.calendar;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.web.rs.WebEntity;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.time.temporal.Temporal;

import static org.silverpeas.core.date.TemporalFormatter.toIso8601;

/**
 * Web entity abstraction which provides common event information of a web entity
 * @author Yohann Chastagnier
 * @deprecated
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Deprecated
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
  @Size(min = 1)
  private final String start;

  @XmlElement(defaultValue = "")
  private final String end;

  @XmlElement(defaultValue = "")
  private final String url;

  protected AbstractEventEntity(final ContributionIdentifier identifier,
      final String title, final String description, final Temporal start, final Temporal end,
      final String url) {
    this.type = identifier.getType();
    this.instanceId = identifier.getComponentInstanceId();
    this.id = identifier.getLocalId();
    this.title = title;
    this.description = description;
    this.start = (start != null ? toIso8601(start, false) : null);
    this.end = (end != null ? toIso8601(end, false) : null);
    allDay = (start == null && end == null);
    this.url = url;
  }

  protected AbstractEventEntity() {
    this(ContributionIdentifier.from("", "", ""), "", "", null, null, "");
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
