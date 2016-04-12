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

package org.silverpeas.core.notification.system;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * It is an abstract implementation of a resource event. It defines the common properties all
 * the concrete events should have. A concrete resource event can extend this class to inherit the
 * basic properties without to implement them by itself.
 * <p>
 * The properties of this abstract class are all annotated with JAXB annotations so that they are
 * ready to be serialized into a text stream (in XML or in JSON). This is particularly useful when
 * the event has to be delivered to external/remote software components.
 * </p>
 * @author mmoquillon
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractResourceEvent<T extends Serializable> implements ResourceEvent<T> {

  @XmlElement
  private Type type;
  @XmlElement
  private StateTransition<T> resource;
  @XmlElement
  private Map<String, String> parameters = new HashMap<String, String>();

  protected AbstractResourceEvent() {

  }

  /**
   * Constructs a new instance representing the specified event type in relation to the specified
   * resource.
   * @param type the type of the event.
   * @param resource the resource implied in the event. For an update, two instances of the same
   * resource is expected:  the first being the resource before the update, the second being the
   * resource after the update (the result of the update).
   */
  public AbstractResourceEvent(Type type, @NotNull T... resource) {
    this.type = type;
    switch (type) {
      case CREATION:
        this.resource = StateTransition.transitionBetween(null, resource[0]);
        break;
      case UPDATE:
        this.resource = StateTransition.transitionBetween(resource[0], resource[1]);
        break;
      case REMOVING:
        this.resource = StateTransition.transitionBetween(resource[0], resource[0]);
        break;
      case DELETION:
        this.resource = StateTransition.transitionBetween(resource[0], null);
        break;
    }
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public StateTransition<T> getTransition() {
    return resource;
  }

  /**
   * Puts a new parameter to this resource. Parameters in event resource carries additional
   * information and has to be simple.
   * @param name the parameter name.
   * @param value the parameter value.
   */
  public void putParameter(String name, String value) {
    parameters.put(name, value);
  }

  /**
   * Gets the value of the specified parameter. If the parameter isn't set in the event resource,
   * then null is returned.
   * @param name the name of the parameter to get.
   * @return the value of the parameter or null if no such parameter is set in this notification.
   */
  public String getParameterValue(String name) {
    return parameters.get(name);
  }

  /**
   * Gets the set of this event resource's parameters.
   * @return an unmodifiable set of parameter names.
   */
  public Set<String> getParameters() {
    return Collections.unmodifiableSet(parameters.keySet());
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
