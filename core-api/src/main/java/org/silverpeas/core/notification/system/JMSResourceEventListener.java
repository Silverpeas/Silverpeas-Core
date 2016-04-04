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

import org.silverpeas.core.util.logging.SilverLogger;

import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * An asynchronous event listener. Asynchronous events are carried by JMS and are collected by this
 * abstract class. It is dedicated to notify asynchronously external or remote software components;
 * the asynchronous notification between Silverpeas inner components shouldn't use this mechanism.
 * <p>
 * All concrete listeners have just to extend this abstract class and to implement
 * first the {@code org.silverpeas.core.notification.system.JMSResourceEventListener#getResourceEventClass()}
 * method to specify the class of the events to receive and then some of the following methods to
 * transparently receive the events on which they are interested:
 * </p>
 * <ul>
 *   <li>{@code org.silverpeas.core.notification.system.ResourceEventListener#onCreation(ResourceEvent} to
 *   receive events about the creation of a resource,</li>
 *   <li>{@code org.silverpeas.core.notification.system.ResourceEventListener#onUpdate(ResourceEvent} to
 *   receive events about the update of a resource,</li>
 *   <li>{@code org.silverpeas.core.notification.system.ResourceEventListener#onRemoving(ResourceEvent} to
 *   receive events about the removing of a resource,</li>
 *   <li>{@code org.silverpeas.core.notification.system.ResourceEventListener#onDeletion(ResourceEvent} to
 *   receive events about the deletion of a resource,</li>
 * </ul>
 * <p>
 *   With JMS, when an exception is thrown, a retry mechanism will replay the event delivery. You
 *   can refine this mechanism by overridden the
 *   {@code org.silverpeas.core.notification.system.JMSResourceEventListener#retryAtFailure()} method; by
 *   default this method returns false, indicating to not to replay the event delivery at exception
 *   raising.
 * </p>
 * @author mmoquillon
 */
public abstract class JMSResourceEventListener<T extends AbstractResourceEvent>
    extends AbstractResourceEventListener<T> implements MessageListener {

  /**
   * Gets the class of the resource events listened by this listener.
   * @return the class of the supported {@code org.silverpeas.core.notification.system.ResourceEvent}.
   */
  protected abstract Class<T> getResourceEventClass();

  /**
   * Should JMS retry to replay the message to the listener at failure? By default returns false.
   * <p>
   * If true, a RuntimeException exception will be thrown if an exception is catched from the
   * message consummation, provoking JMS to replay the message to this listener. If false, only
   * an error level log will output.
   * </p>
   * @return true if the message should be replay by JMS to this listener, false otherwise.
   */
  protected boolean retryAtFailure() {
    return false;
  }

  /**
   * Listens for events related to a resource managed in Silverpeas.
   * <p>
   *   The event is decoded from the specified message and according to the type of the event,
   *   the adequate method is then invoked:
   * </p>
   * @see ResourceEventListener#dispatchEvent(ResourceEvent)
   * @param message the notification message in which is encoded the event. The event should be
   * serialized within the received {@code javax.jms.Message} object.
   * @throws java.lang.RuntimeException if an error occurs while treating the event.
   */
  @Override
  public void onMessage(final Message message) {
    try {
      T event = message.getBody(getResourceEventClass());
      dispatchEvent(event);
    } catch (Exception e) {
      if (retryAtFailure()) {
        throw new RuntimeException(e);
      } else {
        SilverLogger.getLogger(this).error(e.getMessage(), e);
      }
    }
  }
}
