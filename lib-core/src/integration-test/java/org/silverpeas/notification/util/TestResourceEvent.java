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

package org.silverpeas.notification.util;

import org.silverpeas.notification.ResourceEvent;
import org.silverpeas.util.JSONCodec;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author mmoquillon
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TestResourceEvent implements ResourceEvent<TestResource> {

  @XmlElement
  private Type type;
  @XmlElement
  private TestResource resource;

  public static final TestResourceEvent fromMessage(final TextMessage message) throws JMSException {
    return JSONCodec.decode(message.getText(), TestResourceEvent.class);
  }

  protected TestResourceEvent() {

  }

  /**
   * Constructs a new instance representing the specified event type in relation to the specified
   * resource.
   * @param type the type of the event.
   * @param resource the resource related by the event.
   */
  public TestResourceEvent(final Type type, final TestResource resource) {
    this.type = type;
    this.resource = resource;
  }

  @Override
  public Type getType() {
    return this.type;
  }

  @Override
  public TestResource getResource() {
    return this.resource;
  }

  public String toText() {
    return JSONCodec.encode(this);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final TestResourceEvent that = (TestResourceEvent) o;

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
