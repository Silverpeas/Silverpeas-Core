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

import org.silverpeas.notification.util.TextMessageCodec;

import javax.inject.Inject;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;

/**
 * An asynchronous notifier of resource events based on JMS. The asynchronous notifiers should
 * extend this class; they have just to implement the {@code createResourceEventFrom} and the
 * {@code getDestination} methods. The notification by JMS is performed directly by this abstract
 * class.
 * @param <T> the type of the resource event.
 * @author mmoquillon
 */
public abstract class JMSResourceEventNotifier<T extends ResourceEvent>
    implements ResourceEventNotifier<T> {

  @Inject
  private JMSContext context;

  /**
   * Gets the destination of the notification. It is either a queue or a topic in JMS.
   * @return the destination of the notifications sent by this notifier.
   */
  protected abstract Destination getDestination();

  /**
   * Creates a resource event about the specified cause on the specified resource.
   * @param type the event type.
   * @param resource the resource related by the event.
   * @return a resource event.
   */
  protected abstract T createResourceEventFrom(ResourceEvent.Type type, Object resource);

  /**
   * Marshalls the specified event into a text message dedicated to be transmitted into a text
   * stream by JMS.
   * <p>In order to be serialized, the event should use either the JAXB annotations or the JSON
   * annotations specific to Jackson to mark the parts of the event to serialize.</p>
   * @param event the event to serialize.
   * @return a text message.
   */
  protected String marshall(T event) {
    return TextMessageCodec.encode(event);
  }

  @Override
  public final void notify(final T event) {
    JMSProducer producer = context.createProducer();
    producer.send(getDestination(), marshall(event));
  }

  @Override
  public final void notifyEventOn(final ResourceEvent.Type type, final Object resource) {
    notify(createResourceEventFrom(type, resource));
  }
}
