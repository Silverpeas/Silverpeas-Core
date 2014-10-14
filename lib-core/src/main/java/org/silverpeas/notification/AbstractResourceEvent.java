/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.notification;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * It is an abstract implementation of a resource event. It defines the common properties all
 * the concrete events should have. A concrete resource event can extend this class to inherit the
 * basic properties without to implement them by itself.
 * <p>
 * The properties of this abstract class are all annotated with JAXB annotations so that they are
 * ready to be serialized into a text stream (in XML or in JSON).
 * </p>
 * @author mmoquillon
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractResourceEvent<T> implements ResourceEvent<T> {

  @XmlElement
  private Type type;
  @XmlElement
  private T resource;

  protected AbstractResourceEvent() {

  }

  /**
   * Constructs a new instance representing the specified event type in relation to the specified
   * resource.
   * @param type the type of the event.
   * @param resource the resource related by the event.
   */
  public AbstractResourceEvent(Type type, T resource) {
    this.type = type;
    this.resource = resource;
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public T getResource() {
    return resource;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final AbstractResourceEvent that = (AbstractResourceEvent) o;

    if (!resource.equals(that.resource)) {
      return false;
    }
    if (type != that.type) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = type.hashCode();
    result = 31 * result + resource.hashCode();
    return result;
  }
}
