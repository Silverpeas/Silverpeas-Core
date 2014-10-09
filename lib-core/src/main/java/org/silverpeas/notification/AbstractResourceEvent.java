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

import javax.xml.bind.annotation.XmlElement;

/**
 * It is an abstract implementation of a resource event. It defines the common properties all
 * the concrete events should have. A concrete resource event can extend this class to inherit the
 * basic properties without to implement them by itself.
 * @author mmoquillon
 */
public abstract class AbstractResourceEvent<T> implements ResourceEvent<T> {

  private final Type type;
  private final T resource;

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
}
