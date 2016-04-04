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


import javax.jms.Destination;
import javax.jms.JMSProducer;
import java.io.Serializable;

/**
 * An asynchronous notifier of resource events based on JMS. It is dedicated to be used in a
 * notification implying external or remote software components. It shouldn't be used to notify
 * Silverpeas inner components.
 * </p>
 * The asynchronous notifiers should extend this class; they have just to implement the
 * {@code createResourceEventFrom} and the {@code getDestination} methods.
 * The notification by JMS is performed directly by this abstract class.
 * @param <T> the type of the resource event.
 * @author mmoquillon
 */
public abstract class JMSResourceEventNotifier<R extends Serializable, T extends AbstractResourceEvent>
    implements ResourceEventNotifier<R, T> {

  /**
   * Gets the destination of the notification. It is either a queue or a topic in JMS.
   * @return the destination of the notifications sent by this notifier.
   */
  protected abstract Destination getDestination();

  /**
   * Creates a resource event about the specified cause on the specified resource.
   * @param type the event type.
   * @param resource the resource related by the event. In the case of an update, two instances
   * of the same resource are expected: the first being the resource before the update, the second
   * being the resource after the update (the result of the update).
   * @return a resource event.
   */
  protected abstract T createResourceEventFrom(final ResourceEvent.Type type, final R... resource);

  @Override
  public final void notify(final T event) {
    JMSOperation.realize(context -> {
      JMSProducer producer = context.createProducer();
      // TODO the event has to be sent in a text representation. For doing, use JSONCodec to encode the event in JSON.
      producer.send(getDestination(), event);
    });
  }

  @Override
  public final void notifyEventOn(final ResourceEvent.Type type, final R... resource) {
    notify(createResourceEventFrom(type, resource));
  }
}
